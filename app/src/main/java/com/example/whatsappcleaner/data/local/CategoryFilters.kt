package com.example.whatsappcleaner.data.local

import kotlin.math.min

private const val LARGE_FILE_BYTES = 20L * 1024L * 1024L
private const val BLURRY_SMALL_IMAGE_BYTES = 80L * 1024L
private const val BLURRY_MISSING_RESOLUTION_BYTES = 60L * 1024L
private const val OLD_MEDIA_DAYS = 30L
private const val DAY_MS = 24L * 60L * 60L * 1000L

object CategoryFilters {
    val largeFileThresholdBytes: Long = LARGE_FILE_BYTES

    fun duplicateGroups(items: List<SimpleMediaItem>): List<List<SimpleMediaItem>> = items
        .filter { it.size > 0L }
        .groupBy { it.size }
        .values
        .flatMap { sameSizeItems -> sameSizeDuplicateGroups(sameSizeItems) }
        .filter { it.size > 1 }

    fun duplicateCopies(items: List<SimpleMediaItem>): List<SimpleMediaItem> = duplicateGroups(items)
        .flatMap { group -> group.drop(1) }

    fun largeFiles(items: List<SimpleMediaItem>): List<SimpleMediaItem> = items
        .filter { it.size >= LARGE_FILE_BYTES }
        .sortedByDescending { it.size }

    fun blurryImages(items: List<SimpleMediaItem>): List<SimpleMediaItem> = items
        .filter { isBlurryOrLowQualityImage(it) }
        .sortedWith(compareBy<SimpleMediaItem> { it.size }.thenBy { it.name.lowercase() })

    fun oldMedia(items: List<SimpleMediaItem>, nowMillis: Long = System.currentTimeMillis()): List<SimpleMediaItem> {
        val cutoff = nowMillis - (OLD_MEDIA_DAYS * DAY_MS)
        return items
            .filter { item -> storedDateMillis(item)?.let { it < cutoff } == true }
            .sortedBy { storedDateMillis(it) ?: Long.MAX_VALUE }
    }

    fun oldMediaAgeDays(item: SimpleMediaItem, nowMillis: Long = System.currentTimeMillis()): Long {
        val storedDate = storedDateMillis(item) ?: return 0L
        return ((nowMillis - storedDate).coerceAtLeast(0L)) / DAY_MS
    }

    fun storedDateMillis(item: SimpleMediaItem): Long? = when {
        item.modifiedMillis > 0L -> item.modifiedMillis
        item.addedMillis > 0L -> item.addedMillis
        else -> null
    }

    fun sortLargeFiles(items: List<SimpleMediaItem>, sort: LargeFileSort): List<SimpleMediaItem> = when (sort) {
        LargeFileSort.LARGEST -> items.sortedByDescending { it.size }
        LargeFileSort.NEWEST -> items.sortedByDescending { storedDateMillis(it) ?: 0L }
        LargeFileSort.OLDEST -> items.sortedBy { storedDateMillis(it) ?: Long.MAX_VALUE }
    }

    private fun sameSizeDuplicateGroups(sameSizeItems: List<SimpleMediaItem>): List<List<SimpleMediaItem>> {
        val remaining = sameSizeItems.toMutableList()
        val groups = mutableListOf<List<SimpleMediaItem>>()
        while (remaining.isNotEmpty()) {
            val seed = remaining.removeAt(0)
            val matching = remaining.filter { candidate -> isDuplicateCandidate(seed, candidate) }
            if (matching.isNotEmpty()) {
                remaining.removeAll(matching.toSet())
                groups += (listOf(seed) + matching).sortedWith(bestCopyComparator())
            }
        }
        return groups
    }

    private fun isDuplicateCandidate(left: SimpleMediaItem, right: SimpleMediaItem): Boolean {
        if (left.size <= 0L || left.size != right.size) return false
        val leftName = normalizedFilename(left.name)
        val rightName = normalizedFilename(right.name)
        return leftName == rightName ||
            areSimilarNames(leftName, rightName) ||
            haveSameImageDimensions(left, right)
    }

    private fun haveSameImageDimensions(left: SimpleMediaItem, right: SimpleMediaItem): Boolean {
        val leftMime = left.mimeType.orEmpty().lowercase()
        val rightMime = right.mimeType.orEmpty().lowercase()
        return leftMime.startsWith("image/") && rightMime.startsWith("image/") &&
            left.width != null && left.height != null &&
            left.width == right.width && left.height == right.height
    }

    private fun normalizedFilename(name: String): String {
        val extensionless = name.substringBeforeLast('.', name).lowercase()
        return extensionless
            .replace(Regex("\\(\\d+\\)|[-_ ]copy|[-_ ]duplicate|[-_ ]\\d+$"), "")
            .replace(Regex("[^a-z0-9]"), "")
            .ifBlank { extensionless.replace(Regex("[^a-z0-9]"), "") }
    }

    private fun areSimilarNames(left: String, right: String): Boolean {
        if (left.isBlank() || right.isBlank()) return false
        val shorter = min(left.length, right.length)
        if (shorter < 6) return false
        if (left.startsWith(right) || right.startsWith(left)) return true
        return levenshteinDistance(left, right, maxDistance = 2) <= 2
    }

    private fun levenshteinDistance(left: String, right: String, maxDistance: Int): Int {
        if (kotlin.math.abs(left.length - right.length) > maxDistance) return maxDistance + 1
        var previous = IntArray(right.length + 1) { it }
        var current = IntArray(right.length + 1)
        for (i in left.indices) {
            current[0] = i + 1
            var rowMin = current[0]
            for (j in right.indices) {
                val cost = if (left[i] == right[j]) 0 else 1
                current[j + 1] = minOf(current[j] + 1, previous[j + 1] + 1, previous[j] + cost)
                rowMin = min(rowMin, current[j + 1])
            }
            if (rowMin > maxDistance) return maxDistance + 1
            val swap = previous
            previous = current
            current = swap
        }
        return previous[right.length]
    }

    private fun bestCopyComparator(): Comparator<SimpleMediaItem> =
        compareByDescending<SimpleMediaItem> { storedDateMillis(it) ?: 0L }
            .thenByDescending { it.width ?: 0 }
            .thenByDescending { it.height ?: 0 }
            .thenBy { it.name.length }
            .thenBy { it.name.lowercase() }

    private fun isBlurryOrLowQualityImage(item: SimpleMediaItem): Boolean {
        if (!item.mimeType.orEmpty().lowercase().startsWith("image/")) return false
        val marker = "${item.name} ${item.bucketName.orEmpty()} ${item.path}".lowercase()
        val hasWidth = item.width != null && item.width > 0
        val hasHeight = item.height != null && item.height > 0
        return (hasWidth && item.width < 720) ||
            (hasHeight && item.height < 720) ||
            item.size < BLURRY_SMALL_IMAGE_BYTES ||
            marker.contains("thumb") || marker.contains("thumbnail") || marker.contains("cache") ||
            ((!hasWidth || !hasHeight) && item.size < BLURRY_MISSING_RESOLUTION_BYTES)
    }
}

enum class LargeFileSort { LARGEST, NEWEST, OLDEST }
