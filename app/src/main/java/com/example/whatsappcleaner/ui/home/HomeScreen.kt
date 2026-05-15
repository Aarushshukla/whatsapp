package com.example.whatsappcleaner.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.ReminderFreq
import com.example.whatsappcleaner.data.ReminderTime
import com.example.whatsappcleaner.data.local.SimpleMediaItem
import com.example.whatsappcleaner.data.local.formatSize
import com.example.whatsappcleaner.ui.navigation.AppDrawer
import kotlinx.coroutines.launch

private val AppBg = Color(0xFFF7F9FC); private val CardBg = Color(0xFFFFFFFF); private val MainText = Color(0xFF20242A); private val SecondaryText = Color(0xFF6B7280); private val PrimaryBlue = Color(0xFF2F6FED); private val Border = Color(0xFFE5E7EB)
private data class DashboardFeature(val title:String,val subtitle:String,val icon:ImageVector,val count:Int?=null,val size:Long?=null,val percent:Int?=null,val onClick:()->Unit)

@Composable
fun SimpleHomeScreen(items: List<SimpleMediaItem>, onRefreshClick: () -> Unit, summaryInfo: String, isLoading: Boolean, currentFilter: MediaFilter, onFilterChange: (MediaFilter) -> Unit, remindersEnabled: Boolean, onRemindersToggle: (Boolean) -> Unit, memeCount: Int, spamCount: Int, junkCount: Int, duplicateCount: Int, isProUser: Boolean, onNavigateToSmartClean: () -> Unit, onNavigateToPhoneReality: () -> Unit, onNavigateToMemeAnalyzer: () -> Unit, onNavigateToMediaViewer: () -> Unit, onNavigateToJunk: () -> Unit, onNavigateToAnalytics: () -> Unit, onNavigateToSpam: () -> Unit, onNavigateToSettings: () -> Unit, onNavigateToDuplicates: () -> Unit, onBulkDeleteClick: () -> Unit, onUpgradeToPro: () -> Unit, onDeleteConfirmed: () -> Unit, onDeleteItemsRequested: (List<SimpleMediaItem>) -> Unit, onOpenInSystem: (SimpleMediaItem) -> Unit, onOpenSystemStorage: () -> Unit, pendingDeleteUris: Set<String>, isDeleteInProgress: Boolean, deleteSnackbarMessage: String?, onUndoDelete: () -> Unit, onDeleteSnackbarConsumed: () -> Unit, selectedFrequency: ReminderFreq, onFrequencyChange: (ReminderFreq) -> Unit, selectedTime: ReminderTime, allTimeOptions: List<ReminderTime>, onTimeChange: (ReminderTime) -> Unit, largeTodayCount: Int, largeTodaySizeText: String, screenshotTodayCount: Int, screenshotTodaySizeText: String, activeSuggestion: SuggestionType, onSuggestionChange: (SuggestionType) -> Unit, totalFiles: Int, totalSize: Long, oldFilesCount: Int, smartSuggestionSummary: SmartSuggestionSummary, smartSuggestedItems: List<SimpleMediaItem>, suggestionReasonsByUri: Map<String, List<String>>, scanUiState: ScanUiState, onNavigateToFeatures: () -> Unit, onNavigateToScanHistory: () -> Unit, onAiFeatureClick: (AiFeature) -> Unit, onNavigateToPrivacyPolicy: () -> Unit, onNavigateToTerms: () -> Unit, onNavigateToAbout: () -> Unit) {
    val drawerState = androidx.compose.material3.rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val reviewBytes = smartSuggestionSummary.totalSpaceToFree
    val hasScan = totalFiles > 0
    val features = listOf(
        DashboardFeature("Smart Review","Best first cleanup suggestions",Icons.Default.AutoAwesome,count = smartSuggestedItems.size,size = reviewBytes,percent = if (totalSize > 0) ((reviewBytes.toFloat() / totalSize) * 100).toInt() else null,onClick = onNavigateToSmartClean),
        DashboardFeature("Media","Photos, videos, audio, and files",Icons.Default.Image,count = totalFiles,size = totalSize,percent = if (totalSize > 0) 100 else null,onClick = onNavigateToPhoneReality),
        DashboardFeature("Duplicates","Repeated photos and videos",Icons.Default.ContentCopy,count = duplicateCount,size = reviewBytes/3,percent = if (totalSize > 0) (((reviewBytes/3).toFloat() / totalSize) * 100).toInt() else null,onClick = onNavigateToDuplicates),
        DashboardFeature("Large Videos","Big files with high impact",Icons.Default.VideoFile,count = largeTodayCount,size = reviewBytes/3,percent = if (totalSize > 0) (((reviewBytes/3).toFloat() / totalSize) * 100).toInt() else null,onClick = onNavigateToMediaViewer),
        DashboardFeature("Statuses","Old temporary status files",Icons.Default.History,count = oldFilesCount,size = totalSize/8,percent = if (totalSize > 0) (((totalSize/8).toFloat() / totalSize) * 100).toInt() else null,onClick = onNavigateToJunk),
        DashboardFeature("Memes & Stickers","Forwarded and low-value media",Icons.Default.Image,count = memeCount,size = totalSize/10,percent = if (totalSize > 0) (((totalSize/10).toFloat() / totalSize) * 100).toInt() else null,onClick = onNavigateToMemeAnalyzer),
        DashboardFeature("Blurry Images","Low-quality images to review",Icons.Default.BlurOn,count = null,size = null,percent = null,onClick = onNavigateToSmartClean),
        DashboardFeature("Scan History","Previous scans and trends",Icons.Default.History,count = null,size = null,percent = null,onClick = onNavigateToScanHistory)
    )
    ModalNavigationDrawer(drawerState = drawerState, drawerContent = { AppDrawer { route -> scope.launch { drawerState.close() }; when(route){"home"->Unit;"smart_review"->onNavigateToSmartClean();"scan_again"->onRefreshClick();"categories"->onNavigateToFeatures();"media_overview"->onNavigateToPhoneReality();"duplicate_finder"->onNavigateToDuplicates();"large_files"->onNavigateToMediaViewer();"old_media"->onNavigateToJunk();"blurry_images"->onNavigateToSmartClean();"scan_history"->onNavigateToFeatures();"cleanup_receipt"->onNavigateToFeatures();"storage_overview"->onNavigateToAnalytics();"privacy_policy"->onNavigateToPrivacyPolicy();"terms"->onNavigateToTerms();"about"->onNavigateToAbout();"settings"->onNavigateToSettings();"help_feedback"->onNavigateToSettings(); else -> onNavigateToPhoneReality() } } }) {
        Column(Modifier.fillMaxSize().background(AppBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Row(verticalAlignment = Alignment.CenterVertically){IconButton(onClick={scope.launch{drawerState.open()}}){Icon(Icons.Default.Menu,null)}; Column{Text("ChatSweep", fontWeight = FontWeight.Bold, color=MainText); Text("Private offline cleaner", color=SecondaryText, style=MaterialTheme.typography.labelSmall)}}; IconButton(onClick=onRefreshClick){Icon(Icons.Default.Refresh,null,tint=PrimaryBlue)} }
            Card(colors = CardDefaults.cardColors(containerColor = CardBg), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Chat media", color=MainText, fontWeight=FontWeight.SemiBold); Text(formatSize(totalSize), style = MaterialTheme.typography.headlineMedium, color=MainText); Text("Review up to ${formatSize(reviewBytes)}", color=SecondaryText); Text("Total files found: $totalFiles", color=SecondaryText); SegmentedBar(listOf(reviewBytes/4,reviewBytes/4,totalSize/8,totalSize/10,totalSize/12,0,reviewBytes/6), totalSize); Button(onClick = if (hasScan) onNavigateToSmartClean else onRefreshClick, modifier=Modifier.fillMaxWidth()){Text(if(hasScan)"SMART REVIEW" else "START SMART SCAN")}; Text("SCAN AGAIN", color=PrimaryBlue, modifier=Modifier.clickable{onRefreshClick()}); Text("No cloud upload. Nothing is deleted automatically.", color=SecondaryText, style=MaterialTheme.typography.labelMedium) } }
            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(features){f -> FeatureCard(f)} }
        }
    }
}

