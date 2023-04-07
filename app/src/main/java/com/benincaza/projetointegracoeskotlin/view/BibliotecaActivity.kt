package com.benincaza.projetointegracoeskotlin.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.os.bundleOf
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.Util
import com.benincaza.projetointegracoeskotlin.databinding.ActivityBibliotecaBinding
import com.benincaza.projetointegracoeskotlin.fragments.ListaLivrosFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class BibliotecaActivity : AppCompatActivity() {

    lateinit var mGoogleSignClient: GoogleSignInClient
    private lateinit var binding: ActivityBibliotecaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBibliotecaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val bundle = bundleOf("tipo_lista" to "todos")
        val fragment = ListaLivrosFragment()
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignClient = GoogleSignIn.getClient(this, gso)

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

        binding.logout.setOnClickListener{
            firebaseAuth.signOut()
            mGoogleSignClient.signOut()

            startActivity(Intent(this, LoginScreen::class.java))
        }

        binding.home.setOnClickListener{
            val activity = Intent(this, MainActivity::class.java);
            startActivity(activity)
            finish()
        }

        binding.fabAddTask.setOnClickListener{
            val activity = Intent(this, LivrosActivity::class.java);
            startActivity(activity)
        }
    }
}