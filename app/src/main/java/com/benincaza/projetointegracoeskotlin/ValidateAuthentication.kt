package com.benincaza.projetointegracoeskotlin

import android.content.Context
import android.widget.EditText
import java.util.regex.Matcher
import java.util.regex.Pattern

class ValidateAuthentication(val context: Context) {

    @Throws(ValidateAuthenticationException::class)
    fun validaCampoEmail(email: EditText) {
        if (email.text.toString().isEmpty())
            throw ValidateAuthenticationException(context.getString(R.string.campo_email_vazio))

        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email.text.toString())
        if (!matcher.matches()) {
            throw ValidateAuthenticationException(context.getString(R.string.email_invalido))
        }
    }

    @Throws(ValidateAuthenticationException::class)
    fun validaCampoSenha(senha: EditText) {
        if (senha.text.toString().isEmpty())
            throw ValidateAuthenticationException(context.getString(R.string.campo_senha_vazio))

        if(senha.text.toString().trim().length < 6)
            throw ValidateAuthenticationException(context.getString(R.string.password_inadequate_length))
    }

    @Throws(ValidateAuthenticationException::class)
    fun validaCampoRegisterSenha(password: EditText, confirmPassword: EditText) {
        if (password.text.toString().trim().isEmpty() && confirmPassword.text.toString().trim().isEmpty())
            throw ValidateAuthenticationException("Preencher os dois campos de senhas.")

        if(password.text.toString().trim() != confirmPassword.text.toString().trim())
            throw ValidateAuthenticationException(context.getString(R.string.passwords_not_match))

        if(password.text.toString().trim().length < 6)
            throw ValidateAuthenticationException(context.getString(R.string.password_inadequate_length))
    }
}