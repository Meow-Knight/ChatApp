package com.lecaoviethuy.messengerapp.controllers

import android.util.Log
import com.google.firebase.storage.FirebaseStorage

class StorageController {
    companion object{
        fun deleteFile(url : String){
            try {
                FirebaseStorage.getInstance()
                    .getReference()
                    .child(url)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("check_delete", "success delete image")
                    }.addOnFailureListener {
                        Log.d("check_delete", "fail delete image")
                    }
            } catch (e : Exception){
                Log.d("check", url)
            }
        }
    }
}