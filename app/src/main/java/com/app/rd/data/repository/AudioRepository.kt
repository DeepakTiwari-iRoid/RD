package com.app.rd.data.repository

import com.app.rd.data.local.ContentResolverHelper
import com.app.rd.data.local.model.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val contentResolver: ContentResolverHelper
) {

    suspend fun getAudioData(): List<Audio> = withContext(Dispatchers.IO) {
        contentResolver.getAudioData()
    }

}