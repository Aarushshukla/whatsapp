package com.example.whatsappcleaner.data

// Shared data classes used by ViewModel, UI, and Root
data class ReminderFreq(val label: String, val days: Int)
data class ReminderTime(val label: String, val hour: Int, val minute: Int)