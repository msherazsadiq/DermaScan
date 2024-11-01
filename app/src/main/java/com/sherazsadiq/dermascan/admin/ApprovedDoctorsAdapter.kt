package com.sherazsadiq.dermascan.admin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Doctor

class ApprovedDoctorsAdapter(private var doctorsList: List<Doctor>) : RecyclerView.Adapter<ApprovedDoctorsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val doctorNameTextView: TextView = view.findViewById(R.id.doctorNameTextView)
        val doctorEmailTextView: TextView = view.findViewById(R.id.doctorEmailTextView)
        val doctorDateCreatedTextView: TextView = view.findViewById(R.id.doctorDateTextView)
        val gotoDoctorDetails: FrameLayout = view.findViewById(R.id.gotoDoctorDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val doctor = doctorsList[position]

        holder.doctorNameTextView.text = doctorsList[position].Name
        holder.doctorEmailTextView.text = doctorsList[position].Email
        holder.doctorDateCreatedTextView.text = doctorsList[position].CreatedAt


        holder.gotoDoctorDetails.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DoctorDetailsActivity::class.java)
            intent.putExtra("doctor", doctor)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return doctorsList.size
    }

    fun updateData(newDoctorsList: List<Doctor>) {
        doctorsList = newDoctorsList
        notifyDataSetChanged()
    }
}