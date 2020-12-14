package com.lecaoviethuy.messengerapp.controllers

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lecaoviethuy.messengerapp.modelClasses.User

class UserController {
    companion object {
        val usersReference = FirebaseDatabase.getInstance()
            .reference
            .child("Users")

        /**
         * delete child of realtime-database/users having key is uid
         * @param uid: it is user id on authentication firebase and a note on realtime-database/users
         * */
        fun deleteUser(uid : String){
            usersReference.child(uid).removeValue()
                .addOnSuccessListener {
                    DatabaseController.isDeleting = false
                }
        }
    }


}