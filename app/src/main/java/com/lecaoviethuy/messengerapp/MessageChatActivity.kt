package com.lecaoviethuy.messengerapp

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.lecaoviethuy.messengerapp.adapterClasses.ChatsAdapter
import com.lecaoviethuy.messengerapp.fragments.APIService
import com.lecaoviethuy.messengerapp.modelClasses.*
import com.lecaoviethuy.messengerapp.notifications.*
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_message_chat.*
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayOutputStream

class MessageChatActivity : AppCompatActivity() {
    var userIdVisit : String = ""
    var firebaseUser : FirebaseUser ? = null
    var chatsAdapter : ChatsAdapter? = null
    var mChatList : List<Chat>? = null
    private var databaseRef : DatabaseReference? = null
    private var storageRef : StorageReference?= null
    var notify = false
    var apiService : APIService?= null
    var mUser: User?= null
    private var blockPerson : String = ""

    lateinit var recyclerViewChats : RecyclerView

    private var isVisitUserDeleted = false

    companion object {
        @JvmStatic
        val REQUEST_CALL = 1
        @JvmStatic
        val REQUEST_IMAGE_CAPTURE = 813
        @JvmStatic
        val PICK_IMAGE = 438
        @JvmStatic
        val CHANGE_CHAT_BG_IMAGE = 1052
        @JvmStatic
        val TAG: String? = this::class.simpleName
    }

