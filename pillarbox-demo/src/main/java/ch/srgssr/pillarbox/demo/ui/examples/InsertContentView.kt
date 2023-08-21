/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.data.DemoItem

private val keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Uri)

private data class InsertContentData(val uri: String = "", val licenseUrl: String = "") {
    val isValidUrl: Boolean
        get() {
            return uri.startsWith("http")
        }

    fun toDemoItem(): DemoItem {
        return DemoItem(
            title = uri,
            uri = uri,
            licenseUrl = licenseUrl
        )
    }
}

/**
 * Insert content view
 *
 * @param modifier The modifier to use.
 * @param onContentClicked The event when user click button play.
 * @receiver
 */
@Composable
fun InsertContentView(modifier: Modifier = Modifier, onContentClicked: (DemoItem) -> Unit) {
    var insertContentData by remember {
        mutableStateOf(InsertContentData())
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = insertContentData.uri,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            onValueChange = {
                insertContentData = insertContentData.copy(uri = it)
            },
            label = {
                Text(text = "Url or urn")
            },
            trailingIcon = {
                IconButton(onClick = { insertContentData = InsertContentData() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear"
                    )
                }
            },
        )
        if (insertContentData.uri.isNotBlank() && insertContentData.isValidUrl) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                value = insertContentData.licenseUrl,
                keyboardOptions = keyboardOptions,
                singleLine = true,
                onValueChange = {
                    insertContentData = insertContentData.copy(licenseUrl = it)
                },
                label = {
                    Text(text = "License url")
                },
                trailingIcon = {
                    IconButton(onClick = { insertContentData = insertContentData.copy(licenseUrl = "") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                },
            )
        }
        Button(onClick = { onContentClicked(insertContentData.toDemoItem()) }) {
            Text(text = "Play")
        }
    }
}

@Composable
@Preview
private fun InsertContentPreview() {
    InsertContentView() {
    }
}
