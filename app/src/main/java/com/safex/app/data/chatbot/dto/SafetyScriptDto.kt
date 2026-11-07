package com.safex.app.data.chatbot.dto

import com.google.gson.annotations.SerializedName
import com.safex.app.domain.chatbot.model.*

data class ActionDto(
    @SerializedName("type") val type: String? = null,
    @SerializedName("value") val value: String? = null
) {
    fun toDomain(): ScriptAction {
        val actionType = try {
            ActionType.valueOf(type?.uppercase() ?: "NONE")
        } catch (_: Exception) {
            ActionType.NONE
        }
        return ScriptAction(actionType, value ?: "")
    }
}

data class ContactDto(
    @SerializedName("label") val label: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("value") val value: String? = null
) {
    fun toDomain(): ContactInfo {
        val contactType = try {
            ContactType.valueOf(type?.uppercase() ?: "DIAL")
        } catch (_: Exception) {
            ContactType.DIAL
        }
        return ContactInfo(label ?: "", contactType, value ?: "")
    }
}

data class DoNowDto(
    @SerializedName("text") val text: String? = null,
    @SerializedName("action") val action: ActionDto? = null
) {
    fun toDomain(): DoNowStep = DoNowStep(
        text = text ?: "",
        action = action?.toDomain() ?: ScriptAction(ActionType.NONE, "")
    )
}

data class FollowUpChipDto(
    @SerializedName("label") val label: String? = null,
    @SerializedName("topic_id") val topicId: String? = null,
    @SerializedName("ask_ai") val askAi: String? = null
) {
    fun toDomain(): FollowUpChip = FollowUpChip(
        label = label ?: "",
        topicId = topicId,
        askAi = askAi
    )
}

data class TopicDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("chip_label") val chipLabel: String? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("urgency") val urgency: String? = null,
    @SerializedName("verdict_hint") val verdictHint: String? = null,
    @SerializedName("empathy_opening") val empathyOpening: String? = null,
    @SerializedName("headline") val headline: String? = null,
    @SerializedName("why") val why: List<String>? = null,
    @SerializedName("do_now") val doNow: List<DoNowDto>? = null,
    @SerializedName("dont") val dont: List<String>? = null,
    @SerializedName("contacts") val contacts: List<ContactDto>? = null,
    @SerializedName("related_video_ids") val relatedVideoIds: List<String>? = null,
    @SerializedName("video_tags") val videoTags: List<String>? = null,
    @SerializedName("checker_id") val checkerId: String? = null,
    @SerializedName("follow_up_chips") val followUpChips: List<FollowUpChipDto>? = null
) {
    fun toDomain(): SafetyTopic {
        val mappedUrgency = try {
            Urgency.valueOf(urgency?.uppercase() ?: "NORMAL")
        } catch (_: Exception) {
            Urgency.NORMAL
        }
        val mappedVerdict = try {
            Verdict.valueOf(verdictHint?.uppercase() ?: "INFO")
        } catch (_: Exception) {
            Verdict.INFO
        }
        return SafetyTopic(
            id = id ?: "",
            chipLabel = chipLabel ?: "",
            icon = icon ?: "info",
            urgency = mappedUrgency,
            verdictHint = mappedVerdict,
            empathyOpening = empathyOpening ?: "",
            headline = headline ?: "",
            why = why ?: emptyList(),
            doNow = doNow?.map { it.toDomain() } ?: emptyList(),
            dont = dont ?: emptyList(),
            contacts = contacts?.map { it.toDomain() } ?: emptyList(),
            relatedVideoIds = relatedVideoIds ?: emptyList(),
            videoTags = videoTags ?: emptyList(),
            checkerId = checkerId,
            followUpChips = followUpChips?.map { it.toDomain() } ?: emptyList()
        )
    }
}

data class CheckerQuestionDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("critical") val critical: Boolean? = null
) {
    fun toDomain(): CheckerQuestion = CheckerQuestion(
        id = id ?: "",
        text = text ?: "",
        critical = critical ?: false
    )
}

