package com.lecaoviethuy.messengerapp.databaseServices

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class OfflineDatabase {

    companion object Services {

        private var chatListRef:DatabaseReference? = null
        private var chatsRef:DatabaseReference? = null
        private var tokensRef:DatabaseReference? = null
        private var usersRef:DatabaseReference? = null

        private var connectedRef:DatabaseReference? = null

        private var TAG:String? = OfflineDatabase::class.simpleName;

        public fun enablePersistence() {
            Firebase.database.setPersistenceEnabled(true)

            chatListRef = Firebase.database.getReference("chatList")
            chatsRef = Firebase.database.getReference("Chats")
            tokensRef = Firebase.database.getReference("Tokens")
            usersRef = Firebase.database.getReference("Users")

            connectedRef = Firebase.database.getReference(".info/connected");
        }

        public fun keepSynced() {
            chatListRef!!.keepSynced(true)
            chatsRef!!.keepSynced(true)
            tokensRef!!.keepSynced(true)
            usersRef!!.keepSynced(true)
        }

        public fun removeSynced() {
            chatListRef!!.keepSynced(false)
            chatsRef!!.keepSynced(false)
            tokensRef!!.keepSynced(false)
            usersRef!!.keepSynced(false)
        }

        public fun triggerConnectionState(handler: (Boolean) -> Unit) {
            connectedRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false

                    handler(connected)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, "Cancel connected listener")
                }
            })
        }

        public fun setDisconnectionTimestamp(userUid: String?) {
            val userRef: DatabaseReference = usersRef!!.child(userUid!!);
            val userLastOnlineRef: DatabaseReference = userRef.child("lastOnline");

            userLastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
        }
    }
}