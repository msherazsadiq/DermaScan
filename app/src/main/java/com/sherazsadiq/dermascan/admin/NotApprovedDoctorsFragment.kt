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

class NotApprovedDoctorsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotApprovedDoctorsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val firebaseReadService = FirebaseReadService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_not_approved_doctors, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewNotApprovedDoctors)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = NotApprovedDoctorsAdapter(emptyList())
        recyclerView.adapter = adapter

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.DarkBlue)
        swipeRefreshLayout.setColorSchemeResources(R.color.white)
        swipeRefreshLayout.setOnRefreshListener {
            fetchNotApprovedDoctors()
        }

        fetchNotApprovedDoctors()

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchNotApprovedDoctors()
    }

    private fun fetchNotApprovedDoctors() {
        swipeRefreshLayout.isRefreshing = true
        firebaseReadService.fetchUnapprovedDoctors { doctors, error ->
            if (doctors != null) {
                adapter.updateData(doctors)
                Log.d("NotApprovedDoctorsFragment", "Doctors: $doctors")
            }
            Log.d("NotApprovedDoctorsFragment", "Error: $error")
            swipeRefreshLayout.isRefreshing = false
        }
    }
}