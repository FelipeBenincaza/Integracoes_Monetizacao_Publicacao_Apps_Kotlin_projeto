package com.benincaza.projetointegracoeskotlin.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.databinding.ActivityMainBinding
import com.benincaza.projetointegracoeskotlin.fragments.WeatherFragments
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    lateinit var mGoogleSignClient: GoogleSignInClient

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/livros")
    val listItems = ArrayList<String>()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, WeatherFragments()).commit()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignClient = GoogleSignIn.getClient(this, gso)

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
        if(firebaseUser == null){
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }

        binding.logout.setOnClickListener{
            firebaseAuth.signOut()
            mGoogleSignClient.signOut()

            startActivity(Intent(this, LoginScreen::class.java))
        }

        binding.profile.setOnClickListener{
            val activity = Intent(this, ProfileActivity::class.java);
            startActivity(activity)
        }

        binding.fabAddTask.setOnClickListener{
            val activity = Intent(this, LivrosActivity::class.java);
            startActivity(activity)
        }

        binding.txtBiblioteca.setOnClickListener{
            val activity = Intent(this, BibliotecaActivity::class.java);
            startActivity(activity)
        }

        ref.addValueEventListener(object: ValueEventListener {
            val ctx = this@MainActivity;

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listItems.clear()

                for(child in dataSnapshot.children){
                    listItems.add(child.child("titulo").value.toString())
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(ctx, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show()
            }
        })
    }
}