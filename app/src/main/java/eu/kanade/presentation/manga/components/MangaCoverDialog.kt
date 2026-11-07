package eu.kanade.presentation.manga.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.updatePadding
import ca.mpreg.webgpuviewer.renderer.GainmapInput
import ca.mpreg.webgpuviewer.renderer.Image
import ca.mpreg.webgpuviewer.viewer.ImagePage
import ca.mpreg.webgpuviewer.viewer.ImageViewer
import ca.mpreg.webgpuviewer.viewer.ImageViewerState
import coil3.asDrawable
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarIcon
import eu.kanade.presentation.manga.EditCoverAction
import eu.kanade.tachiyomi.data.coil.ImageDecoder
import eu.kanade.tachiyomi.data.coil.newDecoder
import eu.kanade.tachiyomi.ui.reader.viewer.ReaderPageImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Save
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.components.miuix.MiuixPopupSystemBars
import tachiyomi.presentation.core.components.miuix.SnackbarHost
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.clickableNoIndication
import tachiyomi.source.local.isLocal
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.menu.WindowIconDropdownMenu

@Composable
fun MangaCoverDialog(
    manga: Manga,
    isCustomCover: Boolean,
    snackbarHostState: SnackbarHostState,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    onEditClick: ((EditCoverAction) -> Unit)?,
    onDismissRequest: () -> Unit,
) {
    val useNewRenderer = LocalContext.current.appGraph.basePreferences.highQualityRenderer.get()
    val view = LocalView.current

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        MiuixPopupSystemBars()
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent,
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .navigationBarsPadding(),
                ) {
                    ActionsPill {
                        IconButton(onClick = onDismissRequest) {
                            AppBarIcon(
                                imageVector = MiuixIcons.Close,
                                contentDescription = stringResource(MR.strings.action_close),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    ActionsPill {
                        AppBarActions(
                            actions = listOf(
                                AppBar.Action(
                                    title = stringResource(MR.strings.action_share),
                                    icon = MiuixIcons.Share,
                                    onClick = onShareClick,
                                ),
                                AppBar.Action(
                                    title = stringResource(MR.strings.action_save),
                                    icon = MaterialSymbols.Rounded.Save,
                                    onClick = onSaveClick,
                                ),
                            ),
                        )
                        if (onEditClick != null) {
                            if (isCustomCover && (manga.favorite || manga.isLocal())) {
                                WindowIconDropdownMenu(
                                    entry = DropdownEntry(
                                        items = listOf(
                                            DropdownItem(
                                                text = stringResource(MR.strings.action_edit),
                                                onClick = { onEditClick(EditCoverAction.EDIT) },
                                            ),
                                            DropdownItem(
                                                text = stringResource(MR.strings.action_delete),
                                                onClick = { onEditClick(EditCoverAction.DELETE) },
                                            ),
                                        ),
                                    ),
                                    minWidth = 48.dp,
                                    minHeight = 48.dp,
                                ) {
                                    AppBarIcon(
                                        imageVector = MiuixIcons.Edit,
                                        contentDescription = stringResource(MR.strings.action_edit_cover),
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        when {
                                            !manga.favorite && !manga.isLocal() -> {
                                                onEditClick(EditCoverAction.ADD_TO_LIBRARY)
                                            }
                                            else -> onEditClick(EditCoverAction.EDIT)
                                        }
                                    },
                                ) {
                                    AppBarIcon(
                                        imageVector = MiuixIcons.Edit,
                                        contentDescription = stringResource(MR.strings.action_edit_cover),
                                    )
                                }
                            }
                        }
                    }
                }
            },
        ) { contentPadding ->
            if (useNewRenderer) {
                val state = remember(manga) { ImageViewerState() }
                LaunchedEffect(manga) {
                    val request = ImageRequest.Builder(view.context)
                        .data(manga)
                        .size(Size.ORIGINAL)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .newDecoder(true)
                        .build()
                    val result = view.context.imageLoader.execute(request) as? SuccessResult ?: return@LaunchedEffect
                    val res = result.image as? ImageDecoder.DecodeResultImage ?: return@LaunchedEffect
                    // Held, then freed, around the upload: its buffer alone doesn't keep
                    // the frame's native pixels alive.
                    val page = res.frame.use {
                        withContext(Dispatchers.Default) {
                            ImagePage.ImageSingle(
                                Image(
                                    res.image,
                                    res.width,
                                    res.height,
                                    createMipMaps = true,
                                    backgroundColor = 0,
                                    hdr = res.isHdr,
                                    hdrHeadroom = res.hdrHeadroom,
                                    gainmap = res.gainmap?.let {
                                        GainmapInput(
                                            pixels = it.pixels,
                                            width = it.width,
                                            height = it.height,
                                            channels = it.channels,
                                            gamma = it.gamma,
                                            minContentBoost = it.minContentBoost,
                                            maxContentBoost = it.maxContentBoost,
                                            offsetSdr = it.offsetSdr,
                                            offsetHdr = it.offsetHdr,
                                        )
                                    },
                                ),
                            )
                        }
                    }
                    state.apply {
                        fetchPage = { index ->
                            if (index == 0) page else null
                        }
                        invalidate()
                    }
                }

                ImageViewer(state = state)
                return@Scaffold
            }

            val statusBarPaddingPx = with(LocalDensity.current) { contentPadding.calculateTopPadding().roundToPx() }
            val bottomPaddingPx = with(LocalDensity.current) { contentPadding.calculateBottomPadding().roundToPx() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickableNoIndication(onClick = onDismissRequest),
            ) {
                AndroidView(
                    factory = {
                        ReaderPageImageView(it).apply {
                            onViewClicked = onDismissRequest
                            clipToPadding = false
                            clipChildren = false
                        }
                    },
                    update = { view ->
                        val request = ImageRequest.Builder(view.context)
                            .data(manga)
                            .size(Size.ORIGINAL)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .target { image ->
                                val drawable = image.asDrawable(view.context.resources)
                                // Copy bitmap in case it came from memory cache
                                // Because SSIV needs to thoroughly read the image
                                val copy = (drawable as? BitmapDrawable)
                                    ?.bitmap
                                    ?.copy(Bitmap.Config.HARDWARE, false)
                                    ?.toDrawable(view.context.resources)
                                    ?: drawable
                                view.setImage(copy, ReaderPageImageView.Config(zoomDuration = 500))
                            }
                            .build()
                        view.context.imageLoader.enqueue(request)

                        view.updatePadding(top = statusBarPaddingPx, bottom = bottomPaddingPx)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun ActionsPill(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
    ) {
        content()
    }
}
