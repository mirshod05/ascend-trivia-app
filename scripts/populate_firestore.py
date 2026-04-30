import time
import requests
import firebase_admin
from firebase_admin import credentials, firestore
import base64

SERVICE_ACCOUNT_KEY = "serviceAccountKey.json"
CATEGORY = 9
QUESTIONS_PER_CALL = 50
CALLS_PER_DIFFICULTY = 4
DELAY_BETWEEN_CALLS = 8

# ── Firebase init ─────────────────────────────
cred = credentials.Certificate(SERVICE_ACCOUNT_KEY)
firebase_admin.initialize_app(cred)
db = firestore.client()

# ── OpenTDB token ─────────────────────────────

def get_token():
    url = "https://opentdb.com/api_token.php?command=request"
    response = requests.get(url, timeout=10)
    response.raise_for_status()
    return response.json()["token"]

TOKEN = get_token()
print(f"Using OpenTDB token: {TOKEN}")

# ── Fetch questions ───────────────────────────

def fetch_questions(difficulty: str, amount: int = 50) -> list[dict]:
    url = (
        f"https://opentdb.com/api.php"
        f"?amount={amount}"
        f"&category={CATEGORY}"
        f"&difficulty={difficulty}"
        f"&type=multiple"
        f"&encode=base64"
        f"&token={TOKEN}"
    )

    for attempt in range(5):
        response = requests.get(url, timeout=10)

        if response.status_code == 429:
            wait = 10 * (attempt + 1)
            print(f"  Rate limited (429). Retrying in {wait}s...")
            time.sleep(wait)
            continue

        response.raise_for_status()
        return response.json().get("results", [])

    return []

def decode(s: str) -> str:
    return base64.b64decode(s).decode("utf-8").strip()

def process(raw: dict) -> dict:
    return {
        "question": decode(raw["question"]),
        "correctAnswer": decode(raw["correct_answer"]),
        "incorrectAnswers": "|".join(decode(a) for a in raw["incorrect_answers"]),
        "difficulty": decode(raw["difficulty"]),
    }

def upload_batch(questions: list[dict]):
    batch = db.batch()
    col = db.collection("questions")

    for q in questions:
        ref = col.document()
        batch.set(ref, q)

    batch.commit()
    print(f"  ✓ Uploaded {len(questions)} questions")

def main():
    total = 0
    seen = set()

    for difficulty in ["easy", "medium", "hard"]:

        print(f"\n⏳ Cooling down before {difficulty}...")
        time.sleep(30)

        print(f"\nFetching {difficulty} questions…")
        difficulty_count = 0

        for call_num in range(CALLS_PER_DIFFICULTY):
            print(f"  Batch {call_num + 1}/{CALLS_PER_DIFFICULTY}…", end=" ")

            try:
                raw_questions = fetch_questions(difficulty, QUESTIONS_PER_CALL)
            except Exception as e:
                print(f"Error: {e}")
                break

            processed = []
            for raw in raw_questions:
                q = process(raw)
                if q["question"] not in seen:
                    seen.add(q["question"])
                    processed.append(q)

            if processed:
                upload_batch(processed)
                difficulty_count += len(processed)
                total += len(processed)

            if call_num < CALLS_PER_DIFFICULTY - 1:
                print(f"  Waiting {DELAY_BETWEEN_CALLS}s...")
                time.sleep(DELAY_BETWEEN_CALLS)

        print(f"  → {difficulty_count} stored for {difficulty}")

    db.collection("meta").document("questions_version").set({"version": 1})

    print(f"\n✅ Done! {total} total questions uploaded.")

if __name__ == "__main__":
    main()