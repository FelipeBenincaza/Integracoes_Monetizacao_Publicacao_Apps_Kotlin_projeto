package com.benincaza.projetointegracoeskotlin.view

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.Util
import com.benincaza.projetointegracoeskotlin.databinding.ActivityLivrosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class LivrosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLivrosBinding

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db_ref = FirebaseDatabase.getInstance().getReference("/users/$uid/livros")

    var livroId: String = ""
    var status: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivrosBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            datePickerDialog.show()
        }

        binding.btnSave.setOnClickListener{
            createUpdate()
        }
    }

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
                binding.edtGenero.setText(snapshot.child("descricao").value.toString())
                binding.edtPaginas.setText(snapshot.child("paginas").value.toString())
                binding.edtData.setText(snapshot.child("data").value.toString())
                if (snapshot.child("status").value.toString().equals("Lido")){
                    binding.rbLido.isChecked = true
                } else{
                    binding.rbNaoLido.isChecked = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LivrosActivity, "Erro ao carregar livro", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun createUpdate(){
        if(livroId !== ""){
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/tasks/$taskId")

            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!snapshot.exists()) return
                    val task = snapshot.value as HashMap<String, String>

                    task["titulo"] = binding.edtTitulo.text.toString()
                    task["descricao"] = binding.edtGenero.text.toString()
                    task["paginas"] = binding.edtPaginas.text.toString()
                    task["data"] = binding.edtData.text.toString()
                    task["status"] = status

                    ref.setValue(task)
                    Toast.makeText(this@LivrosActivity, "Livro atualizado com sucesso", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LivrosActivity, "Erro ao atualizar livro", Toast.LENGTH_SHORT).show()
                }
            })
        }else{
            val livro =  hashMapOf(
                "titulo" to binding.edtTitulo.text.toString(),
                "descricao" to binding.edtGenero.text.toString(),
                "paginas" to binding.edtPaginas.text.toString(),
                "data" to binding.edtData.text.toString(),
                "status" to status,
            )

            val novoElemento = db_ref.push()
            novoElemento.setValue(livro)

            Util.showToast(this, getString(R.string.livro_criado_sucesso))

            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}