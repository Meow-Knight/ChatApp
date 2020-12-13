package com.lecaoviethuy.messengerapp.utils

import android.app.Activity
import android.content.Intent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.lecaoviethuy.messengerapp.CallingActivity

class OnHasCallVideo {
    companion object {
        fun hasCallVideo (thisActivity : Activity ,databaseRef : DatabaseReference, userRinging :String){
            databaseRef.child("Users").child(userRinging).child("ringing")
                .addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userCalling = snapshot.getValue(String::class.java)
                        if (userCalling != null){
                            val intent = Intent(thisActivity, CallingActivity::class.java)
                            intent.putExtra("kind","2")
                            intent.putExtra("userCalling", userCalling)
                            intent.putExtra("userRinging", userRinging)
                            thisActivity.startActivity(intent)
                        }
                    }

                })
        }
    }
}