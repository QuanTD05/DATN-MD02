package com.example.datn_md02.Api;

import android.util.Log;

import com.example.datn_md02.Constant.AppInfo;
import com.example.datn_md02.Helper.Helpers;

import org.json.JSONObject;

import java.util.Date;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateOrder {

    private static class CreateOrderData {
        String AppId;
        String AppUser;
        String AppTime;
        String Amount;
        String AppTransId;
        String EmbedData;
        String Items;
        String BankCode;
        String Description;
        String Mac;

        private CreateOrderData(String amount) throws Exception {
            long appTime = new Date().getTime();

            AppId = String.valueOf(AppInfo.APP_ID);
            AppUser = AppInfo.APP_USER; // Đảm bảo giống với dashboard ZaloPay
            AppTime = String.valueOf(appTime);
            Amount = amount;
            AppTransId = Helpers.getAppTransId();
            EmbedData = "{}";     // Nên để đơn giản để dễ ký
            Items = "[]";         // Không chứa thông tin phức tạp
            BankCode = "zalopayapp";
            Description = "Merchant pay for order #" + AppTransId;

            // Chuỗi ký
            String inputHMac = String.format("%s|%s|%s|%s|%s|%s|%s",
                    AppId,
                    AppTransId,
                    AppUser,
                    Amount,
                    AppTime,
                    EmbedData,
                    Items
            );

            Log.d("ZALO_INPUT_MAC", inputHMac);

            // Sinh chữ ký MAC
            Mac = Helpers.getMac(AppInfo.MAC_KEY, inputHMac);

            Log.d("ZALO_GEN_MAC", Mac);
        }
    }

    public JSONObject createOrder(String amount) throws Exception {
        CreateOrderData input = new CreateOrderData(amount);

        RequestBody formBody = new FormBody.Builder()
                .add("app_id", input.AppId)
                .add("app_user", input.AppUser)
                .add("app_time", input.AppTime)
                .add("amount", input.Amount)
                .add("app_trans_id", input.AppTransId)
                .add("embed_data", input.EmbedData)
                .add("item", input.Items)
                .add("bank_code", input.BankCode)
                .add("description", input.Description)
                .add("mac", input.Mac)
                .build();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(AppInfo.URL_CREATE_ORDER)
                .post(formBody)
                .build();

        Response response = client.newCall(request).execute();
        String responseStr = response.body().string();

        Log.d("ZALO_ORDER_RESP", responseStr);

        return new JSONObject(responseStr);
    }
}
