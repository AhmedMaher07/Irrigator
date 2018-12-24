package com.maher.irrigator.widget.spinner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.maher.irrigator.R
import com.maher.irrigator.model.l.LPlant


class SpinnerAdapter : SpinnerTextViewBaseAdapter<LPlant>() {

    private lateinit var mListener: OnItemSelectedListener

    fun setListener(listener: OnItemSelectedListener) {
        mListener = listener
    }

    override fun getCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_spinner,
                parent,
                false
            ), mListener
        )
    }

    override fun getBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.setData(getItemForBindView(position), getPosition(position))
        }
    }


    class ItemViewHolder(itemView: View?, listener: OnItemSelectedListener) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {
        private lateinit var mItem: String

        private var mPosition: Int = 0

        private lateinit var mTextTv: TextView

        private var mListener: OnItemSelectedListener = listener

        fun setData(item: LPlant, position: Int) {
            mItem = item.name!!
            mTextTv.text = item.name
            mPosition = position
        }

        override fun onClick(v: View?) {
            mListener.onItemSelected(mItem, mItem, mPosition)
        }

        init {
            itemView?.run {
                mTextTv = itemView as TextView
                mTextTv.setOnClickListener(this@ItemViewHolder)
            }
        }
    }
}