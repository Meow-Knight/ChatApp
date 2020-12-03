package com.lecaoviethuy.messengerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lecaoviethuy.messengerapp.modelClasses.Status
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth : FirebaseAuth
    private lateinit var refUser : DatabaseReference
    private var firebaseUserId : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        bt_register.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val username : String = edt_username_register.text.toString()
        val email : String = edt_email_register.text.toString()
        val password : String = edt_password_register.text.toString()

        if(username == "" || email == "" || password == ""){
            Toast.makeText(this@RegisterActivity, "Please enter full information", Toast.LENGTH_SHORT).show()
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        // email authentication
                        val mUser = mAuth.currentUser
                        mUser!!.sendEmailVerification().addOnSuccessListener {
                            Toast.makeText(this, "Please verify your email", Toast.LENGTH_LONG).show()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Cannot sent verify email", Toast.LENGTH_LONG).show()
                        }

                        // add default user information in realtime database
                        firebaseUserId = mUser.uid
                        refUser = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserId)
                        val userHashMap = getDefaultUserData(firebaseUserId, username)

                        refUser.updateChildren(userHashMap)
                            .addOnCompleteListener{task2 ->
                                if(task2.isSuccessful){
                                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    } else {
                        Toast.makeText(this@RegisterActivity, task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    companion object{
        val DEFAULT_PROFILE_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/messengerapp-a751f.appspot.com/o/avatar-default.png?alt=media&token=4f109fb5-4185-4ff9-b443-9988c3b5e1cc"
        val DEFAULT_COVER_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/messengerapp-a751f.appspot.com/o/cover-default.jpg?alt=media&token=da6f2e3e-4a88-4dee-9b0e-80deec628d22"
        val DEFAULT_FB_URL = "https://m.fb.com"
        val DEFAULT_INSTAGRAM_URL = "https://m.instagram.com"
        val DEFAULT_WEBSITE_URL = "https://www.google.com"

        /*
        * This function is used in register activity and login with google in welcome activity
        */
        fun getDefaultUserData(userId : String, username : String) : HashMap<String, Any>{
            val userHashMap = HashMap<String, Any>()
            userHashMap["uid"] = userId
            userHashMap["username"] = username
            userHashMap["profile"] = DEFAULT_PROFILE_IMAGE_URL
            userHashMap["cover"] = DEFAULT_COVER_IMAGE_URL
            userHashMap["status"] = Status.OFFLINE.statusString
            userHashMap["search"] = username.toLowerCase(Locale.ROOT)
            userHashMap["facebook"] = DEFAULT_FB_URL
            userHashMap["instagram"] = DEFAULT_INSTAGRAM_URL
            userHashMap["website"] = DEFAULT_WEBSITE_URL

            return userHashMap
        }
    }
}