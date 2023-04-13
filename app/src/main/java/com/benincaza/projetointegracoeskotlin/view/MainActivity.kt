package com.benincaza.projetointegracoeskotlin.view

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
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
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    lateinit var mGoogleSignClient: GoogleSignInClient

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/livros")
    val ref_perfil = FirebaseDatabase.getInstance().getReference("/users/$uid/perfil")
    var perfilId: String = ""

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        /*AdMob
        * MobileAds.initialize(this)*/

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

        if(firebaseUser != null){
            val displayName = firebaseUser.displayName
            val photoUrl = firebaseUser.photoUrl

            if(displayName.toString() != "") {
                binding.txtNome.text = displayName.toString()
            } else{
                binding.txtNome.isVisible = false
            }

            if(photoUrl != null){
                Thread{
                    val file = saveLocalFile(photoUrl.toString())
                    runOnUiThread{
                        val bitmap = BitmapFactory.decodeFile(file.path)
                        binding.imgPerfil.setImageBitmap(bitmap)
                    }
                }.start()
            }
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

        binding.btnBiblioteca.setOnClickListener{
            val activity = Intent(this, BibliotecaActivity::class.java);
            startActivity(activity)
        }

        ref.addValueEventListener(object: ValueEventListener {
            val ctx = this@MainActivity;

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var lido = 0
                var naoLido = 0

                for(child in dataSnapshot.children){
                    if (child.child("status").value.toString() == "Lido")
                        lido++
                    else
                        naoLido++
                }

                binding.txtTotalLivros.text = getString(R.string.total_livros) + ": ${dataSnapshot.children.toList().size}"
                binding.txtLivrosLido.text = getString(R.string.lido_var, lido.toString())
                binding.txtLivrosNaoLido.text = getString(R.string.nao_lido_var, naoLido.toString())

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(ctx, getString(R.string.erro_carregar_livros), Toast.LENGTH_SHORT).show()
            }
        })

        ref_perfil.addValueEventListener(object: ValueEventListener {
            val ctx = this@MainActivity;

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.children.toList().size > 0){
                    perfilId = dataSnapshot.children.toList()[0].key.toString()
                    if (perfilId !== ""){
                        val child = dataSnapshot.children.toList()[0]
                        binding.txtPreferencia.setText("Preferêcias: " + child.child("preferencia").value.toString())
                    }
                } else {
                    binding.txtPreferencia.isVisible = false
                    binding.txtNome.isVisible = false
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(ctx, getString(R.string.erro_carrega_perfil), Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun saveLocalFile(_url: String): File {
        val url = URL(_url)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()

        val input = connection.inputStream
        val dir = File(getExternalFilesDir(null), "images")
        if(!dir.exists()){
            dir.mkdir()
        }

        val file = File(dir, "imagem.jpg")
        val output = FileOutputStream(file)

        val buffer = ByteArray(1024)
        var read: Int;
        while(input.read(buffer).also { read = it} != -1){
            output.write(buffer, 0, read)
        }

        output.flush()
        output.close()
        input.close()

        return file
    }
}