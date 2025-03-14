package com.sherazsadiq.dermascan.scan

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.admin.DoctorDetailsActivity
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.ScanData


class ScanHistoryAdapter(private var scanList: List<ScanData>) : RecyclerView.Adapter<ScanHistoryAdapter.ScanViewHolder>() {

    class ScanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val firstDName: TextView = view.findViewById(R.id.FirstDName)
        val firstDPercentage: TextView = view.findViewById(R.id.FirstDPercentage)
        val scanDate: TextView = view.findViewById(R.id.scanDate)
        val scannedImage: ImageView = view.findViewById(R.id.scanImageView)
        val gotoScanDetails: FrameLayout = view.findViewById(R.id.gotoScanDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scan_history, parent, false)
        return ScanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val scan = scanList[position]


        holder.firstDName.text = scan.FirstDName
        holder.firstDPercentage.text = scan.FirstDPercentage

        val combinedDateTime = "${scan.ScanDate} ${scan.ScanTime}"
        holder.scanDate.text = combinedDateTime


        Glide.with(holder.itemView.context)
            .load(scan.ImageURL)
            .transform(
                CenterCrop(),
                RoundedCorners(10)
            ) // Apply both center crop and rounded corners
        .into(holder.scannedImage)


        holder.gotoScanDetails.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ScanDetailsActivity::class.java)
            intent.putExtra("ScanDetails", scan)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = scanList.size

    fun updateData(newScanList: List<ScanData>) {
        scanList = newScanList
        notifyDataSetChanged()
    }

}