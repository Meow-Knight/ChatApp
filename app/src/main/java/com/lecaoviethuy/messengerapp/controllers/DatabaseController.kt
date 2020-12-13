package com.lecaoviethuy.messengerapp.controllers

class DatabaseController {
    companion object{
        var isDeleting = false
        fun deleteAll(uid : String){
            isDeleting = true
            ChatsController.deleteAllWithUserId(uid)
        }

        fun deleteMessage(uid: String, curUid: String) {
            isDeleting = true
            ChatsController.deleteMessage(uid, curUid)
        }
    }
}