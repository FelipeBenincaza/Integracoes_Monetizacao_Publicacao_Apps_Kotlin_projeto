package com.benincaza.projetointegracoeskotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private val PRODUCTS = "produtos"
    lateinit var mGoogleSignClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

        val listViewTasks = findViewById<ListView>(R.id.listViewTasks)
        val edtProduct = findViewById<EditText>(R.id.edt_product)

        val itemList = ListaComprasPreferences(this).getString(PRODUCTS)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, itemList)

        listViewTasks.adapter = adapter
        adapter.notifyDataSetChanged()

        findViewById<View>(R.id.btn_add).setOnClickListener {
            itemList.add(edtProduct.text.toString());
            listViewTasks.adapter = adapter
            adapter.notifyDataSetChanged()

            ListaComprasPreferences(this).storeString(PRODUCTS, itemList)
            edtProduct.text.clear()
        }

        findViewById<View>(R.id.btn_delete).setOnClickListener {
            val position: SparseBooleanArray = listViewTasks.checkedItemPositions
            val count = listViewTasks.count
            var item = count - 1
            while(item >= 0){
                if(position.get(item)){
                    adapter.remove(itemList.get(item))
                }
                item--
            }

            ListaComprasPreferences(this).storeString(PRODUCTS, itemList)
            position.clear()
            adapter.notifyDataSetChanged()
        }

        findViewById<View>(R.id.btn_clear).setOnClickListener {
            itemList.clear()
            ListaComprasPreferences(this).storeString(PRODUCTS, itemList)
            adapter.notifyDataSetChanged()
        }

        findViewById<View>(R.id.logout).setOnClickListener{
            firebaseAuth.signOut()
            mGoogleSignClient.signOut()

            startActivity(Intent(this, LoginScreen::class.java))
        }

        findViewById<View>(R.id.profile).setOnClickListener{
            val activity = Intent(this, ProfileActivity::class.java);
            startActivity(activity)
            finish()
        }
    }
}