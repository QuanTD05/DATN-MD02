package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.CartAdapter;
import com.example.datn_md02.Model.Cart;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private RecyclerView recyclerCart;
    private TextView tvTotalPrice;
    private CheckBox checkboxSelectAll;
    private Button btnCheckout;

    private final List<Cart> cartList = new ArrayList<>();
    private CartAdapter cartAdapter;
    private DatabaseReference cartRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerCart = view.findViewById(R.id.recyclerCart);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        checkboxSelectAll = view.findViewById(R.id.checkboxSelectAll);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(currentUserId);

        setupRecyclerView();
        setupListeners();
        loadCartFromFirebase();

        return view;
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(requireContext(), cartList, new CartAdapter.OnCartActionListener() {
            @Override
            public void onIncrease(Cart item) {
                item.setQuantity(item.getQuantity() + 1);
                cartRef.child(item.getCartId()).setValue(item);
                updateTotal();
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDecrease(Cart item) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    cartRef.child(item.getCartId()).setValue(item);
                    updateTotal();
                    cartAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onDelete(Cart item) {
                cartRef.child(item.getCartId()).removeValue();
                cartList.remove(item);
                updateTotal();
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemCheckedChanged(Cart item, boolean isChecked) {
                item.setSelected(isChecked);
                cartRef.child(item.getCartId()).setValue(item);
                updateTotal();
            }
        });

        recyclerCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCart.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Cart item : cartList) {
                item.setSelected(isChecked);
                cartRef.child(item.getCartId()).setValue(item);
            }
            cartAdapter.notifyDataSetChanged();
            updateTotal();
        });

        btnCheckout.setOnClickListener(v -> {
            List<Cart> selectedItems = new ArrayList<>();
            for (Cart item : cartList) {
                if (item.isSelected()) selectedItems.add(item);
            }

            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn sản phẩm!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Đã chọn " + selectedItems.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
                // TODO: Chuyển sang màn hình thanh toán
            }
        });
    }

    private void updateTotal() {
        double total = 0;
        for (Cart item : cartList) {
            if (item.isSelected()) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f₫", total));
    }

    private void loadCartFromFirebase() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Cart item = data.getValue(Cart.class);
                    if (item != null) {
                        item.setCartId(data.getKey());
                        cartList.add(item);
                    }
                }
                cartAdapter.notifyDataSetChanged();
                updateTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
