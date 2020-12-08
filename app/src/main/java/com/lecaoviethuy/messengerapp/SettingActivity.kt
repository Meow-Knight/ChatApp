package com.lecaoviethuy.messengerapp

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.lecaoviethuy.messengerapp.controllers.DatabaseController
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.activity_setting.back
import kotlinx.android.synthetic.main.activity_setting.bt_verify_email
import kotlinx.android.synthetic.main.activity_setting.cover_image_visit_user
import kotlinx.android.synthetic.main.activity_setting.profile_image_visit_user
import kotlinx.android.synthetic.main.activity_setting.set_cover_image_camera
import kotlinx.android.synthetic.main.activity_setting.set_facebook
import kotlinx.android.synthetic.main.activity_setting.set_instagram
import kotlinx.android.synthetic.main.activity_setting.set_profile_image_camera
import kotlinx.android.synthetic.main.activity_setting.set_website
import kotlinx.android.synthetic.main.activity_setting.username_visit_user
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap

class SettingActivity : AppCompatActivity() {

    var userReference : DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null
    private val GALLERY_CODE = 438
    private val CAMERA_CODE = 123
    private var imageUri : Uri? = null
    private var storageRef : StorageReference?= null
    private var coverChecker : String? = ""
    private var socialChecker : String?= ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        userReference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user: User? = p0.getValue(User::class.java)
                    if ((user!!.getUid()).equals(firebaseUser!!.uid)) {
                        username_visit_user.text = user.getUsername()
                        Picasso.get().load(user.getProfile()).into(profile_image_visit_user)
                        Picasso.get().load(user.getCover()).into(cover_image_visit_user)
                        var fbLink = user.getFacebook()
                        fbLink = fbLink!!.replace("//","/")
                        val fbLinks = fbLink.split("/")
                        if (fbLinks.size > 2 && fbLinks[2] != ""){
                            link_fb.text = fbLinks[2]
                        }

                        var insLink  = user.getInstagram()
                        insLink = insLink!!.replace("//","/")
                        val insLinks = insLink.split("/")
                        if (insLinks.size > 2 && insLinks[2] != ""){
                            link_ins.text = insLinks[2]
                        }
                        var websiteLink = user.getWebsite()
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
            }
        })

        if(firebaseUser!!.isEmailVerified){
            bt_verify_email.visibility = View.GONE
        }

        initEvents()
    }

    private fun initEvents() {
        profile_image_visit_user.setOnClickListener {
            pickImageFromLibrary()
        }

        cover_image_visit_user.setOnClickListener {
            coverChecker = "cover"
            pickImageFromLibrary()
        }

        set_facebook.setOnClickListener {
            socialChecker = "facebook"
            setSocialLink()
        }

        set_instagram.setOnClickListener {
            socialChecker = "instagram"
            setSocialLink()
        }

        set_website.setOnClickListener {
            socialChecker = "website"
            setSocialLink()
        }

        set_name.setOnClickListener {
            setName()
        }

        set_cover_image_camera.setOnClickListener {
            coverChecker = "cover"
            pickImageFromCamera()
        }

        set_profile_image_camera.setOnClickListener {
            pickImageFromCamera()
        }

        bt_verify_email.setOnClickListener {
            firebaseUser!!.sendEmailVerification().addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "Sent verify email, check your email",
                    Toast.LENGTH_LONG
                ).show()
            }.addOnFailureListener {
                Toast.makeText(applicationContext, "Cannot sent verify email", Toast.LENGTH_LONG)
                    .show()
            }
        }
        /*
        * When you click on yes delete button
        * Database Controller will handle delete your account information in realtime database
        * set DatabaseController#isDeleting = true and false when everything deleted
        * this make sure addValueEventListener of firebase database cannot run by the way check if isDeleting in the first line
        * of onDataChange function, if not check => app will not work in right way, can crash app
        * it cannot delete your profile and cover image
        * delete user on authentication and after that sign out google client and log out user
        * finally, back to welcome activity
        * */
        bt_delete_account.setOnClickListener {
            val deleteDialog = AlertDialog.Builder(this@SettingActivity)
            deleteDialog.setTitle("Delete your account?")
                .setMessage("Everything about your account will be deleted")
                .setPositiveButton(
                    "YES"
                ) { dialog, _ ->
                    MainActivity.clearAllListener()
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()

                    val mGoogleSignInClient = GoogleSignIn.getClient(this@SettingActivity, gso)
                    mGoogleSignInClient.signOut()
                    val user = FirebaseAuth.getInstance().currentUser
                    DatabaseController.deleteAll(user!!.uid) // this line can move on top but now i'm tired
                    user!!.delete().addOnCompleteListener {
                        Toast.makeText(
                            this@SettingActivity,
                            "Deleted your account",
                            Toast.LENGTH_SHORT
                        ).show()
                        // log out
                        FirebaseAuth.getInstance().signOut()

                        val intent = Intent(this@SettingActivity, WelcomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        applicationContext.startActivity(intent)

                        dialog.dismiss()
                    }.addOnFailureListener {
                        Log.d("check", "failed delete account")
                    }
                }
                .setNegativeButton(
                    "NO"
                ) { dialog, _ -> dialog.dismiss() }
            deleteDialog.create().show()
        }
        back.setOnClickListener{
            finish()
        }
    }

    private fun setName() {
        val builder : AlertDialog.Builder = AlertDialog.Builder(this@SettingActivity,R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        val editText = EditText(this@SettingActivity)
        builder.setTitle("Change name")
        builder.setView(editText)
        builder.setPositiveButton("Change", DialogInterface.OnClickListener{
                _, _ ->
            val name = editText.text.toString()

            if (name == ""){
                Toast.makeText(this,"Please write something...", Toast.LENGTH_SHORT).show()
            } else {
                saveName(name)
            }
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener{
                dialog, _ ->
            dialog.cancel()
        })

        builder.show()

    }

    private fun saveName(name :String) {
        val mapName = HashMap<String,Any>()
        mapName["username"] = name
        mapName["search"] = name.toLowerCase(Locale.ROOT)
        userReference!!.updateChildren(mapName).addOnCompleteListener{
                task ->
            if (task.isSuccessful){
                Toast.makeText(this,"Update successfully...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setSocialLink() {
        val builder : AlertDialog.Builder = AlertDialog.Builder(
            this,
            R.style.Theme_AppCompat_DayNight_Dialog_Alert
        )
        if (socialChecker == "website"){
            builder.setTitle("Write URL:")
        } else {
            builder.setTitle("Write username")
        }

        val editText = EditText(this)

        if (socialChecker == "website"){
            editText.hint = "e.g www.google.com"
        } else {
            editText.hint = "e.g abcde"
        }

        builder.setView(editText)
        builder.setPositiveButton("create") { _, _ ->
            val str = editText.text.toString()

            if (str == "") {
                Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show()
            } else {
                saveSocialLink(str)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun saveSocialLink(str: String) {
        val mapSocial = HashMap<String, Any>()

        when (socialChecker){
            "facebook" -> {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }

            "instagram" -> {
                mapSocial["instagram"] = "https://m.facebook.com/$str"
            }

            "website" -> {
                mapSocial["website"] = "https://$str"
            }
        }

        userReference!!.updateChildren(mapSocial).addOnCompleteListener{ task ->
            if (task.isSuccessful){
                Toast.makeText(this, "Update successfully...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImageFromLibrary() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,GALLERY_CODE)
    }

    private fun pickImageFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent,CAMERA_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (requestCode == GALLERY_CODE && resultCode == Activity.RESULT_OK && data.data != null){
                imageUri = data.data
                Toast.makeText(this,"uploading....", Toast.LENGTH_SHORT).show()
                uploadFileImageToDatabase()
            }
            if (requestCode == CAMERA_CODE && resultCode == Activity.RESULT_OK){
                val imageBitmap = data.extras!!.get("data") as Bitmap
                Toast.makeText(this,"uploading....", Toast.LENGTH_SHORT).show()
                uploadBitmapImageToDatabase(imageBitmap)
            }
        }
    }

    private fun uploadBitmapImageToDatabase(imageBitmap: Bitmap) {
        val progressBar = ProgressDialog(this)
        progressBar.setMessage("image is uploading, please wait...")
        progressBar.show()

        val stream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

        val b = stream.toByteArray()

        val uploadTask : StorageTask<*>
        uploadTask = fileRef.putBytes(b)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (task.isSuccessful){
                task.exception?.let {
                    throw  it
                }
            }
            return@Continuation fileRef.downloadUrl
        }).addOnCompleteListener{ task ->
            if (task.isSuccessful){
                val downloadUri = task.result
                val url = downloadUri.toString()
                updateInfoUser(url)
                progressBar.dismiss()
            }
        }
    }

    private fun uploadFileImageToDatabase() {
        val progressBar = ProgressDialog(this)
        progressBar.setMessage("image is uploading, please wait...")
        progressBar.show()

        if (imageUri!= null){
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
            val uploadTask : StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (task.isSuccessful) {
                    task.exception?.let {
                        throw  it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    val downloadUri = task.result
                    val url = downloadUri.toString()
                    updateInfoUser(url)
                    progressBar.dismiss()
                }
            }
        }
    }

    private fun updateInfoUser (url :String?){
        if (coverChecker == "cover"){
            val mapCoverImage = HashMap<String, Any?>()
            mapCoverImage["cover"] = url
            userReference!!.updateChildren(mapCoverImage)
            coverChecker = ""
        } else {
            val mapProfileImg = HashMap<String,Any?>()
            mapProfileImg["profile"] = url
            userReference!!.updateChildren(mapProfileImg)
            coverChecker = ""
        }
    }

}