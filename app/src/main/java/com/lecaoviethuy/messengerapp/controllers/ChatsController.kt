package com.lecaoviethuy.messengerapp.controllers

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lecaoviethuy.messengerapp.modelClasses.Chat

class ChatsController {
    companion object {
        var isDeleting = false
        val chatReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("Chats")
        var deleteAllListener : ValueEventListener? = null
        var deleteMessage : ValueEventListener? = null

        /**
         * delete chats have receiver or sender equals with uid on realtime-database/chats
         * @param uid: it is user id on authentication firebase
         * */
        fun deleteAllWithUserId(uid : String){
            deleteAllListener = chatReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!isDeleting){
                        isDeleting = true
                        try {
                            for (shot in snapshot.children) {
                                val chat = shot.getValue(Chat::class.java)

                                if(chat != null){
                                    // check if this is a image message then delete it in storage

                                    if (chat.getReceiver().equals(uid) || chat.getSender().equals(uid)) {
                                        if(chat.getUrl() != null){
                                            StorageController.deleteFile("Chat Images/" + chat.getMessageId())
                                        }
                                        chatReference.child(chat.getMessageId()!!).removeValue()
                                    }
                                }
                            }

                            TokenController.deleteUid(uid)
                        } catch (e : Exception){
                            e.printStackTrace()
                        }
                    }

                    isDeleting = false
                    clearDeleteAllListener()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        fun clearDeleteAllListener(){
            if(deleteAllListener != null){
                chatReference.removeEventListener(deleteAllListener!!)
            }
            if(deleteMessage != null){
                chatReference.removeEventListener(deleteMessage!!)
            }
        }

        fun deleteMessage(curUid : String, uid : String){
            deleteMessage = chatReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!isDeleting){
                        isDeleting = true
                        try {
                            for (shot in snapshot.children) {
                                val chat = shot.getValue(Chat::class.java)

                                if(chat != null){
                                    // check if this is a image message then delete it in storage

                                    if ((chat.getReceiver().equals(uid) && chat.getSender().equals(curUid))
                                                || (chat.getReceiver().equals(curUid) && chat.getSender().equals(uid))) {
                                        if(!chat.getUrl().isNullOrEmpty()){
                                            StorageController.deleteFile("Chat Images/" + chat.getMessageId() + ".jpg")
                                        }
                                        chatReference.child(chat.getMessageId()!!).removeValue()
                                    }
                                }
                            }

                            ChatListController.deleteMessage(curUid, uid)
                        } catch (e : Exception){
                            e.printStackTrace()
                        }
                    }

                    isDeleting = false
                    clearDeleteAllListener()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }
}