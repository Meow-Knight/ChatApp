package com.lecaoviethuy.messengerapp.modelClasses

enum class MessageString (val message : String){
    RECEIVE_IMAGE ("has sent you an image."),
    SENT_IMAGE ("image sent"),
    DEFAULT_MESSAGE("defaultMessage"),
    NO_MESSAGE("no message"),
    NULL_MESSAGE("");
}