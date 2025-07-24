package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.datn_md02.Model.ShippingAddress;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditShippingAddressFragment extends Fragment {

    private EditText edtFullName, edtPhone, edtProvince, edtDistrict, edtWard, edtStreet;
    private Button btnSave;
    private ShippingAddress currentAddress;

    public static EditShippingAddressFragment newInstance(ShippingAddress address) {
        EditShippingAddressFragment fragment = new EditShippingAddressFragment();
        Bundle args = new Bundle();
        args.putSerializable("address", address);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_address, container, false);

        // Ánh xạ view
        edtFullName = view.findViewById(R.id.edtFullName);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtProvince = view.findViewById(R.id.edtProvince);
        edtDistrict = view.findViewById(R.id.edtDistrict);
        edtWard = view.findViewById(R.id.edtWard);
        edtStreet = view.findViewById(R.id.edtStreet);
        btnSave = view.findViewById(R.id.btnSave);

        // Lấy địa chỉ từ Bundle
        if (getArguments() != null) {
            currentAddress = (ShippingAddress) getArguments().getSerializable("address");
            if (currentAddress != null) {
                edtFullName.setText(currentAddress.getName());
                edtPhone.setText(currentAddress.getPhone());
                edtProvince.setText(currentAddress.getCity());
                edtDistrict.setText(currentAddress.getDistrict());
                edtWard.setText(currentAddress.getWard());
                edtStreet.setText(currentAddress.getStreet());
            }
        }

        btnSave.setOnClickListener(v -> updateAddress());

        return view;
    }

    private void updateAddress() {
        String name = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String province = edtProvince.getText().toString().trim();
        String district = edtDistrict.getText().toString().trim();
        String ward = edtWard.getText().toString().trim();
        String street = edtStreet.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(province)
                || TextUtils.isEmpty(district) || TextUtils.isEmpty(ward) || TextUtils.isEmpty(street)) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("shipping_addresses")
                .child(uid)
                .child(currentAddress.getId());

        ShippingAddress updated = new ShippingAddress(
                currentAddress.getId(),
                name,
                phone,
                street,
                district,
                ward,
                province,
                currentAddress.isDefault() // giữ nguyên default
        );

        ref.setValue(updated)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
