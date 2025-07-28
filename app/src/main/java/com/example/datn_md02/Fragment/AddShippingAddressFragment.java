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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class AddShippingAddressFragment extends Fragment {

    private EditText edtFullName, edtPhone, edtProvince, edtDistrict, edtWard, edtStreet;
    private Button btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(com.example.datn_md02.R.layout.fragment_edit_address, container, false);

        edtFullName = view.findViewById(com.example.datn_md02.R.id.edtFullName);
        edtPhone = view.findViewById(com.example.datn_md02.R.id.edtPhone);
        edtProvince = view.findViewById(com.example.datn_md02.R.id.edtProvince);
        edtDistrict = view.findViewById(com.example.datn_md02.R.id.edtDistrict);
        edtWard = view.findViewById(com.example.datn_md02.R.id.edtWard);
        edtStreet = view.findViewById(com.example.datn_md02.R.id.edtStreet);
        btnSave = view.findViewById(R.id.btnSave);

        btnSave.setText("THÊM ĐỊA CHỈ");
        btnSave.setOnClickListener(v -> saveToFirebase());

        return view;
    }

    private void saveToFirebase() {
        String name = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String province = edtProvince.getText().toString().trim();
        String district = edtDistrict.getText().toString().trim();
        String ward = edtWard.getText().toString().trim();
        String street = edtStreet.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(province) || TextUtils.isEmpty(district) ||
                TextUtils.isEmpty(ward) || TextUtils.isEmpty(street)) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("shipping_addresses")
                .child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFirstAddress = !snapshot.exists(); // Nếu chưa có địa chỉ nào thì là địa chỉ mặc định
                String addressId = UUID.randomUUID().toString();

                ShippingAddress newAddress = new ShippingAddress(
                        addressId,
                        name,
                        phone,
                        street,
                        ward,
                        district,
                        province,
                        isFirstAddress
                );

                userRef.child(addressId).setValue(newAddress)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã thêm địa chỉ", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            } else {
                                Toast.makeText(getContext(), "Thêm địa chỉ thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi truy vấn: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
