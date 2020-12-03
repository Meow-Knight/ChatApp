package com.lecaoviethuy.messengerapp.controllers

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class TokenController {
    companion object{

        val tokenReference = FirebaseDatabase.getInstance().reference.child("Tokens")
        var deleteListener : ValueEventListener? = null
        var isDeleting = false

        /**
         * delete child of realtime-database/tokens having key is uid
         * @param uid: it is user id on authentication firebase and a note on realtime-database/tokens
         * */
        fun deleteUid(uid : String){
            deleteListener = tokenReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isDeleting){
                        Log.d("check_activity", "on delete all token controller")
                        try {
                            for (shot in snapshot.children) {
                                if (shot.key == uid){
                                    tokenReference.child(shot.key!!).removeValue()
                                    break
                                }
                            }

                            ChatListController.deleteAllUser(uid)
                        } catch (e1 : Exception){
                            e1.printStackTrace()
                            Log.d("check", "errorrrrrrrrrrrrr in token controller")
                        }
                        isDeleting = true
                        clearDeleteAllListener()
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        public fun clearDeleteAllListener(){
            if (deleteListener != null){
                tokenReference.removeEventListener(deleteListener!!)
            }
        }
    }
}