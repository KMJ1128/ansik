package com.kmj.ansik.ui

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.kmj.ansik.R
import kotlinx.coroutines.CancellationException
import kotlin.math.absoluteValue

/** Selection-keyed state prevents the previous landmark's story flashing on a new pin. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaceStoryEntry(place: PlaceInfo) {
    val config = LocalConfiguration.current
    val language = AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()
        ?: config.locales[0].toLanguageTag()
    val selection = "${place.id}:${place.name}:${place.latitude}:${place.longitude}:$language"
    var story by remember(selection) { mutableStateOf<PlaceStory?>(null) }
    var loading by remember(selection) { mutableStateOf(!place.isRestaurant) }
    var open by remember(selection) { mutableStateOf(false) }
    LaunchedEffect(selection) {
        if (place.isRestaurant) return@LaunchedEffect
        try {
            story = RetrofitClient.api.getPlaceStory(place.name, place.longitude, place.latitude, language)
                .takeIf { it.overview.isNotBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("PlaceStory", "Story unavailable: ${place.name}", e)
        } finally { loading = false }
    }
    if (loading) {
        Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.place_story_loading), style = MaterialTheme.typography.labelMedium)
        }
    } else if (story != null) {
        val preview = remember(story?.overview) {
            HtmlCompat.fromHtml(story?.overview.orEmpty(), HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString().trim()
        }
        Surface(onClick = { open = true }, shape = MaterialTheme.shapes.medium,
            color = AppColors.Info.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.place_story_open),
                    style = MaterialTheme.typography.labelLarge, color = AppColors.Info)
                Text(preview, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    val current = story
    if (open && current != null) {
        ModalBottomSheet(onDismissRequest = { open = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            val images = (current.images + place.imageUrls + place.imageUrl)
                .filter { it.isNotBlank() }.distinct()
            Column(Modifier.fillMaxWidth().fillMaxHeight(0.85f)
                .verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(current.title, modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { open = false }) { Text(stringResource(R.string.place_story_close)) }
                }
                if (images.isNotEmpty()) {
                    val pager = rememberPagerState(pageCount = { images.size })
                    HorizontalPager(state = pager,
                        contentPadding = PaddingValues(horizontal = 20.dp), pageSpacing = 12.dp,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(240.dp)) { index ->
                        Card(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxSize()
                            .graphicsLayer {
                                val distance = ((pager.currentPage - index) +
                                    pager.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
                                scaleY = 1f - 0.06f * distance
                                alpha = 1f - 0.2f * distance
                            }) {
                            PlaceThumbnail(listOf(images[index]), current.title, Modifier.fillMaxSize())
                        }
                    }
                    Text("${pager.currentPage + 1} / ${images.size}",
                        Modifier.align(Alignment.CenterHorizontally).padding(8.dp),
                        style = MaterialTheme.typography.labelMedium)
                }
                if (current.language == "ko" && !language.startsWith("ko")) {
                    Text(stringResource(R.string.place_story_original), Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium)
                }
                val plainText = remember(current.overview) {
                    HtmlCompat.fromHtml(current.overview, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
                }
                Text(plainText, Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.place_story_source), Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
