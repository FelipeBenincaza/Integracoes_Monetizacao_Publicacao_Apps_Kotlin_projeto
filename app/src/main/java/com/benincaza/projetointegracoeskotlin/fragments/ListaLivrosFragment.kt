package com.benincaza.projetointegracoeskotlin.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.adapter.LivroRecyclerView
import com.benincaza.projetointegracoeskotlin.databinding.FragmentListaLivrosBinding
import com.benincaza.projetointegracoeskotlin.`interface`.OnClickRecyclerView
import com.benincaza.projetointegracoeskotlin.model.Livros
import com.benincaza.projetointegracoeskotlin.view.LivrosActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ListaLivrosFragment : Fragment(), OnClickRecyclerView {

    private var _binding: FragmentListaLivrosBinding? = null
    private val binding get() = _binding!!

    lateinit var mGoogleSignClient: GoogleSignInClient

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/livros")
    val listItems = ArrayList<Livros>()
    lateinit var recyclerview: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentListaLivrosBinding.inflate(inflater, container, false)

        val tipoLista = arguments?.getString("tipo_lista")

        //(activity as BibliotecaActivity?)!!
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignClient = GoogleSignIn.getClient(requireActivity(), gso)

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

        recyclerview = binding.recyclerview
        recyclerview.layoutManager = LinearLayoutManager(requireContext())
        val adapter = LivroRecyclerView(listItems, this)
        recyclerview.adapter = adapter

        ref.addValueEventListener(object: ValueEventListener {
            val ctx = requireActivity()

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listItems.clear()

                if (tipoLista === "todos"){
                    for(child in dataSnapshot.children){
                        val status = child.child("status").value.toString().equals("Lido")
                        listItems.add(Livros(child.key.toString(),
                            child.child("titulo").value.toString(),
                            child.child("genero").value.toString(),
                            child.child("paginas").value.toString(),
                            child.child("data").value.toString(),
                            child.child("photoUrl").value.toString(),
                            status))
                    }
                } else {
                    for(child in dataSnapshot.children){
                        if (child.child("status").value.toString() == tipoLista){
                            val status = child.child("status").value.toString() ==" Lido"
                            listItems.add(Livros(child.key.toString(),
                                child.child("titulo").value.toString(),
                                child.child("genero").value.toString(),
                                child.child("paginas").value.toString(),
                                child.child("data").value.toString(),
                                child.child("photoUrl").value.toString(),
                                status))
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(ctx, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }

    override fun onClickItemListener(livro: Livros) {
        val itemId =  livro.key

        val activity = Intent(requireContext(), LivrosActivity::class.java)
        activity.putExtra("id", itemId)
        startActivity(activity)
    }

    override fun onClickLongItemListener(livro: Livros) {
        if(livro.key != null){
            AlertDialog.Builder(requireContext())
                .setTitle("Deletar tarefa")
                .setMessage("Deseja deletar a tarefa?")
                .setPositiveButton("Sim"){ dialog, which ->

                    if (livro.photo != null && livro.photo.isNotEmpty()){
                        val desertRef = FirebaseStorage.getInstance().getReferenceFromUrl(Uri.parse(livro.photo).toString())
                        desertRef.delete().addOnSuccessListener {
                            ref.child(livro.key).removeValue()
                            Toast.makeText(requireContext(), "Livro deletado com sucesso", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "Erro ao deletar livro!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        ref.child(livro.key).removeValue()
                        Toast.makeText(requireContext(), "Livro deletado com sucesso", Toast.LENGTH_SHORT).show()
                    }

                }
                .setNegativeButton("Não"){ dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}