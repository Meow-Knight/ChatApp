package com.lecaoviethuy.messengerapp.controllers

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatListController {
    companion object {
        val chatListReference = FirebaseDatabase.getInstance()
            .reference
            .child("ChatList")
        var deleteAllListener : ValueEventListener? = null
        var isDeleting = false

        /**
         * realtime-database/chatlist have structure:
         * uid: + chatedUser1
         *      + chatedUser2
         *      + chatedUser3
         * => delete note have key equals with uid and child of another uid equals with uid
         * @param uid: it is user id on authentication firebase
         * */
        fun deleteAllUser(uid : String){
            deleteAllListener = chatListReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isDeleting){
                        try {
                            for (shot in snapshot.children) {
                                if (shot.key == uid){
                                    chatListReference.child(shot.key!!).removeValue()
                                } else {
                                    for(shot2 in shot.children){
                                        if (shot2.key == uid){
                                            chatListReference.child(shot.key!!).child(shot2.key!!).removeValue()
                                        }
                                    }
                                }
                            }

                            UserController.deleteAllUser(uid)
                        } catch (e : Exception){
                            e.printStackTrace()
                        }

                        isDeleting = false
                        clearDeleteAllListener()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        fun clearDeleteAllListener(){
            if(deleteAllListener != null){
                chatListReference.removeEventListener(deleteAllListener!!)
            }
        }
    }
}