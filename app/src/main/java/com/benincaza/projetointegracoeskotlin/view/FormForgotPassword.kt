package com.benincaza.projetointegracoeskotlin.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.benincaza.projetointegracoeskotlin.*
import com.benincaza.projetointegracoeskotlin.databinding.FormForgotPasswordBinding
import com.benincaza.projetointegracoeskotlin.fragments.VerificaEmailFragment

class FormForgotPassword(private val context: LoginScreen) {

    lateinit var edtEmail: VerificaEmailFragment

    fun show(
        eventoCriado: (eventoCriado: EventoForgotPassword) -> Unit
    ) {
        val binding = FormForgotPasswordBinding
            .inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .show()

        edtEmail = context.supportFragmentManager.findFragmentById(R.id.textInputLayoutEmail) as VerificaEmailFragment

        binding.imgClose.setOnClickListener {
            dialog.dismiss()
        }

        binding.botaoSalvar.setOnClickListener {
            try {
                val valida = ValidateAuthentication(context)
                valida.validaCampoEmail(edtEmail)

                val titulo = edtEmail.text.toString()
                val evento = EventoForgotPassword(titulo)
                dialog.dismiss()
                eventoCriado(evento)
            } catch (e: ValidateAuthenticationException){
                Util.showToast(context, e.message.toString())
            }catch (e: Exception){
                Util.showToast(context, context.getString(R.string.campo_email_vazio))
            }
        }
    }
}