package com.safex.app.chatbot

import com.safex.app.domain.chatbot.model.EmergencyLevel
import com.safex.app.domain.chatbot.usecase.DetectEmergencyUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DetectEmergencyUseCaseTest {

    private lateinit var detectEmergencyUseCase: DetectEmergencyUseCase

    @Before
    fun setUp() {
        detectEmergencyUseCase = DetectEmergencyUseCase()
    }

    @Test
    fun testEmergencyDetection15Cases() {
        // 1
        val r1 = detectEmergencyUseCase("mujhe digital arrest bola hai")
        assertTrue(r1.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r1.level)
        assertEquals("digital_arrest", r1.topicId)

        // 2
        val r2 = detectEmergencyUseCase("police arrest warrant video call")
        assertTrue(r2.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r2.level)
        assertEquals("digital_arrest", r2.topicId)

        // 3
        val r3 = detectEmergencyUseCase("सीबीआई ने डिजिटल अरेस्ट कर लिया")
        assertTrue(r3.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r3.level)
        assertEquals("digital_arrest", r3.topicId)

        // 4
        val r4 = detectEmergencyUseCase("paise kat gaye mere account se")
        assertTrue(r4.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r4.level)
        assertEquals("money_lost", r4.topicId)

        // 5
        val r5 = detectEmergencyUseCase("paisa transfer kar diya galti se")
        assertTrue(r5.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r5.level)
        assertEquals("money_lost", r5.topicId)

        // 6
        val r6 = detectEmergencyUseCase("मेरे बैंक से पैसे कट गए हैं अभी")
        assertTrue(r6.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r6.level)
        assertEquals("money_lost", r6.topicId)

        // 7
        val r7 = detectEmergencyUseCase("unauthorized money debited right now")
        assertTrue(r7.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r7.level)
        assertEquals("money_lost", r7.topicId)

        // 8
        val r8 = detectEmergencyUseCase("mujhe photo viral karne ki dhamki di")
        assertTrue(r8.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r8.level)
        assertEquals("blackmail", r8.topicId)

        // 9
        val r9 = detectEmergencyUseCase("someone is blackmailing me with morphed photos")
        assertTrue(r9.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r9.level)
        assertEquals("blackmail", r9.topicId)

        // 10
        val r10 = detectEmergencyUseCase("वीडियो वायरल करने की धमकी दे रहा है")
        assertTrue(r10.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r10.level)
        assertEquals("blackmail", r10.topicId)

        // 11
        val r11 = detectEmergencyUseCase("mera whatsapp hack ho gaya hai")
        assertTrue(r11.isEmergency)
        assertEquals(EmergencyLevel.URGENT, r11.level)
        assertEquals("account_hacked", r11.topicId)

        // 12
        val r12 = detectEmergencyUseCase("account hacked cannot login")
        assertTrue(r12.isEmergency)
        assertEquals(EmergencyLevel.URGENT, r12.level)
        assertEquals("account_hacked", r12.topicId)

        // 13
        val r13 = detectEmergencyUseCase("खाता हैक हो गया है")
        assertTrue(r13.isEmergency)
        assertEquals(EmergencyLevel.URGENT, r13.level)
        assertEquals("account_hacked", r13.topicId)

        // 14
        val r14 = detectEmergencyUseCase("otp share kar diya scammer ko")
        assertTrue(r14.isEmergency)
        assertEquals(EmergencyLevel.EMERGENCY, r14.level)
        assertEquals("money_lost", r14.topicId)

        // 15 - Non-emergency
        val r15 = detectEmergencyUseCase("i just want to learn about general cybersecurity tips")
        assertFalse(r15.isEmergency)
        assertEquals(EmergencyLevel.NONE, r15.level)
        assertNull(r15.topicId)
    }
}
