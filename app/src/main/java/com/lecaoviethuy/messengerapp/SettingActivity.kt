package com.lecaoviethuy.messengerapp

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.lecaoviethuy.messengerapp.common.SaveFunctionalInterface
import com.lecaoviethuy.messengerapp.controllers.DatabaseController
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.lecaoviethuy.messengerapp.utils.OnHasCallVideo
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
import java.util.function.Consumer
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
    private var currentUser : User?=null

    @RequiresApi(Build.VERSION_CODES.N)
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
                        currentUser = user
                        username_visit_user.text = user.getUsername()
                        phone_number.text = user.getPhone()
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

    @RequiresApi(Build.VERSION_CODES.N)
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

        set_name_phone.setOnClickListener {
            Log.d("check__", "in")
            setPhoneAndName()
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

        /**
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setPhoneAndName() {
        val action1 = object : SaveFunctionalInterface{
            override fun apply(value: String) {
                saveName(value)
            }
        }

        val action2 = object : SaveFunctionalInterface{
            override fun apply(value: String) {
                savePhone(value)
            }
        }

        showInputDialog("Change username and phone number",
                "Input your username and phone number below",
            currentUser!!.getUsername()!!,
            currentUser!!.getPhone(),
            action1,
            action2)

        Log.d("check__", currentUser!!.getPhone().toString())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setName() {
        val action = object : SaveFunctionalInterface{
            override fun apply(value: String) {
                saveName(value)
            }
        }

        showInputDialog("Change username", "Input your username below", "username", null, action, null)
    }

    private fun savePhone(phone: String) {
        val mapPhone = HashMap<String,Any>()
        mapPhone["phone"] = phone
        userReference!!.updateChildren(mapPhone).addOnCompleteListener{
                task ->
            if (task.isSuccessful){
                Toast.makeText(this,"Update successfully...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveName(name : String) {
        if (name.isEmpty()){
            Toast.makeText(this, "Invalid Name!", Toast.LENGTH_LONG).show()
        }
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setSocialLink() {
        var header = ""
        var content = ""
        var hint = ""

        when(socialChecker){
            "facebook" -> {
                header = "Change your facebook link"
                content = "Input your facebook below"
                hint = "http://facebook.com/..."
            }

            "instagram" -> {
                header = "Change your instagram link"
                content = "Input your instagram below"
                hint = "http://instagram.com/..."
            }

            "website" -> {
                header = "Change your website link"
                content = "Input your website below"
                hint = "http://github.com/..."
            }
        }

        val action = object : SaveFunctionalInterface{
            override fun apply(value : String) {
                saveSocialLink(value)
            }

        }
        showInputDialog(header, content, hint, null, action, null)
    }

    /**
     * This function will create a dialog to input value to change
     * @param header give header title of dialog
     * @param content give what user have to do
     * @param hint1 hint of edittext where user input new value
     * @param hint2 is an option, default input dialog have one input field at least, it's function like hint1
     * @param action1 is a functional interface, just do an action when click on apply button on dialog
     * @param action2 is an option, and it's function like action1
     * */
    @RequiresApi(Build.VERSION_CODES.N)
    fun showInputDialog(header : String, content : String,
                        hint1 : String, hint2 : String?,
                        action1 : SaveFunctionalInterface, action2 : SaveFunctionalInterface?){

        val inputDialog = Dialog(this)
        inputDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        inputDialog.setContentView(R.layout.dialog_input)

        val tvHeader = inputDialog.findViewById<TextView>(R.id.tv_input_header)
        val tvInputContent = inputDialog.findViewById<TextView>(R.id.tv_content_input)
        val etInput = inputDialog.findViewById<EditText>(R.id.et_input)
        val btCancel = inputDialog.findViewById<Button>(R.id.bt_cancel)
        val btApply = inputDialog.findViewById<Button>(R.id.bt_apply)

        var etInput2 : EditText? = null
        if(hint2 != null){
            etInput2 = inputDialog.findViewById(R.id.et_input2)
            etInput2.visibility = View.VISIBLE
            etInput2.hint = hint2
        }

        val window = inputDialog.window
        if(window != null){
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        tvHeader.text = header
        tvInputContent.text = content
        etInput.hint = hint1

        btCancel.setOnClickListener {
            inputDialog.dismiss()
        }

        btApply.setOnClickListener {
            if(hint2 != null){
                action2!!.apply(etInput2!!.text.toString())
            }
            action1.apply(etInput.text.toString())
            inputDialog.dismiss()
        }

        inputDialog.show()
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

    fun uploadBitmapImageToDatabase(imageBitmap: Bitmap) {
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