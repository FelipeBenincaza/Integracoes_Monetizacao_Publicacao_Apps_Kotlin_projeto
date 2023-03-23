package com.benincaza.projetointegracoeskotlin

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import com.benincaza.projetointegracoeskotlin.databinding.FormForgotPasswordBinding

class FormForgotPassword(private val context: Context) {

    fun show(
        supportFragmentManager: FragmentManager,
        quandoEventoCriado: (eventoCriado: EventoForgotPassword) -> Unit
    ) {
        val binding = FormForgotPasswordBinding
            .inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .show()

        binding.botaoSalvar.setOnClickListener {
            val titulo = binding.email.text.toString()
            val evento = EventoForgotPassword(titulo)
            dialog.dismiss()
            quandoEventoCriado(evento)
        }
    }
}