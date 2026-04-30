package com.example.ascend_app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ascend_app.ui.theme.AscendappTheme
import kotlinx.coroutines.delay
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AscendappTheme {
                var currentScreen by remember { mutableStateOf(Screen.Home) }
                BackHandler(enabled = currentScreen == Screen.About || currentScreen == Screen.Game) {
                    currentScreen = Screen.Home
                }
                when (currentScreen) {
                    Screen.Home -> HomeScreen(
                        onStartGame = { currentScreen = Screen.Game },
                        onAbout = { currentScreen = Screen.About }
                    )

                    Screen.About -> AboutScreen(onBack = { currentScreen = Screen.Home })
                    Screen.Game -> GameScreen(onBack = { currentScreen = Screen.Home })
                }
            }
        }
    }
    override fun onPause() {
        super.onPause()
        MusicManager.pause()
    }

    override fun onResume() {
        super.onResume()
        MusicManager.resume()
    }
}

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val callback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() { onBack() }
        }
    }
    SideEffect { callback.isEnabled = enabled }
    DisposableEffect(dispatcher) {
        dispatcher?.addCallback(callback)
        onDispose { callback.remove() }
    }
}

enum class Screen { Home, About, Game }

private val GradientBlue   = Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFFD6D9FF), Color(
    0xFF7DCBFF
)
))
private val GradientDark   = Brush.verticalGradient(listOf(Color(0xFF263238), Color(0xFF37474F), Color(0xFF455A64)))
private val GradientPurple = Brush.verticalGradient(listOf(Color(0xFF311B92), Color(0xFF512DA8), Color(0xFF7C4DFF)))
private val GoldColor  = Color(0xFFFFD700)
private val GreenColor = Color(0xFF4CAF50)
private val RedColor   = Color(0xFFF44336)

