package com.safex.app.presentation.chatbot

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChatDimensConfig(
    val minTouchTarget: Dp,
    val bubblePaddingHorizontal: Dp,
    val bubblePaddingVertical: Dp,
    val headlineSize: TextUnit,
    val bodySize: TextUnit,
    val subtextSize: TextUnit,
    val cardCornerRadius: Dp,
    val orbSize: Dp
)

object ChatDimens {
    val Normal = ChatDimensConfig(
        minTouchTarget = 48.dp,
        bubblePaddingHorizontal = 14.dp,
        bubblePaddingVertical = 10.dp,
        headlineSize = 16.sp,
        bodySize = 14.sp,
        subtextSize = 12.sp,
        cardCornerRadius = 16.dp,
        orbSize = 200.dp
    )

    val Elder = ChatDimensConfig(
        minTouchTarget = 56.dp,
        bubblePaddingHorizontal = 18.dp,
        bubblePaddingVertical = 14.dp,
        headlineSize = 20.sp,
        bodySize = 17.sp,
        subtextSize = 14.sp,
        cardCornerRadius = 20.dp,
        orbSize = 260.dp
    )

    fun get(elderMode: Boolean): ChatDimensConfig = if (elderMode) Elder else Normal
}
