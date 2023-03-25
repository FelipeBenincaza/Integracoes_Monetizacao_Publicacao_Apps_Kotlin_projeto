package com.benincaza.projetointegracoeskotlin.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.benincaza.projetointegracoeskotlin.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginScreen : AppCompatActivity() {

    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText

    val Req_Code:Int=123456;
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
        supportActionBar?.hide()

        edtEmail = findViewById<EditText>(R.id.edt_email)
        edtPassword = findViewById<EditText>(R.id.edt_password)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        firebaseAuth = FirebaseAuth.getInstance()

        findViewById<View>(R.id.txt_esqueci_senha).setOnClickListener{
            FormForgotPassword(this)
                .show() { eventoCriado ->
                    firebaseAuth.sendPasswordResetEmail(eventoCriado.email).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Util.showToast(this, getString(R.string.email_enviado_suceso))
                        }
                    }
                }
        }

        findViewById<View>(R.id.txt_register_view).setOnClickListener{
            startActivity(Intent(this, CreateAccount::class.java))
        }

        findViewById<View>(R.id.btn_signin).setOnClickListener {
            Util.showToast(this, getString(R.string.login_com_google))
            signInGoogle()
        }

        findViewById<View>(R.id.btn_enter).setOnClickListener {
            sign()
        }
    }

    private fun signInGoogle(){
        val signIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signIntent, Req_Code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == Req_Code){
            val result = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(result)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>){
        try{
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            Util.showToast(this, getString(R.string.login_success))
            if(account != null){
                UpdateUser(account)
            }
        }catch (e: ApiException){
            println(e)
            Util.showToast(this, getString(R.string.login_failed))
        }
    }

    private fun UpdateUser(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {task ->
            if(task.isSuccessful){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun sign(){
        try {
            val valida = ValidateAuthentication(this)
            valida.validaCampoEmail(edtEmail)
            valida.validaCampoSenha(edtPassword)

            val userEmail = edtEmail.text.toString().trim()
            val userPassword = edtPassword.text.toString().trim()

            firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
                    if(firebaseUser != null && firebaseUser.isEmailVerified()){
                        startActivity(Intent(this, MainActivity::class.java))
                        Util.showToast(this, getString(R.string.login_success))
                        finish()
                    }else if(firebaseUser != null && !firebaseUser.isEmailVerified()){
                        firebaseAuth.signOut()
                        Util.showToast(this, getString(R.string.check_your_email))
                    }else{
                        Util.showToast(this, getString(R.string.login_failed))
                    }
                }else{
                    Util.showToast(this, getString(R.string.login_failed))
                }
            }
        } catch (e : ValidateAuthenticationException){
            Util.showToast(this, e.message.toString())
        }
    }
}