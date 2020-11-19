package com.lecaoviethuy.messengerapp.AdapterClasses

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.lecaoviethuy.messengerapp.R
import com.lecaoviethuy.messengerapp.modelClasses.Chat
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter (
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String
    ) : RecyclerView.Adapter<ChatsAdapter.ViewHolder?>() {

    private val mContext: Context;
    private val mChatList: List<Chat>;
    private val imageUrl: String;

    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser!!;

    init {
        this.mChatList = mChatList;
        this.mContext = mContext;
        this.imageUrl = imageUrl;
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profile_image: CircleImageView? = null;
        var show_text_message: TextView? = null;
        var left_image_view: ImageView? = null;
        var right_image_view: ImageView? = null;
        var text_seen: TextView? = null;

        init {
            profile_image = itemView.findViewById(R.id.profile_image);
            show_text_message = itemView.findViewById(R.id.show_text_message);
            left_image_view = itemView.findViewById(R.id.left_image_view);
            text_seen = itemView.findViewById(R.id.text_seen);
            right_image_view = itemView.findViewById(R.id.right_image_view);
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // viewType == 1 -> user sent
        return if (viewType == 1) {
            val view : View  = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent,false);
            return ViewHolder(view);
        } else { // viewType == 0 -> visit user sent
            val view : View  = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent,false);
            return ViewHolder(view);
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position];

        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.avatar)
            .into(holder.profile_image);

        // image message
        if (chat.getMessage().equals("has sent you an image.") && !chat.getUrl().equals("")) {
            // image message - right side
            if (chat.getSender().equals(firebaseUser!!.uid)) {
                holder.show_text_message!!.visibility = View.GONE;
                holder.right_image_view!!.visibility = View.VISIBLE;
                Picasso.get().load(chat.getUrl()).into(holder.right_image_view)
            }

            // image message - left side
            if (!chat.getSender().equals(firebaseUser!!.uid)) {
                holder.show_text_message!!.visibility = View.GONE;
                holder.left_image_view!!.visibility = View.VISIBLE;
                Picasso.get().load(chat.getUrl()).into(holder.left_image_view)
            }
        }

        // text message
        else {
            holder.show_text_message!!.text = chat.getMessage();
        }

        // sent and seen message
        if (position == mChatList.size - 1) {
            if (chat.getIsSeen()) {
                holder.text_seen!!.text = "Seen";

                if (chat.getMessage().equals("has sent you an image.") && !chat.getUrl().equals("")) {
                    val layoutParams: RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?;
                    layoutParams!!.setMargins(0, 245, 10, 0);
                    holder.text_seen!!.layoutParams = layoutParams;
                }
            } else {
                holder.text_seen!!.text = "Sent";

                if (chat.getMessage().equals("has sent you an image.") && !chat.getUrl().equals("")) {
                    val layoutParams: RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?;
                    layoutParams!!.setMargins(0, 245, 10, 0);
                    holder.text_seen!!.layoutParams = layoutParams;
                }
            }
        } else {
            holder.text_seen!!.visibility = View.GONE;
        }
    }

    override fun getItemCount(): Int {
        return mChatList.size;
    }

    override fun getItemViewType(position: Int): Int {
        // viewType == 1 -> user sent
        // viewType == 0 -> visit user sent
        return if (mChatList[position].getSender().equals(firebaseUser!!.uid))
            1
        else
            0
    }
}