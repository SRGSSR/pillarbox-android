/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.srgssr.pillarbox.demo.BuildConfig
import ch.srgssr.pillarbox.demo.R

/**
 * Demo info fragment that display some useful information.
 *
 * @constructor Create empty Demo info fragment
 */
class DemoInfoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_informations, container, false)
        val versionView: TextView = view.findViewById(R.id.version_text_view)
        versionView.text = BuildConfig.VERSION_NAME
        return view
    }
}
