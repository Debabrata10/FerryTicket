package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.amtron.ferryticket.R
import com.amtron.ferryticket.model.SourceDestinationData
import com.google.gson.Gson

class SourceDestinationAdapter(
    private val sourceDestinationDataList: List<SourceDestinationData>
) :
    RecyclerView.Adapter<SourceDestinationAdapter.ViewHolder>() {
    private lateinit var mItemClickListener: OnSourceDestinationRecyclerViewItemClickListener
    private lateinit var sourceDestinationData: SourceDestinationData

    fun setOnItemClickListener(mItemClickListener: OnSourceDestinationRecyclerViewItemClickListener?) {
        this.mItemClickListener = mItemClickListener!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.sorce_destination_card_view, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        sourceDestinationData = sourceDestinationDataList[position]

        holder.srcGhat.text = sourceDestinationData.source.ghat_name
        holder.destGhat.text = sourceDestinationData.destination.ghat_name
        holder.srcDest.setOnClickListener {
            mItemClickListener.onSrcDestItemClickListener(
                position,
                Gson().toJson(sourceDestinationData)
            )
        }
    }

    override fun getItemCount(): Int {
        return sourceDestinationDataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val srcDest: LinearLayout = itemView.findViewById(R.id.src_dest_ll)
        val srcGhat: TextView = itemView.findViewById(R.id.src)
        val destGhat: TextView = itemView.findViewById(R.id.dest)
        val timing: TextView = itemView.findViewById(R.id.timing)
    }
}