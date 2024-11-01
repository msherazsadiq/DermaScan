package com.sherazsadiq.dermascan.admin

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.sherazsadiq.dermascan.R

class PDFPageAdapter(private val pdfPages: List<Bitmap>) : RecyclerView.Adapter<PDFPageAdapter.PdfPageViewHolder>() {

    class PdfPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfPageImageView: ImageView = itemView.findViewById(R.id.pdfPageImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pdf_page, parent, false)
        return PdfPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfPageViewHolder, position: Int) {
        holder.pdfPageImageView.setImageBitmap(pdfPages[position])
    }

    override fun getItemCount(): Int = pdfPages.size
}