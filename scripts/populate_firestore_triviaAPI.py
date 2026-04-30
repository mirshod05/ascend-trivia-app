"""
populate_firestore_trivia_api.py

Fetches general knowledge questions from the-trivia-api.com and uploads
them to the same Firestore 'questions' collection as your existing questions.
Deduplicates against questions already in Firestore by question text.

Setup: same folder as serviceAccountKey.json
Run:   python populate_firestore_trivia_api.py
"""

import time
import requests
import firebase_admin
from firebase_admin import credentials, firestore

# ── Config ────────────────────────────────────────────────────────────────────

SERVICE_ACCOUNT_KEY  = "serviceAccountKey.json"
BASE_URL             = "https://the-trivia-api.com/v2/questions"
CATEGORY             = "general_knowledge"
REQUESTS_PER_DIFF    = 20       # 20 requests × 50 questions = up to 1000 per difficulty
LIMIT_PER_REQUEST    = 50       # free tier max
DELAY_BETWEEN_CALLS  = 2        # seconds — be polite, no stated rate limit

# ── Firebase init ─────────────────────────────────────────────────────────────

cred = credentials.Certificate(SERVICE_ACCOUNT_KEY)

# Won't crash if already initialized (e.g. if you run both scripts together)
try:
    firebase_admin.initialize_app(cred)
except ValueError:
    pass

db = firestore.client()

# ── Load existing question texts from Firestore (for deduplication) ───────────

def load_existing_questions() -> set:
    print("Loading existing questions from Firestore for deduplication...")
    existing = set()
    docs = db.collection("questions").stream()
    for doc in docs:
        data = doc.to_dict()
        text = data.get("question", "").strip().lower()
        if text:
            existing.add(text)
    print(f"  Found {len(existing)} existing questions in Firestore")
    return existing

# ── Fetch from The Trivia API ─────────────────────────────────────────────────

def fetch_questions(difficulty: str) -> list[dict]:
    params = {
        "limit":       LIMIT_PER_REQUEST,
        "categories":  CATEGORY,
        "difficulties": difficulty,
        "types":       "text_choice",   # only multiple choice, no image or text input
    }
    response = requests.get(BASE_URL, params=params, timeout=10)
    response.raise_for_status()
    return response.json()

# ── Process raw question into our Firestore format ────────────────────────────

def process(raw: dict, difficulty: str) -> dict | None:
    # Skip niche questions — too obscure for a general audience
    if raw.get("isNiche", False):
        return None

    # Skip regional questions — they may not make sense globally
    if raw.get("regions"):
        return None

    # Question text is nested under question.text
    question_text = raw.get("question", {}).get("text", "").strip()
    if not question_text:
        return None

    correct = raw.get("correctAnswer", "").strip()
    incorrect = [a.strip() for a in raw.get("incorrectAnswers", []) if a.strip()]

    # Must have exactly 3 incorrect answers for a 4-choice question
    if not correct or len(incorrect) != 3:
        return None

    return {
        "question":         question_text,
        "correctAnswer":    correct,
        "incorrectAnswers": "|".join(incorrect),
        "difficulty":       raw.get("difficulty", difficulty),
    }

# ── Upload batch to Firestore ─────────────────────────────────────────────────

def upload_batch(questions: list[dict]):
    batch = db.batch()
    col   = db.collection("questions")
    for q in questions:
        ref = col.document()
        batch.set(ref, q)
    batch.commit()

# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    existing = load_existing_questions()
    seen_this_run = set()   # track within this run too
    grand_total = 0

    for difficulty in ["easy", "medium", "hard"]:
        print(f"\nFetching {difficulty} questions...")
        diff_total = 0
        consecutive_dupes = 0

        for i in range(REQUESTS_PER_DIFF):
            print(f"  Request {i + 1}/{REQUESTS_PER_DIFF}...", end=" ")

            try:
                raw_list = fetch_questions(difficulty)
            except Exception as e:
                print(f"Error: {e}")
                break

            batch = []
            for raw in raw_list:
                processed = process(raw, difficulty)
                if processed is None:
                    continue

                key = processed["question"].strip().lower()
                if key in existing or key in seen_this_run:
                    consecutive_dupes += 1
                    continue

                seen_this_run.add(key)
                existing.add(key)
                batch.append(processed)

            if batch:
                upload_batch(batch)
                diff_total  += len(batch)
                grand_total += len(batch)
                consecutive_dupes = 0
                print(f"uploaded {len(batch)} new questions")
            else:
                print(f"all duplicates, skipping")

            # If we keep getting dupes, the API is probably cycling —stop early
            if consecutive_dupes >= 150:
                print(f"  Too many duplicates in a row — API likely exhausted for {difficulty}")
                break

            if i < REQUESTS_PER_DIFF - 1:
                time.sleep(DELAY_BETWEEN_CALLS)

        print(f"  → {diff_total} new {difficulty} questions added")

    # Bump Firestore version so the app re-syncs on next launch
    current = db.collection("meta").document("questions_version").get()
    current_version = current.to_dict().get("version", 1) if current.exists else 1
    new_version = current_version + 1
    db.collection("meta").document("questions_version").set({"version": new_version})

    print(f"\n✅ Done! {grand_total} new questions uploaded.")
    print(f"   Firestore version bumped to {new_version} — app will re-sync on next launch.")

if __name__ == "__main__":
    main()