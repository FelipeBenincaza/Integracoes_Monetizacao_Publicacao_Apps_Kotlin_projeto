package com.benincaza.projetointegracoeskotlin

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.benincaza.projetointegracoeskotlin.databinding.FormForgotPasswordBinding

class FormForgotPassword(private val context: Context) {

    fun show(
        eventoCriado: (eventoCriado: EventoForgotPassword) -> Unit
    ) {
        val binding = FormForgotPasswordBinding
            .inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .show()

        binding.imgClose.setOnClickListener {
            dialog.dismiss()
        }

        binding.botaoSalvar.setOnClickListener {
            try {
                val valida = ValidateAuthentication(context)
                valida.validaCampoEmail(binding.email)

                val titulo = binding.email.text.toString()
                val evento = EventoForgotPassword(titulo)
                dialog.dismiss()
                eventoCriado(evento)
            } catch (e: ValidateAuthenticationException){
                Util.showToast(context, e.message.toString())
            }
        }
    }
}