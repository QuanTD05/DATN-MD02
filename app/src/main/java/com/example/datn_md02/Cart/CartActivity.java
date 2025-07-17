package com.example.datn_md02.Cart;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.datn_md02.Fragment.CartFragment;
import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.R;

public class CartActivity extends AppCompatActivity {

    public static final String EXTRA_CART_ITEM = "cartItem";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart); // XML phải có FrameLayout hoặc ConstraintLayout với id là cart_fragment_container

        CartItem item = (CartItem) getIntent().getSerializableExtra(EXTRA_CART_ITEM);

        CartFragment cartFragment = new CartFragment();
        if (item != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(EXTRA_CART_ITEM, item);
            cartFragment.setArguments(bundle);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.cart_fragment_container, cartFragment)
                .commit();
    }
}
