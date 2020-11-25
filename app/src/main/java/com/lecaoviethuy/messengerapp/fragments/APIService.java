package com.lecaoviethuy.messengerapp.fragments;

import com.lecaoviethuy.messengerapp.Notifications.MyResponse;
import com.lecaoviethuy.messengerapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAbYkgARg:APA91bG04NyMvwtSv7AWT9fggbB_v9ChVqd1jdTA-pSFGdbG2YcmLr00uCPG1vlysTcPySwmv7Wj9o5v5T9V5EeRd1cTYIEAPhunILMFDKOvSwVhlMeCHVF0tWesLA06MbVnvZSjSJoW"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification (@Body Sender body);
}
