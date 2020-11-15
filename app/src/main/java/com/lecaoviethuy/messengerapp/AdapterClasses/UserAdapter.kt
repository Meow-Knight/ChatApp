package com.lecaoviethuy.messengerapp.AdapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lecaoviethuy.messengerapp.R
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
            userNameTxt = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profile_image);
            onLineTxt  = itemView.findViewById(R.id.image_online);
            offLineTxt = itemView.findViewById(R.id.image_offline);
            lastMessageTxt = itemView.findViewById(R.id.message_last);

        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view : View  = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, viewGroup,false)
        return UserAdapter.ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return mUsers.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user : User = mUsers.get(position)
        holder.userNameTxt.text = user!!.getUsername()
        Picasso.get()
            .load(user.getProfile())
            .placeholder(R.drawable.avatar)
            .into(holder.profileImage)
    }
}