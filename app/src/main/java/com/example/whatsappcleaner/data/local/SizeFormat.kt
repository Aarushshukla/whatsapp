package com.example.whatsappcleaner.data

fun formatSize(bytes: Long): String {
    val kb = bytes.toDouble() / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> "${"%.1f".format(mb)} MB"
        kb >= 1.0 -> "${"%.0f".format(kb)} KB"
        else -> "$bytes B"
    }
}
