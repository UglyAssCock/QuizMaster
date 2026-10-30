package com.quizmaster.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizmaster.app.model.GameMode
import com.quizmaster.app.viewmodel.ActiveQuizSession
import com.quizmaster.app.viewmodel.QuizMasterViewModel

@Composable
fun ActiveQuizGameplayScreen(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val mode = GameMode.entries.firstOrNull { it.id == session.quiz.gameModeId } ?: GameMode.CLASSIC_TRIVIA
    val currentQ = session.questions.getOrNull(session.currentQuestionIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top HUD: Timer, Score, Progress, Pause
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${session.remainingSeconds / 60}:${(session.remainingSeconds % 60).toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = mode.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(mode.badgeColorHex)
                    )
                    Text(
                        text = "${session.score} pts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { viewModel.togglePauseSession() }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            if (session.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause"
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                val progress = if (mode == GameMode.NAME_ALL) {
                    session.namedItemsFound.size.toFloat() / session.questions.size.coerceAtLeast(1)
                } else {
                    (session.currentQuestionIndex + 1).toFloat() / session.questions.size.coerceAtLeast(1)
                }
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
            }
        }

        if (session.isPaused) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Game Paused", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.togglePauseSession() }) {
                        Text("Resume Quiz")
                    }
                    TextButton(onClick = { viewModel.finishActiveQuiz() }) {
                        Text("Give Up & View Results")
                    }
                }
            }
            return
        }

        session.lastAnswerFeedback?.let { fb ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(
                    text = fb,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Specialized UI per Game Mode
        Box(modifier = Modifier.weight(1f)) {
            when (mode) {
                GameMode.NAME_ALL -> NameAllSporcleGridEngine(session, viewModel)
                GameMode.MAP_QUIZ -> InteractiveMapQuizEngine(session, viewModel)
                GameMode.FLAG_QUIZ -> FlagQuizEngine(session, viewModel)
                GameMode.MATCHING -> MatchingColumnsEngine(session, viewModel)
                GameMode.CONNECTIONS -> Connections4x4Engine(session, viewModel)
                GameMode.WHO_AM_I -> WhoAmIProgressiveEngine(session, viewModel)
                GameMode.ORDERING, GameMode.TIMELINE -> OrderingTimelineEngine(session, viewModel)
                GameMode.MULTIPLE_ANSWERS -> MultipleAnswersEngine(session, viewModel)
                GameMode.IMAGE_REVEAL, GameMode.PICTURE_QUIZ -> ImageRevealPictureEngine(session, viewModel)
                GameMode.TYPE_ANSWER, GameMode.MISSING_WORDS, GameMode.WORD_SCRAMBLE, GameMode.CROSSWORD ->
                    TypeAndWordPuzzleEngine(session, viewModel)
                else -> StandardAndRapidChoiceEngine(session, viewModel)
            }
        }

        // Bottom Controls: Hint, Skip, Give Up
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (session.quiz.allowHints) {
                OutlinedButton(onClick = { viewModel.useHint() }) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hint (-15 pts)")
                }
            }
            if (session.quiz.allowSkipping && mode != GameMode.NAME_ALL) {
                OutlinedButton(onClick = { viewModel.skipQuestion() }) {
                    Text("Skip")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
            TextButton(onClick = { viewModel.finishActiveQuiz() }) {
                Text("Finish")
            }
        }

        session.hintTextShown?.let { hint ->
            Text(
                text = "Hint: $hint",
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun NameAllSporcleGridEngine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    var liveInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${session.quiz.title} (${session.namedItemsFound.size} / ${session.questions.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = liveInput,
            onValueChange = { text ->
                liveInput = text
                if (viewModel.submitNameAllEntry(text)) {
                    liveInput = ""
                }
            },
            label = { Text("Type any answer—auto-matches instantly!") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(session.questions) { q ->
                val found = session.namedItemsFound.contains(q.id)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (found) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = q.prompt,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (found) Color(0xFF14532D) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (found) q.correctAnswer else "???",
                            fontWeight = FontWeight.Bold,
                            color = if (found) Color(0xFF15803D) else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveMapQuizEngine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val q = session.questions.getOrNull(session.currentQuestionIndex) ?: return
    val markers = listOf(
        Triple("North America", 0.22f, 0.32f),
        Triple("South America", 0.32f, 0.68f),
        Triple("Europe", 0.49f, 0.28f),
        Triple("East Asia", 0.82f, 0.36f)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Tap the correct glowing target pin on the interactive world map:", fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C4A6E))
                .pointerInput(q.id) {
                    detectTapGestures { tapOffset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val nearest = markers.minByOrNull { (_, mx, my) ->
                            val dx = tapOffset.x - (mx * w)
                            val dy = tapOffset.y - (my * h)
                            dx * dx + dy * dy
                        }
                        if (nearest != null) {
                            viewModel.submitAnswer(nearest.first)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            // Simplified stylized continent masses
            drawOval(Color(0xFF15803D), Offset(w * 0.1f, h * 0.16f), Size(w * 0.24f, h * 0.30f))
            drawOval(Color(0xFF16A34A), Offset(w * 0.24f, h * 0.50f), Size(w * 0.16f, h * 0.38f))
            drawOval(Color(0xFF15803D), Offset(w * 0.42f, h * 0.16f), Size(w * 0.18f, h * 0.24f))
            drawOval(Color(0xFF16A34A), Offset(w * 0.45f, h * 0.42f), Size(w * 0.18f, h * 0.36f))
            drawOval(Color(0xFF15803D), Offset(w * 0.62f, h * 0.18f), Size(w * 0.28f, h * 0.34f))

            markers.forEach { (_, mx, my) ->
                drawCircle(Color.White, radius = 18f, center = Offset(mx * w, my * h))
                drawCircle(Color(0xFFEF4444), radius = 12f, center = Offset(mx * w, my * h))
            }
        }

        Spacer(Modifier.height(12.dp))
        q.options.forEach { opt ->
            OutlinedButton(
                onClick = { viewModel.submitAnswer(opt) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            ) {
                Text(opt)
            }
        }
    }
}

@Composable
private fun FlagQuizEngine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val q = session.questions.getOrNull(session.currentQuestionIndex) ?: return
    Column(modifier = Modifier.fillMaxSize()) {
        Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        FlagCanvasView(flagCode = q.visualCode)
        Spacer(Modifier.height(16.dp))
        q.options.forEach { opt ->
            Button(
                onClick = { viewModel.submitAnswer(opt) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(opt, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun MatchingColumnsEngine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val q = session.questions.firstOrNull() ?: return
    val pairs = q.extraPairs
    val leftItems = remember(q.id) { pairs.keys.toList() }
    val rightItems = remember(q.id) { pairs.values.shuffled() }
    var selectedLeft by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                leftItems.forEach { left ->
                    val matched = session.matchedPairs.contains(left)
                    Button(
                        onClick = { if (!matched) selectedLeft = left },
                        enabled = !matched,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedLeft == left) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(left, fontSize = 12.sp)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rightItems.forEach { right ->
                    val isMatched = pairs.entries.any { (k, v) -> v == right && session.matchedPairs.contains(k) }
                    OutlinedButton(
                        onClick = {
                            val left = selectedLeft
                            if (left != null && pairs[left] == right) {
                                viewModel.markPairMatched(left, pairs.size)
                                selectedLeft = null
                            } else {
                                selectedLeft = null
                            }
                        },
                        enabled = !isMatched,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(right, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Connections4x4Engine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val q = session.questions.firstOrNull() ?: return
    val map = q.extraPairs
    var selectedTiles by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        session.connectionsSolvedGroups.forEach { grp ->
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            ) {
                Text(
                    "Solved Group: $grp",
                    color = Color(0xFF166534),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        val remainingTiles = q.options.filter { tile ->
            val group = map[tile]
            group != null && !session.connectionsSolvedGroups.contains(group)
        }

        remainingTiles.chunked(4).forEach { rowTiles ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowTiles.forEach { tile ->
                    val isSel = selectedTiles.contains(tile)
                    Surface(
                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clickable {
                                val next = if (isSel) selectedTiles - tile else selectedTiles + tile
                                if (next.size == 4) {
                                    val groups = next.mapNotNull { map[it] }.toSet()
                                    if (groups.size == 1) {
                                        viewModel.markConnectionsGroupSolved(groups.first())
                                    }
                                    selectedTiles = emptySet()
                                } else {
                                    selectedTiles = next
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                            Text(
                                text = tile,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WhoAmIProgressiveEngine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val q = session.questions.getOrNull(session.currentQuestionIndex) ?: return
    Column(modifier = Modifier.fillMaxSize()) {
        Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        q.clues.take(session.revealedClueCount).forEach { clue ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(clue, modifier = Modifier.padding(10.dp), fontSize = 13.sp)
            }
        }
        if (session.revealedClueCount < q.clues.size) {
            TextButton(onClick = { viewModel.revealNextClueOrTile() }) {
                Text("Reveal Next Clue (Reduces Multiplier)")
            }
        }
        Spacer(Modifier.height(8.dp))
        q.options.forEach { opt ->
            Button(
                onClick = { viewModel.submitAnswer(opt) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            ) {
                Text(opt)
            }
        }
    }
}

@Composable
private fun OrderingTimelineEngine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val q = session.questions.getOrNull(session.currentQuestionIndex) ?: return
    var picked by remember(q.id) { mutableStateOf(listOf<String>()) }
    val shuffled = remember(q.id) { q.options.shuffled() }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Your Sequence: ${picked.joinToString(" -> ").ifBlank { "(Tap items in order below)" }}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))

        shuffled.forEach { item ->
            val alreadyPicked = picked.contains(item)
            OutlinedButton(
                onClick = {
                    val next = picked + item
                    picked = next
                    if (next.size == q.options.size) {
                        viewModel.submitAnswer(next.joinToString(","))
                    }
                },
                enabled = !alreadyPicked,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(item)
            }
        }
        if (picked.isNotEmpty()) {
            TextButton(onClick = { picked = emptyList() }) { Text("Reset Sequence") }
        }
    }
}

@Composable
private fun MultipleAnswersEngine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val q = session.questions.getOrNull(session.currentQuestionIndex) ?: return
    var selected by remember(q.id) { mutableStateOf(setOf<String>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        q.options.forEach { opt ->
            val checked = selected.contains(opt)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selected = if (checked) selected - opt else selected + opt }
                    .padding(vertical = 6.dp)
            ) {
                Checkbox(checked = checked, onCheckedChange = { selected = if (checked) selected - opt else selected + opt })
                Spacer(Modifier.width(8.dp))
                Text(opt, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.submitAnswer(selected.sorted().joinToString(",")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lock In Selected Answers")
        }
    }
}

@Composable
private fun ImageRevealPictureEngine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val q = session.questions.getOrNull(session.currentQuestionIndex) ?: return
    Column(modifier = Modifier.fillMaxSize()) {
        Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFEF3C7))
                .clickable { viewModel.revealNextClueOrTile() }
        ) {
            val w = size.width
            val h = size.height
            // Draw golden pyramid landmark illustration underneath
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.5f, h * 0.15f)
                lineTo(w * 0.8f, h * 0.85f)
                lineTo(w * 0.2f, h * 0.85f)
                close()
            }
            drawPath(path, Color(0xFFD97706))
            // Draw obscuring tiles based on imageRevealStep
            val hiddenTiles = (4 - session.imageRevealStep).coerceAtLeast(0)
            for (i in 0 until hiddenTiles) {
                drawRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(i * (w / 4f), 0f),
                    size = Size(w / 4f - 4f, h)
                )
            }
        }
        Text("Tap image to uncover more tiles", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        q.options.forEach { opt ->
            Button(
                onClick = { viewModel.submitAnswer(opt) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(opt)
            }
        }
    }
}

@Composable
private fun TypeAndWordPuzzleEngine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val q = session.questions.getOrNull(session.currentQuestionIndex) ?: return
    var typed by remember(q.id) { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = typed,
            onValueChange = { typed = it },
            label = { Text("Type your answer") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                viewModel.submitAnswer(typed)
                typed = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Answer")
        }
    }
}

@Composable
private fun StandardAndRapidChoiceEngine(
    session: ActiveQuizSession,
    viewModel: QuizMasterViewModel
) {
    val q = session.questions.getOrNull(session.currentQuestionIndex) ?: return
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Question ${session.currentQuestionIndex + 1} of ${session.questions.size}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(q.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        q.options.forEach { opt ->
            ElevatedButton(
                onClick = { viewModel.submitAnswer(opt) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(opt, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
