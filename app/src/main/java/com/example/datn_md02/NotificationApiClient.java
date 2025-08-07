package com.example.datn_md02;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

// Request bodies
class TokenRequest { String userId, token; TokenRequest(String u,String t){userId=u;token=t;} }
class OrderRequest { String userId, orderId; OrderRequest(String u,String o){userId=u;orderId=o;} }
class ChatRequest  { String toUserId, fromName, text; ChatRequest(String t,String f,String m){toUserId=t;fromName=f;text=m;} }

interface NotificationService {
    @POST("/api/register-token")
    Call<Void> registerToken(@Body TokenRequest req);

    @POST("/api/orders")
    Call<Void> postOrder(@Body OrderRequest req);

    @POST("/api/chat")
    Call<Void> sendChat(@Body ChatRequest req);
}

public class NotificationApiClient {
    private static final String TAG = "NotifApiClient";
    private static final String BASE_URL = "http://192.168.32.101:3000"; // your server
    private static NotificationService service;

    static {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(NotificationService.class);
    }

    public static void registerToken(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { Log.w(TAG, "No user"); return; }
        String uid = user.getUid();
        service.registerToken(new TokenRequest(uid, token))
                .enqueue(simpleCallback("registerToken"));
    }

    public static void postOrder(String userId, String orderId) {
        service.postOrder(new OrderRequest(userId, orderId))
                .enqueue(simpleCallback("postOrder"));
    }

    public static void sendChat(String toUserId, String fromName, String text) {
        service.sendChat(new ChatRequest(toUserId, fromName, text))
                .enqueue(simpleCallback("sendChat"));
    }

    private static Callback<Void> simpleCallback(final String tag) {
        return new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) Log.d(TAG, tag+" success");
                else Log.w(TAG, tag+" fail: code="+r.code());
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                Log.e(TAG, tag+" error", t);
            }
        };
    }
}