package com.safex.app.presentation.chatbot.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.safex.app.domain.chatbot.model.Checker
import com.safex.app.domain.chatbot.model.CheckerAnswer
import com.safex.app.presentation.chatbot.ChatDimensConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckerSheet(
    checker: Checker,
    currentIndex: Int,
    answers: Map<String, CheckerAnswer>,
    dimens: ChatDimensConfig,
    onAnswer: (questionId: String, answer: CheckerAnswer) -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    onSubmit: () -> Unit
) {
    val totalQuestions = checker.questions.size
    val currentQuestion = checker.questions.getOrNull(currentIndex) ?: return
    val progress = (currentIndex + 1).toFloat() / totalQuestions.toFloat()
    val currentAnswer = answers[currentQuestion.id]

    ModalBottomSheet(
        onDismissRequest = onClose,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentIndex > 0) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Text(
                    text = checker.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimens.headlineSize
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Linear Progress
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Question ${currentIndex + 1} of $totalQuestions",
                fontSize = dimens.subtextSize,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Critical warning chip
            if (currentQuestion.critical) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Critical",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Critical Indicator",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = dimens.subtextSize
                        )
                    }
                }
            }

            // Question Text
            Text(
                text = currentQuestion.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = dimens.headlineSize,
                lineHeight = dimens.headlineSize.times(1.35f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: YES / NO / NOT SURE
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onAnswer(currentQuestion.id, CheckerAnswer.YES)
                        if (currentIndex == totalQuestions - 1) onSubmit()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dimens.minTouchTarget),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentAnswer == CheckerAnswer.YES) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Yes",
                        fontWeight = FontWeight.Bold,
                        fontSize = dimens.bodySize
                    )
                }

                OutlinedButton(
                    onClick = {
                        onAnswer(currentQuestion.id, CheckerAnswer.NO)
                        if (currentIndex == totalQuestions - 1) onSubmit()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dimens.minTouchTarget),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No",
                        fontWeight = FontWeight.Bold,
                        fontSize = dimens.bodySize
                    )
                }

                FilledTonalButton(
                    onClick = {
                        onAnswer(currentQuestion.id, CheckerAnswer.NOT_SURE)
                        if (currentIndex == totalQuestions - 1) onSubmit()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dimens.minTouchTarget),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Not sure",
                        fontWeight = FontWeight.Medium,
                        fontSize = dimens.bodySize
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
