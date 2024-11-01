package com.sherazsadiq.dermascan.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.FirebaseReadService

class ApprovedDoctorsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApprovedDoctorsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val firebaseReadService = FirebaseReadService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_approved_doctors, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewApprovedDoctors)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ApprovedDoctorsAdapter(emptyList())
        recyclerView.adapter = adapter

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.DarkBlue)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            fetchApprovedDoctors()
        }

        fetchApprovedDoctors()

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchApprovedDoctors()
    }

    private fun fetchApprovedDoctors() {
        swipeRefreshLayout.isRefreshing = true
        firebaseReadService.fetchApprovedDoctors { doctors, error ->
            if (doctors != null) {
                adapter.updateData(doctors)
                Log.d("ApprovedDoctorsFragment", "Doctors: $doctors")
            }
            Log.d("ApprovedDoctorsFragment", "Error: $error")
            swipeRefreshLayout.isRefreshing = false
        }
    }
}