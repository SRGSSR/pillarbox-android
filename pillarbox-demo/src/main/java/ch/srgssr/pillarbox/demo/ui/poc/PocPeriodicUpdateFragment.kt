/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.poc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ch.srgssr.pillarbox.demo.MainNavigationDirections
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.data.SwiMediaItemSource

/**
 * Poc periodic update fragment to test continuous update inside the PillarboxMediaSource
 */
class PocPeriodicUpdateFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_poc_periodic_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val buttonWithError: Button = view.findViewById(R.id.button_start_with_error)
        val buttonFiniteUpdate: Button = view.findViewById(R.id.button_finite_updates)
        val buttonInfiniteUpdate: Button = view.findViewById(R.id.button_infinite_updates)

        buttonWithError.setOnClickListener {
            startPlayerWithIds(arrayOf(SwiMediaItemSource.FAILING_REQUEST_SWI_ID, SwiMediaItemSource.SIMPLE_SWI_ID))
        }
        buttonFiniteUpdate.setOnClickListener {
            startPlayerWithIds(arrayOf(SwiMediaItemSource.TWO_TIMES_UPDATES_SWI_ID, SwiMediaItemSource.SIMPLE_SWI_ID))
        }
        buttonInfiniteUpdate.setOnClickListener {
            startPlayerWithIds(
                arrayOf(SwiMediaItemSource.SIMPLE_SWI_ID, SwiMediaItemSource.INFINITE_UPDATE_SWI_ID)/*, SwiMediaItemSource
                .TWO_TIMES_UPDATES_SWI_ID)*/
            )
        }
    }

    private fun startPlayerWithIds(array: Array<String>) {
        findNavController().navigate(MainNavigationDirections.openSimplePlayer(array))
    }
}
