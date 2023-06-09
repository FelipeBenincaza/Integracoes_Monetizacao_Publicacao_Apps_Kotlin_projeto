package com.benincaza.projetointegracoeskotlin

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LivroPreferences(context: Context) {

    private val livro: SharedPreferences =
        context.getSharedPreferences("notification", Context.MODE_PRIVATE)

    private var gson = Gson()

    fun storeString(key: String, array: ArrayList<String>){
        val arrayJson = gson.toJson(array)
        livro.edit().putString(key, arrayJson).apply()
    }

    fun getString(key: String): ArrayList<String>{
        val arrayJson = livro.getString(key, "")
        return if(arrayJson.isNullOrEmpty()){
            arrayListOf()
        }else{
            gson.fromJson(arrayJson, object: TypeToken<ArrayList<String>>(){}.type)
        }
    }
}