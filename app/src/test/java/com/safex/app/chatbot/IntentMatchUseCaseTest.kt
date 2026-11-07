package com.safex.app.chatbot

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.safex.app.domain.chatbot.model.ChatLanguage
import com.safex.app.domain.chatbot.model.Checker
import com.safex.app.domain.chatbot.model.SafetyScriptBundle
import com.safex.app.domain.chatbot.model.SafetyTopic
import com.safex.app.domain.chatbot.repository.ScriptRepository
import com.safex.app.domain.chatbot.usecase.MatchIntentUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileReader

class IntentMatchUseCaseTest {

    private lateinit var matchIntentUseCase: MatchIntentUseCase

    @Before
    fun setUp() {
        val keywordsFile = File("src/main/assets/chat/intent_keywords.json")
        val gson = Gson()
        val type = object : TypeToken<Map<String, List<String>>>() {}.type
        val keywordsMap: Map<String, List<String>> = FileReader(keywordsFile).use { reader ->
            gson.fromJson(reader, type)
        }

        val testRepo = object : ScriptRepository {
            override suspend fun getScriptBundle(language: ChatLanguage): SafetyScriptBundle = throw NotImplementedError()
            override suspend fun getTopic(topicId: String, language: ChatLanguage): SafetyTopic? = null
            override suspend fun getChecker(checkerId: String, language: ChatLanguage): Checker? = null
            override suspend fun getIntentKeywords(): Map<String, List<String>> = keywordsMap
        }

        matchIntentUseCase = MatchIntentUseCase(testRepo)
    }

    @Test
    fun testIntentMatching40Cases() = runBlocking {
        // --- 10 ENGLISH CASES ---
        assertEquals("email_check", matchIntentUseCase("I got a phishing email from bank").bestTopicId)
        assertEquals("call_received", matchIntentUseCase("Suspicious fake call from fedex").bestTopicId)
        assertEquals("setup_2fa", matchIntentUseCase("How to set up two step verification code").bestTopicId)
        assertEquals("otp_pin_asked", matchIntentUseCase("Someone asked for my bank otp and pin").bestTopicId)
        assertEquals("money_lost", matchIntentUseCase("Fraud transaction happened and money lost").bestTopicId)
        assertEquals("link_qr_check", matchIntentUseCase("Is this link or qr code safe to scan").bestTopicId)
        assertEquals("digital_arrest", matchIntentUseCase("They say I have a digital arrest warrant").bestTopicId)
        assertEquals("account_hacked", matchIntentUseCase("My whatsapp account got hacked").bestTopicId)
        assertEquals("blackmail", matchIntentUseCase("Someone is doing blackmail with video viral threat").bestTopicId)
        assertEquals("shared_info", matchIntentUseCase("I shared my aadhaar card details").bestTopicId)

        // --- 10 HINDI DEVANAGARI CASES ---
        assertEquals("email_check", matchIntentUseCase("मुझे फ़िशिंग ईमेल आया है").bestTopicId)
        assertEquals("call_received", matchIntentUseCase("अनजान नंबर से फर्जी कॉल आई है").bestTopicId)
        assertEquals("setup_2fa", matchIntentUseCase("टू स्टेप वेरिफिकेशन कैसे लगाएं").bestTopicId)
        assertEquals("otp_pin_asked", matchIntentUseCase("कॉल पर ओटीपी और पिन मांगा").bestTopicId)
        assertEquals("money_lost", matchIntentUseCase("मेरे खाते से पैसे कट गए हैं").bestTopicId)
        assertEquals("link_qr_check", matchIntentUseCase("क्या यह क्यूआर कोड स्कैन सुरक्षित है").bestTopicId)
        assertEquals("digital_arrest", matchIntentUseCase("डिजिटल अरेस्ट और गिरफ्तारी का वारंट").bestTopicId)
        assertEquals("account_hacked", matchIntentUseCase("मेरा व्हाट्सएप हैक हो गया है").bestTopicId)
        assertEquals("blackmail", matchIntentUseCase("वीडियो वायरल करने की धमकी और ब्लैकमेल").bestTopicId)
        assertEquals("shared_info", matchIntentUseCase("मैंने अपना आधार और पैन कार्ड शेयर कर दिया").bestTopicId)

        // --- 10 HINGLISH CASES ---
        assertEquals("email_check", matchIntentUseCase("phishing mail aaya hai").bestTopicId)
        assertEquals("call_received", matchIntentUseCase("fake courier call aaya tha").bestTopicId)
        assertEquals("setup_2fa", matchIntentUseCase("two step kaise kare whatsapp pe").bestTopicId)
        assertEquals("otp_pin_asked", matchIntentUseCase("caller ne otp maanga aur pin pucha").bestTopicId)
        assertEquals("money_lost", matchIntentUseCase("mere paise kat gaye account empty ho gaya").bestTopicId)
        assertEquals("link_qr_check", matchIntentUseCase("payment link aaya aur qr scan bola").bestTopicId)
        assertEquals("digital_arrest", matchIntentUseCase("police video call digital arrest bola").bestTopicId)
        assertEquals("account_hacked", matchIntentUseCase("account hack ho gaya mera").bestTopicId)
        assertEquals("blackmail", matchIntentUseCase("video viral karne ki dhamki de raha hai blackmail").bestTopicId)
        assertEquals("shared_info", matchIntentUseCase("aadhaar details share kar di maine").bestTopicId)

        // --- 10 SHOULD-RETURN-NULL CASES (-> AI) ---
        assertNull(matchIntentUseCase("what is the weather in Delhi today").bestTopicId)
        assertNull(matchIntentUseCase("recipe for cooking butter chicken at home").bestTopicId)
        assertNull(matchIntentUseCase("tell me a funny and hilarious joke").bestTopicId)
        assertNull(matchIntentUseCase("who won the cricket world cup match yesterday").bestTopicId)
        assertNull(matchIntentUseCase("how to bake chocolate sponge cake").bestTopicId)
        assertNull(matchIntentUseCase("good morning have a wonderful and pleasant day").bestTopicId)
        assertNull(matchIntentUseCase("what is the capital city of France").bestTopicId)
        assertNull(matchIntentUseCase("how to fix a bicycle tire flat puncture").bestTopicId)
        assertNull(matchIntentUseCase("play the latest soothing hindi bollywood songs").bestTopicId)
        assertNull(matchIntentUseCase("explain how astronauts live in space exploration stations").bestTopicId)
    }
}
