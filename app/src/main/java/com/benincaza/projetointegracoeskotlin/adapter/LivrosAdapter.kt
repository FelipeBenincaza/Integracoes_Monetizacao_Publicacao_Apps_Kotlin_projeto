package com.benincaza.projetointegracoeskotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.model.Livros

class LivrosAdapter(private val context: Context, private val livroLista: ArrayList<Livros>) : BaseAdapter(){

    private lateinit var titulo: TextView
    private lateinit var genero: TextView
    private lateinit var paginas: TextView

    override fun getCount(): Int {
        return livroLista.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        view = LayoutInflater.from(context).inflate(R.layout.row_livro, parent, false)
        val livro = livroLista[position]

        genero = view.findViewById(R.id.txt_genero)
        titulo = view.findViewById(R.id.txt_titulo)
        paginas = view.findViewById(R.id.txt_paginas)

        titulo.text = livro.titulo
        genero.text = "Gênero: ${livro.genero}"
        paginas.text = "Número de páginas: ${livro.paginas}"

        return view
    }
}