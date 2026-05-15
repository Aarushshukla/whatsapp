package com.example.whatsappcleaner.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whatsappcleaner.data.local.formatSize

private val AppBg = Color(0xFFF7F9FC)
private val CardBg = Color(0xFFFFFFFF)
private val MainText = Color(0xFF20242A)
private val SecondaryText = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF2F6FED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardSubScreen(title:String, subtitle:String, onBack:()->Unit, content:@Composable ColumnScope.()->Unit) {
    Scaffold(containerColor = AppBg, topBar = {
        TopAppBar(title={ Column{ Text(title,color=MainText); Text(subtitle,color=SecondaryText, style=MaterialTheme.typography.labelMedium)}}, navigationIcon={ IconButton(onClick=onBack){ Icon(Icons.AutoMirrored.Filled.ArrowBack, null)}})
    }) { p ->
        LazyColumn(modifier=Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Card(colors=CardDefaults.cardColors(containerColor = CardBg), shape= RoundedCornerShape(16.dp)){ Column(Modifier.padding(16.dp), content=content) } } }
    }
}

@Composable
fun SimpleStatRow(label:String, value:String){ Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){ Text(label,color=SecondaryText); Text(value,color=MainText,fontWeight=FontWeight.Medium) } }

@Composable
fun ListNavRow(label:String, meta:String, onClick:()->Unit){ Card(onClick=onClick, colors=CardDefaults.cardColors(containerColor=CardBg)){ Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){ Column(Modifier.weight(1f)){ Text(label,color=MainText,fontWeight=FontWeight.SemiBold); Text(meta,color=SecondaryText, style=MaterialTheme.typography.bodySmall)} Icon(Icons.Default.ChevronRight,null,tint=PrimaryBlue)}} }

@Composable
fun MediaOverviewScreen(items: List<DashboardMediaBucket>, onBack:()->Unit, onOpen:(String)->Unit){
    DashboardSubScreen("Media Overview","Photos, videos, audio, and files", onBack){
        items.forEach { b -> ListNavRow(b.name, "${b.count} files • ${formatSize(b.bytes)} • ${b.percent}%", { onOpen(b.name) }) }
    }
}

data class DashboardMediaBucket(val name:String,val count:Int,val bytes:Long,val percent:Int)
