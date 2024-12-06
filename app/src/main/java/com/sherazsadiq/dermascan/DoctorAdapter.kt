package com.sherazsadiq.dermascan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.sherazsadiq.dermascan.firebase.Doctor

class DoctorAdapter(private val doctors: List<Doctor>) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val name: TextView = itemView.findViewById(R.id.name)
        val specialization: TextView = itemView.findViewById(R.id.specialization)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor_home, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]
        holder.name.text = doctor.Name
        holder.specialization.text = doctor.Specialization

        // Load image with rounded corners and fit it inside the background
        Glide.with(holder.itemView.context)
            .load(doctor.ProfilePic)
            .transform(CenterCrop(), RoundedCorners(50)) // Apply both center crop and rounded corners
            .into(holder.profileImage)
    }


    override fun getItemCount(): Int {
        return doctors.size
    }
}