package com.safex.app.data.chatbot

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.safex.app.data.datastore.SettingsDataStore
import com.safex.app.data.local.dao.ChatDao

class ChatCleanupWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val chatDao: ChatDao,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Read configured retention days
            var autoDeleteDays = 0
            settingsDataStore.chatAutoDeleteDays.collect {
                autoDeleteDays = it
                return@collect
            }

            if (autoDeleteDays > 0) {
                val cutoffTimestamp = System.currentTimeMillis() - (autoDeleteDays * 24 * 60 * 60 * 1000L)
                chatDao.deleteMessagesOlderThan(cutoffTimestamp)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
