package com.hmncube.juiceme.history

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.hmncube.juiceme.R
import com.hmncube.juiceme.data.CardNumber
import javax.inject.Inject

class HistoryAdapter @Inject constructor(private val listener: OptionsMenuClickListener) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var dataSet = mutableListOf<CardNumber>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout : MaterialCardView
        val number : TextView
        val date : TextView

        init {
            layout = view.findViewById(R.id.historyLayout)
            number = view.findViewById(R.id.numberTv)
            date = view.findViewById(R.id.dateTv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.number.text = dataSet[position].number
        holder.date.text = DateFormat.format("dd MMM hh:mm:ss a", dataSet[position].date)
        holder.layout.setOnClickListener {
            listener.onOptionsMenuClicked(dataSet[position], position)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun setData(data : MutableList<CardNumber>) {
        dataSet = data
        notifyItemRangeInserted(0, dataSet.size)
    }

    fun deletedItem(position: Int) {
        dataSet.removeAt(position)
        notifyItemRemoved(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    // notifyItemRangeRemoved(0, dataSet.size) does not work
    fun clearAll() {
        dataSet.removeAll(dataSet)
        notifyDataSetChanged()
    }
}

interface OptionsMenuClickListener {
    fun onOptionsMenuClicked(cardNumber: CardNumber, position: Int)
}
