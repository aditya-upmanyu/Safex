package com.safex.app.domain.chatbot.usecase

import com.safex.app.domain.chatbot.model.Checker
import com.safex.app.domain.chatbot.model.CheckerAnswer
import com.safex.app.domain.chatbot.model.CheckerEvaluation
import com.safex.app.domain.chatbot.model.CheckerResultText
import com.safex.app.domain.chatbot.model.Verdict
import javax.inject.Inject

class RunCheckerUseCase @Inject constructor() {

    operator fun invoke(
        checker: Checker,
        answers: Map<String, CheckerAnswer>
    ): CheckerEvaluation {
        var totalScore = 0
        var criticalYesCount = 0
        var totalYesOrNotSure = 0

        for (question in checker.questions) {
            val answer = answers[question.id] ?: CheckerAnswer.NO
            val points = when (answer) {
                CheckerAnswer.YES -> 2
                CheckerAnswer.NOT_SURE -> 1
                CheckerAnswer.NO -> 0
            }
            totalScore += points

            if (answer == CheckerAnswer.YES && question.critical) {
                criticalYesCount++
            }

            if (answer == CheckerAnswer.YES || answer == CheckerAnswer.NOT_SURE) {
                totalYesOrNotSure++
            }
        }

        // Determine verdict according to rules:
        // 1. any critical Yes AND at least one other Yes/Not-sure -> LIKELY_SCAM
        // 2. any critical Yes alone -> SUSPICIOUS
        // 3. total >= 6 -> LIKELY_SCAM
        // 4. total 2-5 -> SUSPICIOUS
        // 5. total 0-1 -> LOW_RISK
        val verdict: Verdict = when {
            criticalYesCount >= 1 && totalYesOrNotSure > 1 -> Verdict.LIKELY_SCAM
            criticalYesCount >= 1 && totalYesOrNotSure == 1 -> Verdict.SUSPICIOUS
            totalScore >= 6 -> Verdict.LIKELY_SCAM
            totalScore in 2..5 -> Verdict.SUSPICIOUS
            else -> Verdict.LOW_RISK
        }

        val resultText = checker.resultTexts[verdict] ?: CheckerResultText(
            headline = "Assessment Completed",
            body = "Safety score evaluated. Always exercise caution and never share OTP or personal credentials.",
            nextTopic = null
        )

        return CheckerEvaluation(
            checkerId = checker.id,
            score = totalScore,
            verdict = verdict,
            resultText = resultText,
            answeredCount = answers.size,
            totalCount = checker.questions.size,
            criticalYesCount = criticalYesCount
        )
    }
}
