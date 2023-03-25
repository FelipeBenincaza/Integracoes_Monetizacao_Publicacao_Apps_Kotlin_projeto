package com.benincaza.projetointegracoeskotlin.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.Util
import com.benincaza.projetointegracoeskotlin.ValidateAuthentication
import com.benincaza.projetointegracoeskotlin.ValidateAuthenticationException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class CreateAccount : AppCompatActivity() {

    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText
    lateinit var edtConfirmPassword: EditText
    lateinit var createAccountInputArray: Array<EditText>

    val Req_Code:Int=123456;
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        supportActionBar?.hide()

        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()

        edtEmail = findViewById<EditText>(R.id.edt_email)
        edtPassword = findViewById<EditText>(R.id.edt_password)
        edtConfirmPassword = findViewById<EditText>(R.id.edt_confirm_password)

        createAccountInputArray = arrayOf(edtEmail, edtPassword, edtConfirmPassword)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<View>(R.id.txt_login_view).setOnClickListener{
            val activity = Intent(this, LoginScreen::class.java)
            startActivity(activity)
        }

        findViewById<View>(R.id.btn_signin).setOnClickListener {
            Util.showToast(this, getString(R.string.registro_com_google))
            signInGoogle()
        }

        findViewById<View>(R.id.btn_create_account).setOnClickListener {
            signIn()
        }
    }

    private fun signInGoogle(){
        val signIntent: Intent = mGoogleSignInClient.signInIntent;
        startActivityForResult(signIntent, Req_Code);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == Req_Code){
            val result = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleResult(result);
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>){
        try{
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java);
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
        val credential = GoogleAuthProvider.getCredential(account.idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {task ->
            if(task.isSuccessful){
                val intent = Intent(this, MainActivity::class.java);
                startActivity(intent);
                finish()
            }
        }
    }

    private fun signIn() {
        try {
            val valida = ValidateAuthentication(this)
            valida.validaCampoEmail(edtEmail)
            valida.validaCampoRegisterSenha(edtPassword, edtConfirmPassword)

            val userEmail = edtEmail.text.toString().trim()
            val userPassword = edtPassword.text.toString().trim()

            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        sendEmailVerification()
                        Util.showToast(this, getString(R.string.user_created_success))
                        finish()
                    } else {
                        val exception = task.exception;
                        if (exception is FirebaseAuthException && exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                            Util.showToast(this, getString(R.string.email_registered))
                        } else {
                            Util.showToast(this, getString(R.string.erro_create_user))
                        }
                    }
                }
        }catch (e: ValidateAuthenticationException){
            Util.showToast(this, e.message.toString())
        }
    }

    private fun sendEmailVerification(){
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

        firebaseUser?.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Util.showToast(this, getString(R.string.email_success_sent))
                }
            }
        }
    }
}