@Composable private fun FeatureCard(feature: DashboardFeature){ AnimatedVisibility(true, enter = fadeIn()+slideInVertically()){ Card(modifier=Modifier.fillMaxWidth().clickable{feature.onClick()}, colors=CardDefaults.cardColors(containerColor = CardBg), border=BorderStroke(1.dp,Border), shape=RoundedCornerShape(14.dp)){ Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)){ Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){ Box(Modifier.size(36.dp).background(PrimaryBlue.copy(.12f), CircleShape), contentAlignment = Alignment.Center){Icon(feature.icon,null,tint=PrimaryBlue)}; Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SecondaryText) }; Text(feature.title,color=MainText,fontWeight=FontWeight.SemiBold); Text(feature.subtitle,color=SecondaryText,style=MaterialTheme.typography.labelSmall); val details = buildList { feature.count?.let { add("$it") }; feature.size?.takeIf { it > 0 }?.let { add(formatSize(it)) }; feature.percent?.let { add("$it%") } }.joinToString(" • "); Text(if(details.isNotBlank()) details else "Tap to open", color=SecondaryText, style=MaterialTheme.typography.labelMedium) } } }}

@Composable private fun SegmentedBar(values: List<Long>, total: Long) { Row(Modifier.fillMaxWidth().height(10.dp).background(Border, RoundedCornerShape(99.dp))) { values.forEachIndexed { i, v -> val target = if (total>0) (v.toFloat()/total).coerceAtLeast(0.02f) else 0.02f; val w by animateFloatAsState(target, label="seg"); Box(Modifier.weight(w).fillMaxSize().background(listOf(Color(0xFF2F6FED),Color(0xFF5E8CF0),Color(0xFF7AA3F4),Color(0xFF8EAFEF),Color(0xFFAEC4F8),Color(0xFF9AD0C2),Color(0xFFF5A623)).getOrElse(i){Color(0xFF2F6FED)})) } } }
