package com.lecaoviethuy.messengerapp.controllers

class DatabaseController {
    companion object{
        var isDeleting = false
        fun deleteAll(uid : String){
            isDeleting = true
            ChatsController.deleteAllWithUserId(uid)
        }
    }
}