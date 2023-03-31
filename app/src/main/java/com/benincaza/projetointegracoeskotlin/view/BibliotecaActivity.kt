package com.benincaza.projetointegracoeskotlin.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.databinding.ActivityBibliotecaBinding
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

class BibliotecaActivity : AppCompatActivity() {

    lateinit var mGoogleSignClient: GoogleSignInClient

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/livros")
    val listItems = ArrayList<String>()

    private lateinit var binding: ActivityBibliotecaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBibliotecaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

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

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        val listView = binding.listViewTasks
        listView.adapter = adapter

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

        ref.addValueEventListener(object: ValueEventListener {
            val ctx = this@BibliotecaActivity;

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listItems.clear()

                for(child in dataSnapshot.children){
                    listItems.add(child.child("titulo").value.toString())
                }

                adapter.notifyDataSetChanged()

                listView.setOnItemLongClickListener { parent, view, position, id ->
                    val itemId =  dataSnapshot.children.toList()[position].key

                    if(itemId != null){
                        AlertDialog.Builder(ctx)
                            .setTitle("Deletar tarefa")
                            .setMessage("Deseja deletar a tarefa?")
                            .setPositiveButton("Sim"){ dialog, which ->
                                ref.child(itemId).removeValue()
                                Toast.makeText(ctx, "Tarefa deletada com sucesso", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Não"){ dialog, which ->
                                dialog.dismiss()
                            }
                            .show()
                    }

                    true
                }
                listView.setOnItemClickListener { parent, view, position, id ->
                    val itemId =  dataSnapshot.children.toList()[position].key

                    val activity = Intent(ctx, LivrosActivity::class.java)
                    activity.putExtra("id", itemId)
                    startActivity(activity)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(ctx, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show()
            }
        })
    }
}