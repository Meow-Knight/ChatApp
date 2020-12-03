package com.lecaoviethuy.messengerapp.fragments

import android.content.Context
import android.os.Bundle
<<<<<<< HEAD
=======
import android.util.Log
import androidx.fragment.app.Fragment
>>>>>>> a0a4c1e... Add delete your account feature
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.lecaoviethuy.messengerapp.R
import com.lecaoviethuy.messengerapp.adapterClasses.UserAdapter
import com.lecaoviethuy.messengerapp.modelClasses.Chat
import com.lecaoviethuy.messengerapp.modelClasses.Chatlist
import com.lecaoviethuy.messengerapp.modelClasses.ItemChatlist
import com.lecaoviethuy.messengerapp.modelClasses.User
import com.lecaoviethuy.messengerapp.notifications.Token
import kotlin.collections.ArrayList

class ChatsFragment : Fragment() {

    private var userAdapter : UserAdapter? = null
    private var mUsers : List<User>? = null
    private var usersChatList : List<Chatlist>? = null
    private var firebaseUser : FirebaseUser? = null

    lateinit var recyclerViewChatlist : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         val view = inflater.inflate(R.layout.fragment_chats, container, false)
         return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewChatlist = view.findViewById(R.id.recycler_view_chatlist)
        recyclerViewChatlist.setHasFixedSize(true)
        recyclerViewChatlist.layoutManager = LinearLayoutManager(view.context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()

        chatListReference = FirebaseDatabase
                            .getInstance()
                            .reference
                            .child("ChatList")
                            .child(FirebaseAuth
                                .getInstance()
                                .currentUser!!.uid)
        chatListRetrieveListener = chatListReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (usersChatList as ArrayList).clear()

                for (shot in snapshot.children) {
                    val chatlist = Chatlist(shot.key!!)

                    (usersChatList as ArrayList).add(chatlist)
                }

                retrieveChatList(view.context)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        // update new token
        updateToken(FirebaseInstanceId.getInstance().token)
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token!!)
        ref.child(firebaseUser!!.uid).setValue(token1)
    }

    private fun retrieveChatList(context: Context) {
        mUsers = ArrayList()

        userReference = FirebaseDatabase.getInstance().reference.child("Users")
        userRetrieveListener = userReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList).clear()

                for (shot in snapshot.children) {
                    val user = shot.getValue(User::class.java)

                    for (eachChatList in usersChatList!!) {
                        if (user!!.getUid().equals(eachChatList.getId())) {
                            (mUsers as ArrayList).add(user)
                        }
                    }
                }
                val ref = FirebaseDatabase.getInstance().reference.child("Chats")
                ref.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val itemChatLists =  ArrayList<ItemChatlist>()
                        var lastChat = Chat()
                        for (user in mUsers as ArrayList<User>){
                            for(dataSnapShot in snapshot.children){
                                val chat : Chat? = dataSnapShot.getValue(Chat::class.java)
                                if(firebaseUser != null && chat != null){
                                    if((chat.getSender() == firebaseUser!!.uid && chat.getReceiver() == user.getUid())
                                        || (chat.getSender() == user.getUid() && chat.getReceiver() == firebaseUser!!.uid)){
                                         lastChat = chat
                                    }
                                }
                            }
                            itemChatLists.add(ItemChatlist(user,lastChat.getTime()))
                        }
                        // sorting chat list in chat fragment
                        itemChatLists.sortWith(nullsLast(compareByDescending(ItemChatlist::getTimeLastMessage)))
                        (mUsers as ArrayList<User>).clear()
                        for (itemChatList in itemChatLists){
                            itemChatList.getUser()?.let { (mUsers as ArrayList<User>).add(it) }
                        }

                        userAdapter = UserAdapter(context, (mUsers as ArrayList<User>), true)
                        recyclerViewChatlist.adapter = userAdapter
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onPause() {
        super.onPause()

        chatListReference!!.removeEventListener(chatListRetrieveListener!!)
        userReference!!.removeEventListener(userRetrieveListener!!)
    }

    companion object{
        var chatListReference : DatabaseReference? = null
        var userReference : DatabaseReference? = null
        var userRetrieveListener : ValueEventListener? = null
        var chatListRetrieveListener : ValueEventListener? = null

        fun clearAllListener(){
            if (chatListReference != null && chatListRetrieveListener != null){
                chatListReference!!.removeEventListener(chatListRetrieveListener!!)
            }

            if (userReference != null && userRetrieveListener != null){
                userReference!!.removeEventListener(userRetrieveListener!!)
            }
        }
    }
}