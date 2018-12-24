package com.maher.irrigator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class EmptyBaseAdapter<T>(context: Context?) : RecyclerView.Adapter<EmptyBaseAdapter<T>.GenericViewHolder>() {

    var obj: T? = null
    var inflater: LayoutInflater = LayoutInflater.from(context)

    protected fun inflate(@LayoutRes id: Int, container: ViewGroup): View {
        return inflater.inflate(id, container, false)
    }

    fun setData(data: T?) {
        this.obj = data
        notifyDataSetChanged()
    }

    fun getData() = obj

    abstract inner class GenericViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        abstract fun onBindData(position: Int)
    }
}