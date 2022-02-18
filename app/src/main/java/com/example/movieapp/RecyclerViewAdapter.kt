package com.example.movieapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.movieapp.models.MovieSearch
import com.example.movieapp.databinding.GridItemRowBinding
import com.example.movieapp.databinding.ScrollLoadLayoutBinding

class RecyclerViewAdapter(context: Context, var list: ArrayList<MovieSearch?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mContext = context
    lateinit var itemViewBinding: GridItemRowBinding
    lateinit var scrollLoadBinding: ScrollLoadLayoutBinding
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    var onItemClick: ((String) -> Unit)? = null

    companion object {
        const val VIEW_TYPE_DATA = 0;
        const val VIEW_TYPE_PROGRESS = 1;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewtype: Int): RecyclerView.ViewHolder {
        return when (viewtype) {
            VIEW_TYPE_DATA -> {
                itemViewBinding = GridItemRowBinding.inflate(inflater, parent, false)
                DataViewHolder(itemViewBinding)
            }
            VIEW_TYPE_PROGRESS -> {
                scrollLoadBinding = ScrollLoadLayoutBinding.inflate(inflater, parent, false)
                ProgressViewHolder(scrollLoadBinding)
            }
            else -> throw IllegalArgumentException("Unknown type")
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            null -> VIEW_TYPE_PROGRESS
            else -> VIEW_TYPE_DATA
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        if (holder is DataViewHolder) {
            val options = RequestOptions.overrideOf(300, 450)

            Glide.with(mContext)
                .load(list[pos]?.poster)
                .placeholder(R.drawable.ic_noimg)
                .apply(options)
                .into(holder.imageView)

            holder.textview.text = list[pos]?.title

            holder.itemView.setOnClickListener {
                list[pos]?.imdbID?.let { it -> onItemClick?.invoke(it) }
            }
        }
    }

    inner class DataViewHolder(itemViewBinding: GridItemRowBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        var imageView = itemViewBinding.gridItemImageview
        var textview = itemViewBinding.gridItemTitle
    }

    inner class ProgressViewHolder(scrollLoadBinding: ScrollLoadLayoutBinding) :
        RecyclerView.ViewHolder(scrollLoadBinding.root)

}