package com.benincaza.projetointegracoeskotlin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.adapter.LivrosAdapter
import com.benincaza.projetointegracoeskotlin.databinding.FragmentListaLivrosBinding
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

class ListaLivrosFragment : Fragment() {

    private var _binding: FragmentListaLivrosBinding? = null
    private val binding get() = _binding!!

    lateinit var mGoogleSignClient: GoogleSignInClient

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/livros")
    val listItems = ArrayList<Livros>()

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

        val adapter = LivrosAdapter(requireActivity(), listItems)
        val listView = binding.listViewTasks
        listView.adapter = adapter

        ref.addValueEventListener(object: ValueEventListener {
            val ctx = requireActivity()

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listItems.clear()

                if (tipoLista === "todos"){
                    for(child in dataSnapshot.children){
                        val status = child.child("status").value.toString().equals("Lido")
                        listItems.add(Livros(child.child("titulo").value.toString(),
                            child.child("genero").value.toString(),
                            child.child("paginas").value.toString(),
                            child.child("key").value.toString(),
                            status))
                    }
                } else {
                    for(child in dataSnapshot.children){
                        if (child.child("status").value.toString() == tipoLista){
                            val status = child.child("status").value.toString() ==" Lido"
                            listItems.add(Livros(child.child("titulo").value.toString(),
                                child.child("genero").value.toString(),
                                child.child("paginas").value.toString(),
                                child.child("key").value.toString(),
                                status))
                        }
                    }
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

        return binding.root
    }
}