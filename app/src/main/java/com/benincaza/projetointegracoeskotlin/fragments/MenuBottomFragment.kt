package com.benincaza.projetointegracoeskotlin.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.benincaza.projetointegracoeskotlin.R
import com.benincaza.projetointegracoeskotlin.Util
import com.benincaza.projetointegracoeskotlin.view.BibliotecaActivity
import com.benincaza.projetointegracoeskotlin.view.LivrosLidosActivity
import com.benincaza.projetointegracoeskotlin.view.NaoLidosActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuBottomFragment : Fragment() {

    private lateinit var bottomMenu: BottomNavigationView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_menu_bottom, container, false)

        bottomMenu = view.findViewById(R.id.bt_bottom_navigation)

        bottomMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_all -> {
                    val activity = Intent(requireContext(), BibliotecaActivity::class.java);
                    startActivity(activity)
                    true
                }
                R.id.nav_lidos -> {
                    val activity = Intent(requireContext(), LivrosLidosActivity::class.java);
                    startActivity(activity)
                    true
                }
                R.id.nav_nao_lidos -> {
                    val activity = Intent(requireContext(), NaoLidosActivity::class.java);
                    startActivity(activity)
                    true
                }
                else -> false
            }

        }

        return view
    }
}