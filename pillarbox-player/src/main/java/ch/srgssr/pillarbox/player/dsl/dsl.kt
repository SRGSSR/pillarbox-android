/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.dsl

import android.content.Context
import android.os.Looper
import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.monitoring.MonitoringMessageHandler
import ch.srgssr.pillarbox.player.monitoring.NoOpMonitoringMessageHandler
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

fun PillarboxExoPlayer(
    context: Context,
    coroutineContext: CoroutineContext,
    monitoringMessageHandler: MonitoringMessageHandler,
    block: ExoPlayer.Builder.() -> Unit,
): PillarboxExoPlayer {
    return PillarboxExoPlayer(
        context = context,
        coroutineContext = coroutineContext,
        exoPlayer = ExoPlayer.Builder(context)
            .apply(block)
            .build(),
        monitoringMessageHandler = monitoringMessageHandler,
    )
}

@DslMarker
annotation class PillarboxDsl

@PillarboxDsl
interface MessageHandlerFactory {
    fun create(): MonitoringMessageHandler
}

interface MessageHandlerConfig<T : MessageHandlerFactory> {
    fun create(): T

    object NoOp : MessageHandlerConfig<NoOp.NoOpMessageHandlerFactory> {
        override fun create(): NoOpMessageHandlerFactory {
            return NoOpMessageHandlerFactory()
        }

        class NoOpMessageHandlerFactory : MessageHandlerFactory {
            override fun create(): NoOpMonitoringMessageHandler {
                return NoOpMonitoringMessageHandler
            }
        }
    }

    object Http : MessageHandlerConfig<Http.HttpMessageHandlerFactory> {
        override fun create(): HttpMessageHandlerFactory {
            return HttpMessageHandlerFactory()
        }

        class HttpMessageHandlerFactory : MessageHandlerFactory {
            var url: String = ""

            override fun create(): NoOpMonitoringMessageHandler {
                return NoOpMonitoringMessageHandler
            }
        }
    }
}

@PillarboxDsl
abstract class PlayerFactory {
    var monitoring: MonitoringMessageHandler = NoOpMonitoringMessageHandler
        private set
    var playbackLooper: Looper? = null

    fun <T : MessageHandlerFactory> monitoring(type: MessageHandlerConfig<T>, builder: T.() -> Unit) {
        monitoring = type.create()
            .apply(builder)
            .create()
    }

    protected open fun createExoPlayerBuilder(context: Context): ExoPlayer.Builder {
        return ExoPlayer.Builder(context)
            .apply { playbackLooper?.let(::setPlaybackLooper) }
    }

    fun create(context: Context): PillarboxExoPlayer {
        return PillarboxExoPlayer(
            context = context,
            coroutineContext = Dispatchers.Default,
            exoPlayer = createExoPlayerBuilder(context).build(),
            monitoringMessageHandler = monitoring,
        )
    }
}

interface PlayerConfig<T : PlayerFactory> {
    fun create(): T

    object Default : PlayerConfig<Default.DefaultPlayerFactory> {
        override fun create(): DefaultPlayerFactory {
            return DefaultPlayerFactory()
        }

        class DefaultPlayerFactory : PlayerFactory()
    }

    object Sample : PlayerConfig<Sample.SamplePlayerFactory> {
        override fun create(): SamplePlayerFactory {
            return SamplePlayerFactory()
        }

        class SamplePlayerFactory : PlayerFactory() {
            init {
                monitoring(MessageHandlerConfig.Http) {
                }
            }
        }
    }
}

fun pillarbox(context: Context, builder: PlayerConfig.Default.DefaultPlayerFactory.() -> Unit = {}): PillarboxExoPlayer {
    return pillarbox(context, PlayerConfig.Default, builder)
}

fun <T : PlayerFactory> pillarbox(context: Context, type: PlayerConfig<T>, builder: T.() -> Unit = {}): PillarboxExoPlayer {
    return type.create()
        .apply(builder)
        .create(context)
}

fun main(context: Context) {
    pillarbox(context)

    pillarbox(context) {
        monitoring(MessageHandlerConfig.Http) {
            url = ""
        }
    }

    pillarbox(context, PlayerConfig.Sample)
}
