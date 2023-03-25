package com.benincaza.projetointegracoeskotlin

import android.content.Context
import android.widget.Toast

class Util private constructor(){
    companion object{
        fun showToast(context: Context, message: String){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}