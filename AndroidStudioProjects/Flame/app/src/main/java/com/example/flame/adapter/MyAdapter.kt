package com.example.alimentapet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flame.R
import com.example.flame.dataClass.history

class MyAdapter(
    private val historyList: MutableList<history>,
)  : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder((itemView))
    }

    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {
        val feeder = historyList[position]

        if(feeder.status == "" || feeder.status == "None"){
            holder.status.visibility = View.GONE
        }else{
            holder.status.text = "Status: " + feeder.status
        }

        if(feeder.location == "" || feeder.location == "None"){
            holder.location.visibility = View.GONE
        }else{
            holder.location.text = "Location: " + feeder.location
        }

        if(feeder.time == "" || feeder.time == "None"){
            holder.time.visibility = View.GONE
        }else{
            holder.time.text = "Timestamp: " + feeder.time
        }

        if(feeder.alerttype == "" || feeder.alerttype == "None"){
            holder.alert.visibility = View.GONE
        }else{
            holder.alert.text = "Alert Type: " + feeder.alerttype
        }
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    inner class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val status :TextView = itemView.findViewById(R.id.status)
        val location :TextView = itemView.findViewById(R.id.location)
        val time :TextView = itemView.findViewById(R.id.time)
        val alert :TextView = itemView.findViewById(R.id.alert)
    }
}

