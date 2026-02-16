package com.example.whatsappcleaner.data

import android.content.Context

class MediaStoreRepository(
    private val context: Context
) {

    // Temporary stub so app can run without Room.
    suspend fun scanTodayWhatsAppMedia() {
        // TODO: later add real MediaStore + storage logic here.
    }
}
