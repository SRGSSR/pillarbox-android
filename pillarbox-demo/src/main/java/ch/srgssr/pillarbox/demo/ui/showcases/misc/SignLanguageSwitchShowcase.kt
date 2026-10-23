/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignLanguage
import androidx.compose.material.icons.outlined.SignLanguage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * A showcase demonstrating how to switch between a content and its sign language interpreted version.
 */
@Composable
fun SignLanguageSwitchShowcase(signLanguageSwitchViewModel: SignLanguageSwitchViewModel = viewModel()) {
    val player = signLanguageSwitchViewModel.player
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = SignLanguageSwitchViewModel.content.label,
                style = MaterialTheme.typography.bodyLarge,
            )
            SignLanguageToggleButton(
                isSigned = signLanguageSwitchViewModel.isSigned,
                toggle = signLanguageSwitchViewModel::toggleSigned
            )
        }
    }
}

@Composable
private fun SignLanguageToggleButton(
    isSigned: Boolean,
    toggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedIconToggleButton(
        modifier = modifier,
        checked = isSigned,
        onCheckedChange = {
            toggle()
        }
    ) {
        val icon = if (isSigned) Icons.Filled.SignLanguage else Icons.Outlined.SignLanguage
        Icon(icon, "Toggle sign language")
    }
}
