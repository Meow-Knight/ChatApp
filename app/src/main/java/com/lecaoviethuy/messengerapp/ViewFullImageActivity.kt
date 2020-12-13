package com.lecaoviethuy.messengerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.lecaoviethuy.messengerapp.utils.OnHasCallVideo
import com.squareup.picasso.Picasso

class ViewFullImageActivity : AppCompatActivity() {
    private var imageViewer : ImageView? = null
    private var imageUrl : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)

        imageUrl = intent.getStringExtra("url").toString()
        imageViewer = findViewById(R.id.image_viewer)

        Picasso.get()
                .load(imageUrl)
                .into(imageViewer)
    }

//    override fun onResume() {
//        super.onResume()
//        OnHasCallVideo.hasCallVideo(this@ViewFullImageActivity, FirebaseDatabase.getInstance().reference
//            , FirebaseAuth.getInstance().currentUser!!.uid)
//    }
}