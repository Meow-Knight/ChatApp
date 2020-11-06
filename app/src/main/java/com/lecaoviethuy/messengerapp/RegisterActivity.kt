package com.lecaoviethuy.messengerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth : FirebaseAuth
    private lateinit var refUser : DatabaseReference
    private var firebaseUserId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar : Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

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
                        firebaseUserId = mAuth.currentUser!!.uid
                        refUser = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserId)

                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = firebaseUserId
                        userHashMap["username"] = username
                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-a751f.appspot.com/o/profile.png?alt=media&token=cf6fe803-7d8e-4545-9014-09cd4c705b88"
                        userHashMap["background"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-a751f.appspot.com/o/background.jpg?alt=media&token=0701a80a-4ceb-40a5-91f1-31328f30bb8a"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = username.toLowerCase()
                        userHashMap["facebook"] = "https://m.fb.com"
                        userHashMap["instagram"] = "https://m.instagram.com"
                        userHashMap["website"] = "https://www.google.com"

                        refUser.updateChildren(userHashMap)
                            .addOnCompleteListener{task ->
                                if(task.isSuccessful){
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
}