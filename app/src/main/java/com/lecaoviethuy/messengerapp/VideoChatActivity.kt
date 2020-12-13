package com.lecaoviethuy.messengerapp

import android.Manifest
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*
import com.opentok.android.*
import kotlinx.android.synthetic.main.activity_video_chat.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
class VideoChatActivity : AppCompatActivity(), Session.SessionListener,PublisherKit.PublisherListener{

    private var databaseRef : DatabaseReference? = null
    private var userCalling : String? = ""
    private var userRinging : String? = ""

    private var mSession : Session?= null
    private var mPublisher : Publisher? = null
    private var mSubcriber : Subscriber? = null
    companion object {
        const val RC_VIDEO_APP_PERM = 124
        const val API_KEY = "47043784"
        const val SESSION_ID = "1_MX40NzA0Mzc4NH5-MTYwNzkyNzQ0ODc5NH40SEVsTVlETHdrZEU4KzdYVzdaYXhFVkl-fg"
        const val TOKEN = "T1==cGFydG5lcl9pZD00NzA0Mzc4NCZzaWc9NGU4Nzc2YmI0ZDhkNGJiNmVlNDcwNjIwMGFjMDhlNDgxNjQ4MTczYzpzZXNzaW9uX2lkPTFfTVg0ME56QTBNemM0Tkg1LU1UWXdOemt5TnpRME9EYzVOSDQwU0VWc1RWbEVUSGRyWkVVNEt6ZFlWemRhWVhoRlZrbC1mZyZjcmVhdGVfdGltZT0xNjA3OTI3NDk2Jm5vbmNlPTAuODgxODUxMzQ5ODg2NTI4OCZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjEwNTE5NDk1JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat)
        databaseRef = FirebaseDatabase.getInstance().reference
        userCalling = intent.getStringExtra("userCalling")
        userRinging = intent.getStringExtra("userRinging")
        initEvent()
    }

    private fun initEvent() {
        requestPermission()
        cv_call_end.setOnClickListener {
            databaseRef!!.child("Users").child(userRinging!!).child("ringing").removeValue()
            databaseRef!!.child("Users").child(userRinging!!).child("acceptcall").removeValue()
            finish()
        }
    }


    override fun onResume() {
        super.onResume()
        databaseRef!!.child("Users").child(userRinging!!).child("ringing")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val userId = snapshot.getValue(String::class.java)
                    if (userId == null){
                        finish()
                    }
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this@VideoChatActivity)
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private fun requestPermission (){
        val perms = arrayOf<String>(Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        if (EasyPermissions.hasPermissions(this, *perms)){
            mSession = Session.Builder(this, API_KEY, SESSION_ID).build()
            mSession!!.setSessionListener(this@VideoChatActivity)
            mSession!!.connect(TOKEN)
        } else {
            EasyPermissions.requestPermissions(this,"Please allow Mic and Camera permission", RC_VIDEO_APP_PERM, *perms)
        }
    }

    override fun onStreamCreated(p0: PublisherKit?, p1: Stream?) {

    }

    override fun onStreamDestroyed(p0: PublisherKit?, p1: Stream?) {

    }

    override fun onError(p0: PublisherKit?, p1: OpentokError?) {

    }

    // 2. Publishing a stream to the session
    override fun onConnected(p0: Session?) {
        mPublisher = Publisher.Builder(this).build()
        mPublisher!!.setPublisherListener(this)

        publisher_container.addView(mPublisher!!.view)

        if (mPublisher!!.view is GLSurfaceView){
            mPublisher!!.view.z = 1f
        }

        mSession!!.publish(mPublisher)
    }

    override fun onDisconnected(p0: Session?) {

    }

    //3. Subscribing to the stream
    override fun onStreamReceived(session: Session?, stream: Stream?) {
        if (mSubcriber == null){
            mSubcriber = Subscriber.Builder(this,stream).build()
            mSession!!.subscribe(mSubcriber)
            subscriber_container.addView(mSubcriber!!.view)
        }
    }

    override fun onStreamDropped(p0: Session?, p1: Stream?) {
         if (mSubcriber != null){
             mSubcriber = null
             subscriber_container.removeAllViews()
         }
    }

    override fun onError(p0: Session?, p1: OpentokError?) {

    }

    override fun onPause() {
        super.onPause()
        mSession!!.disconnect()
    }
}