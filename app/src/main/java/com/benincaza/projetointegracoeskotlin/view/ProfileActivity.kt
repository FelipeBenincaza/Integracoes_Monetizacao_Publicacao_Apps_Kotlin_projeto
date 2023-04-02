package com.benincaza.projetointegracoeskotlin.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.Util
import com.benincaza.projetointegracoeskotlin.databinding.ActivityProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.HashMap

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    lateinit var mGoogleSignClient: GoogleSignInClient

    private val PERMISSION_REQUEST_CAMERA = 0
    private val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1

    var _image: Bitmap? = null

    val _uid = FirebaseAuth.getInstance().currentUser?.uid
    val db_ref = FirebaseDatabase.getInstance().getReference("/users/$_uid/perfil")
    var perfilId: String = ""

    companion object{
        private const val REQUEST_IMAGE_GALLERY = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignClient = GoogleSignIn.getClient(this, gso)
        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

        val user = firebaseAuth.currentUser
        if(user != null){
            val displayName = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl

            if(displayName.toString() != "") {
                val nameSplit = displayName.toString().split(" ")
                if(nameSplit.size > 1){
                    binding.edtName.setText(nameSplit[0])
                    binding.edtLastName.setText(nameSplit[1])
                }else{
                    binding.edtName.setText(displayName.toString())
                }
            }
            binding.edtEmail.setText(email)

            if(photoUrl != null){
                Thread{
                    val file = saveLocalFile(photoUrl.toString())
                    runOnUiThread{
                        val bitmap = BitmapFactory.decodeFile(file.path)
                        binding.profileImage.setImageBitmap(bitmap)
                    }
                }.start()
            }
        }

        db_ref.addValueEventListener(object: ValueEventListener {
            val ctx = this@ProfileActivity;

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                perfilId = dataSnapshot.children.toList()[0].key.toString()
                if (perfilId !== ""){
                    val child = dataSnapshot.children.toList()[0]
                    binding.edtPreferenciaGenero.setText(child.child("preferencia").value.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(ctx, getString(R.string.erro_carrega_perfil), Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnUpdateProfile.setOnClickListener{
            saveProfile()
        }

        binding.home.setOnClickListener{
            val activity = Intent(this, MainActivity::class.java);
            startActivity(activity)
            finish()
        }

        binding.fabFiles.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
        }

        binding.fabCamera.setOnClickListener{
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
        }

        binding.logout.setOnClickListener{
            firebaseAuth.signOut();
            mGoogleSignClient.signOut();

            val activity = Intent(this, LoginScreen::class.java)
            startActivity(activity)
            finish()
        }

        binding.btnChangePassword.setOnClickListener {
            FormReplacePassword(this).show() { eventoCriado ->
                val user = firebaseAuth.currentUser

                user!!.updatePassword(eventoCriado.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Util.showToast(this, getString(R.string.senha_alterada))
                        }
                    }
            }
        }
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

    fun saveProfile(){
        val name = binding.edtName.text.toString()
        val last_name = binding.edtLastName.text.toString()

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        val imageRef = storageRef.child("profile_images/${uid}")
        val baos = ByteArrayOutputStream()
        this._image?.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnFailureListener {
            Util.showToast(this, getString(R.string.falha_salvar_imagem))
        }.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val profileUpdates = userProfileChangeRequest {
                    displayName = "${name} ${last_name}"
                    photoUri = Uri.parse(uri.toString())
                }

                user!!.updateProfile(profileUpdates).addOnCompleteListener{ task ->
                    if(task.isSuccessful){
                        Util.showToast(this, getString(R.string.perfil_atualizado_sucesso))
                    }else{
                        Util.showToast(this, getString(R.string.falha_atualizar_perfil))
                    }
                }

                createUpdatePerfil(Uri.parse(uri.toString()).toString())
            }
        }
    }

    fun createUpdatePerfil(uri : String){
        if(perfilId !== ""){
            val ref = FirebaseDatabase.getInstance().getReference("/users/$_uid/perfil/$perfilId")

            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!snapshot.exists()) return
                    val task = snapshot.value as HashMap<String, String>

                    task["email"] = binding.edtEmail.text.toString()
                    task["nome"] = binding.edtName.text.toString()
                    task["sobrenome"] = binding.edtLastName.text.toString()
                    task["preferencia"] = binding.edtPreferenciaGenero.text.toString()
                    task["photoPerfil"] = uri

                    ref.setValue(task)
                    Util.showToast(this@ProfileActivity, getString(R.string.livro_atualizado_suceso))
                }

                override fun onCancelled(error: DatabaseError) {
                    Util.showToast(this@ProfileActivity, getString(R.string.erro_atualizar_livro))
                }
            })
        }else{
            val perfil =  hashMapOf(
                "email" to binding.edtEmail.text.toString(),
                "nome" to binding.edtName.text.toString(),
                "sobrenome" to binding.edtLastName.text.toString(),
                "preferencia" to binding.edtPreferenciaGenero.text.toString(),
                "photoPerfil" to uri
            )

            val novoElemento = db_ref.push()
            novoElemento.setValue(perfil)

            Util.showToast(this, getString(R.string.perfil_criado_sucesso))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_REQUEST_CAMERA){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Util.showToast(this, getString(R.string.permissao_camera_concedida))
            }else{
                Util.showToast(this, getString(R.string.permissao_camera_negada))
            }
        }else if(requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Util.showToast(this, getString(R.string.permissao_galeria_concedida))
            }else{
                Util.showToast(this, getString(R.string.permissao_galeria_negada))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK){
            when(requestCode){
                REQUEST_IMAGE_GALLERY -> {
                    val selectedImage: Uri? = data?.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)

                    binding.profileImage.setImageBitmap(imageBitmap)
                    this._image = imageBitmap
                }
                REQUEST_IMAGE_CAPTURE -> {
                    val imageCaptured =  data?.extras?.get("data") as Bitmap
                    binding.profileImage.setImageBitmap(imageCaptured)
                    this._image = imageCaptured
                }
            }
        }
    }
}