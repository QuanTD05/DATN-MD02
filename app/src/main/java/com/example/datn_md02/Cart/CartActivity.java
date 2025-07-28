package com.example.datn_md02.Cart;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.datn_md02.Fragment.CartFragment;
import com.example.datn_md02.R;

public class CartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart); // XML phải có FrameLayout với id là cart_fragment_container

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.cart_fragment_container, new CartFragment())
                .commit();
    }
}
