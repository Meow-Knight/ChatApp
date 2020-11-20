package com.lecaoviethuy.messengerapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lecaoviethuy.messengerapp.AdapterClasses.UserAdapter
import com.lecaoviethuy.messengerapp.R
import com.lecaoviethuy.messengerapp.modelClasses.Chatlist
import com.lecaoviethuy.messengerapp.modelClasses.User

class ChatsFragment : Fragment() {

    private var userAdapter : UserAdapter? = null
    private var mUsers : List<User>? = null
    private var usersChatList : List<Chatlist>? = null;
    private var firebaseUser : FirebaseUser? = null;

    lateinit var recycler_view_chatlist : RecyclerView;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         val view = inflater.inflate(R.layout.fragment_chats, container, false);

        recycler_view_chatlist = view.findViewById(R.id.recycler_view_chatlist);
        recycler_view_chatlist.setHasFixedSize(true);
        recycler_view_chatlist.layoutManager = LinearLayoutManager(context);

        firebaseUser = FirebaseAuth.getInstance().currentUser;

        usersChatList = ArrayList();

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid);
        ref!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (usersChatList as ArrayList).clear();

                for (shot in snapshot.children) {
                    val chatlist = Chatlist(shot.key!!);

                    (usersChatList as ArrayList).add(chatlist!!);
                    retrieveChatList();
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        return view;
    }

    private fun retrieveChatList() {
        mUsers = ArrayList();

        val ref = FirebaseDatabase.getInstance().reference.child("Users");
        ref!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList).clear();

                for (shot in snapshot.children) {
                    val user = shot.getValue(User::class.java);

                    for (eachChatList in usersChatList!!) {
                        if (user!!.getUid().equals(eachChatList.getId())) {
                            (mUsers as ArrayList).add(user!!);
                        }
                    }
                }

                userAdapter = UserAdapter(context!!, (mUsers as ArrayList<User>), true);
                recycler_view_chatlist.adapter = userAdapter;
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}