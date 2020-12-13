package com.lecaoviethuy.messengerapp.controllers

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class TokenController {
    companion object{
        private val tokenReference = FirebaseDatabase.getInstance().reference.child("Tokens")
        /**
         * delete child of realtime-database/tokens having key is uid
         * @param uid: it is user id on authentication firebase and a note on realtime-database/tokens
         * */
        fun deleteUid(uid : String){
            tokenReference.child(uid).removeValue()
                .addOnSuccessListener {
                    ChatListController.deleteUser(uid)
                }
        }
    }
}