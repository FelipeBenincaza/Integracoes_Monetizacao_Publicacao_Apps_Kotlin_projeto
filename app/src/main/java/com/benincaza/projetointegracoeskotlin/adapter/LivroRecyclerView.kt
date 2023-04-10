package com.benincaza.projetointegracoeskotlin.adapter

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.`interface`.OnClickRecyclerView
import com.benincaza.projetointegracoeskotlin.model.Livros
import com.google.firebase.storage.FirebaseStorage

class LivroRecyclerView(private val mList: List<Livros>, private val onClickListener: OnClickRecyclerView) : RecyclerView.Adapter<LivroRecyclerView.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_livro, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val livro = mList[position]

        if (livro.photo != null && livro.photo.isNotEmpty()){
            val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(Uri.parse(livro.photo).toString())
            imageRef.getBytes(10 * 1024 * 1024).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                holder.capa.setImageBitmap(bitmap)
            }.addOnFailureListener {
                // Handle any errors
            }
        }

        holder.titulo.text = livro.titulo
        holder.genero.text = "Gênero: ${livro.genero}"
        holder.paginas.text = "Número de páginas: ${livro.paginas}"

        holder.itemView.setOnClickListener {
            onClickListener.onClickItemListener(livro)
        }

        holder.itemView.setOnLongClickListener {
            onClickListener.onClickLongItemListener(livro)
            true
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val titulo: TextView = itemView.findViewById(R.id.txt_titulo)
        val genero: TextView = itemView.findViewById(R.id.txt_genero)
        val paginas: TextView = itemView.findViewById(R.id.txt_paginas)
        val capa: ImageView = itemView.findViewById(R.id.img_capa)

    }
}