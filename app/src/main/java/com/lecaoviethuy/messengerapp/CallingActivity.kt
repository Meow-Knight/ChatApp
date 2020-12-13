package com.lecaoviethuy.messengerapp

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.database.*
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_calling.*

class CallingActivity : AppCompatActivity() {

    private var databaseRef : DatabaseReference? = null
    private var userRinging : String? = ""
    private var userCalling : String? = ""
    private var mediaPlayer : MediaPlayer?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)

        databaseRef = FirebaseDatabase.getInstance().reference
        userRinging = intent.getStringExtra("userRinging")
        userCalling = intent.getStringExtra("userCalling")
        val kind = intent.getStringExtra("kind")
        if (kind == "1"){
            cv_call_start.visibility = View.GONE
            displayInfo(userRinging)
        } else {
            displayInfo(userCalling)
        }
        initEvent()
    }

    private fun displayInfo(userId :String?) {
        databaseRef!!.child("Users").child(userId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    username_call.text = user!!.getUsername()
                    Picasso.get()
                        .load(user.getProfile())
                        .placeholder(R.drawable.avatar)
                        .into(profile_image_call)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun initEvent() {
        mediaPlayer = MediaPlayer.create(this, R.raw.ringing)
        mediaPlayer!!.isLooping = true
        mediaPlayer!!.start()
        databaseRef!!.child("Users").child(userRinging!!).child("ringing")
                .addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val ringing = snapshot.getValue(String::class.java)
                        if (ringing == null){
                            finish()
                        }
                    }

                })
        databaseRef!!.child("Users").child(userRinging!!).child("acceptcall")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val acceptCall = snapshot.getValue(Boolean::class.java)
                    if (acceptCall == true){
                        databaseRef!!.child("Users").child(userRinging!!).child("acceptcall").setValue(true)
                        val intent = Intent(this@CallingActivity, VideoChatActivity::class.java)
                        intent.putExtra("userCalling", userCalling)
                        intent.putExtra("userRinging", userRinging)
                        startActivity(intent)
                    }
                }

            })
        call_end.setOnClickListener{
            finish()
        }

        call_start.setOnClickListener{
            databaseRef!!.child("Users").child(userRinging!!).child("acceptcall").setValue(true)
            val intent = Intent(this@CallingActivity, VideoChatActivity::class.java)
            intent.putExtra("userCalling", userCalling)
            intent.putExtra("userRinging", userRinging)
            startActivity(intent)
        }
    }
    override fun onPause() {
        super.onPause()
        mediaPlayer!!.stop()
        databaseRef!!.child("Users").child(userRinging!!).child("acceptcall")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    val acceptCall = snapshot.getValue(Boolean::class.java)
                    if (acceptCall == true){
                        databaseRef!!.child("Users").child(userRinging!!).child("acceptcall").setValue(true)
                        val intent = Intent(this@CallingActivity, VideoChatActivity::class.java)
                        intent.putExtra("userCalling", userCalling)
                        intent.putExtra("userRinging", userRinging)
                        startActivity(intent)
                    } else {
                        databaseRef!!.child("Users").child(userRinging!!).child("ringing").removeValue()
                    }
                }

            })
    }
}