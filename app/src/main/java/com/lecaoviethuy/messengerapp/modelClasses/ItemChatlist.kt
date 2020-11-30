package com.lecaoviethuy.messengerapp.modelClasses

class ItemChatlist {
    private var user : User? = null
    private var timeLastMessage : Long? = null

    constructor()
    constructor(
        user: User,
        timeLastMessage : Long?
    ){
        this.user = user
        this.timeLastMessage = timeLastMessage
    }

    fun setUser (user: User){
        this.user = user
    }

    fun getUser () : User? {
        return this.user
    }

    fun setTimeLastMessage (timeLastMessage: Long){
        this.timeLastMessage =  timeLastMessage
    }

    fun getTimeLastMessage (): Long? {
        return this.timeLastMessage
    }
}