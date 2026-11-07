package com.safex.app.domain.chatbot.model

data class SafetyTopic(
    val id: String,
    val chipLabel: String,
    val icon: String,
    val urgency: Urgency,
    val verdictHint: Verdict,
    val empathyOpening: String,
    val headline: String,
    val why: List<String>,
    val doNow: List<DoNowStep>,
    val dont: List<String>,
    val contacts: List<ContactInfo>,
    val relatedVideoIds: List<String> = emptyList(),
    val videoTags: List<String> = emptyList(),
    val checkerId: String? = null,
    val followUpChips: List<FollowUpChip> = emptyList()
)

data class CheckerQuestion(
    val id: String,
    val text: String,
    val critical: Boolean = false
)

data class CheckerResultText(
    val headline: String,
    val body: String,
    val nextTopic: String? = null
)

data class Checker(
    val id: String,
    val title: String,
    val intro: String,
    val questions: List<CheckerQuestion>,
    val resultTexts: Map<Verdict, CheckerResultText>
)

enum class CheckerAnswer {
    YES,
    NO,
    NOT_SURE
}

data class CheckerEvaluation(
    val checkerId: String,
    val score: Int,
    val verdict: Verdict,
    val resultText: CheckerResultText,
    val answeredCount: Int,
    val totalCount: Int,
    val criticalYesCount: Int
)

data class TriageOption(
    val id: String,
    val label: String
)

data class ChatUiStrings(
    val welcome: String,
    val triagePrompt: String,
    val triageOptions: List<TriageOption>,
    val happenedPrompt: String,
    val happenedOptions: List<TriageOption>,
    val learnPrompt: String,
    val emergencyTitle: String,
    val emergencyBody: String,
    val listening: String,
    val thinking: String,
    val speaking: String,
    val tapToTalk: String,
    val aiUnavailable: String,
    val rateLimited: String,
    val offline: String,
    val disclaimer: String,
    val maskedNotice: String,
    val emptyVideos: String
)

data class SafetyScriptBundle(
    val version: Int,
    val language: String,
    val ui: ChatUiStrings,
    val topics: List<SafetyTopic>,
    val checkers: List<Checker>
)
