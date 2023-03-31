package com.benincaza.projetointegracoeskotlin.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.benincaza.projetointegracoeskotlin.EventReplacePassword
import com.benincaza.projetointegracoeskotlin.Util
import com.benincaza.projetointegracoeskotlin.ValidateAuthentication
import com.benincaza.projetointegracoeskotlin.ValidateAuthenticationException
import com.benincaza.projetointegracoeskotlin.databinding.FormReplacePasswordBinding

class FormReplacePassword(private val context: Context) {

    fun show(
        eventoCriado: (eventoCriado: EventReplacePassword) -> Unit
    ) {
        val binding = FormReplacePasswordBinding
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
                valida.validaCampoRegisterSenha(binding.password, binding.confirmPassword)

                val password = binding.password.text.toString()
                val evento = EventReplacePassword(password)
                dialog.dismiss()
                eventoCriado(evento)
            } catch (e: ValidateAuthenticationException){
                Util.showToast(context, e.message.toString())
            }


        }
    }
}