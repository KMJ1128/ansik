package com.kmj.ansik.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.kmj.ansik.R

/** Try supplied URLs in order; do not rewrite a provider's URL scheme. */
@Composable
internal fun PlaceThumbnail(urls: List<String>, contentDescription: String?, modifier: Modifier = Modifier) {
    val candidates = remember(urls) {
        urls.map(String::trim).filter { (it.startsWith("https://") || it.startsWith("http://")) &&
            it != RestaurantRepository.DEFAULT_IMAGE_URL }.distinct()
    }
    var index by remember(candidates) { mutableIntStateOf(0) }
    val current = candidates.getOrNull(index)
    AsyncImage(model = current ?: R.drawable.ansik_logo_final,
        contentDescription = contentDescription, contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.ansik_logo_final),
        error = painterResource(R.drawable.ansik_logo_final), modifier = modifier,
        onError = { if (current != null && candidates.getOrNull(index) == current) index++ })
}
