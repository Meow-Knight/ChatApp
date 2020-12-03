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
        var deleteAllListener : ValueEventListener? = null
        var isDeleting = false

        /**
         * delete child of realtime-database/users having key is uid
         * @param uid: it is user id on authentication firebase and a note on realtime-database/users
         * */
        fun deleteAllUser(uid : String){
            deleteAllListener = usersReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isDeleting){
                        try {
                            for (shot in snapshot.children) {
                                if (shot.key == uid){
                                    val user = shot.getValue(User::class.java)
                                    usersReference.child(shot.key!!).removeValue()
                                    break
                                }
                            }

                            clearDeleteAllListener()

                        } catch (e : Exception){
                            e.printStackTrace()
                        }

                        isDeleting = false
                        DatabaseController.isDeleting = false
                        clearDeleteAllListener()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        public fun clearDeleteAllListener(){
            if(deleteAllListener != null){
                usersReference.removeEventListener(deleteAllListener!!)
            }
        }
    }


}