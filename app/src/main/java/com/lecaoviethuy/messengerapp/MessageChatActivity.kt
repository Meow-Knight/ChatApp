package com.lecaoviethuy.messengerapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*

class MessageChatActivity : AppCompatActivity() {
    var userIdVisit : String = "";
    var firebaseUser : FirebaseUser ? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        // Get visit user ID
        userIdVisit = intent.getStringExtra("visit_id")!!;
        // Current user
        firebaseUser = FirebaseAuth.getInstance().currentUser;

        // visit User
        val reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit);

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue(User::class.java);

                username_mchat.text = user!!.getUsername();
                Picasso.get().load(user.getProfile()).into(profile_image_mchat);
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageChatActivity,"Can't get user profile!", Toast.LENGTH_SHORT).show();
            }
        })

        // Add send btn event
        send_message_btn.setOnClickListener {
            val message = text_message.text.toString();
            if (message == "") {
                Toast.makeText(this@MessageChatActivity, "Please write a message, please...", Toast.LENGTH_SHORT).show()
            } else {
                sendMessageToUser(firebaseUser !! .uid, userIdVisit, message);
            }

            text_message.setText("");
        }

        // Add image event
        attach_image_file_btn.setOnClickListener {
            val intent = Intent();
            intent.action = Intent.ACTION_GET_CONTENT;
            intent.type = "image/*";
            startActivityForResult(Intent.createChooser(intent,"Pick Image"), 438);
        }
    }

    // how to send message to user
    private fun sendMessageToUser(senderId: String, receiverId : String?, message: String) {
        val reference = FirebaseDatabase.getInstance().reference;
        // new message
        val messageKey = reference.push().key;

        // body data
        val messageHashMap = HashMap<String, Any?>();
        messageHashMap["sender"] = senderId;
        messageHashMap["message"] = message;
        messageHashMap["receiver"] = receiverId;
        messageHashMap["isSeen"] = false;
        messageHashMap["url"] = "";
        messageHashMap["messageId"] = messageKey;

        // add new message
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatsListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit);

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                chatsListReference.child("id").setValue(userIdVisit);
                            }

                            val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid);

                            chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid);
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@MessageChatActivity,"sent message error, try again...", Toast.LENGTH_SHORT).show();
                        }
                    })

                    // change ID
                    chatsListReference.child("id").setValue(firebaseUser!!.uid);

                    // implement the push notification using fcm

                    val reference = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseUser!!.uid);
                }
            };
    }

    // handle after intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // show progressbar
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("image is uploading, please wait...")
            progressBar.show();

            // upload chat image
            val fileUri = data.data;
            val storageReference =  FirebaseStorage.getInstance().reference.child("Chat Images");
            val ref = FirebaseDatabase.getInstance().reference;
            val messageId = ref.push().key;
            val filePath = storageReference.child("$messageId.jpg");

            var uploadTask : StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!);

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl;
            }).addOnCompleteListener {task ->
                if (task.isSuccessful){
                    val downloadUri = task.result
                    val url = downloadUri.toString();

                    val messageHashMap = HashMap<String, Any?>();
                    messageHashMap["sender"] = firebaseUser!!.uid;
                    messageHashMap["message"] = "has sent you an image.";
                    messageHashMap["receiver"] = userIdVisit;
                    messageHashMap["isSeen"] = false;
                    messageHashMap["url"] = url;
                    messageHashMap["messageId"] = messageId;

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap);

                    progressBar.dismiss();
                }
            };
        }
    }
}