// ── HomeScreen ────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(onStartGame: () -> Unit, onAbout: () -> Unit) {
    var isMuted by remember{mutableStateOf(false)}
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        MusicManager.play(context, R.raw.menu_music)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.main_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 1f   // adjust between 0.2f (more transparent) and 0.6f (more visible)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF939393).copy(alpha = 0.1f)), // dark overlay so text stays readable
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                Text(
                    "Ascend",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                Text(
                    "Road to a Million Trivia Game", fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )
                Button(
                    onClick = onStartGame, shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF3949AB)
                    ),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text(
                        "START GAME",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onAbout, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text(
                        "ABOUT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    isMuted = !isMuted
                    MusicManager.setMuted(isMuted)
                }) {
                    Text(
                        if (isMuted) "🔇 Music Off" else "🔊 Music On",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ── AboutScreen ───────────────────────────────────────────────────────────────

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        MusicManager.play(context, R.raw.menu_music)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.main_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 1f   // adjust between 0.2f (more transparent) and 0.6f (more visible)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF939393).copy(alpha = 0.1f)), // dark overlay so text stays readable
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(48.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "ABOUT",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(32.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            "How to Play", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            color = Color.White, modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            "• Answer 15 trivia questions correctly\n" +
                                    "• Each question has multiple choice answers\n" +
                                    "• Climb the money ladder from \$100 to \$1,000,000\n" +
                                    "• Guaranteed safe havens at \$1,000, \$32,000, and \$1,000,000\n" +
                                    "• Wrong answers drop you to the last guaranteed level",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 28.sp
                        )
                    }
                }
                Spacer(Modifier.height(40.dp))
                Button(
                    onClick = onBack, shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF311B92)
                    ),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text(
                        "BACK",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

// ── GameScreen ────────────────────────────────────────────────────────────────
@SuppressLint("ContextCastToActivity")
@Composable
fun GameScreen(onBack: () -> Unit, vm: GameViewModel = viewModel()) {
    val state by vm.gameState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    LaunchedEffect(Unit) {
        MusicManager.play(context, R.raw.game_music)
        activity?.let {
            val controller = WindowInsetsControllerCompat(it.window, it.window.decorView)
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.let {
                val controller = WindowInsetsControllerCompat(it.window, it.window.decorView)
                controller.show(WindowInsetsCompat.Type.navigationBars())
            }
            MusicManager.play(context, R.raw.menu_music)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.gameshow_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 1f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF939393).copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            when (val s = state) {
                is GameState.Loading -> LoadingView()
                is GameState.Error -> ErrorView(
                    message = s.message,
                    onRetry = vm::loadQuestions,
                    onBack = onBack
                )
                is GameState.GameOver -> GameOverView(
                    winnings = s.winnings,
                    onPlayAgain = vm::loadQuestions,
                    onHome = onBack
                )
                is GameState.Playing -> {
                    val gm = s.gameManager
                    if (gm.isGameOver()) {
                        ResultView(
                            winnings = gm.currentWinnings,
                            onPlayAgain = vm::loadQuestions,
                            onHome = onBack
                        )
                    } else {
                        // ── State that must survive key(updateId) recompose ──
                        var showPhoneDialog by remember { mutableStateOf(false) }
                        var showAudienceDialog by remember { mutableStateOf(false) }
                        var isSecondDoubleDipAttempt by remember { mutableStateOf(false) }
                        var timeLeft by remember { mutableStateOf(60) }
                        var showResult by remember { mutableStateOf(false) }
                        val shuffledAnswers = remember(gm.currentIndex) {
                            gm.getCurrentQuestion().allAnswersShuffled
                        }

                        // Reset per-question state when question changes
                        LaunchedEffect(gm.currentIndex) {
                            isSecondDoubleDipAttempt = false
                            timeLeft = 60
                            showResult = false
                        }

                        // Timer — lives here so it survives lifeline recompose
                        LaunchedEffect(gm.currentIndex) {
                            while (timeLeft > 0) {
                                delay(1000L)
                                if (!showResult) timeLeft--
                            }
                            if (timeLeft == 0) vm.submitAnswer("")
                        }

                        key(s.updateId) {
                            QuestionView(
                                gameManager = gm,
                                onAnswerFinished = { answer ->
                                    showResult = false
                                    vm.submitAnswer(answer) },
                                onQuit = { vm.giveUp() },
                                onFiftyFifty = { vm.useFiftyFifty() },
                                onPhoneAFriend = {
                                    vm.usePhoneAFriend()
                                    showPhoneDialog = true
                                },
                                onAskAudience = {
                                    vm.useAskAudience()
                                    showAudienceDialog = true
                                },
                                onDoubleDip = { vm.useDoubleDip() },
                                isSecondAttempt = isSecondDoubleDipAttempt,
                                onSetSecondAttempt = { isSecondDoubleDipAttempt = true },
                                timeLeft = timeLeft,
                                showResult = showResult,
                                onShowResult = { showResult = true },
                                onResetResult = { showResult = false },
                                shuffledAnswers = shuffledAnswers
                            )
                        }

                        if (showPhoneDialog) {
                            PhoneFriendDialog(
                                correctAnswer = gm.getCurrentQuestion().correctAnswer,
                                onDismiss = { showPhoneDialog = false }
                            )
                        }
                        if (showAudienceDialog) {
                            AskAudienceDialog(
                                correctAnswer = gm.getCurrentQuestion().correctAnswer,
                                allAnswers = shuffledAnswers,
                                onDismiss = { showAudienceDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── LoadingView ───────────────────────────────────────────────────────────────

@Composable
fun LoadingView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = GoldColor, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(20.dp))
        Text("Loading questions…", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text("This may take a few seconds", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
    }
}

// ── ErrorView ─────────────────────────────────────────────────────────────────

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
        Text("Oops!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = RedColor)
        Spacer(Modifier.height(12.dp))
        Text(message, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
            modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("RETRY", fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("HOME", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ── QuestionView ──────────────────────────────────────────────────────────────
@Composable
fun QuestionView(
    gameManager: GameManager,
    onAnswerFinished: (String) -> Unit,
    onQuit: () -> Unit,
    onFiftyFifty: () -> Unit,
    onPhoneAFriend: () -> Unit,
    onAskAudience: () -> Unit,
    onDoubleDip: () -> Unit,
    isSecondAttempt: Boolean,
    onSetSecondAttempt: () -> Unit,
    timeLeft: Int,
    showResult: Boolean,
    onShowResult: () -> Unit,
    onResetResult: () -> Unit,
    shuffledAnswers: List<String>
) {
    val question = gameManager.getCurrentQuestion()
    val correctAnswer = question.correctAnswer

    // Only selectedAnswer stays local — everything else lifted to GameScreen
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    val prize = formatMoney(gameManager.getCurrentPrize())
    val guaranteed = formatMoney(gameManager.guaranteedWinnings)

    // Animation + handoff to VM — no game logic here
    LaunchedEffect(showResult) {
        if (!showResult) return@LaunchedEffect
        val answer = selectedAnswer ?: return@LaunchedEffect
        val isCorrect = answer == correctAnswer

        if (!isCorrect && gameManager.doubleDipActive && !isSecondAttempt) {
            // First wrong double dip attempt — show red, then reset for retry
            delay(1200L)
            onAnswerFinished(answer)  // tells VM: firstWrongSaved = true, stay in Playing
            onSetSecondAttempt()
            selectedAnswer = null
            onResetResult()
        } else {
            // Correct, final wrong, or timed out — show color then advance
            delay(1200L)
            onAnswerFinished(answer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Q ${gameManager.currentIndex + 1} / 15",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            Text(
                "$timeLeft s",
                color = if (timeLeft <= 15) RedColor else Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(prize, color = GoldColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        if (gameManager.guaranteedWinnings > 0) {
            Text(
                "Guaranteed: $guaranteed",
                color = GreenColor.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
        ) {
            Text(
                text = question.question,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Lifeline buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LifelineButton("50:50", gameManager.lifelines.fiftyFiftyUsed,
                onClick = onFiftyFifty, modifier = Modifier.weight(1f))
            LifelineButton("Phone", gameManager.lifelines.phoneAFriendUsed,
                onClick = onPhoneAFriend, modifier = Modifier.weight(1f))
            LifelineButton("Audience", gameManager.lifelines.askAudienceUsed,
                onClick = onAskAudience, modifier = Modifier.weight(1f))
            LifelineButton("2x", gameManager.lifelines.doubleDipUsed,
                onClick = onDoubleDip, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))

        // 2×2 answer grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (row in 0 until 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (col in 0 until 2) {
                        val index = row * 2 + col
                        val answer = shuffledAnswers.getOrNull(index) ?: continue
                        val eliminated = gameManager.eliminated.contains(answer)
                        val isSelected = selectedAnswer == answer
                        val isCorrect = answer == correctAnswer

                        val targetColor = when {
                            showResult && isCorrect && (!gameManager.doubleDipActive || isSecondAttempt) -> GreenColor
                            showResult && isSelected && !isCorrect -> RedColor
                            else -> Color.White.copy(alpha = if (eliminated) 0.05f else 0.12f)
                        }
                        val bgColor by animateColorAsState(
                            targetValue = targetColor,
                            animationSpec = tween(400),
                            label = "btnColor"
                        )
                        val label = listOf("A", "B", "C", "D").getOrElse(index) { "" }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(bgColor)
                                .clickable(enabled = !eliminated && !showResult) {
                                    selectedAnswer = answer
                                    onShowResult()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "$label.  ",
                                    fontWeight = FontWeight.Bold,
                                    color = if (eliminated) Color.White.copy(alpha = 0.3f)
                                    else if (showResult && isCorrect) Color.White
                                    else GoldColor,
                                    fontSize = 16.sp
                                )
                                Text(
                                    answer,
                                    fontSize = 14.sp,
                                    color = if (eliminated) Color.White.copy(alpha = 0.3f) else Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onQuit) {
            Text(
                "Walk Away  •  keep $guaranteed",
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

// ── LifelineButton ────────────────────────────────────────────────────────────

@Composable
fun LifelineButton(label: String, used: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (used) {
        // Take up space but show nothing — keeps row layout stable
        Box(modifier = modifier.padding(vertical = 8.dp))
        return
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (used) Color.Gray.copy(alpha = 0.3f) else Color(0xFF2196F3).copy(alpha = 0.8f))
            .clickable(enabled = !used) { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (used) Color.White.copy(alpha = 0.5f) else Color.White,
            fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

// ── Dialogs ───────────────────────────────────────────────────────────────────

@Composable
fun PhoneFriendDialog(correctAnswer: String, onDismiss: () -> Unit) {
    val quote = remember {
        listOf(
            "I'm pretty confident the answer is",
            "Let me think... I'd go with",
            "My gut feeling says",
            "I'm fairly certain it's"
        ).random()
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A5F))) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📞 Phone a Friend", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GoldColor)
                Spacer(Modifier.height(16.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.1f)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your friend says:", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("\"$quote...\"", color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Text(correctAnswer, color = GoldColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Thanks!", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AskAudienceDialog(correctAnswer: String, allAnswers: List<String>, onDismiss: () -> Unit) {
    val finalData = remember(correctAnswer) {
        val raw = allAnswers.map {
            it to if (it == correctAnswer) (60..85).random() else (5..25).random()
        }
        val total = raw.sumOf { it.second }
        raw.map { it.first to (it.second * 100) / total }
            .sortedByDescending { it.second }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A5F))) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👥 Ask the Audience", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GoldColor)
                Spacer(Modifier.height(20.dp))
                finalData.forEach { (answer, pct) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(if (answer.length > 20) answer.take(20) + "…" else answer,
                            color = if (answer == correctAnswer) GoldColor else Color.White,
                            fontSize = 13.sp, modifier = Modifier.width(100.dp))
                        LinearProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(4.dp)),
                            color      = if (answer == correctAnswer) GoldColor else GreenColor,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Text("$pct%", color = if (answer == correctAnswer) GoldColor else Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            modifier = Modifier.width(50.dp).padding(start = 8.dp))
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Thanks!", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Result screens ────────────────────────────────────────────────────────────

@Composable
fun GameOverView(winnings: Int, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
        Text("Wrong!", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = RedColor)
        Spacer(Modifier.height(16.dp))
        Text("You walk away with", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
        Text(formatMoney(winnings), color = GoldColor, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(40.dp))
        Button(onClick = onPlayAgain, colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
            modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("PLAY AGAIN", fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("HOME", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ResultView(winnings: Int, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
        Text("🏆 MILLIONAIRE!", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = GoldColor)
        Spacer(Modifier.height(16.dp))
        Text("You won", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
        Text(formatMoney(winnings), color = GoldColor, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(40.dp))
        Button(onClick = onPlayAgain, colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
            modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("PLAY AGAIN", fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("HOME", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

fun formatMoney(amount: Int): String = "$" + "%,d".format(amount)