/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaLibraryInfo
import ch.srgssr.pillarbox.demo.BuildConfig
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.ui.HomeDestination
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.demo.shared.R as sharedR

/**
 * Main menu content, displayed in the navigation drawer.
 *
 * @param onSettingsClick Called when the "Settings" item is clicked.
 * @param modifier The [Modifier] to apply to the layout.
 */
@Composable
fun MainMenu(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(modifier = modifier) {
        Column(modifier = Modifier.fillMaxHeight()) {
            MenuHeader()

            HorizontalDivider(modifier = Modifier.padding(horizontal = DrawerHorizontalPadding))

            NavigationDrawerItem(
                label = { Text(text = stringResource(HomeDestination.Settings.labelResId)) },
                selected = false,
                onClick = onSettingsClick,
                modifier = Modifier
                    .padding(top = MaterialTheme.paddings.small)
                    .padding(NavigationDrawerItemDefaults.ItemPadding),
                icon = { Icon(imageVector = HomeDestination.Settings.imageVector, contentDescription = null) },
            )

            Spacer(modifier = Modifier.weight(1f))

            VersionInformationSection()
        }
    }
}

@Composable
private fun MenuHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DrawerHorizontalPadding, vertical = MaterialTheme.paddings.medium),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colorResource(sharedR.color.ic_launcher_pillarbox_background)),
        ) {
            Image(
                painter = painterResource(sharedR.mipmap.ic_launcher_pillarbox_foreground),
                contentDescription = null,
                // The adaptive icon foreground contains a safe zone margin, so we zoom in to fill the circle
                modifier = Modifier.scale(LauncherForegroundScale),
            )
        }

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun VersionInformationSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DrawerHorizontalPadding, vertical = MaterialTheme.paddings.baseline)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.mini),
    ) {
        Text(
            text = stringResource(R.string.settings_version_information),
            modifier = Modifier.padding(bottom = MaterialTheme.paddings.mini),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )

        VersionRow(name = "Pillarbox", version = BuildConfig.VERSION_NAME)

        VersionRow(name = "Media3", version = MediaLibraryInfo.VERSION)
    }
}

@Composable
private fun VersionRow(name: String, version: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = version,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private val DrawerHorizontalPadding = 28.dp
private const val LauncherForegroundScale = 1.5f

@Preview(showBackground = true)
@Composable
private fun MainMenuPreview() {
    PillarboxTheme {
        MainMenu(onSettingsClick = {})
    }
}
