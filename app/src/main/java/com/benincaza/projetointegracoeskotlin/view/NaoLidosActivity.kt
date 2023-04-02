package com.benincaza.projetointegracoeskotlin.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.os.bundleOf
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.Util
import com.benincaza.projetointegracoeskotlin.databinding.ActivityLivrosLidosBinding
import com.benincaza.projetointegracoeskotlin.databinding.ActivityNaoLidosBinding
import com.benincaza.projetointegracoeskotlin.fragments.ListaLivrosFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class NaoLidosActivity : AppCompatActivity() {

    lateinit var mGoogleSignClient: GoogleSignInClient
    private lateinit var binding: ActivityNaoLidosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaoLidosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val bundle = bundleOf("tipo_lista" to "Não lido")
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

        binding.bottomNavigation.getMenu().findItem(R.id.nav_lidos).isChecked = true

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_all -> {
                    val activity = Intent(this, BibliotecaActivity::class.java);
                    startActivity(activity)
                    finish()
                    true
                }
                R.id.nav_lidos -> {
                    val activity = Intent(this, LivrosLidosActivity::class.java);
                    startActivity(activity)
                    finish()
                    true
                }
                R.id.nav_nao_lidos -> {
                    true
                }
                else -> false
            }

        }

        binding.bottomNavigation.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.nav_all -> {
                    Util.showToast(this, getString(R.string.todos_livros))
                }
                R.id.nav_lidos -> {
                    Util.showToast(this, getString(R.string.livros_lidos))
                }
                R.id.nav_nao_lidos -> {
                    Util.showToast(this, getString(R.string.livros_nao_lidos))
                }
            }
        }
    }
}