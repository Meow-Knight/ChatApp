package com.lecaoviethuy.messengerapp.modelClasses

class Chat {
    private var sender : String = ""
    private var message : String = ""
    private var receiver : String = ""
    private var isSeen = false
    private var url : String = ""
    private var messageId : String = ""
    private var time : Long? = null

    constructor()
    constructor(
        sender: String,
        message: String,
        receiver: String,
        isSeen: Boolean,
        url: String,
        messageId: String,
        time : Long
    ) {
        this.sender = sender
        this.message = message
        this.receiver = receiver
        this.isSeen = isSeen
        this.url = url
        this.messageId = messageId
        this.time = time
    }

    fun getSender() : String? {
        return this.sender
    }

    fun setSender(sender: String?) {
        this.sender = sender!!
    }

    fun setMessage(message: String?) {
        this.message = message!!
    }

    fun getMessage() : String? {
        return this.message
    }

    fun setReceiver(receiver: String?) {
        this.receiver = receiver!!
    }

    fun getReceiver() : String? {
        return this.receiver
    }

    fun getIsSeen() : Boolean {
        return this.isSeen
    }

    fun setIsSeen(isSeen: Boolean) {
        this.isSeen = isSeen
    }

    fun getUrl() : String? {
        return this.url
    }

    fun setUrl(url: String?) {
        this.url = url!!
    }

    fun getMessageId() : String? {
        return this.messageId
    }

    fun setMessageId(messageId: String?) {
        this.messageId = messageId!!
    }

    fun setTime(time: Long?) {
        this.time = time
    }

    fun getTime () : Long? {
        return this.time
    }
}