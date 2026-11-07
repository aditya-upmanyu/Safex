package com.safex.app.domain.chatbot.usecase

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaskSensitiveDataUseCaseImpl @Inject constructor() : MaskSensitiveDataUseCase {

    private val devanagariDigits = mapOf(
        '०' to '0', '१' to '1', '२' to '2', '३' to '3', '४' to '4',
        '५' to '5', '६' to '6', '७' to '7', '८' to '8', '९' to '9'
    )

    override fun invoke(rawText: String): MaskResult {
        if (rawText.isBlank()) {
            return MaskResult("", emptyList())
        }

        // Convert Devanagari digits to ASCII for consistent pattern evaluation
        val mappedText = StringBuilder()
        for (ch in rawText) {
            mappedText.append(devanagariDigits[ch] ?: ch)
        }
        var current = mappedText.toString()

        val replacements = mutableListOf<MaskReplacement>()

        // 1. Preserve URLs and URLs with parameters first so URLs are never corrupted
        val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
        val urlHolders = mutableListOf<String>()
        current = urlRegex.replace(current) { match ->
            urlHolders.add(match.value)
            "__URL_PRESERVED_${urlHolders.size - 1}__"
        }

        // 2. Preserve IFSC codes (4 letters, 0, 6 alphanumeric): e.g. SBIN0001234
        val ifscRegex = Regex("""\b[A-Z]{4}0[A-Z0-9]{6}\b""", RegexOption.IGNORE_CASE)
        val ifscHolders = mutableListOf<String>()
        current = ifscRegex.replace(current) { match ->
            ifscHolders.add(match.value)
            "__IFSC_PRESERVED_${ifscHolders.size - 1}__"
        }

        // 3. Secret codes after otp / pin / cvv / password words (within 25 chars)
        // e.g. "otp hai 4821", "सीवीवी 321", "my pin is 908123"
        val codeTriggerRegex = Regex(
            """(?iU)\b(otp|cvv|cvc|password|passcode|ओटीपी|पिन|सीवीवी|pin(?!\s+code))\b([^0-9\n]{1,25}?)([0-9]{3,8})\b"""
        )
        var codeCount = 0
        current = codeTriggerRegex.replace(current) { match ->
            codeCount++
            val trigger = match.groupValues[1]
            val separator = match.groupValues[2]
            "$trigger$separator[CODE]"
        }
        if (codeCount > 0) replacements.add(MaskReplacement("CODE", codeCount))

        // 4. PAN card: 5 letters + 4 digits + 1 letter (e.g. ABCDE1234F)
        val panRegex = Regex("""\b[A-Za-z]{5}[0-9]{4}[A-Za-z]\b""")
        val panMatches = panRegex.findAll(current).count()
        if (panMatches > 0) {
            current = panRegex.replace(current, "[PAN]")
            replacements.add(MaskReplacement("PAN", panMatches))
        }

        // 5. Bank Account: 9-18 digits following words account, a/c, khata, खाता
        val accountRegex = Regex(
            """(?iU)\b(account|a/c|acct|khata|खाता)\b[^0-9\n]{1,20}?([0-9]{9,18})\b"""
        )
        var accountCount = 0
        current = accountRegex.replace(current) { match ->
            accountCount++
            val word = match.groupValues[1]
            "$word [ACCOUNT]"
        }
        if (accountCount > 0) replacements.add(MaskReplacement("ACCOUNT", accountCount))

        // 6. Payment Card numbers: 13-19 digits (optionally grouped in 4-4-4-4)
        val cardRegex = Regex("""\b(?:[0-9]{4}[\s-]){3}[0-9]{4}\b|\b[0-9]{13,19}\b""")
        var cardCount = 0
        current = cardRegex.replace(current) { match ->
            val digitsOnly = match.value.replace(Regex("""[\s-]"""), "")
            if (digitsOnly.length in 13..19) {
                cardCount++
                "[CARD]"
            } else {
                match.value
            }
        }
        if (cardCount > 0) replacements.add(MaskReplacement("CARD", cardCount))

        // 7. Aadhaar Number: exactly 12 digits (optionally 4-4-4 grouped with spaces or dashes) starting with 2-9
        val aadhaarRegex = Regex("""\b[2-9][0-9]{3}[\s-]?[0-9]{4}[\s-]?[0-9]{4}(?![\s-]?[0-9])\b""")
        val aadhaarMatches = aadhaarRegex.findAll(current).count()
        if (aadhaarMatches > 0) {
            current = aadhaarRegex.replace(current, "[AADHAAR]")
            replacements.add(MaskReplacement("AADHAAR", aadhaarMatches))
        }

        // 8. UPI ID: something@bankhandle (e.g. rahul@okaxis, 9876543210@paytm)
        val upiRegex = Regex("""\b([A-Za-z0-9._-]+)@(okhdfcbank|okaxis|oksbi|okicici|paytm|ybl|ibl|axl|apl|upi)\b""", RegexOption.IGNORE_CASE)
        var upiCount = 0
        current = upiRegex.replace(current) { match ->
            upiCount++
            val local = match.groupValues[1]
            val handle = match.groupValues[2]
            val maskedLocal = if (local.length > 2) "${local.take(1)}***" else "***"
            "$maskedLocal@$handle"
        }
        if (upiCount > 0) replacements.add(MaskReplacement("UPI", upiCount))

        // 9. Indian Mobile numbers: 10 digits starting with 6-9, optional +91 or 0 prefix
        val mobileRegex = Regex("""(?:\+91[\s-]?)?(?:\b0)?([6-9][0-9]{9})\b""")
        var mobileCount = 0
        current = mobileRegex.replace(current) { match ->
            mobileCount++
            val tenDigits = match.groupValues[1]
            val masked = "${tenDigits.take(2)}XXXXXX${tenDigits.takeLast(2)}"
            masked
        }
        if (mobileCount > 0) replacements.add(MaskReplacement("MOBILE", mobileCount))

        // 10. Email addresses: keep domain, mask local part (e.g. rahul123@gmail.com -> r***@gmail.com)
        val emailRegex = Regex("""\b([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\.[A-Za-z]{2,})\b""")
        var emailCount = 0
        current = emailRegex.replace(current) { match ->
            emailCount++
            val local = match.groupValues[1]
            val domain = match.groupValues[2]
            val maskedLocal = if (local.length > 1) "${local.take(1)}***" else "***"
            "$maskedLocal@$domain"
        }
        if (emailCount > 0) replacements.add(MaskReplacement("EMAIL", emailCount))

        // Restore preserved URLs
        urlHolders.forEachIndexed { index, url ->
            current = current.replace("__URL_PRESERVED_${index}__", url)
        }

        // Restore preserved IFSCs
        ifscHolders.forEachIndexed { index, ifsc ->
            current = current.replace("__IFSC_PRESERVED_${index}__", ifsc)
        }

        return MaskResult(current, replacements)
    }
}
