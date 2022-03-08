package com.caglar.ktmaps.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.caglar.ktmaps.databinding.RecyclerRowBinding
import com.caglar.ktmaps.model.Place
import com.caglar.ktmaps.view.MapsActivity

class PlaceAdapter(val placeList: List<Place>) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {

    class PlaceHolder(val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.recyclerRowBinding.recyclerViewTextView.text = placeList[position].name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra("selectedPlace", placeList[position])
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }


}