package com.lecaoviethuy.messengerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.lecaoviethuy.messengerapp.controllers.AppController
import com.lecaoviethuy.messengerapp.controllers.AppController.ValueChangeListener
import com.lecaoviethuy.messengerapp.controllers.DatabaseController
import com.lecaoviethuy.messengerapp.fragments.ChatsFragment
import com.lecaoviethuy.messengerapp.fragments.SearchFragment
import com.lecaoviethuy.messengerapp.fragments.SettingsFragment
import com.lecaoviethuy.messengerapp.modelClasses.Chat
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.lecaoviethuy.messengerapp.databaseServices.OfflineDatabase
import com.lecaoviethuy.messengerapp.modelClasses.Status
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var mUser : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        mUser = FirebaseAuth.getInstance().currentUser
        refUser = FirebaseDatabase.getInstance().reference.child("Users").child(mUser!!.uid)

        val toolbar : Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        val tabLayout : TabLayout = findViewById(R.id.tab_layout)
        val viewPage : ViewPager = findViewById(R.id.viewpager)

        ref = FirebaseDatabase.getInstance().reference.child("Chats")
        unreadCountListener = ref!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!DatabaseController.isDeleting){
                    val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                    var countUnreadMessages = 0

                    for (shot in snapshot.children) {
                        val chat = shot.getValue(Chat::class.java)

                        if (chat!!.getReceiver().equals(mUser!!.uid) && !chat.getIsSeen()) {
                            ++countUnreadMessages
                        }
                    }

                    if (countUnreadMessages == 0) {
                        viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
                    } else {
                        viewPagerAdapter.addFragment(ChatsFragment(), "(${countUnreadMessages}) Chats")
                    }

                    viewPagerAdapter.addFragment(SearchFragment(), "Search")
                    viewPagerAdapter.addFragment(SettingsFragment(), "Settings")

                    viewPage.adapter = viewPagerAdapter
                    tabLayout.setupWithViewPager(viewPage)
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

        AppController.instance!!.setOnVisibilityChangeListener(object : ValueChangeListener {
            override fun onChanged(value: Boolean?) {
                Log.d("isAppInBackground", value.toString())
            }
        })

        // Trigger offline
        OfflineDatabase.triggerConnectionState(fun (isConnected:Boolean) {
            if (isConnected) {
                tv_status.visibility = View.GONE;
            } else {
                tv_status.setText(Status.OFFLINE.statusString)
                tv_status.visibility = View.VISIBLE;
            }
        });
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_logout -> {
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
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager){

        private val fragments : ArrayList<Fragment>
        private val titles : ArrayList<String>

        init {
            fragments = ArrayList()
            titles = ArrayList()
        }
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String){
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

    override fun onResume() {
        AppController.updateStatus(Status.ONLINE)
        super.onResume()
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