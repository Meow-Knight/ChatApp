package com.lecaoviethuy.messengerapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_visit_user_profile.*

class VisitUserProfileActivity : AppCompatActivity() {
    private var visitUserId : String = ""
    private var user : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_user_profile)

        visitUserId = intent.getStringExtra("visit_user_id").toString()
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(visitUserId)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    user = snapshot.getValue(User::class.java)
                    username_visit_user.text = user!!.getUsername()
                    Picasso.get().load(user!!.getCover()).into(cover_image_visit_user)
                    Picasso.get().load(user!!.getProfile()).into(profile_image_visit_user)
                }
            }
        })

        initEvents()
    }

    private fun initEvents() {
        set_facebook.setOnClickListener {
            val uri = Uri.parse(user!!.getFacebook())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        set_instagram.setOnClickListener {
            val uri = Uri.parse(user!!.getInstagram())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        set_website.setOnClickListener {
            val uri = Uri.parse(user!!.getWebsite())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        bt_send_message.setOnClickListener {
            val intent = Intent(this, MessageChatActivity::class.java)
            intent.putExtra("visit_id", user!!.getUid())
            startActivity(intent)
        }
    }
}