/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.streams

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.data.DemoItem

/**
 * Demo item list adapter
 *
 * @property onItemClick
 * @constructor Create empty Demo item list adapter
 */
class DemoItemListAdapter(private val onItemClick: (DemoItem) -> Unit) : ListAdapter<DemoItem, DemoItemListAdapter.DemoItemViewHolder>(
    object : DiffUtil.ItemCallback<DemoItem>() {
        override fun areItemsTheSame(oldItem: DemoItem, newItem: DemoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DemoItem, newItem: DemoItem): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoItemViewHolder {
        return DemoItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_demo_item, parent, false))
    }

    override fun onBindViewHolder(holder: DemoItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Demo item view holder
     *
     * @constructor
     *
     * @param view
     */
    inner class DemoItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView = view.findViewById(R.id.title_view)
        private val subtileView: TextView = view.findViewById(R.id.subtitle_view)
        private val contentView: View = view.findViewById(R.id.demo_item_content_view)

        /**
         * Bind views to [item]
         *
         * @param item
         */
        fun bind(item: DemoItem) {
            titleView.text = item.title
            subtileView.text = item.description
            contentView.setOnClickListener { onItemClick.invoke(item) }
        }
    }
}