    init {
        // Get Firebase services
        storageRef = FirebaseStorage.getInstance().reference
        databaseRef = FirebaseDatabase.getInstance().reference
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        isVisitUserDeleted = false
        // Get visit user ID
        userIdVisit = intent.getStringExtra("visit_id")!!
        // Current user
        firebaseUser = FirebaseAuth.getInstance().currentUser

        // Recycler view message
        recyclerViewChats = findViewById(R.id.recycler_view_chats)
        recyclerViewChats.setHasFixedSize(true)

        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerViewChats.layoutManager = linearLayoutManager

        // visit User
        val usersReference = databaseRef!!
            .child("Users").child(userIdVisit)

        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mUser = snapshot.getValue(User::class.java)
                if (mUser == null) {
                    isVisitUserDeleted = true
                    val intent = Intent(this@MessageChatActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    username_mchat.text = mUser!!.getUsername()
                    Picasso.get()
                        .load(mUser!!.getProfile())
                        .placeholder(R.drawable.avatar)
                        .into(profile_image_mchat)

                    val status: String? = mUser!!.getStatus();
                    tv_status.text = status
                    if (status == Status.ONLINE.statusString) {
                        cv_status.background.setTint(resources.getColor(R.color.secondary))
                    } else {
                        cv_status.background.setTint(resources.getColor(R.color.gray))
                    }

                    retrieveMessage(firebaseUser!!.uid, userIdVisit, mUser!!.getProfile())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MessageChatActivity,
                    "Can't get user profile!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Add btn event
        send_message_btn.setOnClickListener {
            notify = true
            val message = text_message.text.toString()
            if (message == "") {
                Toast.makeText(
                    this@MessageChatActivity,
                    "Please write a message, please...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }

            text_message.setText("")
        }

        take_picture_btn.setOnClickListener {
            dispatchTakePictureIntent()
        }

        change_chat_bg_image_btn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, "Pick Image"),
                MessageChatActivity.CHANGE_CHAT_BG_IMAGE
            )
        }

        block_btn.setOnClickListener {
            toggleBlockPerson()
        }

        back_button.setOnClickListener {
            finish()
        }

        // Add attach image event
        attach_image_file_btn.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, "Pick Image"),
                MessageChatActivity.PICK_IMAGE
            )
        }

        // navigate when tap in avatar
        profile_image_mchat.setOnClickListener {
            val intent = Intent(this, VisitUserProfileActivity::class.java)
            intent.putExtra("visit_user_id", userIdVisit)
            startActivity(intent)
        }

        //call phone
        cv_call_phone.setOnClickListener{
            makePhoneCall()
        }

        // call video
        cv_call_video.setOnClickListener{
            var intent = Intent (this@MessageChatActivity, CallingActivity::class.java)
            intent.putExtra("userCalling", firebaseUser!!.uid)
            intent.putExtra("userRinging", userIdVisit)
            intent.putExtra("kind", "1")
            val callerRef = databaseRef!!.child("Users").child(userIdVisit)
            callerRef.child("ringing").setValue(firebaseUser!!.uid)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(intent)
                    }
                }
        }
        // seen message action
        seenMessage(userIdVisit)
    }

    private fun makePhoneCall() {
        val number = mUser!!.getPhone()
        if (number != null && number != ""){
            if (ContextCompat.checkSelfPermission(this@MessageChatActivity,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this@MessageChatActivity, arrayOf(Manifest.permission.CALL_PHONE),
                    REQUEST_CALL )
            } else {
                val dial = "tel:$number"
                val intent = Intent(Intent.ACTION_CALL, Uri.parse(dial))
                startActivity(intent)
            }
        } else {
            Toast.makeText(this, "Not a phone number", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall()
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // how to send message to user
    private fun sendMessageToUser(
        senderId: String,
        receiverId: String?,
        message: String,
        imageUrl: String? = "",
        messageId: String? = null
    ) {
        val reference = FirebaseDatabase.getInstance().reference

        // new message
        var messageKey: String? = messageId
        if (messageId == null) {
            messageKey = reference.push().key
        }

        // body data
        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = imageUrl
        messageHashMap["messageId"] = messageKey
        messageHashMap["time"] = System.currentTimeMillis()

        // add new message
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateChatList()

                    // implement the push notification using fcm
                    val userReference = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseUser!!.uid)

                    userReference.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.getValue(User::class.java) != null) {
                                val user = snapshot.getValue(User::class.java)!!
                                if (notify) {
                                    sendNotification(receiverId, user.getUsername(), message)
                                }
                            }

                            notify = false
                        }
                    })
                }
            }
    }

    // View created
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        // Chat information
        val chatInfoRef: DatabaseReference = databaseRef!!
            .child("ChatList")
            .child(firebaseUser!!.uid)
            .child(userIdVisit)

        chatInfoRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatInfo: ChatInfo = snapshot.getValue(ChatInfo::class.java) ?: return

                // get block person status
                blockPerson = chatInfo!!.getBlockPerson()
                configBlockLayout(blockPerson)

                // fill background chat image
                val backgroundChatImageUrl: String = chatInfo!!.getBackgroundChatImageUrl()
                fillBackgroundImage(backgroundChatImageUrl)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MessageChatActivity,
                    "Can't get chat background image!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateChatList(){
        val chatsListReference = FirebaseDatabase.getInstance()
            .reference
            .child("ChatList")
            .child(firebaseUser!!.uid)
            .child(userIdVisit)

        chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    chatsListReference.child("id").setValue(userIdVisit)
                }

                val chatsListReceiverRef = FirebaseDatabase.getInstance()
                    .reference
                    .child("ChatList")
                    .child(userIdVisit)
                    .child(firebaseUser!!.uid)

                chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MessageChatActivity,
                    "sent message error, try again...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // change ID
        chatsListReference.child("id").setValue(firebaseUser!!.uid)
    }

    private fun sendNotification(receiverId: String?, username: String?, message: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for (snapshot in p0.children) {
                    val token: Token? = snapshot.getValue(Token::class.java)
                    val data = Data(
                        firebaseUser!!.uid,
                        R.mipmap.ic_icon_foreground,
                        "$username: $message",
                        "New Message",
                        userIdVisit
                    )
                    val sender = Sender(data, token!!.getToken().toString())
                    apiService!!.sendNotification(sender)
                        .enqueue(object : retrofit2.Callback<MyResponse> {
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if (response.code() == 200) {
                                    if (response.body()!!.success != 1) {
                                        Toast.makeText(
                                            this@MessageChatActivity,
                                            "Failed, Nothing happen.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                            }
                        })
                }
            }
        })
    }

    // handle after intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MessageChatActivity.PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            uploadFileImageToDatabase(data.data)
        }

        if (requestCode == MessageChatActivity.CHANGE_CHAT_BG_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            changeBackgroundChatImage(data.data)
        }

        if (requestCode == MessageChatActivity.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
            val imageBitmap = data.extras!!.get("data") as Bitmap
            uploadBitmapImageToDatabase(imageBitmap)
        }
    }


    private var retrieveListener : ValueEventListener? = null
    private fun retrieveMessage(senderId: String, receiverId: String?, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        retrieveListener = reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isVisitUserDeleted) {
                    (mChatList as ArrayList<Chat>).clear()
                    for (shot in snapshot.children) {
                        val chat = shot.getValue(Chat::class.java)

                        if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(
                                receiverId
                            )
                            || chat.getReceiver().equals(receiverId) && chat.getSender().equals(
                                senderId
                            )
                        ) {
                            (mChatList as ArrayList<Chat>).add(chat)
                        }

                        chatsAdapter = ChatsAdapter(
                            this@MessageChatActivity,
                            mChatList as ArrayList<Chat>,
                            receiverImageUrl!!
                        )
                        recyclerViewChats.adapter = chatsAdapter
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageChatActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var seenListener: ValueEventListener? = null
    private fun seenMessage(userId: String) {
        val chatsReference = databaseRef!!.child("Chats")

        if (chatsReference != null){
            seenListener = chatsReference!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (shot in snapshot.children) {
                        val chat = shot.getValue(Chat::class.java)

                        if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat.getSender()
                                .equals(
                                    userId
                                )
                        ) {
                            val hashMap = HashMap<String, Any>()
                            hashMap["isSeen"] = true
                            shot.ref.updateChildren(hashMap)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

    }

    // Capture image functions
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (error: Exception) {
            error.printStackTrace()
            Toast.makeText(this, "Can't take a picture now", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadBitmapImageToDatabase(imageBitmap: Bitmap?) {
        val progressBar = ProgressDialog(this)
        progressBar.setMessage("image is uploading, please wait...")
        progressBar.show()

        if (imageBitmap != null) {
            val stream = ByteArrayOutputStream()
            imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            val storageReference = storageRef!!.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key

            val fileRef = storageReference.child("$messageId.jpg")
            val uploadTask: StorageTask<*>
            uploadTask = fileRef.putBytes(stream.toByteArray());
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (task.isSuccessful) {
                    task.exception?.let {
                        throw  it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val imageUrl = downloadUri.toString()

                    sendMessageToUser(
                        firebaseUser!!.uid,
                        userIdVisit,
                        MessageString.RECEIVE_IMAGE.message,
                        imageUrl,
                        messageId
                    )

                    progressBar.dismiss()
                }
            }
        } else {
            Toast.makeText(this, "Can't get image Uri", Toast.LENGTH_SHORT).show()
            progressBar.dismiss()
        }
    }

    private fun changeBackgroundChatImage(imageUri: Uri?) {
        val progressBar = ProgressDialog(this)
        progressBar.setMessage("Background image is uploading, please wait...")
        progressBar.show()

//        val compressedImageFile: File = Compressor(this)
//            .setMaxWidth(1000)
//            .setMaxHeight(1000)
//            .setCompressFormat(Bitmap.CompressFormat.JPEG)
//            .compressToFile(imageUri!!.toFile())

        if (imageUri!= null){
            val backgroundImageUserRef: DatabaseReference = databaseRef!!
                .child("ChatList")
                .child(firebaseUser!!.uid)
                .child(userIdVisit)
                .child("backgroundChatImageUrl")

            val backgroundImageVisitUserRef: DatabaseReference = databaseRef!!
                .child("ChatList")
                .child(userIdVisit)
                .child(firebaseUser!!.uid)
                .child("backgroundChatImageUrl")

            backgroundImageUserRef.removeValue()
                .addOnCompleteListener {
                    val storageReference =  storageRef!!.child("Background Chat Images")
                    val fileName: String = ChatInfo.getBackgroundChatImageFileName(firebaseUser!!.uid, userIdVisit)
                    val fileRef = storageReference.child(fileName)
                    val uploadTask : StorageTask<*>
                    uploadTask = fileRef.putFile(imageUri)
                    uploadTask.addOnSuccessListener{ snapshot ->
                        val getUrlTask: Task<Uri> = snapshot.metadata!!.reference!!.downloadUrl
                        getUrlTask.addOnSuccessListener { uriResult ->
                            val downloadUri: Uri = uriResult
                            val imageUrl = downloadUri.toString()

                            backgroundImageUserRef.setValue(imageUrl)
                            backgroundImageVisitUserRef.setValue(imageUrl)

                            progressBar.dismiss()
                        }
                    }.addOnFailureListener {
                        progressBar.dismiss()
                        it.printStackTrace()
                        Toast.makeText(this, "Can't change the background chat image now", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    progressBar.dismiss()
                    it.printStackTrace()
                    Toast.makeText(this, "Can't remove the background chat image now", Toast.LENGTH_SHORT).show()
                }

        } else {
            Toast.makeText(this, "Can't get image Uri", Toast.LENGTH_SHORT).show()
            progressBar.dismiss()
        }
    }

    private fun uploadFileImageToDatabase(imageUri: Uri?) {
        val progressBar = ProgressDialog(this)
        progressBar.setMessage("image is uploading, please wait...")
        progressBar.show()

        if (imageUri!= null){
            val storageReference =  storageRef!!.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key

            val fileRef = storageReference.child("$messageId.jpg")
            val uploadTask : StorageTask<*>
            uploadTask = fileRef.putFile(imageUri)
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
                    val imageUrl = downloadUri.toString()

                    sendMessageToUser(
                        firebaseUser!!.uid,
                        userIdVisit,
                        MessageString.RECEIVE_IMAGE.message,
                        imageUrl,
                        messageId
                    )

                    progressBar.dismiss()
                }
            }
        } else {
            Toast.makeText(this, "Can't get image Uri", Toast.LENGTH_SHORT).show()
            progressBar.dismiss()
        }
    }

    private fun fillBackgroundImage(backgroundChatImageUrl: String) {
        if (backgroundChatImageUrl.isEmpty())
            return

        Picasso.get()
            .load(backgroundChatImageUrl)
            .resize(container.width, container.height)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    container.background = BitmapDrawable(resources, bitmap)
                }

                override fun onBitmapFailed(error: Exception?, errorDrawable: Drawable?) {
                    error?.printStackTrace()
                    Toast.makeText(
                        this@MessageChatActivity,
                        "Load background chat image fail",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    null
                }

            })

        // make something transparent
        header.setBackgroundColor(resources.getColor(android.R.color.transparent))
        divider_line.setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    // Block person feature
    private fun toggleBlockPerson() {
        val blockPersonUserRef: DatabaseReference = databaseRef!!
            .child("ChatList")
            .child(firebaseUser!!.uid)
            .child(userIdVisit)
            .child("blockPerson")

        val blockPersonVisitUserRef: DatabaseReference = databaseRef!!
            .child("ChatList")
            .child(userIdVisit)
            .child(firebaseUser!!.uid)
            .child("blockPerson")

        if (blockPerson.isNotEmpty()) {
            blockPersonUserRef.removeValue()
            blockPersonVisitUserRef.removeValue()
        } else {
            blockPersonUserRef.setValue(firebaseUser!!.uid)
            blockPersonVisitUserRef.setValue(firebaseUser!!.uid)
        }
    }

    private fun configBlockLayout(blockPerson: String) {
        if (blockPerson.isNotEmpty()) {
            relative_layout_bottom.visibility = View.GONE
            relative_layout_bottom_block.visibility = View.VISIBLE
            cv_call_phone.visibility = View.GONE
            cv_call_video.visibility = View.GONE

            Picasso.get()
                .load(R.drawable.icon_unlock)
                .noFade()
                .into(block_btn)

            if (blockPerson == firebaseUser!!.uid) {
                block_text.text = "You have been block ${username_mchat.text} already"
                cv_block.setCardBackgroundColor(resources.getColor(R.color.primary))
                block_btn.isEnabled = true
            } else {
                block_text.text = "You can't send message to this person now"
                cv_block.setCardBackgroundColor(resources.getColor(R.color.lightPrimary))
                block_btn.isEnabled = false
            }

        } else {
            relative_layout_bottom.visibility = View.VISIBLE
            relative_layout_bottom_block.visibility = View.GONE
            cv_call_phone.visibility = View.VISIBLE
            cv_call_video.visibility = View.VISIBLE

            Picasso.get()
                .load(R.drawable.icon_block)
                .noFade()
                .into(block_btn)
        }
    }

    override fun onPause() {
        super.onPause()
        databaseRef!!.removeEventListener(seenListener!!)
    }

    override fun onDestroy() {
        if (retrieveListener != null){
            databaseRef!!.removeEventListener(retrieveListener!!)
        }
        super.onDestroy()
    }
}