package com.example.whatsappcleaner.data

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val path: String? = null,
    val name: String,
    val sizeBytes: Long = 0L,
    val mimeType: String = "application/octet-stream",
    val dateModified: Long = 0L,
    val dateAdded: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null,
    val bucketName: String? = null,
    val category: CleanCategory = CleanCategory.OTHER,
    val riskLevel: RiskLevel = RiskLevel.REVIEW_CAREFULLY,
    val confidenceScore: Int = 0,
    val duplicateGroupId: String? = null,
    val isBestCopy: Boolean = false,
    val isSelected: Boolean = false,
    val isProtected: Boolean = false,
)

enum class CleanCategory {
    DUPLICATE,
    LARGE_FILE,
    OLD_FILE,
    BLURRY_IMAGE,
    MEME_OR_STICKER,
    STATUS,
    FORWARDED_DUPLICATE,
    REVIEW_REQUIRED,
    PROTECTED,
    OTHER,
}

enum class RiskLevel {
    SAFE_TO_DELETE,
    PROBABLY_JUNK,
    REVIEW_CAREFULLY,
    PROTECTED,
}

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class Loading(val stage: String, val progress: Float) : ScanUiState
    data class Success(val result: ScanResult) : ScanUiState
    data object Empty : ScanUiState
    data class Error(val message: String) : ScanUiState
}

data class ScanResult(
    val totalFiles: Int,
    val totalSizeBytes: Long,
    val potentialCleanupBytes: Long,
    val categories: List<CategorySummary>,
    val files: List<MediaItem>,
    val monthlyBreakdown: List<MonthlyStorageSummary>,
    val scanStartedAt: Long,
    val scanCompletedAt: Long,
)

data class CategorySummary(
    val category: CleanCategory,
    val title: String,
    val description: String,
    val fileCount: Int,
    val sizeBytes: Long,
    val riskLevel: RiskLevel,
)

data class MonthlyStorageSummary(
    val year: Int,
    val month: Int,
    val totalSizeBytes: Long,
    val fileCount: Int,
)

data class CleanupReceipt(
    val deletedFileCount: Int,
    val failedFileCount: Int,
    val freedBytes: Long,
    val categoryBreakdown: List<CategorySummary>,
    val deletedAt: Long,
)
