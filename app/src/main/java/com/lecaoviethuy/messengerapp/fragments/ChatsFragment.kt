package com.lecaoviethuy.messengerapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.lecaoviethuy.messengerapp.AdapterClasses.UserAdapter
import com.lecaoviethuy.messengerapp.R
import com.lecaoviethuy.messengerapp.modelClasses.Chatlist
import com.lecaoviethuy.messengerapp.modelClasses.User

class ChatsFragment : Fragment() {

    private var userAdapter : UserAdapter? = null
    private var mUsers : List<User>? = null
    private var usersChatList : List<Chatlist>? = null
    private var firebaseUser : FirebaseUser? = null
    private var userReference : DatabaseReference? = null
    private var chatListReference : DatabaseReference? = null

    lateinit var recyclerViewChatlist : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    var chatListRetrieveListener : ValueEventListener? = null
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

        chatListReference = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
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
    }

    var userRetrieveListener : ValueEventListener? = null
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

                userAdapter = UserAdapter(context, (mUsers as ArrayList<User>), true)
                recyclerViewChatlist.adapter = userAdapter
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
}