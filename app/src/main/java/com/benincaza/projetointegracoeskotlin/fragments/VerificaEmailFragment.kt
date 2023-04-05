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
import androidx.core.view.isVisible
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.ValidateAuthenticationException
import java.util.regex.Matcher
import java.util.regex.Pattern

class VerificaEmailFragment : Fragment() {

    private lateinit var txtEmailValido: TextView
    lateinit var edtEmail: EditText
    lateinit var text: Editable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_verifica_email, container, false)

        txtEmailValido = view.findViewById(R.id.txt_email_valido)
        edtEmail = view.findViewById(R.id.edt_email)

        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(edit: Editable?) {
                if (edit != null) {
                    text = edit
                }

                val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
                val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
                val matcher: Matcher = pattern.matcher(text)
                if (!matcher.matches()) {
                    txtEmailValido.text = resources.getString(R.string.email_invalido)
                }else{
                    txtEmailValido.text = getString(R.string.email_valido)
                }
            }

        })

        return view
    }
}