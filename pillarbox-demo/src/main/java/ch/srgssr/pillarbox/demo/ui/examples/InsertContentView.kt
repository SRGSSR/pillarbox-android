/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.demo.EnvironmentConfig
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.getServers
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import java.net.MalformedURLException
import java.net.URL

private data class InsertContentData(
    val uri: String = "",
    val licenseUrl: String = "",
    val environmentConfig: EnvironmentConfig? = null,
) {
    val hasValidInput: Boolean
        get() = uri.isNotBlank()

    val isValidUrl: Boolean
        get() = try {
            URL(uri)
            true
        } catch (_: MalformedURLException) {
            false
        }

    val isValidUrn: Boolean
        get() = uri.isValidMediaUrn()

    fun toDemoItem(): DemoItem {
        return when {
            isValidUrl -> DemoItem.URL(
                title = uri,
                uri = uri,
                licenseUri = licenseUrl,
            )

            isValidUrn -> DemoItem.URN(
                title = uri,
                urn = uri,
                host = checkNotNull(environmentConfig).host,
                forceSAM = environmentConfig.forceSAM,
                forceLocation = environmentConfig.location,
            )

            else -> error("Invalid URI: $uri")
        }
    }
}

/**
 * Insert content view.
 *
 * @param modifier The [Modifier] to apply on the root of the screen.
 * @param onPlayClick The action to perform when the user clicks on "Play".
 */
@Composable
fun InsertContentView(
    modifier: Modifier = Modifier,
    onPlayClick: (DemoItem) -> Unit
) {
    val (insertContentData, setInsertContentData) = remember {
        mutableStateOf(InsertContentData())
    }

    InsertContentViewInternal(
        insertContentData = insertContentData,
        modifier = modifier,
        setInsertContentData = setInsertContentData,
        onPlayClick = onPlayClick,
    )
}

@Composable
private fun InsertContentViewInternal(
    insertContentData: InsertContentData,
    modifier: Modifier = Modifier,
    setInsertContentData: (InsertContentData) -> Unit,
    onPlayClick: (DemoItem) -> Unit,
) {
    Column(
        modifier = modifier.padding(bottom = MaterialTheme.paddings.small),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InsertContentTextField(
            value = insertContentData.uri,
            label = stringResource(R.string.enter_url_or_urn),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.paddings.mini),
            onValueChange = { setInsertContentData(insertContentData.copy(uri = it)) },
            onClearClick = { setInsertContentData(InsertContentData()) },
        )

        AnimatedVisibility(visible = insertContentData.isValidUrl) {
            InsertContentTextField(
                value = insertContentData.licenseUrl,
                label = stringResource(R.string.licence_url),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.paddings.mini),
                onValueChange = { setInsertContentData(insertContentData.copy(licenseUrl = it)) },
                onClearClick = { setInsertContentData(insertContentData.copy(licenseUrl = "")) },
            )
        }

        AnimatedVisibility(visible = insertContentData.isValidUrn) {
            val context = LocalContext.current
            val servers = remember { getServers(context) }

            LaunchedEffect(servers) {
                setInsertContentData(insertContentData.copy(environmentConfig = servers[0]))
            }

            if (insertContentData.environmentConfig != null) {
                InsertContentDropDown(
                    value = insertContentData.environmentConfig,
                    label = stringResource(R.string.server),
                    entries = servers,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.paddings.mini),
                    onEntrySelected = { setInsertContentData(insertContentData.copy(environmentConfig = it)) },
                )
            }
        }

        AnimatedVisibility(visible = insertContentData.hasValidInput) {
            Button(onClick = { onPlayClick(insertContentData.toDemoItem()) }) {
                Text(text = stringResource(R.string.play))
            }
        }
    }
}

@Composable
private fun InsertContentTextField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (value: String) -> Unit,
    onClearClick: () -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Uri),
        singleLine = true,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        trailingIcon = {
            AnimatedVisibility(
                visible = value.isNotBlank(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear)
                    )
                }
            }
        },
    )
}

@Composable
private fun InsertContentDropDown(
    value: EnvironmentConfig,
    label: String,
    entries: List<EnvironmentConfig>,
    modifier: Modifier = Modifier,
    onEntrySelected: (entry: EnvironmentConfig) -> Unit,
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        LaunchedEffect(isPressed) {
            if (isPressed) {
                showDropdownMenu = true
            }
        }

        OutlinedTextField(
            value = value.displayName,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = {
                val iconRotation by animateFloatAsState(
                    targetValue = if (showDropdownMenu) -180f else 0f,
                    label = "icon_rotation_animation",
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(iconRotation),
                )
            },
            interactionSource = interactionSource,
        )

        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false },
            offset = DpOffset(x = 0.dp, y = MaterialTheme.paddings.small),
        ) {
            entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(text = entry.displayName) },
                    onClick = {
                        onEntrySelected(entry)
                        showDropdownMenu = false
                    },
                )
            }
        }
    }
}

@Composable
@Preview(group = "InsertContentView", showBackground = true)
private fun InsertContentViewEmptyPreview() {
    val (insertContentData, setInsertContentData) = remember {
        mutableStateOf(InsertContentData())
    }

    PillarboxTheme {
        InsertContentViewInternal(
            insertContentData = insertContentData,
            setInsertContentData = setInsertContentData,
            onPlayClick = {},
        )
    }
}

@Composable
@Preview(group = "InsertContentView", showBackground = true)
private fun InsertContentViewUrlPreview() {
    val (insertContentData, setInsertContentData) = remember {
        mutableStateOf(InsertContentData(uri = "https://www.example.com/", licenseUrl = "https://www.license.com/"))
    }

    PillarboxTheme {
        InsertContentViewInternal(
            insertContentData = insertContentData,
            setInsertContentData = setInsertContentData,
            onPlayClick = {},
        )
    }
}

@Composable
@Preview(group = "InsertContentView", showBackground = true)
private fun InsertContentViewUrnPreview() {
    val (insertContentData, setInsertContentData) = remember {
        mutableStateOf(InsertContentData(uri = "urn:rts:video:1234567"))
    }

    PillarboxTheme {
        InsertContentViewInternal(
            insertContentData = insertContentData,
            setInsertContentData = setInsertContentData,
            onPlayClick = {},
        )
    }
}

@Composable
@Preview(group = "InsertContentTextField", showBackground = true)
private fun InsertContentTextFieldEmptyPreview() {
    val (value, setValue) = remember {
        mutableStateOf("")
    }

    PillarboxTheme {
        InsertContentTextField(
            value = value,
            label = "Label",
            onValueChange = setValue,
            onClearClick = { setValue("") },
        )
    }
}

@Composable
@Preview(group = "InsertContentTextField", showBackground = true)
private fun InsertContentTextFieldValuePreview() {
    val (value, setValue) = remember {
        mutableStateOf("Value")
    }

    PillarboxTheme {
        InsertContentTextField(
            value = value,
            label = "Label",
            onValueChange = setValue,
            onClearClick = { setValue("") },
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun InsertContentDropDownPreview() {
    val context = LocalContext.current
    val servers = getServers(context)
    val (server, setServer) = remember {
        mutableStateOf(servers[0])
    }

    PillarboxTheme {
        InsertContentDropDown(
            value = server,
            label = "Label",
            entries = servers,
            onEntrySelected = setServer,
        )
    }
}
