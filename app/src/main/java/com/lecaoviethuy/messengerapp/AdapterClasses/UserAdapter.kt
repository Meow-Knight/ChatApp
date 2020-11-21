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
import com.lecaoviethuy.messengerapp.MainActivity
import com.lecaoviethuy.messengerapp.MessageChatActivity
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

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            );

            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext);
            builder.setTitle("What do you want?");
            builder.setItems(options, DialogInterface.OnClickListener{ dialog, position ->
                if (position == 0) {
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("visit_id", user.getUid());
                    mContext.startActivity(intent);
                }

                if (position == 1) {

                }
            })

            builder.show();
        }
    }
}