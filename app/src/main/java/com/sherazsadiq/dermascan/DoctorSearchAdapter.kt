package com.sherazsadiq.dermascan

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
import com.sherazsadiq.dermascan.admin.DoctorDetailsActivity
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.DocLocation

class DoctorSearchAdapter(private var doctorLocationPairs: List<Pair<Doctor, DocLocation?>>) : RecyclerView.Adapter<DoctorSearchAdapter.DoctorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val (doctor, location) = doctorLocationPairs[position]
        holder.bind(doctor, location)
    }

    override fun getItemCount(): Int = doctorLocationPairs.size

    fun updateList(newDoctorLocationPairs: List<Pair<Doctor, DocLocation?>>) {
        doctorLocationPairs = newDoctorLocationPairs
        notifyDataSetChanged()
    }

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val doctorName: TextView = itemView.findViewById(R.id.doctorNameTextView)
        private val doctorSpecialization: TextView = itemView.findViewById(R.id.DoctorSpecialityTextView)
        private val doctorExperience: TextView = itemView.findViewById(R.id.DoctorExperienceTextView)
        private val doctorImage: ImageView = itemView.findViewById(R.id.doctorImageView)

        private val gotoDocDetails: FrameLayout = itemView.findViewById(R.id.gotoDoctorDetails)

        fun bind(doctor: Doctor, location: DocLocation?) {
            doctorName.text = doctor.Name
            doctorSpecialization.text = doctor.Specialization
            doctorExperience.text = "Experience: ${doctor.Experience} Year"


            Glide.with(itemView.context)
                .load(doctor.ProfilePic)
                .transform(CenterCrop(), RoundedCorners(10))
                .into(doctorImage)

            gotoDocDetails.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DoctorProfileActivity::class.java)
                intent.putExtra("doctor", doctor)
                context.startActivity(intent)
            }
        }
    }
}