package com.safex.app.chatbot

import com.google.gson.Gson
import com.safex.app.data.chatbot.dto.ScriptBundleDto
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.FileReader

class ScriptAssetValidationTest {

    private val gson = Gson()
    private val assetsDir = File("src/main/assets/chat")

    private fun loadBundle(fileName: String): ScriptBundleDto {
        val file = File(assetsDir, fileName)
        assertTrue("Asset file $fileName must exist at ${file.absolutePath}", file.exists())
        FileReader(file).use { reader ->
            return gson.fromJson(reader, ScriptBundleDto::class.java)
        }
    }

    @Test
    fun testScriptFilesLoadAndHaveIdenticalIds() {
        val en = loadBundle("scripts_en.json")
        val hi = loadBundle("scripts_hi.json")
        val hinglish = loadBundle("scripts_hinglish.json")

        assertNotNull("English bundle null", en)
        assertNotNull("Hindi bundle null", hi)
        assertNotNull("Hinglish bundle null", hinglish)

        val enTopicIds = en.topics?.mapNotNull { it.id } ?: emptyList()
        val hiTopicIds = hi.topics?.mapNotNull { it.id } ?: emptyList()
        val hinglishTopicIds = hinglish.topics?.mapNotNull { it.id } ?: emptyList()

        assertEquals("Topic count must match across languages", 10, enTopicIds.size)
        assertEquals("Hindi topic ids must match English", enTopicIds, hiTopicIds)
        assertEquals("Hinglish topic ids must match English", enTopicIds, hinglishTopicIds)

        val enCheckerIds = en.checkers?.mapNotNull { it.id } ?: emptyList()
        val hiCheckerIds = hi.checkers?.mapNotNull { it.id } ?: emptyList()
        val hinglishCheckerIds = hinglish.checkers?.mapNotNull { it.id } ?: emptyList()

        assertEquals("Checker count must match", 3, enCheckerIds.size)
        assertEquals("Hindi checker ids must match English", enCheckerIds, hiCheckerIds)
        assertEquals("Hinglish checker ids must match English", enCheckerIds, hinglishCheckerIds)
    }

    @Test
    fun testNoEmptyStringsInRequiredFields() {
        val bundles = listOf(
            "en" to loadBundle("scripts_en.json"),
            "hi" to loadBundle("scripts_hi.json"),
            "hinglish" to loadBundle("scripts_hinglish.json")
        )

        for ((lang, bundle) in bundles) {
            assertNotNull("UI block missing in $lang", bundle.ui)
            assertTrue("welcome empty in $lang", !bundle.ui?.welcome.isNullOrBlank())
            assertTrue("emergency_title empty in $lang", !bundle.ui?.emergencyTitle.isNullOrBlank())
            assertTrue("disclaimer empty in $lang", !bundle.ui?.disclaimer.isNullOrBlank())

            bundle.topics?.forEach { topic ->
                val tid = topic.id
                assertTrue("Topic id empty in $lang", !tid.isNullOrBlank())
                assertTrue("empathyOpening empty in $lang topic $tid", !topic.empathyOpening.isNullOrBlank())
                assertTrue("headline empty in $lang topic $tid", !topic.headline.isNullOrBlank())
                assertTrue("why empty in $lang topic $tid", !topic.why.isNullOrEmpty())
                assertTrue("doNow empty in $lang topic $tid", !topic.doNow.isNullOrEmpty())
                assertTrue("dont empty in $lang topic $tid", !topic.dont.isNullOrEmpty())
                assertTrue("contacts empty in $lang topic $tid", !topic.contacts.isNullOrEmpty())
            }

            bundle.checkers?.forEach { checker ->
                val cid = checker.id
                assertTrue("Checker id empty in $lang", !cid.isNullOrBlank())
                assertTrue("Checker title empty in $lang", !checker.title.isNullOrBlank())
                assertTrue("Checker questions empty in $lang $cid", !checker.questions.isNullOrEmpty())
                assertTrue("Checker result_texts empty in $lang $cid", !checker.resultTexts.isNullOrEmpty())
            }
        }
    }

    @Test
    fun testEveryTopicAndCheckerReferenceResolves() {
        val bundle = loadBundle("scripts_en.json")
        val allTopicIds = bundle.topics?.mapNotNull { it.id }?.toSet() ?: emptySet()
        val allCheckerIds = bundle.checkers?.mapNotNull { it.id }?.toSet() ?: emptySet()

        bundle.topics?.forEach { topic ->
            topic.checkerId?.let { cid ->
                assertTrue("checkerId '$cid' in topic '${topic.id}' does not exist", allCheckerIds.contains(cid))
            }

            topic.doNow?.forEach { step ->
                val action = step.action
                if (action?.type == "OPEN_TOPIC") {
                    val targetTopic = action.value
                    assertTrue("OPEN_TOPIC target '$targetTopic' in topic '${topic.id}' does not exist", allTopicIds.contains(targetTopic))
                }
                if (action?.type == "OPEN_CHECKER") {
                    val targetChecker = action.value
                    assertTrue("OPEN_CHECKER target '$targetChecker' in topic '${topic.id}' does not exist", allCheckerIds.contains(targetChecker))
                }
            }

            topic.followUpChips?.forEach { chip ->
                chip.topicId?.let { targetTopic ->
                    assertTrue("followUpChip target '$targetTopic' in topic '${topic.id}' does not exist", allTopicIds.contains(targetTopic))
                }
            }
        }

        bundle.checkers?.forEach { checker ->
            checker.resultTexts?.forEach { (_, textDto) ->
                textDto.nextTopic?.let { nextTid ->
                    assertTrue("next_topic '$nextTid' in checker '${checker.id}' does not exist", allTopicIds.contains(nextTid))
                }
            }
        }
    }
}