data class CheckerResultTextDto(
    @SerializedName("headline") val headline: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("next_topic") val nextTopic: String? = null
) {
    fun toDomain(): CheckerResultText = CheckerResultText(
        headline = headline ?: "",
        body = body ?: "",
        nextTopic = nextTopic
    )
}

data class CheckerDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("intro") val intro: String? = null,
    @SerializedName("questions") val questions: List<CheckerQuestionDto>? = null,
    @SerializedName("result_texts") val resultTexts: Map<String, CheckerResultTextDto>? = null
) {
    fun toDomain(): Checker {
        val mappedResults = mutableMapOf<Verdict, CheckerResultText>()
        resultTexts?.forEach { (key, dto) ->
            try {
                val verdict = Verdict.valueOf(key.uppercase())
                mappedResults[verdict] = dto.toDomain()
            } catch (_: Exception) {
            }
        }
        return Checker(
            id = id ?: "",
            title = title ?: "",
            intro = intro ?: "",
            questions = questions?.map { it.toDomain() } ?: emptyList(),
            resultTexts = mappedResults
        )
    }
}

data class TriageOptionDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("label") val label: String? = null
) {
    fun toDomain(): TriageOption = TriageOption(id ?: "", label ?: "")
}

data class UiStringsDto(
    @SerializedName("welcome") val welcome: String? = null,
    @SerializedName("triage_prompt") val triagePrompt: String? = null,
    @SerializedName("triage_options") val triageOptions: List<TriageOptionDto>? = null,
    @SerializedName("happened_prompt") val happenedPrompt: String? = null,
    @SerializedName("happened_options") val happenedOptions: List<TriageOptionDto>? = null,
    @SerializedName("learn_prompt") val learnPrompt: String? = null,
    @SerializedName("emergency_title") val emergencyTitle: String? = null,
    @SerializedName("emergency_body") val emergencyBody: String? = null,
    @SerializedName("listening") val listening: String? = null,
    @SerializedName("thinking") val thinking: String? = null,
    @SerializedName("speaking") val speaking: String? = null,
    @SerializedName("tap_to_talk") val tapToTalk: String? = null,
    @SerializedName("ai_unavailable") val aiUnavailable: String? = null,
    @SerializedName("rate_limited") val rateLimited: String? = null,
    @SerializedName("offline") val offline: String? = null,
    @SerializedName("disclaimer") val disclaimer: String? = null,
    @SerializedName("masked_notice") val maskedNotice: String? = null,
    @SerializedName("empty_videos") val emptyVideos: String? = null
) {
    fun toDomain(): ChatUiStrings = ChatUiStrings(
        welcome = welcome ?: "",
        triagePrompt = triagePrompt ?: "",
        triageOptions = triageOptions?.map { it.toDomain() } ?: emptyList(),
        happenedPrompt = happenedPrompt ?: "",
        happenedOptions = happenedOptions?.map { it.toDomain() } ?: emptyList(),
        learnPrompt = learnPrompt ?: "",
        emergencyTitle = emergencyTitle ?: "",
        emergencyBody = emergencyBody ?: "",
        listening = listening ?: "",
        thinking = thinking ?: "",
        speaking = speaking ?: "",
        tapToTalk = tapToTalk ?: "",
        aiUnavailable = aiUnavailable ?: "",
        rateLimited = rateLimited ?: "",
        offline = offline ?: "",
        disclaimer = disclaimer ?: "",
        maskedNotice = maskedNotice ?: "",
        emptyVideos = emptyVideos ?: ""
    )
}

data class ScriptBundleDto(
    @SerializedName("version") val version: Int? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("ui") val ui: UiStringsDto? = null,
    @SerializedName("topics") val topics: List<TopicDto>? = null,
    @SerializedName("checkers") val checkers: List<CheckerDto>? = null
) {
    fun toDomain(): SafetyScriptBundle = SafetyScriptBundle(
        version = version ?: 1,
        language = language ?: "en",
        ui = ui?.toDomain() ?: UiStringsDto().toDomain(),
        topics = topics?.map { it.toDomain() } ?: emptyList(),
        checkers = checkers?.map { it.toDomain() } ?: emptyList()
    )
}
