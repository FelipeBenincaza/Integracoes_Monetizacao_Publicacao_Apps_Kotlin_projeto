package com.benincaza.projetointegracoeskotlin

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.benincaza.projetointegracoeskotlin.databinding.FormReplacePasswordBinding

class FormReplacePassword(private val context: Context) {

    fun show(
        quandoEventoCriado: (eventoCriado: EventReplacePassword) -> Unit
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
            val password = binding.password.text.toString()
            val evento = EventReplacePassword(password)
            dialog.dismiss()
            quandoEventoCriado(evento)
        }
    }
}