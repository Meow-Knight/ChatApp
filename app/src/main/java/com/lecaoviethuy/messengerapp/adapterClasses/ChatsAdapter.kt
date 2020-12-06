package com.lecaoviethuy.messengerapp.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lecaoviethuy.messengerapp.R
import com.lecaoviethuy.messengerapp.ViewFullImageActivity
import com.lecaoviethuy.messengerapp.controllers.StorageController
import com.lecaoviethuy.messengerapp.modelClasses.Chat
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter (
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String
    ) : RecyclerView.Adapter<ChatsAdapter.MessageHolder?>() {

    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String

    var firebaseUser: FirebaseUser? = null

    companion object {
        @JvmStatic
        val USER_TYPE = 1
        @JvmStatic
        val USER_VISIT_TYPE = 0
        @JvmStatic
        val SENT_MESSAGE: String = "sent"
        @JvmStatic
        val SENT_ICON = R.drawable.icon_check;
        @JvmStatic
        val SEEN_MESSAGE: String = "seen"
        @JvmStatic
        val SEEN_ICON = R.drawable.icon_check_circle;
    }

    init {
        Log.d("check_activity", "on constructor chat adapter")
        this.mChatList = mChatList
        this.mContext = mContext
        this.imageUrl = imageUrl
        this.firebaseUser = FirebaseAuth.getInstance().currentUser!!
    }

    inner class MessageHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var container : CardView? = null
        var showTextMessage: TextView? = null
        var imageView: ImageView? = null
        var imageViewArea: CardView? = null
        var textSeen: TextView? = null
        var textSeenIcon: ImageView? = null
        var textSeenArea: RelativeLayout? = null
        var imageSeen: TextView? = null
        var imageSeenIcon: ImageView? = null
        var imageSeenArea: RelativeLayout? = null

        init {
            container = itemView.findViewById(R.id.cv_show_text_message)
            showTextMessage = itemView.findViewById(R.id.show_text_message)
            imageView = itemView.findViewById(R.id.image_view)
            imageViewArea = itemView.findViewById(R.id.image_view_area)

            when (viewType) {
                ChatsAdapter.USER_TYPE -> {
                    textSeen = itemView.findViewById(R.id.text_seen)
                    imageSeen = itemView.findViewById(R.id.image_seen)
                    textSeenIcon = itemView.findViewById(R.id.text_seen_icon)
                    textSeenArea = itemView.findViewById(R.id.text_seen_area)
                    imageSeenIcon = itemView.findViewById(R.id.image_seen_icon)
                    imageSeenArea = itemView.findViewById(R.id.image_seen_area)
                }

                ChatsAdapter.USER_VISIT_TYPE -> {
                    null
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        var viewResult: MessageHolder? = null;
        when (viewType) {
            ChatsAdapter.USER_TYPE -> {
                val view : View  = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent,false)
                viewResult = MessageHolder(view, viewType);
            }

            ChatsAdapter.USER_VISIT_TYPE -> {
                val view : View  = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent,false)
                viewResult = MessageHolder(view, viewType);
            }
        }

        return  viewResult!!;
    }

    override fun onBindViewHolder(holder: ChatsAdapter.MessageHolder, position: Int) {
        val chat: Chat = mChatList[position];

        // image message
        if (chat.getMessage().equals("has sent you an image.") && !chat.getUrl().equals("")) {
            // image message - right side
            // show full image when click on it
            // show dialog delete when long click on it
            holder.imageViewArea!!.visibility = View.VISIBLE
            holder.container!!.visibility = View.GONE
            Picasso.get()
                .load(chat.getUrl())
                .placeholder(R.drawable.image_chat_placeholder)
                .into(holder.imageView)

            // show full image when click on it
            holder.imageView!!.setOnClickListener{
                val intent = Intent(mContext, ViewFullImageActivity::class.java)
                intent.putExtra("url", chat.getUrl())
                mContext.startActivity(intent)
            }

            // set options behavior for image
            if (chat.getSender().equals(firebaseUser!!.uid)) {
                holder.imageView!!.setOnLongClickListener{
                    val options = arrayOf<CharSequence>(
                            "Delete Image",
                            "Cancel"
                    )

                    val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want?")
                    builder.setItems(options) { _, i ->
                        when(i){
                            0 -> {
                                val imageRef: StorageReference = FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(chat.getUrl()!!)
                                imageRef.delete()
                                    .addOnSuccessListener {
                                        deleteSentMessage(position, holder)
                                    }
                                    .addOnFailureListener {
                                        it.printStackTrace()
                                        Toast.makeText(mContext, "Can't delete that message now", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    builder.show()
                    true
                }
            }
        }

        // text message
        else {
            holder.showTextMessage!!.text = chat.getMessage()
        }

        // sent and seen message
        if (position == mChatList.size - 1) {
            if (chat.getMessage().equals("has sent you an image.") && !chat.getUrl().equals("")) {
                holder.imageViewArea!!.visibility = View.VISIBLE
                holder.container!!.visibility = View.GONE
            } else {
                holder.imageViewArea!!.visibility = View.GONE
                holder.container!!.visibility = View.VISIBLE
            }

            if (chat.getSender() == firebaseUser!!.uid) {

                when (chat.getIsSeen()) {
                    true -> {
                        if (chat.getMessage().equals("has sent you an image.") && !chat.getUrl().equals("")) {
                            holder.imageSeen!!.text = ChatsAdapter.SEEN_MESSAGE
                            holder.imageSeenIcon!!.setImageResource(ChatsAdapter.SEEN_ICON)
                        } else {
                            holder.textSeen!!.text = ChatsAdapter.SEEN_MESSAGE
                            holder.textSeenIcon!!.setImageResource(ChatsAdapter.SEEN_ICON)
                        }
                    }

                    false -> {
                        if (chat.getMessage().equals("has sent you an image.") && !chat.getUrl().equals("")) {
                            holder.imageSeen!!.text = ChatsAdapter.SENT_MESSAGE
                            holder.imageSeenIcon!!.setImageResource(ChatsAdapter.SENT_ICON)
                        } else {
                            holder.textSeen!!.text = ChatsAdapter.SENT_MESSAGE
                            holder.textSeenIcon!!.setImageResource(ChatsAdapter.SENT_ICON)
                        }
                    }
                }
            }
        } else {
            if (chat.getSender() == firebaseUser!!.uid) {
                holder.textSeenArea!!.visibility = View.GONE
                holder.imageSeenArea!!.visibility = View.GONE
            }
            if(firebaseUser!!.uid == chat.getSender()){
                holder.showTextMessage!!.setOnClickListener{
                    val options = arrayOf<CharSequence>(
                            "Delete Message",
                            "Cancel"
                    )

                    val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want?")
                    builder.setItems(options) { _, i ->
                        when(i){
                            0 -> {
                                deleteSentMessage(position, holder)
                            }
                        }
                    }
                    builder.show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (mChatList[position].getSender().equals(firebaseUser!!.uid))
            ChatsAdapter.USER_TYPE
        else
            ChatsAdapter.USER_VISIT_TYPE
    }

    private fun deleteSentMessage(position: Int, holder: MessageHolder){
        FirebaseDatabase.getInstance().reference
                .child("Chats")
                .child(mChatList[position].getMessageId()!!)
                .removeValue()
                .addOnCompleteListener{
                    task ->
                    if (task.isSuccessful){
                        Toast.makeText(holder.itemView.context, "Deleted!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(holder.itemView.context, "Delete Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
    }
}