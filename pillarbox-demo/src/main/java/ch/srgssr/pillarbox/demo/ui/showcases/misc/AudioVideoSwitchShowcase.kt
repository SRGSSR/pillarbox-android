/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * A showcase demonstrating how switching audio / video live.
 */
@Composable
fun AudioVideoSwitchShowcase(audioVideoLiveSwitchViewModel: AudioVideoLiveSwitchViewModel = viewModel()) {
    val player = audioVideoLiveSwitchViewModel.player
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.medium)) {
        PlayerView(
            player = player,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.paddings.baseline),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            ContentTypeToggleButton(
                currentContentType = audioVideoLiveSwitchViewModel.currentContentType,
                toggle = audioVideoLiveSwitchViewModel::toggleContentType
            )
            Spacer(modifier = Modifier.size(MaterialTheme.paddings.baseline))
            ContentSelector(
                contents = AudioVideoLiveSwitchViewModel.contents,
                currentContent = audioVideoLiveSwitchViewModel.currentContent,
                loadContent = audioVideoLiveSwitchViewModel::load,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentSelector(
    contents: List<AudioVideoLiveSwitchViewModel.LiveContent>,
    currentContent: AudioVideoLiveSwitchViewModel.LiveContent?,
    loadContent: (AudioVideoLiveSwitchViewModel.LiveContent) -> Unit,
    modifier: Modifier = Modifier
) {
    val textFieldState = rememberTextFieldState(currentContent?.label ?: "Select")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(modifier = modifier, expanded = expanded, onExpandedChange = {
        expanded = it
    }) {
        TextField(
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. A read-only text field has
            // the anchor type `PrimaryNotEditable`.
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            state = textFieldState,
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text("Channel") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            contents.forEach { content ->
                DropdownMenuItem(
                    text = { Text(content.label, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(content.label)
                        expanded = false
                        loadContent(content)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun ContentTypeToggleButton(
    currentContentType: AudioVideoLiveSwitchViewModel.ContentType,
    toggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedIconToggleButton(
        modifier = modifier,
        checked = currentContentType == AudioVideoLiveSwitchViewModel.ContentType.Video,
        onCheckedChange = {
            toggle()
        }
    ) {
        val icon = when (currentContentType) {
            AudioVideoLiveSwitchViewModel.ContentType.Video -> Icons.Default.Videocam
            AudioVideoLiveSwitchViewModel.ContentType.Audio -> Icons.Default.Audiotrack
        }
        Icon(icon, "Toggle video or audio")
    }
}
