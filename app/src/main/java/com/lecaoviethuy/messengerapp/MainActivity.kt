package com.lecaoviethuy.messengerapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.lecaoviethuy.messengerapp.adapterClasses.ViewPagerAdapter
import com.lecaoviethuy.messengerapp.controllers.AppController
import com.lecaoviethuy.messengerapp.controllers.DatabaseController
import com.lecaoviethuy.messengerapp.databaseServices.OfflineDatabase
import com.lecaoviethuy.messengerapp.fragments.ChatsFragment
import com.lecaoviethuy.messengerapp.fragments.SearchFragment
import com.lecaoviethuy.messengerapp.modelClasses.Chat
import com.lecaoviethuy.messengerapp.modelClasses.Status
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mUser : FirebaseUser? = null

    private var mViewPagerAdapter : ViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mUser = FirebaseAuth.getInstance().currentUser
        refUser = FirebaseDatabase.getInstance().reference.child("Users").child(mUser!!.uid)

        mViewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        mViewPagerAdapter!!.addFragment(ChatsFragment())
        mViewPagerAdapter!!.addFragment(SearchFragment())
        viewpager.adapter = mViewPagerAdapter

        ref = FirebaseDatabase.getInstance().reference.child("Chats")
        unreadCountListener = ref!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!DatabaseController.isDeleting){
                    var countUnreadMessages = 0

                    for (shot in snapshot.children) {
                        val chat = shot.getValue(Chat::class.java)

                        if (chat!!.getReceiver().equals(mUser!!.uid) && !chat.getIsSeen()) {
                            ++countUnreadMessages
                        }
                    }

                    if (countUnreadMessages == 0) {
                        bt_chats.text = "Chats"
                    } else {
                        bt_chats.text = "Chats (${countUnreadMessages})"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        // load profile image
        loadProfileListener = refUser!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(!DatabaseController.isDeleting){
                    if (snapshot.exists()) {
                        val user: User? = snapshot.getValue(User::class.java)
                        tv_username.text = user!!.getUsername()
                        Picasso.get()
                            .load(user.getProfile())
                            .placeholder(R.drawable.avatar)
                            .into(iv_avatar)
                    }
                }
            }
        })

        // Trigger offline
        OfflineDatabase.triggerConnectionState(fun (isConnected:Boolean) {
            if (isConnected) {
                tv_status.text = Status.ONLINE.statusString
                tv_status.setTextColor(ContextCompat.getColor(this, R.color.secondary))
            } else {
                tv_status.text = Status.OFFLINE.statusString
                tv_status.setTextColor(ContextCompat.getColor(this, R.color.primary))
            }
        })

        initEvents()
    }

    private fun initEvents() {
        bt_chats.setOnClickListener {
            if(viewpager.currentItem == 1){
                viewpager.currentItem = 0
                changeToSearchFragment()
            }
        }

        bt_search.setOnClickListener {
            if(viewpager.currentItem == 0){
                viewpager.currentItem = 1
                changeToChatFragment()
            }
        }

        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if(position == 0){
                    changeToSearchFragment()
                } else {
                    changeToChatFragment()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

        iv_avatar.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        bt_logout.setOnClickListener {
            AppController.updateStatus(Status.OFFLINE)
            FirebaseAuth.getInstance().signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
            mGoogleSignInClient.signOut()
            val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun changeToChatFragment(){
        bt_chats.setBackgroundResource(R.drawable.rectangle_white)
        bt_chats.setTextColor(Color.BLACK)
        bt_search.setBackgroundResource(R.drawable.rectangle_pink)
        bt_search.setTextColor(Color.WHITE)
    }

    private fun changeToSearchFragment(){
        bt_chats.setBackgroundResource(R.drawable.rectangle_pink)
        bt_chats.setTextColor(Color.WHITE)
        bt_search.setBackgroundResource(R.drawable.rectangle_white)
        bt_search.setTextColor(Color.BLACK)
    }

    override fun onResume() {
        AppController.updateStatus(Status.ONLINE)
        super.onResume()
    }

    override fun onBackPressed() {
        if (viewpager.currentItem == 0){
            super.onBackPressed()
        } else {
            viewpager.currentItem--
        }
    }

    companion object{
        var ref : DatabaseReference? = null
        var refUser : DatabaseReference?= null
        var unreadCountListener : ValueEventListener? = null
        var loadProfileListener : ValueEventListener? = null

        fun clearAllListener(){
            if(ref != null && unreadCountListener != null){
                ref!!.removeEventListener(unreadCountListener!!)
            }

            if(refUser != null && loadProfileListener != null){
                refUser!!.removeEventListener(loadProfileListener!!)
            }
        }
    }
}