package com.lecaoviethuy.messengerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.lecaoviethuy.messengerapp.controllers.DatabaseController
import com.lecaoviethuy.messengerapp.modelClasses.User
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {
    private var mUser : FirebaseUser? = null
    private var mFirebaseAuth: FirebaseAuth? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var refUser : DatabaseReference? = null

    private var loginWithGoogleListener : ValueEventListener? = null

    // final variables
    private val RC_SIGN_IN = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        initialEvents()

        createRequest()

        mFirebaseAuth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)
    }

    override fun onStart() {
        super.onStart()

        mUser = FirebaseAuth.getInstance().currentUser
        if(mUser != null){
            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initialEvents() {
        bt_register_welcome.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        bt_login_welcome.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        bt_login_google.setOnClickListener {
            progress_bar.visibility = View.VISIBLE
            signIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!.idToken)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mFirebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isCheckedExistUser = false
                mUser = FirebaseAuth.getInstance().currentUser
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    // check if email already exist (logged with email-password)
                    val mUserId = mUser!!.uid
                    refUser = FirebaseDatabase.getInstance().reference.child("Users")
                        .child(mUserId)

                    loginWithGoogleListener = refUser!!.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (isCheckedExistUser){
                                return
                            }

                            val user = snapshot.getValue(User::class.java)
                            if (user == null){
                                createNewChild(refUser!!)
                            } else {
                                val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            isCheckedExistUser = true
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

                } else {
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createNewChild(refUser : DatabaseReference){
        if(mUser != null){
            val mUserId = mUser!!.uid
            val username: String = mUser!!.displayName.toString()
            val userHashMap = RegisterActivity.getDefaultUserData(mUserId, username)

            refUser.updateChildren(userHashMap)
                .addOnCompleteListener{ task2 ->
                    if(task2.isSuccessful){
                        Log.d("createNewChild", "Success")
                    }
                }
        }

        val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()

    }
    /*
    * If email used in email-password provider is not verified, it will be overwritten when logging with google account
    * */
    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onPause() {
        if(refUser != null){
            refUser!!.removeEventListener(loginWithGoogleListener!!)
        }
        super.onPause()
    }

    companion object{
        // use for check after login with google account, if have account exist in database it will not create again another user data
        // set true after check
        // set false before check
        // use on firebaseAuthWithGoogle function
        var isCheckedExistUser = false
    }
}