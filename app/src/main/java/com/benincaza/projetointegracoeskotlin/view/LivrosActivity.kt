package com.benincaza.projetointegracoeskotlin.view

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.benincaza.projetointegracoeskotlin.LivroPreferences
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.Util
import com.benincaza.projetointegracoeskotlin.databinding.ActivityLivrosBinding
import com.benincaza.projetointegracoeskotlin.services.NotificationReceiver
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class LivrosActivity : AppCompatActivity() {

    private val LIVRO_SHARED = "livro"
    private lateinit var binding: ActivityLivrosBinding

    private val PERMISSION_REQUEST_CAMERA = 0
    private val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1

    var _image: Bitmap? = null


    private lateinit var firebaseAnalytics: FirebaseAnalytics
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db_ref = FirebaseDatabase.getInstance().getReference("/users/$uid/livros")

    var livroId: String = ""
    var status: String = "Não lido"

    companion object{
        private const val REQUEST_IMAGE_GALLERY = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivrosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        load()

        val in_date = binding.edtData

        val current_date_time = Calendar.getInstance()
        val day = current_date_time.get(Calendar.DAY_OF_MONTH)
        val month = current_date_time.get(Calendar.MONTH)
        val year = current_date_time.get(Calendar.YEAR)

        in_date.setText(String.format("%02d/%02d/%04d", day, month + 1, year))

        binding.btnDate.setOnClickListener{
            val datePickerDialog = DatePickerDialog(this, {_, yearOfYear, monthOfYear, dayOfMonth ->
                in_date.setText(String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, yearOfYear))
            }, year, month, day)
            datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
            datePickerDialog.show()
        }

        binding.btnSave.setOnClickListener{
            if (notEmpty()){
                saveImagemLivro()
            } else {
                Util.showToast(this, getString(R.string.livro_preeencher_campos))
            }
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
        }

        binding.fabFiles.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, LivrosActivity.REQUEST_IMAGE_GALLERY)
        }

        binding.fabCamera.setOnClickListener{
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, LivrosActivity.REQUEST_IMAGE_CAPTURE)
        }

        createNotificationChannel()
    }

    private fun notEmpty(): Boolean = binding.edtTitulo.text.toString().trim().isNotEmpty() &&
            binding.edtGenero.text.toString().trim().isNotEmpty() && binding.edtPaginas.text.toString().trim().isNotEmpty()

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.rb_nao_lido ->
                    if (checked) {
                        status = view.text.toString()
                    }
                R.id.rb_lido ->
                    if (checked) {
                        status = view.text.toString()
                    }
            }
        }
    }

    fun load(){
        this.livroId = intent.getStringExtra("id") ?: ""
        if(livroId === "") return

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/livros/$livroId")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) return

                binding.edtTitulo.setText(snapshot.child("titulo").value.toString())
                binding.edtGenero.setText(snapshot.child("genero").value.toString())
                binding.edtPaginas.setText(snapshot.child("paginas").value.toString())
                binding.edtData.setText(snapshot.child("data").value.toString())
                if (snapshot.child("status").value.toString().equals("Lido")){
                    binding.rbLido.isChecked = true
                } else{
                    binding.rbNaoLido.isChecked = true
                }

                val photo = snapshot.child("photoUrl").value.toString()
                if (photo != null && photo.isNotEmpty()){
                    val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(Uri.parse(photo).toString())
                    imageRef.getBytes(10 * 1024 * 1024).addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                        binding.capaImage.setImageBitmap(bitmap)
                    }.addOnFailureListener {
                        // Handle any errors
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LivrosActivity, "Erro ao carregar livro", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun createUpdate(uri: String){
        if(livroId !== ""){
            val bundleUpdate = Bundle()
            bundleUpdate.putString(FirebaseAnalytics.Param.ITEM_ID, uid)
            bundleUpdate.putString(FirebaseAnalytics.Param.ITEM_NAME, "update_livro")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleUpdate)

            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/livros/$livroId")

            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!snapshot.exists()) return
                    val task = snapshot.value as HashMap<String, String>

                    task["titulo"] = binding.edtTitulo.text.toString()
                    task["genero"] = binding.edtGenero.text.toString()
                    task["paginas"] = binding.edtPaginas.text.toString()
                    task["data"] = binding.edtData.text.toString()
                    task["photoUrl"] = uri
                    task["status"] = status

                    ref.setValue(task)
                    Toast.makeText(this@LivrosActivity, "Livro atualizado com sucesso", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LivrosActivity, "Erro ao atualizar livro", Toast.LENGTH_SHORT).show()
                }
            })
        }else{
            val bundleCreate = Bundle()
            bundleCreate.putString(FirebaseAnalytics.Param.ITEM_ID, uid)
            bundleCreate.putString(FirebaseAnalytics.Param.ITEM_NAME, "create_livro")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleCreate)

            val livro =  hashMapOf(
                "titulo" to binding.edtTitulo.text.toString(),
                "genero" to binding.edtGenero.text.toString(),
                "paginas" to binding.edtPaginas.text.toString(),
                "data" to binding.edtData.text.toString(),
                "photoUrl" to uri,
                "status" to status,
            )

            val novoElemento = db_ref.push()
            novoElemento.setValue(livro)

            Util.showToast(this, getString(R.string.livro_criado_sucesso))

            LivroPreferences(this).storeString(LIVRO_SHARED, arrayListOf(binding.edtTitulo.text.toString(), binding.edtData.text.toString()))
            scheduleNotification(binding.edtData.text.toString(), "19:00")

            Intent(this, BibliotecaActivity::class.java).also {
                startActivity(it)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(){
        val name = "Notification Channel INFNET"
        val descriptionText = "Channel for INFNET notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("INFNET", name, importance).apply {
            description = descriptionText
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleNotification(data: String, hora: String){
        val itemList = LivroPreferences(this).getString(LIVRO_SHARED)

        val intent = Intent(this, NotificationReceiver::class.java)
        val titulo = getString(R.string.notification_titulo)
        val mensagem = getString(R.string.notification_mensagem, itemList[0])

        intent.putExtra("titulo", titulo)
        intent.putExtra("mensagem", mensagem)


        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        //calendar.add(Calendar.MINUTE, 1)
        val data_dia = data.substring(0, 2).toInt()
        val data_mes = data.substring(3, 5).toInt() - 1
        val data_ano = data.substring(6, 10).toInt()
        val hora_hora = hora.substring(0, 2).toInt()
        val hora_minuto = hora.substring(3, 5).toInt()

        calendar.set(
            data_ano,
            data_mes,
            data_dia,
            hora_hora,
            hora_minuto,
            0
        )
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun saveImagemLivro(){
        if (this._image != null){
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val user = FirebaseAuth.getInstance().currentUser
            val uid = user?.uid

            val date = Calendar.getInstance().time

            var dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            var nomeImg = dateTimeFormat.format(date).replace("/", "_")
            nomeImg = nomeImg.replace(" ", "_")
            nomeImg = nomeImg.replace(":", "_")

            val imageRef = storageRef.child("capa_livro/${nomeImg}")
            val baos = ByteArrayOutputStream()
            this._image?.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            val data = baos.toByteArray()
            val uploadTask = imageRef.putBytes(data)

            uploadTask.addOnFailureListener {
                Util.showToast(this, getString(R.string.falha_salvar_imagem))
            }.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    createUpdate(Uri.parse(uri.toString()).toString())
                }
            }
        } else {
            createUpdate("")
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
                LivrosActivity.REQUEST_IMAGE_GALLERY -> {
                    val selectedImage: Uri? = data?.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)

                    binding.capaImage.setImageBitmap(imageBitmap)
                    this._image = imageBitmap
                }
                LivrosActivity.REQUEST_IMAGE_CAPTURE -> {
                    val imageCaptured =  data?.extras?.get("data") as Bitmap
                    binding.capaImage.setImageBitmap(imageCaptured)
                    this._image = imageCaptured
                }
            }
        }
    }
}