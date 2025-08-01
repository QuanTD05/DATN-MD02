
package com.example.datn_md02;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.example.datn_md02.Fragment.CartFragment;

public class ZaloPayCallbackActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        if (data != null) {
            String status = data.getQueryParameter("status"); // 1: success, -1: fail, 0: cancel

            switch (status) {
                case "1":
                    Toast.makeText(this, "Thanh toán ZaloPay thành công!", Toast.LENGTH_LONG).show();
                    break;
                case "-1":
                    Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_LONG).show();
                    break;
                case "0":
                    Toast.makeText(this, "Bạn đã huỷ giao dịch", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(this, "Không xác định kết quả", Toast.LENGTH_SHORT).show();
            }
        }

        startActivity(new Intent(this, CartFragment.class));
        finish();
    }
}
