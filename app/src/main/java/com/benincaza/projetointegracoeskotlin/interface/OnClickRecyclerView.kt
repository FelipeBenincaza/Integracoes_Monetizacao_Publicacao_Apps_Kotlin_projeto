package com.benincaza.projetointegracoeskotlin.`interface`

import com.benincaza.projetointegracoeskotlin.model.Livros

interface OnClickRecyclerView {
    fun onClickItemListener(livro: Livros)
    fun onClickLongItemListener(livro: Livros)
}