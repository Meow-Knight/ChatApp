package com.lecaoviethuy.messengerapp.modelClasses

class User {
    private var uid: String = ""
    private var username: String = ""
    private var profile: String = ""
    private var cover: String = ""
    private var status: String = ""
    private var search: String = ""
    private var facebook: String = ""
    private var instagram: String = ""
    private var website: String = ""
    private var phone : String =""

    constructor()

    constructor(
        uid: String,
        username: String,
        profile: String,
        cover: String,
        status: String,
        search: String,
        facebook: String,
        instagram: String,
        website: String,
        phone :String
    ) {
        this.uid = uid
        this.username = username
        this.profile = profile
        this.cover = cover
        this.status = status
        this.search = search
        this.facebook = facebook
        this.instagram = instagram
        this.website = website
        this.phone = phone
    }

    fun setUid(uid : String){
        this.uid = uid
    }

    fun getUid() : String?{
        return uid
    }

    fun setUsername(username : String){
        this.username = username
    }

    fun getUsername() : String?{
        return username
    }

    fun setProfile(profile : String){
        this.profile = profile
    }

    fun getProfile() : String?{
        return profile
    }

    fun setCover(cover : String){
        this.cover = cover
    }

    fun getCover() : String?{
        return cover
    }

    fun setStatus(status : String){
        this.status = status
    }

    fun getStatus() : String?{
        return status
    }

    fun setSearch(search : String){
        this.search = search
    }

    fun getSearch() : String?{
        return search
    }

    fun setFacebook(facebook : String){
        this.facebook = facebook
    }

    fun getFacebook() : String?{
        return facebook
    }

    fun setInstagram(instagram : String){
        this.instagram = instagram
    }

    fun getInstagram() : String?{
        return instagram
    }

    fun setWebsite(website : String){
        this.website = website
    }

    fun getWebsite() : String?{
        return website
    }

    fun setPhone(phone : String){
        this.phone = phone
    }

    fun getPhone() : String?{
        return phone
    }

    override fun toString(): String {
        return "[uid] $uid, [name] $username, [profile] $profile, [cover] $cover"

    }
}