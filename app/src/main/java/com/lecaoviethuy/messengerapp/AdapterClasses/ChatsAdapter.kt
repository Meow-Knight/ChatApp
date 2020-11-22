package com.lecaoviethuy.messengerapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.lecaoviethuy.messengerapp.R
import com.lecaoviethuy.messengerapp.ViewFullImageActivity
import com.lecaoviethuy.messengerapp.WelcomeActivity
import com.lecaoviethuy.messengerapp.modelClasses.Chat
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter (
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String
    ) : RecyclerView.Adapter<ChatsAdapter.ViewHolder?>() {

    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String

    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mChatList = mChatList
        this.mContext = mContext
        this.imageUrl = imageUrl
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView? = null
        var showTextMessage: TextView? = null
        var leftImageView: ImageView? = null
        var rightImageView: ImageView? = null
        var textSeen: TextView? = null

        init {
            profileImage = itemView.findViewById(R.id.profile_image)
            showTextMessage = itemView.findViewById(R.id.show_text_message)
            leftImageView = itemView.findViewById(R.id.left_image_view)
            textSeen = itemView.findViewById(R.id.text_seen)
            rightImageView = itemView.findViewById(R.id.right_image_view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // viewType == 1 -> user sent
        return if (viewType == 1) {
            val view : View  = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent,false)
            ViewHolder(view)
        } else { // viewType == 0 -> visit user sent
            val view : View  = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent,false)
            ViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position]

        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.avatar)
            .into(holder.profileImage)

        // image message
        if (chat.getMessage().equals("has sent you an image.") && !chat.getUrl().equals("")) {
            // image message - right side
            // show full image when click on it
            // show dialog delete when long click on it
            if (chat.getSender().equals(firebaseUser!!.uid)) {
                holder.showTextMessage!!.visibility = View.GONE
                holder.rightImageView!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.rightImageView)

                holder.rightImageView!!.setOnLongClickListener{
                    val options = arrayOf<CharSequence>(
                            "Delete Image",
                            "Cancel"
                    )

                    val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want?")
                    builder.setItems(options) { _, i ->
                        when(i){
                            0 -> deleteSentMessage(position, holder)
                        }
                    }
                    builder.show()
                    true
                }

                holder.rightImageView!!.setOnClickListener{
                    val intent = Intent(mContext, ViewFullImageActivity::class.java)
                    intent.putExtra("url", chat.getUrl())
                    mContext.startActivity(intent)
                }
            }

            // image message - left side
            // show full image when click on it
            if (!chat.getSender().equals(firebaseUser!!.uid)) {
                holder.showTextMessage!!.visibility = View.GONE
                holder.leftImageView!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.leftImageView)

                holder.leftImageView!!.setOnClickListener{
                    val intent = Intent(mContext, ViewFullImageActivity::class.java)
                    intent.putExtra("url", chat.getUrl())
                    mContext.startActivity(intent)
                }
            }
        }

        // text message
        else {
            holder.showTextMessage!!.text = chat.getMessage()
        }

        // sent and seen message
        if (position == mChatList.size - 1) {
            if (chat.getIsSeen()) {
                holder.textSeen!!.text = "Seen"

                if (chat.getMessage().equals("has sent you an image.") && !chat.getUrl().equals("")) {
                    val layoutParams: RelativeLayout.LayoutParams? = holder.textSeen!!.layoutParams as RelativeLayout.LayoutParams?
                    layoutParams!!.setMargins(0, 245, 10, 0)
                    holder.textSeen!!.layoutParams = layoutParams
                }
            } else {
                holder.textSeen!!.text = "Sent"

                if (chat.getMessage().equals("has sent you an image.") && !chat.getUrl().equals("")) {
                    val layoutParams: RelativeLayout.LayoutParams? = holder.textSeen!!.layoutParams as RelativeLayout.LayoutParams?
                    layoutParams!!.setMargins(0, 245, 10, 0)
                    holder.textSeen!!.layoutParams = layoutParams
                }
            }
        } else {
            holder.textSeen!!.visibility = View.GONE
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
        // viewType == 1 -> user sent
        // viewType == 0 -> visit user sent
        return if (mChatList[position].getSender().equals(firebaseUser!!.uid))
            1
        else
            0
    }

    private fun deleteSentMessage(position: Int, holder: ViewHolder){
        val ref = FirebaseDatabase.getInstance().reference
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