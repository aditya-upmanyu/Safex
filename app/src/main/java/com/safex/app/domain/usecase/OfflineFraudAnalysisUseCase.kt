package com.safex.app.domain.usecase

import com.safex.app.domain.model.FraudAnalysis
import com.safex.app.domain.model.RiskLevel
import com.safex.app.util.Constants
import javax.inject.Inject

/**
 * Offline fraud detection - works without API keys
 * Detects common Indian fraud patterns from voice transcripts
 */
class OfflineFraudAnalysisUseCase @Inject constructor() {

    suspend operator fun invoke(transcript: String): Result<FraudAnalysis> {
        return try {
            val analysis = analyzeTranscript(transcript)
            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun analyzeTranscript(transcript: String): FraudAnalysis {
        val lowerTranscript = transcript.lowercase()
        
        // Initialize scoring
        var riskScore = 0
        val detectedPatterns = mutableListOf<String>()
        val keywords = mutableListOf<String>()
        var verdict = RiskLevel.SAFE
        var confidence = 0f
        var reason = ""
        var reasonHindi = ""
        var recommendedAction = ""

        // 1. Digital Arrest Detection (Highest Priority)
        val digitalArrestMatch = checkDigitalArrest(lowerTranscript)
        if (digitalArrestMatch.found) {
            riskScore += 85
            detectedPatterns.add("Digital Arrest Scam")
            keywords.addAll(digitalArrestMatch.keywords)
            reason = "Detected claims of legal action by fake government authority (${digitalArrestMatch.authority})"
            reasonHindi = "नकली सरकारी अधिकारी द्वारा कानूनी कार्रवाई का दावा"
            recommendedAction = "DO NOT TRANSFER MONEY. Hang up immediately. Report to cyber.gov.in"
            recommendedAction += "\n1. Contact real ${digitalArrestMatch.authority} verification line\n2. File complaint at NCRP: cybercrime.gov.in\n3. Save call recording"
        }

        // 2. KYC/Bank Fraud
        val kycMatch = checkKycFraud(lowerTranscript)
        if (kycMatch.found) {
            riskScore += 75
            detectedPatterns.add("KYC/Bank Fraud")
            keywords.addAll(kycMatch.keywords)
            reason = "Caller claiming to be bank official requesting sensitive account information"
            reasonHindi = "बैंक अधिकारी होने का दावा करके खाता जानकारी मांग रहा है"
            recommendedAction = "Banks NEVER ask for passwords/OTP via calls. Hang up and call your bank directly."
        }

        // 3. OTP/Money Transfer
        val otpMatch = checkOtpFraud(lowerTranscript)
        if (otpMatch.found) {
            riskScore += 80
            detectedPatterns.add("OTP/Money Transfer Scam")
            keywords.addAll(otpMatch.keywords)
            reason = "Caller requesting OTP or asking to transfer money"
            reasonHindi = "OTP या पैसे ट्रांसफर करने के लिए कह रहा है"
            recommendedAction = "NEVER share OTP with anyone. Legitimate banks never request OTP via calls."
        }

        // 4. Lottery/Prize Scam
        val lotteryMatch = checkLotteryScam(lowerTranscript)
        if (lotteryMatch.found) {
            riskScore += 70
            detectedPatterns.add("Lottery/Prize Scam")
            keywords.addAll(lotteryMatch.keywords)
            reason = "Caller claiming you've won lottery/prize without participation"
            reasonHindi = "बिना भाग लिए लॉटरी जीतने का दावा"
            recommendedAction = "Legitimate lotteries don't call winners. Ignore and block."
        }

        // 5. Investment/Job Scam
        val investmentMatch = checkInvestmentScam(lowerTranscript)
        if (investmentMatch.found) {
            riskScore += 65
            detectedPatterns.add("Investment/Job Scam")
            keywords.addAll(investmentMatch.keywords)
            reason = "Unrealistic investment returns or job offer with upfront payment"
            reasonHindi = "अवास्तविक निवेश रिटर्न या नौकरी का प्रस्ताव"
            recommendedAction = "No legitimate job/investment requires upfront payment. Research company independently."
        }

        // 6. Courier/Package Scam
        val courierMatch = checkCourierScam(lowerTranscript)
        if (courierMatch.found) {
            riskScore += 60
            detectedPatterns.add("Courier/Package Scam")
            keywords.addAll(courierMatch.keywords)
            reason = "Fake package delivery claim asking for money or info"
            reasonHindi = "नकली पैकेज डिलीवरी का दावा करके पैसे या जानकारी मांग रहा है"
            recommendedAction = "Check with courier company directly. Don't share personal info."
        }

        // 7. UPI/Payment Fraud (NEW)
        val upiMatch = checkUpiPaymentFraud(lowerTranscript)
        if (upiMatch.found) {
            riskScore += 70
            detectedPatterns.add("UPI/Payment Fraud")
            keywords.addAll(upiMatch.keywords)
            reason = "Attempting to manipulate payment system or reverse transactions"
            reasonHindi = "पेमेंट सिस्टम को मेरी करने या लेनदेन को उलट देने का प्रयास"
            recommendedAction = "Never share UPI details. Contact your bank directly."
        }

        // 8. Tech Support Scam (NEW)
        val techMatch = checkTechSupportScam(lowerTranscript)
        if (techMatch.found) {
            riskScore += 65
            detectedPatterns.add("Tech Support Scam")
            keywords.addAll(techMatch.keywords)
            reason = "Fake tech support claiming device issues, requesting remote access"
            reasonHindi = "डिवाइस की समस्याओं का दावा करके रिमोट एक्सेस मांग रहा है"
            recommendedAction = "Legitimate tech support never asks for remote access unsolicited. Hang up."
        }

        // 7. Urgency/Pressure Tactics
        val urgencyMatch = checkUrgencyTactics(lowerTranscript)
        if (urgencyMatch.found) {
            riskScore += 20 // Amplifying factor
            detectedPatterns.add("High Pressure/Urgency Tactics")
            keywords.addAll(urgencyMatch.keywords)
            reason = reason + " | Using aggressive urgency tactics"
            reasonHindi = reasonHindi + " | आक्रामक जल्दबाजी की रणनीति"
        }

        // 8. Emotional Manipulation
        val emotionalMatch = checkEmotionalManipulation(lowerTranscript)
        if (emotionalMatch.found) {
            riskScore += 15 // Amplifying factor
            detectedPatterns.add("Emotional Manipulation")
            keywords.addAll(emotionalMatch.keywords)
        }

        // Normalize risk score to 0-100
        riskScore = riskScore.coerceIn(0, 100)

        // Determine verdict and confidence based on score
        when {
            riskScore >= 70 -> {
                verdict = RiskLevel.FRAUD
                confidence = when {
                    riskScore >= 85 -> 0.95f
                    riskScore >= 75 -> 0.85f
                    else -> 0.75f
                }
            }
            riskScore >= 40 -> {
                verdict = RiskLevel.SUSPICIOUS
                confidence = 0.70f
            }
            else -> {
                verdict = RiskLevel.SAFE
                confidence = 0.90f
                reason = "No significant fraud indicators detected"
                reasonHindi = "कोई महत्वपूर्ण धोखाधड़ी संकेत नहीं"
                recommendedAction = "Call appears safe. Normal conversation patterns detected."
            }
        }

        return FraudAnalysis(
            riskScore = riskScore,
            verdict = verdict,
            confidence = confidence,
            detectedPatterns = detectedPatterns,
            keywords = keywords,
            reason = reason.ifEmpty { "Could not determine risk level" },
            reasonHindi = reasonHindi.ifEmpty { "जोखिम स्तर निर्धारित नहीं कर सके" },
            recommendedAction = recommendedAction.ifEmpty { "Review call recording for suspicious content" }
        )
    }

    private fun checkDigitalArrest(text: String): PatternMatch {
        val patterns = mapOf(
            "CBI" to Constants.DIGITAL_ARREST_KEYWORDS.filter { it.contains("CBI", ignoreCase = true) },
            "ED" to Constants.DIGITAL_ARREST_KEYWORDS.filter { it.contains("ED", ignoreCase = true) },
            "RBI" to Constants.DIGITAL_ARREST_KEYWORDS.filter { it.contains("RBI", ignoreCase = true) },
            "Income Tax" to Constants.DIGITAL_ARREST_KEYWORDS.filter { it.contains("income tax", ignoreCase = true) }
        )

        for ((authority, keywords) in patterns) {
            val matchedKeywords = keywords.filter { text.contains(it, ignoreCase = true) }
            if (matchedKeywords.isNotEmpty()) {
                return PatternMatch(
                    found = true,
                    authority = authority,
                    keywords = matchedKeywords
                )
            }
        }

        return PatternMatch(found = false)
    }

    private fun checkKycFraud(text: String): SimpleMatch {
        val keywordMatches = Constants.KYC_FRAUD_KEYWORDS
            .filter { text.contains(it, ignoreCase = true) }
        
        val sensitiveInfoRequests = listOf(
            "password", "pin", "cvv", "otp", "account number", 
            "aadhar", "pan", "bank details", "card number"
        )
        val infoRequests = sensitiveInfoRequests.filter { text.contains(it, ignoreCase = true) }

        val found = keywordMatches.size >= 2 || (keywordMatches.isNotEmpty() && infoRequests.isNotEmpty())
        
        return SimpleMatch(
            found = found,
            keywords = (keywordMatches + infoRequests).distinct()
        )
    }

    private fun checkOtpFraud(text: String): SimpleMatch {
        val keywordMatches = Constants.OTP_FRAUD_KEYWORDS
            .filter { text.contains(it, ignoreCase = true) }
        
        val found = keywordMatches.size >= 1
        
        return SimpleMatch(
            found = found,
            keywords = keywordMatches
        )
    }

    private fun checkLotteryScam(text: String): SimpleMatch {
        val keywordMatches = Constants.LOTTERY_SCAM_KEYWORDS
            .filter { text.contains(it, ignoreCase = true) }
        
        val negativeIndicators = listOf(
            "congratulation", "won", "claimed", "prize", "reward",
            "jackpot", "lottery", "draw result"
        )
        val negativeMatches = negativeIndicators.filter { text.contains(it, ignoreCase = true) }

        val found = keywordMatches.isNotEmpty() || negativeMatches.size >= 2
        
        return SimpleMatch(
            found = found,
            keywords = (keywordMatches + negativeMatches).distinct()
        )
    }

    private fun checkInvestmentScam(text: String): SimpleMatch {
        val keywordMatches = Constants.INVESTMENT_SCAM_KEYWORDS
            .filter { text.contains(it, ignoreCase = true) }
        
        val found = keywordMatches.size >= 2
        
        return SimpleMatch(
            found = found,
            keywords = keywordMatches
        )
    }

    private fun checkCourierScam(text: String): SimpleMatch {
        val keywordMatches = Constants.COURIER_SCAM_KEYWORDS
            .filter { text.contains(it, ignoreCase = true) }
        
        val paymentRequests = listOf("pay", "money", "transfer", "fee", "charge", "deposit")
        val paymentMatches = paymentRequests.filter { text.contains(it, ignoreCase = true) }

        val found = keywordMatches.isNotEmpty() && paymentMatches.isNotEmpty()
        
        return SimpleMatch(
            found = found,
            keywords = (keywordMatches + paymentMatches).distinct()
        )
    }

    private fun checkUrgencyTactics(text: String): SimpleMatch {
        val urgencyKeywords = listOf(
            "immediately", "urgently", "right now", "don't wait", "act now",
            "time limited", "expires", "only today", "hurry", "quick",
            "fast", "asap", "emergency", "alert"
        )
        
        val urgencyMatches = urgencyKeywords.filter { text.contains(it, ignoreCase = true) }
        
        return SimpleMatch(
            found = urgencyMatches.size >= 2,
            keywords = urgencyMatches
        )
    }

    private fun checkEmotionalManipulation(text: String): SimpleMatch {
        val emotionalKeywords = listOf(
            "afraid", "worried", "scared", "trust", "believe",
            "deserve", "lucky", "family", "help", "urgent help",
            "problem", "issue", "concern", "attention"
        )
        
        val emotionalMatches = emotionalKeywords.filter { text.contains(it, ignoreCase = true) }
        
        return SimpleMatch(
            found = emotionalMatches.size >= 3,
            keywords = emotionalMatches
        )
    }

    private fun checkUpiPaymentFraud(text: String): SimpleMatch {
        val keywordMatches = Constants.UPI_FRAUD_KEYWORDS
            .filter { text.contains(it, ignoreCase = true) }
        
        val actionWords = listOf("reverse", "refund", "transfer", "link", "verify")
        val actionMatches = actionWords.filter { text.contains(it, ignoreCase = true) }

        val found = keywordMatches.isNotEmpty() && actionMatches.isNotEmpty()
        
        return SimpleMatch(
            found = found,
            keywords = (keywordMatches + actionMatches).distinct()
        )
    }

    private fun checkTechSupportScam(text: String): SimpleMatch {
        val keywordMatches = Constants.TECH_SUPPORT_KEYWORDS
            .filter { text.contains(it, ignoreCase = true) }
        
        val remoteAccessWords = listOf(
            "remote access", "allow access", "click link",
            "download", "install", "run command", "execute"
        )
        val remoteMatches = remoteAccessWords.filter { text.contains(it, ignoreCase = true) }

        val found = keywordMatches.size >= 2 || (keywordMatches.isNotEmpty() && remoteMatches.isNotEmpty())
        
        return SimpleMatch(
            found = found,
            keywords = (keywordMatches + remoteMatches).distinct()
        )
    }

    private data class PatternMatch(
        val found: Boolean,
        val authority: String = "",
        val keywords: List<String> = emptyList()
    )

    private data class SimpleMatch(
        val found: Boolean,
        val keywords: List<String> = emptyList()
    )
}
