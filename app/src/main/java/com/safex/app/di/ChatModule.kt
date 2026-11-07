package com.safex.app.di

import com.safex.app.data.chatbot.ChatRepositoryImpl
import com.safex.app.data.chatbot.FakeAiGuardRepository
import com.safex.app.data.chatbot.ScriptRepositoryImpl
import com.safex.app.domain.chatbot.repository.AiGuardRepository
import com.safex.app.domain.chatbot.repository.ChatRepository
import com.safex.app.domain.chatbot.repository.ScriptRepository
import com.safex.app.domain.chatbot.usecase.MaskSensitiveDataUseCase
import com.safex.app.domain.chatbot.usecase.MaskSensitiveDataUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {

    @Binds
    @Singleton
    abstract fun bindScriptRepository(
        scriptRepositoryImpl: ScriptRepositoryImpl
    ): ScriptRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindAiGuardRepository(
        fakeAiGuardRepository: FakeAiGuardRepository
    ): AiGuardRepository

    @Binds
    @Singleton
    abstract fun bindMaskSensitiveDataUseCase(
        maskSensitiveDataUseCaseImpl: MaskSensitiveDataUseCaseImpl
    ): MaskSensitiveDataUseCase
}
