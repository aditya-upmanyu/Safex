package com.safex.app.data.chatbot

import com.safex.app.domain.chatbot.model.AiReply
import com.safex.app.domain.chatbot.model.ChatLanguage
import com.safex.app.domain.chatbot.model.Verdict
import com.safex.app.domain.chatbot.repository.AiGuardRepository
import com.safex.app.domain.chatbot.repository.AiRequest
import com.safex.app.domain.chatbot.repository.AiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAiGuardRepository @Inject constructor() : AiGuardRepository {
    override suspend fun ask(request: AiRequest): AiResult {
        val reply = when (request.language) {
            ChatLanguage.HI -> AiReply(
                verdict = Verdict.SUSPICIOUS,
                headline = "सतर्क रहें: यह धोखाधड़ी का प्रयास हो सकता है",
                why = listOf(
                    "अनजान स्रोतों से आने वाले संदेशों में सुरक्षा जोखिम होता है।",
                    "साइबर अपराधी अक्सर जल्दबाजी या डर पैदा करके गलत कदम उठवाते हैं।"
                ),
                doNow = listOf(
                    "किसी भी अनजान लिंक या क्यूआर कोड को तुरंत न खोलें।",
                    "यदि पैसे का नुकसान हुआ है तो तुरंत 1930 पर संपर्क करें।"
                ),
                dont = listOf(
                    "अपना OTP, पिन या कार्ड विवरण कभी किसी को न बताएं।",
                    "अनजान कॉलर के कहने पर कोई ऐप इंस्टॉल न करें।"
                ),
                reportTo = listOf("1930", "cybercrime.gov.in"),
                suggestedTopicIds = listOf("otp_pin_asked", "money_lost"),
                followUpQuestions = listOf("यदि पैसे कट गए हों तो क्या करें?", "2FA कैसे चालू करें?")
            )
            ChatLanguage.HINGLISH -> AiReply(
                verdict = Verdict.SUSPICIOUS,
                headline = "Alert: Yeh fraud attempt ho sakta hai, savdhan rahein",
                why = listOf(
                    "Unknown messages aur calls mein cyber security risks hote hain.",
                    "Scammers fake urgency create karte hain taaki aap bina soche transfer kardein."
                ),
                doNow = listOf(
                    "Kisi bhi unknown link ya QR code ko scan na karein.",
                    "Agar paise debit hue hain toh turant 1930 par call karein."
                ),
                dont = listOf(
                    "Apna OTP, UPI PIN ya card details share na karein.",
                    "Caller ke kehne par screen share ya remote app install na karein."
                ),
                reportTo = listOf("1930", "cybercrime.gov.in"),
                suggestedTopicIds = listOf("otp_pin_asked", "money_lost"),
                followUpQuestions = listOf("Paise katne par kya karein?", "2FA kaise set karein?")
            )
            ChatLanguage.EN -> AiReply(
                verdict = Verdict.SUSPICIOUS,
                headline = "Caution: Potential Scam or Security Threat Detected",
                why = listOf(
                    "Unsolicited contacts demanding urgent action often indicate fraud.",
                    "Attackers use deceptive claims to harvest credentials or trigger payments."
                ),
                doNow = listOf(
                    "Do not open unsolicited links or scan unknown QR codes.",
                    "If financial loss occurred, call national helpline 1930 immediately."
                ),
                dont = listOf(
                    "Never disclose OTP, PIN, CVV, or passwords to anyone.",
                    "Do not install remote assistance applications upon request."
                ),
                reportTo = listOf("1930", "cybercrime.gov.in"),
                suggestedTopicIds = listOf("otp_pin_asked", "money_lost"),
                followUpQuestions = listOf("What to do if money was debited?", "How to enable 2FA?")
            )
        }
        return AiResult.Success(reply)
    }
}
