package com.sherazsadiq.dermascan.manageprofile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.sherazsadiq.dermascan.R

class AutocompleteAdapter(
    private val predictions: List<AutocompletePrediction>,
    private val onItemClick: (AutocompletePrediction) -> Unit
) : RecyclerView.Adapter<AutocompleteAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_autocomplete, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prediction = predictions[position]
        holder.textView.text = prediction.getFullText(null)
        holder.itemView.setOnClickListener {
            onItemClick(prediction)
        }
    }

    override fun getItemCount(): Int {
        return predictions.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}