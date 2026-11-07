package com.safex.app.chatbot

import com.safex.app.domain.chatbot.model.*
import com.safex.app.domain.chatbot.usecase.RunCheckerUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RunCheckerUseCaseTest {

    private lateinit var runCheckerUseCase: RunCheckerUseCase
    private lateinit var sampleChecker: Checker

    @Before
    fun setUp() {
        runCheckerUseCase = RunCheckerUseCase()
        sampleChecker = Checker(
            id = "test_check",
            title = "Test Safety Checker",
            intro = "Testing checker logic",
            questions = listOf(
                CheckerQuestion(id = "Q1", text = "Non-critical question 1", critical = false),
                CheckerQuestion(id = "Q2", text = "Non-critical question 2", critical = false),
                CheckerQuestion(id = "Q3", text = "Critical question 3", critical = true),
                CheckerQuestion(id = "Q4", text = "Non-critical question 4", critical = false),
                CheckerQuestion(id = "Q5", text = "Critical question 5", critical = true),
                CheckerQuestion(id = "Q6", text = "Non-critical question 6", critical = false)
            ),
            resultTexts = mapOf(
                Verdict.LOW_RISK to CheckerResultText("Low Risk", "Looks safe"),
                Verdict.SUSPICIOUS to CheckerResultText("Suspicious", "Be careful"),
                Verdict.LIKELY_SCAM to CheckerResultText("Likely Scam", "High danger")
            )
        )
    }

    @Test
    fun testCheckerScoring20Cases() {
        // 1. All NO -> Score 0 -> LOW_RISK
        val eval1 = runCheckerUseCase(sampleChecker, mapOf("Q1" to CheckerAnswer.NO, "Q2" to CheckerAnswer.NO))
        assertEquals(0, eval1.score)
        assertEquals(Verdict.LOW_RISK, eval1.verdict)

        // 2. Score 1 boundary: Single NOT_SURE non-critical -> Score 1 -> LOW_RISK
        val eval2 = runCheckerUseCase(sampleChecker, mapOf("Q1" to CheckerAnswer.NOT_SURE))
        assertEquals(1, eval2.score)
        assertEquals(Verdict.LOW_RISK, eval2.verdict)

        // 3. Score 2 boundary: Single YES non-critical -> Score 2 -> SUSPICIOUS
        val eval3 = runCheckerUseCase(sampleChecker, mapOf("Q1" to CheckerAnswer.YES))
        assertEquals(2, eval3.score)
        assertEquals(Verdict.SUSPICIOUS, eval3.verdict)

        // 4. Score 3: One YES non-critical + One NOT_SURE non-critical -> Score 3 -> SUSPICIOUS
        val eval4 = runCheckerUseCase(sampleChecker, mapOf("Q1" to CheckerAnswer.YES, "Q2" to CheckerAnswer.NOT_SURE))
        assertEquals(3, eval4.score)
        assertEquals(Verdict.SUSPICIOUS, eval4.verdict)

        // 5. Score 4: Two YES non-critical -> Score 4 -> SUSPICIOUS
        val eval5 = runCheckerUseCase(sampleChecker, mapOf("Q1" to CheckerAnswer.YES, "Q2" to CheckerAnswer.YES))
        assertEquals(4, eval5.score)
        assertEquals(Verdict.SUSPICIOUS, eval5.verdict)

        // 6. Score 5 boundary: Two YES non-critical + One NOT_SURE -> Score 5 -> SUSPICIOUS
        val eval6 = runCheckerUseCase(sampleChecker, mapOf(
            "Q1" to CheckerAnswer.YES,
            "Q2" to CheckerAnswer.YES,
            "Q4" to CheckerAnswer.NOT_SURE
        ))
        assertEquals(5, eval6.score)
        assertEquals(Verdict.SUSPICIOUS, eval6.verdict)

        // 7. Score 6 boundary: Three YES non-critical -> Score 6 -> LIKELY_SCAM
        val eval7 = runCheckerUseCase(sampleChecker, mapOf(
            "Q1" to CheckerAnswer.YES,
            "Q2" to CheckerAnswer.YES,
            "Q4" to CheckerAnswer.YES
        ))
        assertEquals(6, eval7.score)
        assertEquals(Verdict.LIKELY_SCAM, eval7.verdict)

        // 8. Score 8: Four YES non-critical -> Score 8 -> LIKELY_SCAM
        val eval8 = runCheckerUseCase(sampleChecker, mapOf(
            "Q1" to CheckerAnswer.YES,
            "Q2" to CheckerAnswer.YES,
            "Q4" to CheckerAnswer.YES,
            "Q6" to CheckerAnswer.YES
        ))
        assertEquals(8, eval8.score)
        assertEquals(Verdict.LIKELY_SCAM, eval8.verdict)

        // 9. Critical YES alone -> SUSPICIOUS ("any critical Yes alone -> SUSPICIOUS")
        val eval9 = runCheckerUseCase(sampleChecker, mapOf(
            "Q3" to CheckerAnswer.YES,
            "Q1" to CheckerAnswer.NO,
            "Q2" to CheckerAnswer.NO
        ))
        assertEquals(2, eval9.score)
        assertEquals(1, eval9.criticalYesCount)
        assertEquals(Verdict.SUSPICIOUS, eval9.verdict)

        // 10. Critical YES + one NOT_SURE -> LIKELY_SCAM ("any critical Yes AND at least one other Yes/Not-sure -> LIKELY_SCAM")
        val eval10 = runCheckerUseCase(sampleChecker, mapOf(
            "Q3" to CheckerAnswer.YES,
            "Q1" to CheckerAnswer.NOT_SURE
        ))
        assertEquals(3, eval10.score)
        assertEquals(Verdict.LIKELY_SCAM, eval10.verdict)

        // 11. Critical YES + one non-critical YES -> LIKELY_SCAM
        val eval11 = runCheckerUseCase(sampleChecker, mapOf(
            "Q3" to CheckerAnswer.YES,
            "Q1" to CheckerAnswer.YES
        ))
        assertEquals(4, eval11.score)
        assertEquals(Verdict.LIKELY_SCAM, eval11.verdict)

        // 12. Two Critical YES -> LIKELY_SCAM
        val eval12 = runCheckerUseCase(sampleChecker, mapOf(
            "Q3" to CheckerAnswer.YES,
            "Q5" to CheckerAnswer.YES
        ))
        assertEquals(4, eval12.score)
        assertEquals(2, eval12.criticalYesCount)
        assertEquals(Verdict.LIKELY_SCAM, eval12.verdict)

        // 13. Critical question NOT_SURE alone -> Score 1 -> LOW_RISK
        val eval13 = runCheckerUseCase(sampleChecker, mapOf("Q3" to CheckerAnswer.NOT_SURE))
        assertEquals(1, eval13.score)
        assertEquals(0, eval13.criticalYesCount)
        assertEquals(Verdict.LOW_RISK, eval13.verdict)

        // 14. Critical question NOT_SURE + non-critical NOT_SURE -> Score 2 -> SUSPICIOUS
        val eval14 = runCheckerUseCase(sampleChecker, mapOf(
            "Q3" to CheckerAnswer.NOT_SURE,
            "Q1" to CheckerAnswer.NOT_SURE
        ))
        assertEquals(2, eval14.score)
        assertEquals(Verdict.SUSPICIOUS, eval14.verdict)

        // 15. Empty answers map -> Score 0 -> LOW_RISK
        val eval15 = runCheckerUseCase(sampleChecker, emptyMap())
        assertEquals(0, eval15.score)
        assertEquals(Verdict.LOW_RISK, eval15.verdict)

        // 16. Second critical question Q5 alone YES -> SUSPICIOUS
        val eval16 = runCheckerUseCase(sampleChecker, mapOf("Q5" to CheckerAnswer.YES))
        assertEquals(2, eval16.score)
        assertEquals(Verdict.SUSPICIOUS, eval16.verdict)

        // 17. Second critical Q5 YES + Q6 NOT_SURE -> LIKELY_SCAM
        val eval17 = runCheckerUseCase(sampleChecker, mapOf("Q5" to CheckerAnswer.YES, "Q6" to CheckerAnswer.NOT_SURE))
        assertEquals(3, eval17.score)
        assertEquals(Verdict.LIKELY_SCAM, eval17.verdict)

        // 18. Q3 critical YES + all others NO -> SUSPICIOUS
        val eval18 = runCheckerUseCase(sampleChecker, mapOf(
            "Q1" to CheckerAnswer.NO,
            "Q2" to CheckerAnswer.NO,
            "Q3" to CheckerAnswer.YES,
            "Q4" to CheckerAnswer.NO,
            "Q5" to CheckerAnswer.NO,
            "Q6" to CheckerAnswer.NO
        ))
        assertEquals(Verdict.SUSPICIOUS, eval18.verdict)

        // 19. All questions NOT_SURE (6 total points without critical YES) -> Score 6 -> LIKELY_SCAM
        val eval19 = runCheckerUseCase(sampleChecker, mapOf(
            "Q1" to CheckerAnswer.NOT_SURE,
            "Q2" to CheckerAnswer.NOT_SURE,
            "Q3" to CheckerAnswer.NOT_SURE,
            "Q4" to CheckerAnswer.NOT_SURE,
            "Q5" to CheckerAnswer.NOT_SURE,
            "Q6" to CheckerAnswer.NOT_SURE
        ))
        assertEquals(6, eval19.score)
        assertEquals(Verdict.LIKELY_SCAM, eval19.verdict)

        // 20. All questions YES -> Score 12 -> LIKELY_SCAM
        val eval20 = runCheckerUseCase(sampleChecker, sampleChecker.questions.associate { it.id to CheckerAnswer.YES })
        assertEquals(12, eval20.score)
        assertEquals(Verdict.LIKELY_SCAM, eval20.verdict)
    }
}
