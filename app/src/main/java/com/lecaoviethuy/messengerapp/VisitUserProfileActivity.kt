package com.lecaoviethuy.messengerapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.lecaoviethuy.messengerapp.utils.OnHasCallVideo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_visit_user_profile.*
import kotlinx.android.synthetic.main.activity_visit_user_profile.back
import kotlinx.android.synthetic.main.activity_visit_user_profile.cover_image_visit_user
import kotlinx.android.synthetic.main.activity_visit_user_profile.link_fb
import kotlinx.android.synthetic.main.activity_visit_user_profile.link_ins
import kotlinx.android.synthetic.main.activity_visit_user_profile.link_web
import kotlinx.android.synthetic.main.activity_visit_user_profile.profile_image_visit_user
import kotlinx.android.synthetic.main.activity_visit_user_profile.set_facebook
import kotlinx.android.synthetic.main.activity_visit_user_profile.set_instagram
import kotlinx.android.synthetic.main.activity_visit_user_profile.set_website
import kotlinx.android.synthetic.main.activity_visit_user_profile.username_visit_user

class VisitUserProfileActivity : AppCompatActivity() {
    private var visitUserId : String = ""
    private var user : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_user_profile)

        back.setOnClickListener{
            finish()
        }

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
                    var fbLink = user!!.getFacebook()
                    fbLink = fbLink!!.replace("//","/")
                    val fbLinks = fbLink.split("/")
                    if (fbLinks.size > 2 && fbLinks[2] != ""){
                        link_fb.text = fbLinks[2]
                    }

                    var insLink  = user!!.getInstagram()
                    insLink = insLink!!.replace("//","/")
                    val insLinks = insLink.split("/")
                    if (insLinks.size > 2 && insLinks[2] != ""){
                        link_ins.text = insLinks[2]
                    }
                    var websiteLink = user!!.getWebsite()
                    if (websiteLink != "https://www.google.com"){
                        websiteLink = websiteLink!!.replace("//","/")
                        val websiteLinks = websiteLink.split("/")
                        var text = ""
                        for (i in 1 until websiteLinks.size){
                            text += websiteLinks[i] + "/"
                        }
                        link_web.text = text
                    }
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

//    override fun onResume() {
//        super.onResume()
//        OnHasCallVideo.hasCallVideo(this@VisitUserProfileActivity, FirebaseDatabase.getInstance().reference
//                 ,FirebaseAuth.getInstance().currentUser!!.uid)
//    }
}