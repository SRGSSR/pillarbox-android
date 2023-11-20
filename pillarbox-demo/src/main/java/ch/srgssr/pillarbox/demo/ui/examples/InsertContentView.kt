/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

private data class InsertContentData(
    val uri: String = "",
    val licenseUrl: String = ""
) {
    val isValidUrl: Boolean
        get() = uri.startsWith("http")

    fun toDemoItem(): DemoItem {
        return DemoItem(
            title = uri,
            uri = uri,
            licenseUrl = licenseUrl
        )
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
    var insertContentData by remember {
        mutableStateOf(InsertContentData())
    }

    Column(
        modifier = modifier.padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InsertContentTextField(
            value = insertContentData.uri,
            label = stringResource(R.string.enter_url_or_urn),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            onValueChange = { insertContentData = insertContentData.copy(uri = it) },
            onClearClick = { insertContentData = InsertContentData() }
        )

        AnimatedVisibility(visible = insertContentData.isValidUrl) {
            InsertContentTextField(
                value = insertContentData.licenseUrl,
                label = stringResource(R.string.licence_url),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                onValueChange = { insertContentData = insertContentData.copy(licenseUrl = it) },
                onClearClick = { insertContentData = insertContentData.copy(licenseUrl = "") }
            )
        }

        AnimatedVisibility(visible = insertContentData.uri.isNotBlank()) {
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
@Preview(showBackground = true)
private fun InsertContentViewPreview() {
    PillarboxTheme {
        InsertContentView {
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun InsertContentTextFieldPreview() {
    PillarboxTheme {
        InsertContentTextField(
            value = "Value",
            label = "Label",
            onValueChange = {},
            onClearClick = {}
        )
    }
}
