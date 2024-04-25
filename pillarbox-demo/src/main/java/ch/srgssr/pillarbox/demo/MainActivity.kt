/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import com.google.android.gms.cast.framework.CastContext
import com.google.common.util.concurrent.MoreExecutors

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            CastContext.getSharedInstance(applicationContext, MoreExecutors.directExecutor()).result
        } catch (e: RuntimeException) {
            Log.e("Coucou", "Fail to get cast", e)
        }
        setContent {
            PillarboxTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    MainNavigation()
                }
            }
        }
    }
}
