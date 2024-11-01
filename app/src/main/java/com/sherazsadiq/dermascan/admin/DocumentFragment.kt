package com.sherazsadiq.dermascan.admin

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.FirebaseManager
import java.io.File

class DocumentFragment : Fragment() {

    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var recyclerView: RecyclerView
    private val pdfPages = mutableListOf<Bitmap>()
    private var documentType: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_document, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = PDFPageAdapter(pdfPages)
        documentType = arguments?.getString("documentType")
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        documentType?.let { downloadAndRenderPDF(it) }
    }

    private fun downloadAndRenderPDF(documentType: String) {
        val doctor = (activity as DoctorDetailsActivity).doctor
        val firebaseManager = FirebaseManager()
        firebaseManager.getFileUrl(doctor.UID, documentType) { url ->
            url?.let { pdfUrl ->
                Log.d("PDF Viewer", "Downloading PDF from URL: $pdfUrl")
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
                val localFile = File(requireContext().getExternalFilesDir(null), "$documentType.pdf")

                storageRef.getFile(localFile).addOnSuccessListener {
                    Log.d("PDF Viewer", "PDF downloaded successfully.")
                    renderPDFPages(localFile)
                }.addOnFailureListener { e ->
                    Log.e("PDF Viewer", "Failed to download PDF: ", e)
                    Toast.makeText(context, "Failed to download PDF", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Log.e("PDF Viewer", "URL is null, unable to open PDF.")
                Toast.makeText(context, "Unable to retrieve PDF URL.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderPDFPages(file: File) {
        try {
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor)

            for (i in 0 until pdfRenderer.pageCount) {
                val page = pdfRenderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                pdfPages.add(bitmap)
                page.close()
            }

            recyclerView.adapter?.notifyDataSetChanged()

        } catch (e: Exception) {
            Log.e("PDF Viewer", "Error rendering PDF pages: ", e)
            Toast.makeText(context, "Error rendering PDF pages", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(documentType: String): DocumentFragment {
            val fragment = DocumentFragment()
            val args = Bundle()
            args.putString("documentType", documentType)
            fragment.arguments = args
            return fragment
        }
    }
}