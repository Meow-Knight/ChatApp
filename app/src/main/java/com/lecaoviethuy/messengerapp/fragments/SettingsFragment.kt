package com.lecaoviethuy.messengerapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.lecaoviethuy.messengerapp.R
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*


/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    var userReference :DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null
    private val REQUEST_CODE = 438
    private var imageUri : Uri? = null
    private var storageRef : StorageReference?= null
    private var coverChecker : String? = ""
    private var socialChecker : String?= ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")
        userReference!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                 if (p0.exists()){
                     val user : User? = p0.getValue(User::class.java)
                     if ((user!!.getUid()).equals(firebaseUser!!.uid)){
                         view.username_visit_user.text = user!!.getUsername()
                         Picasso.get().load(user!!.getProfile()).into(view.profile_image_visit_user)
                         Picasso.get().load(user!!.getCover()).into(view.cover_image_visit_user)
                     }
                 }
            }
        })

        view.profile_image_visit_user.setOnClickListener{
             pickImage()
        }

        view.cover_image_visit_user.setOnClickListener{
            coverChecker = "cover"
            pickImage()
        }

        view.set_facebook.setOnClickListener{
            socialChecker = "facebook"
            setSocialLink()
        }

        view.set_instagram.setOnClickListener{
            socialChecker = "instagram"
            setSocialLink()
        }

        view.set_website.setOnClickListener{
            socialChecker = "website"
            setSocialLink()
        }

        view.username_visit_user.setOnClickListener{
            setName()
        }


        return view
    }

    private fun setName() {
        val builder : AlertDialog.Builder = AlertDialog.Builder(context,R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        val editText = EditText(context)
        builder.setTitle("Change name")
        builder.setView(editText)
        builder.setPositiveButton("Change",DialogInterface.OnClickListener{
                dialog, which ->
            val name = editText.text.toString()

            if (name == ""){
                Toast.makeText(context,"Please write something...", Toast.LENGTH_SHORT).show()
            } else {
                saveName(name)
            }
        })
        builder.setNegativeButton("Cancel",DialogInterface.OnClickListener{
                dialog, which ->
            dialog.cancel()
        })

        builder.show()

    }

    private fun saveName(name :String) {
        val mapName = HashMap<String,Any>()
        mapName["username"] = name
        mapName["search"] = name.toString().toLowerCase()
        userReference!!.updateChildren(mapName).addOnCompleteListener{
                task ->
            if (task.isSuccessful){
                Toast.makeText(context,"Update successfully...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setSocialLink() {
        val builder : AlertDialog.Builder = AlertDialog.Builder(context,R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        if (socialChecker == "website"){
            builder.setTitle("Write URL:")
        } else {
            builder.setTitle("Write username")
        }

        val editText = EditText(context)

        if (socialChecker == "website"){
            editText.hint = "e.g www.google.com"
        } else {
            editText.hint = "e.g abcde"
        }

        builder.setView(editText)
        builder.setPositiveButton("create",DialogInterface.OnClickListener{
            dialog, which ->
            val str = editText.text.toString()

            if (str == ""){
                Toast.makeText(context,"Please write something...", Toast.LENGTH_SHORT).show()
            } else {
                saveSocialLink(str)
            }
        })
        builder.setNegativeButton("Cancel",DialogInterface.OnClickListener{
                dialog, which ->
            dialog.cancel()
        })

        builder.show()
    }

    private fun saveSocialLink(str: String) {
        val mapSocial = HashMap<String,Any>()

        when (socialChecker){
            "facebook" ->{
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }

            "instagram" -> {
                mapSocial["instagram"] = "https://m.facebook.com/$str"
            }

            "website" -> {
                mapSocial["website"] = "https://$str"
            }
        }

        userReference!!.updateChildren(mapSocial).addOnCompleteListener{
            task ->
            if (task.isSuccessful){
                Toast.makeText(context,"Update successfully...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data.data != null){
                imageUri = data.data
                Toast.makeText(context,"uploading....", Toast.LENGTH_SHORT).show()
                uploadImageToDatabase()
            }
        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("image is uploading, please wait...")
        progressBar.show()

        if (imageUri!= null){
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
            var uploadTask : StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot,Task<Uri>>{ task ->
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
                    if (coverChecker == "cover"){
                        val mapCoverImage = HashMap<String, Any>()
                        mapCoverImage["cover"] = url
                        userReference!!.updateChildren(mapCoverImage)
                        coverChecker = ""
                    } else {
                        val mapProfileImg = HashMap<String,Any>()
                        mapProfileImg["profile"] = url
                        userReference!!.updateChildren(mapProfileImg)
                        coverChecker = ""
                    }
                    progressBar.dismiss()
                }
            }
        }
    }
}