package com.benincaza.projetointegracoeskotlin.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.benincaza.projetointegracoeskotlin.R

class DificuldadeSenhaFragment : Fragment() {

    private lateinit var txtDificuldade : TextView
    lateinit var edtSenha : EditText
    lateinit var text: Editable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dificuldade_senha, container, false)

        txtDificuldade = view.findViewById(R.id.txt_dificuldade)
        edtSenha = view.findViewById(R.id.edt_senha)

        edtSenha.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(edit: Editable?) {
                if (edit != null) {
                    text = edit
                }

                if (text.toString().length < 6)
                    txtDificuldade.text = resources.getString(R.string.senha_invalida)
                else if (text.toString().length < 8)
                    txtDificuldade.text = resources.getString(R.string.senha_fraca)
                else if (text.toString().length < 10)
                    txtDificuldade.text = resources.getString(R.string.senha_media)
                else if (text.toString().length < 12)
                    txtDificuldade.text = resources.getString(R.string.senha_forte)
                else
                    txtDificuldade.text = resources.getString(R.string.senha_forte)
            }

        })

        return view
    }

}