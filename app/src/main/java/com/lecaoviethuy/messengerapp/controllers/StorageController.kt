package com.lecaoviethuy.messengerapp.controllers

import android.util.Log
import com.google.firebase.storage.FirebaseStorage

class StorageController {
    companion object{
        fun deleteFile(path : String){
            try {
                FirebaseStorage.getInstance()
                    .reference
                    .child(path)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("check_delete", "success delete image")
                    }.addOnFailureListener {
                        Log.d("check_delete", path)
                    }
            } catch (e : Exception){
                Log.d("check", path)
            }
        }
    }
}