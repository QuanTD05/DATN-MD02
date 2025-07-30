package com.example.datn_md02.Fragment;

import android.content.Intent;
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
import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private RecyclerView recyclerCart;
    private TextView tvTotalPrice;
    private CheckBox checkboxSelectAll;
    private Button btnCheckout;

    private final List<CartItem> cartList = new ArrayList<>();
    private CartAdapter cartAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // Ánh xạ View
        recyclerCart = view.findViewById(R.id.recyclerCart);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        checkboxSelectAll = view.findViewById(R.id.checkboxSelectAll);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        setupRecyclerView();
        setupListeners();

        loadDummyCart(); // Load dữ liệu giả để test
        return view;
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(requireContext(), cartList, new CartAdapter.OnCartActionListener() {
            @Override
            public void onIncrease(CartItem item) {
                item.setQuantity(item.getQuantity() + 1);
                updateTotal();
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDecrease(CartItem item) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    updateTotal();
                    cartAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onDelete(CartItem item) {
                cartList.remove(item);
                updateTotal();
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemCheckedChanged(CartItem item, boolean isChecked) {
                item.setSelected(isChecked);
                updateTotal();
            }
        });

        recyclerCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCart.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        // Xử lý chọn tất cả
        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CartItem item : cartList) {
                item.setSelected(isChecked);
            }
            cartAdapter.notifyDataSetChanged();
            updateTotal();
        });

        // Nút thanh toán
        btnCheckout.setOnClickListener(v -> {
            List<CartItem> selectedItems = new ArrayList<>();
            for (CartItem item : cartList) {
                if (item.isSelected()) selectedItems.add(item);
            }

            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn sản phẩm!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Đã chọn " + selectedItems.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
                // TODO: Chuyển sang màn hình thanh toán
                Intent intent = new Intent(requireContext(), PayFragment.class);
                startActivity(intent);
            }
        });
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartList) {
            if (item.isSelected()) {
                total += item.getPrice() * item.getQuantity();
            }
        }

        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f₫", total));
    }

    private void loadDummyCart() {
        cartList.clear();
        cartList.add(new CartItem("1", "Ghế sofa", "https://via.placeholder.com/150", 5000000, 1));
        cartList.add(new CartItem("2", "Bàn ăn", "https://via.placeholder.com/150", 3500000, 2));
        cartAdapter.notifyDataSetChanged();
        updateTotal();
    }
}
