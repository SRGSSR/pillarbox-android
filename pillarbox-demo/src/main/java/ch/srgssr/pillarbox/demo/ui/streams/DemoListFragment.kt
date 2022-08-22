/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.streams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import kotlinx.coroutines.flow.collectLatest

/**
 * Demo list fragment showing a list of clickable DemoItem views.
 *
 * @constructor Create empty Demo list fragment
 */
class DemoListFragment : Fragment() {

    private val viewmodel: DemoListViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_demo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        val adapter = DemoItemListAdapter {
            SimplePlayerActivity.startPlayer(requireActivity(), arrayOf(it.id))
        }
        recyclerView.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewmodel.listDemoItem.collectLatest {
                adapter.submitList(it)
            }
        }
    }
}
