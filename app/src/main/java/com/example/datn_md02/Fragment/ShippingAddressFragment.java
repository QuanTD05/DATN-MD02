package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.datn_md02.Adapter.ShippingAddressAdapter;
import com.example.datn_md02.Model.ShippingAddress;
import com.example.datn_md02.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShippingAddressFragment extends Fragment {

    private LinearLayout addressListLayout;
    private List<ShippingAddress> addressList;
    private DatabaseReference dbRef;
    private ImageView btnBack;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(com.example.datn_md02.R.layout.fragment_shipping_address, container, false);
        addressListLayout = view.findViewById(com.example.datn_md02.R.id.addressListLayout);
        ImageView btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed(); // hoặc requireActivity().onBackPressed();
            }
        });
        // Nút thêm địa chỉ
        FloatingActionButton btnAdd = view.findViewById(com.example.datn_md02.R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(com.example.datn_md02.R.id.main_content, new AddShippingAddressFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        addressList = new ArrayList<>();

        // Lấy UID người dùng hiện tại
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Trỏ đến danh sách địa chỉ của UID đó
        dbRef = FirebaseDatabase.getInstance()
                .getReference("shipping_addresses")
                .child(uid);

        // Lắng nghe dữ liệu
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressList.clear();
                addressListLayout.removeAllViews();

                for (DataSnapshot item : snapshot.getChildren()) {
                    ShippingAddress address = item.getValue(ShippingAddress.class);
                    if (address != null) {
                        addressList.add(address);
                    }
                }

                new ShippingAddressAdapter(requireContext(), addressList, addressListLayout,
                        new ShippingAddressAdapter.OnAddressActionListener() {
                            @Override
                            public void onEdit(ShippingAddress address) {
                                EditShippingAddressFragment fragment = EditShippingAddressFragment.newInstance(address);
                                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.main_content, fragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }

                            @Override
                            public void onDelete(ShippingAddress address) {
                                dbRef.child(address.getId()).removeValue()
                                        .addOnSuccessListener(unused -> Toast.makeText(requireContext(),
                                                "Đã xóa: " + address.getName(), Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(requireContext(),
                                                "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }, uid); // ✅ Truyền thêm uid cho logic đặt mặc định
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
