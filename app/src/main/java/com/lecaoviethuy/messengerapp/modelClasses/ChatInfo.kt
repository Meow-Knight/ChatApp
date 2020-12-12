package com.lecaoviethuy.messengerapp.modelClasses

class ChatInfo {
    private var backgroundChatImageUrl: String = ""
    private var blockPerson: String = ""

    constructor()

    fun getBackgroundChatImageUrl(): String {
        return this.backgroundChatImageUrl
    }

    fun getBlockPerson(): String {
        return this.blockPerson
    }

    companion object {
        @JvmStatic
        fun getBackgroundChatImageFileName(userId: String, visitUserId: String): String {
            val extension = ".jpg"

            if (userId > visitUserId)
                return userId + "_" + visitUserId + extension
            return visitUserId + "_" + userId + extension
        }
    }
}