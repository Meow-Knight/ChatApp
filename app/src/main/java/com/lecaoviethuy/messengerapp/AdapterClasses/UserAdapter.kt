package com.lecaoviethuy.messengerapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lecaoviethuy.messengerapp.MainActivity
import com.lecaoviethuy.messengerapp.MessageChatActivity
import com.lecaoviethuy.messengerapp.R
import com.lecaoviethuy.messengerapp.VisitUserProfileActivity
import com.lecaoviethuy.messengerapp.modelClasses.Chat
import com.lecaoviethuy.messengerapp.modelClasses.Status
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_search_item_layout.view.*

class UserAdapter (mContext : Context,
                    mUsers : List<User>,
                   isChatCheck : Boolean) : RecyclerView.Adapter<UserAdapter.ViewHolder?> ()
{

    private var mContext : Context
    private var mUsers : List<User>
    private var isChatCheck: Boolean

    init {
        this.mUsers = mUsers
        this.isChatCheck = isChatCheck
        this.mContext = mContext
    }
    class ViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        var userNameTxt : TextView
        var profileImage : CircleImageView
        var onLineTxt : CircleImageView
        var offLineTxt : CircleImageView
        var lastMessageTxt : TextView

        init {
            userNameTxt = itemView.findViewById(R.id.username)
            profileImage = itemView.findViewById(R.id.profile_image)
            onLineTxt  = itemView.findViewById(R.id.image_online)
            offLineTxt = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)

        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view : View  = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, viewGroup,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user : User = mUsers[position]
        holder.userNameTxt.text = user.getUsername()
        Picasso.get()
            .load(user.getProfile())
            .placeholder(R.drawable.avatar)
            .into(holder.profileImage)

        if(isChatCheck){
            retrieveLastMessage(user.getUid(), holder.lastMessageTxt)
            if(user.getStatus() == Status.ONLINE.statusString){
                holder.onLineTxt.visibility = View.VISIBLE
                holder.offLineTxt.visibility = View.GONE
            } else {
                holder.onLineTxt.visibility = View.GONE
                holder.offLineTxt.visibility = View.VISIBLE
            }
        } else {
            holder.onLineTxt.visibility = View.GONE
            holder.offLineTxt.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )

            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want?")
            builder.setItems(options) { _, position ->
                if (position == 0) {
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("visit_id", user.getUid())
                    mContext.startActivity(intent)
                }

                if (position == 1) {
                    val intent = Intent(mContext, VisitUserProfileActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("visit_user_id", user.getUid())
                    mContext.startActivity(intent)
                }
            }

            builder.show()
        }
    }

    private fun retrieveLastMessage(chatUserId: String?, lastMessageTxt: TextView) {
        var lastMessage = "defaultMessage"

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")

        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for(dataSnapShot in snapshot.children){
                    val chat : Chat? = dataSnapShot.getValue(Chat::class.java)

                    if(firebaseUser != null && chat != null){
                        if((chat.getSender() == firebaseUser.uid && chat.getReceiver() == chatUserId)
                                || (chat.getSender() == chatUserId && chat.getReceiver() == firebaseUser.uid)){
                            lastMessage = chat.getMessage()!!
                        }
                    }
                }

                when (lastMessage){
                    "defaultMessage" -> lastMessageTxt.text = "no message"
                    "sent you an image" -> lastMessageTxt.text = "image sent"
                    else -> lastMessageTxt.text = lastMessage
                }
                lastMessage = "defaultMessage"
            }

        })
    }
}