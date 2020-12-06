package com.lecaoviethuy.messengerapp.modelClasses

class ChatInfo {
    private var backgroundChatImageUrl: String = ""

    constructor()
    constructor(backgroundChatImageUrl: String) {
        this.backgroundChatImageUrl = backgroundChatImageUrl
    }

    fun getbackgroundChatImageUrl(): String {
        return this.backgroundChatImageUrl
    }
}