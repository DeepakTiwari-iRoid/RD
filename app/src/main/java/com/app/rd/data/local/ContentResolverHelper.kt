package com.app.rd.data.local

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import com.app.rd.data.local.model.Audio
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ContentResolverHelper @Inject constructor(
    @ApplicationContext val context: Context
) {

    private var mCursor: Cursor? = null

    private val projection: Array<String> = arrayOf(
        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.TITLE,
    )

    private val selectionClause: String = "${MediaStore.Audio.AudioColumns.IS_MUSIC} >= ?" +
            "AND ${MediaStore.Audio.Media.MIME_TYPE} NOT IN (?,?,?)"

    val selectionArgs = arrayOf(
        "1",
//        keep only 1 remove else
//        "audio/amr",
//        "audio/3gpp",
//        "audio/aac"
    )

    val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

    @WorkerThread
    fun getAudioData(): List<Audio> {
        return getCursorData()
    }

    private fun getCursorData(): MutableList<Audio> {

        val audioList = mutableListOf<Audio>()

        mCursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selectionClause,
            selectionArgs,
            sortOrder
        )

        mCursor?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val artistColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val dataColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
            val titleColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)

            cursor.apply {
                if (count == 0) {
                    Log.d("Cursor", "getCursorData: Cursor is Empty")
                } else {
                    while (cursor.moveToNext()) {
                        val displayName = getString(displayNameColumn)
                        val id = cursor.getLong(idColumn)
                        val artist = cursor.getString(artistColumn)
                        val data = cursor.getString(dataColumn)
                        val duration = cursor.getInt(durationColumn)
                        val title = cursor.getString(titleColumn)
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        // Stores column values and the contentUri in a local object
                        // that represents the media file.
                        audioList += Audio(
                            uri = uri,
                            displayName = displayName,
                            id = id,
                            artist = artist,
                            data = data,
                            duration = duration,
                            title = title
                        )
                    }
                }
            }
        }
        return audioList
    }

}
