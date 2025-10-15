/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import com.google.android.gms.cast.tv.media.MediaCommandCallback
import com.google.android.gms.cast.tv.media.QueueInsertRequestData
import com.google.android.gms.cast.tv.media.QueueRemoveRequestData
import com.google.android.gms.cast.tv.media.QueueReorderRequestData
import com.google.android.gms.cast.tv.media.QueueUpdateRequestData
import com.google.android.gms.tasks.Task

class ReceiverCallback: MediaCommandCallback() {
    override fun onQueueUpdate(p0: String?, requestData: QueueUpdateRequestData): Task<Void?> {
        Log.d("ReceiverCallback", "onQueueUpdate")
        return super.onQueueUpdate(p0, requestData)
    }
    override fun onQueueInsert(p0: String?, requestData: QueueInsertRequestData): Task<Void?> {
        Log.d("ReceiverCallback", "QueueInsertRequestData")
        return super.onQueueInsert(p0, requestData)
    }

    override fun onQueueRemove(p0: String?, requestData: QueueRemoveRequestData): Task<Void?> {
        Log.d("ReceiverCallback", "QueueRemoveRequestData")
        return super.onQueueRemove(p0, requestData)
    }

    override fun onQueueReorder(p0: String?, requestData: QueueReorderRequestData): Task<Void?> {
        Log.d("ReceiverCallback", "QueueReorderRequestData")
        return super.onQueueReorder(p0, requestData)
    }
}
