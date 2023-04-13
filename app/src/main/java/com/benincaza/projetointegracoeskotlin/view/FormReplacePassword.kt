package com.benincaza.projetointegracoeskotlin.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.benincaza.projetointegracoeskotlin.*
import com.benincaza.projetointegracoeskotlin.databinding.FormReplacePasswordBinding
import com.benincaza.projetointegracoeskotlin.fragments.DificuldadeSenhaFragment

class FormReplacePassword(private val context: ProfileActivity) {

    lateinit var edtPassword: DificuldadeSenhaFragment
    fun show(
        eventoCriado: (eventoCriado: EventReplacePassword) -> Unit
    ) {
        val binding = FormReplacePasswordBinding
            .inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .show()

        edtPassword = context.supportFragmentManager.findFragmentById(R.id.textInputLayoutPassword) as DificuldadeSenhaFragment

        binding.imgClose.setOnClickListener {
            dialog.dismiss()
        }

        binding.botaoSalvar.setOnClickListener {
            try {
                val valida = ValidateAuthentication(context)
                valida.validaCampoRegisterSenha(edtPassword, binding.confirmPassword)

                val password = edtPassword.text.toString()
                val evento = EventReplacePassword(password)
                dialog.dismiss()
                eventoCriado(evento)
            } catch (e: ValidateAuthenticationException){
                Util.showToast(context, e.message.toString())
            }catch (e: Exception){
                Util.showToast(context, context.getString(R.string.preencher_campos_senhas))
            }
        }
    }
}