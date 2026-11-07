package com.safex.app.chatbot

import com.safex.app.domain.chatbot.usecase.MaskSensitiveDataUseCaseImpl
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MaskSensitiveDataUseCaseTest {

    private lateinit var maskUseCase: MaskSensitiveDataUseCaseImpl

    @Before
    fun setUp() {
        maskUseCase = MaskSensitiveDataUseCaseImpl()
    }

    @Test
    fun test40MaskingCases() {
        // --- 1 to 5: Aadhaar Cases ---
        // 1. Plain 12-digit Aadhaar
        val m1 = maskUseCase("My aadhaar is 234567890123")
        assertTrue(m1.maskedText.contains("[AADHAAR]"))
        assertFalse(m1.maskedText.contains("234567890123"))

        // 2. 4-4-4 space-separated Aadhaar
        val m2 = maskUseCase("Aadhaar: 3456 7890 1234")
        assertTrue(m2.maskedText.contains("[AADHAAR]"))

        // 3. 4-4-4 dash-separated Aadhaar
        val m3 = maskUseCase("Aadhaar: 4567-8901-2345")
        assertTrue(m3.maskedText.contains("[AADHAAR]"))

        // 4. Devanagari digits Aadhaar: २३४५६७८९०१२३
        val m4 = maskUseCase("आधार नंबर २३४५६७८९०१२३")
        assertTrue(m4.maskedText.contains("[AADHAAR]"))

        // 5. In complex sentence
        val m5 = maskUseCase("I gave my number 9876-5432-1098 to the caller")
        assertTrue(m5.maskedText.contains("[AADHAAR]"))

        // --- 6 to 10: Card Numbers ---
        // 6. 16-digit contiguous card
        val m6 = maskUseCase("Card number 4111222233334444")
        assertTrue(m6.maskedText.contains("[CARD]"))

        // 7. 4-4-4-4 spaced card
        val m7 = maskUseCase("Card 5123 4567 8901 2345")
        assertTrue(m7.maskedText.contains("[CARD]"))

        // 8. 4-4-4-4 dashed card
        val m8 = maskUseCase("Card 6011-1234-5678-9012")
        assertTrue(m8.maskedText.contains("[CARD]"))

        // 9. 13-digit card
        val m9 = maskUseCase("Old card 4000123456789")
        assertTrue(m9.maskedText.contains("[CARD]"))

        // 10. 19-digit card
        val m10 = maskUseCase("Card 4000123456789012345")
        assertTrue(m10.maskedText.contains("[CARD]"))

        // --- 11 to 15: Secret Codes (OTP, PIN, CVV) ---
        // 11. Hinglish "otp hai 4821"
        val m11 = maskUseCase("otp hai 4821")
        assertTrue(m11.maskedText.contains("[CODE]"))
        assertFalse(m11.maskedText.contains("4821"))

        // 12. Hindi "सीवीवी 928"
        val m12 = maskUseCase("सीवीवी 928")
        assertTrue(m12.maskedText.contains("[CODE]"))

        // 13. "pin is 123456"
        val m13 = maskUseCase("my secret pin is 123456")
        assertTrue(m13.maskedText.contains("[CODE]"))

        // 14. "password: 87654321"
        val m14 = maskUseCase("password: 87654321")
        assertTrue(m14.maskedText.contains("[CODE]"))

        // 15. Devanagari digits after ओटीपी: "ओटीपी ४८२१"
        val m15 = maskUseCase("ओटीपी ४८२१")
        assertTrue(m15.maskedText.contains("[CODE]"))

        // --- 16 to 20: False Positives (MUST NOT MASK) ---
        // 16. Year 2025
        val m16 = maskUseCase("In the year 2025 digital crime increased")
        assertTrue(m16.maskedText.contains("2025"))
        assertFalse(m16.maskedText.contains("[CODE]"))

        // 17. Year 2024
        val m17 = maskUseCase("Report was released in 2024")
        assertTrue(m17.maskedText.contains("2024"))

        // 18. Postal PIN code of a location (6 digits without trigger word)
        val m18 = maskUseCase("My delivery address is in New Delhi 110001 near CP")
        assertTrue(m18.maskedText.contains("110001"))
        assertFalse(m18.maskedText.contains("[CODE]"))

        // 19. Currency amount 15000
        val m19 = maskUseCase("They demanded 15000 rupees immediately")
        assertTrue(m19.maskedText.contains("15000"))
        assertFalse(m19.maskedText.contains("[CODE]"))

        // 20. Large amount 50000 rs
        val m20 = maskUseCase("Lost 50000 rs in fake investment")
        assertTrue(m20.maskedText.contains("50000"))
        assertFalse(m20.maskedText.contains("[CODE]"))

        // --- 21 to 25: PAN Card ---
        // 21. Uppercase PAN
        val m21 = maskUseCase("My PAN is ABCDE1234F")
        assertTrue(m21.maskedText.contains("[PAN]"))
        assertFalse(m21.maskedText.contains("ABCDE1234F"))

        // 22. Lowercase PAN
        val m22 = maskUseCase("pan number is abcde1234f")
        assertTrue(m22.maskedText.contains("[PAN]"))

        // 23. PAN in Hindi sentence
        val m23 = maskUseCase("मेरा पैन कार्ड नंबर BZRPK9876L है")
        assertTrue(m23.maskedText.contains("[PAN]"))

        // 24. PAN with colon
        val m24 = maskUseCase("PAN: ALMPR4321K")
        assertTrue(m24.maskedText.contains("[PAN]"))

        // 25. Another PAN
        val m25 = maskUseCase("verified with pan CZXPQ5544M")
        assertTrue(m25.maskedText.contains("[PAN]"))

        // --- 26 to 30: Bank Account Numbers ---
        // 26. account no 123456789012
        val m26 = maskUseCase("account no 123456789012")
        assertTrue(m26.maskedText.contains("[ACCOUNT]"))

        // 27. a/c 987654321012
        val m27 = maskUseCase("money went from a/c: 987654321012")
        assertTrue(m27.maskedText.contains("[ACCOUNT]"))

        // 28. Hindi खाता संख्या 112233445566
        val m28 = maskUseCase("खाता संख्या 112233445566")
        assertTrue(m28.maskedText.contains("[ACCOUNT]"))

        // 29. Hinglish khata 99887766554433
        val m29 = maskUseCase("khata no 99887766554433 se transfer hua")
        assertTrue(m29.maskedText.contains("[ACCOUNT]"))

        // 30. acct 556677889900
        val m30 = maskUseCase("acct 556677889900")
        assertTrue(m30.maskedText.contains("[ACCOUNT]"))

        // --- 31 to 35: Indian Mobile Numbers ---
        // 31. 10-digit starting 98
        val m31 = maskUseCase("Call me on 9876543210")
        assertTrue(m31.maskedText.contains("98XXXXXX10"))

        // 32. +91 prefix
        val m32 = maskUseCase("Phone +91 9812345678")
        assertTrue(m32.maskedText.contains("98XXXXXX78"))

        // 33. Starting with 8
        val m33 = maskUseCase("Number 8765432109")
        assertTrue(m33.maskedText.contains("87XXXXXX09"))

        // 34. Starting with 7
        val m34 = maskUseCase("Number 7890123456")
        assertTrue(m34.maskedText.contains("78XXXXXX56"))

        // 35. Starting with 6
        val m35 = maskUseCase("Number 6123456789")
        assertTrue(m35.maskedText.contains("61XXXXXX89"))

        // --- 36 to 38: Email Addresses ---
        // 36. Gmail address
        val m36 = maskUseCase("Email is john.doe@gmail.com")
        assertTrue(m36.maskedText.contains("@gmail.com"))
        assertFalse(m36.maskedText.contains("john.doe@gmail.com"))

        // 37. Phishing domain email
        val m37 = maskUseCase("From alert@sbi-updates.biz")
        assertTrue(m37.maskedText.contains("@sbi-updates.biz"))
        assertFalse(m37.maskedText.contains("alert@sbi-updates.biz"))

        // 38. Another email
        val m38 = maskUseCase("Contact victim@yahoo.co.in")
        assertTrue(m38.maskedText.contains("@yahoo.co.in"))

        // --- 39: UPI ID ---
        val m39 = maskUseCase("Pay to rahul.kumar@okaxis")
        assertTrue(m39.maskedText.contains("@okaxis"))
        assertFalse(m39.maskedText.contains("rahul.kumar@okaxis"))

        // --- 40: Preserved IFSC & URLs ---
        val m40 = maskUseCase("Check https://cybercrime.gov.in and IFSC code SBIN0001234")
        assertTrue("URL must be preserved", m40.maskedText.contains("https://cybercrime.gov.in"))
        assertTrue("IFSC must be preserved", m40.maskedText.contains("SBIN0001234"))
    }
}
