package com.lecaoviethuy.messengerapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lecaoviethuy.messengerapp.adapterClasses.UserAdapter
import com.lecaoviethuy.messengerapp.R
import com.lecaoviethuy.messengerapp.modelClasses.User
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment() {

    private var userAdapter : UserAdapter? = null
    private var mUsers : List<User>? = null
    private var rvUsers : RecyclerView? = null
    private var searchUser : SearchView? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_search, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchUser = view.findViewById(R.id.searchUser)
        rvUsers = view.findViewById(R.id.searchList)
        rvUsers!!.setHasFixedSize(true)
        rvUsers!!.layoutManager = LinearLayoutManager(view.context)
        mUsers = ArrayList()
        retrieveAllUsers(view.context)

        searchUser!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if(p0 != null){
                    searchForUser(p0.toString().toLowerCase(Locale.ROOT), view.context)
                }

                return true
            }
        })

    }

    private fun retrieveAllUsers(context:Context) {
         val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
         val refUser = FirebaseDatabase.getInstance().reference.child("Users")
         refUser.addValueEventListener(object : ValueEventListener
         {
             override fun onCancelled(error: DatabaseError) {

             }

             override fun onDataChange(p0: DataSnapshot) {
                 (mUsers as ArrayList<User>).clear()
                 if (searchUser!!.query.toString() == ""){
                     for (snapshot in p0.children){
                         val user : User? = snapshot.getValue(User::class.java)
                         if (!(user!!.getUid()).equals(firebaseUserID)){
                             (mUsers as ArrayList<User>).add(user)
                         }
                     }
                     userAdapter = UserAdapter(context,mUsers!!, false)
                     rvUsers!!.adapter = userAdapter
                 }
             }
         })
    }

    private fun searchForUser (str :String, context: Context){
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUser = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")
        queryUser.addValueEventListener(object :  ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<User>).clear()

                for (snapshot in p0.children){
                    val user : User? = snapshot.getValue(User::class.java)
                    if (!(user!!.getUid()).equals(firebaseUserID)){
                        (mUsers as ArrayList<User>).add(user)
                    }
                }
                userAdapter = UserAdapter(context , mUsers!!, false)
                rvUsers!!.adapter = userAdapter
            }
        })
    }
}