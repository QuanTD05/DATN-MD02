package com.example.datn_md02.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.datn_md02.R;
import com.example.datn_md02.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView btnLogout, imgAvatar;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ View
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        imgAvatar = view.findViewById(R.id.imgAvatar); // Thêm avatar

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            tvEmail.setText(firebaseUser.getEmail());

            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String imageUrl = snapshot.child("avatar").getValue(String.class);

                    if (fullName != null && !fullName.isEmpty()) {
                        tvName.setText(fullName);
                    } else {
                        tvName.setText("Người dùng");
                    }

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_user)
                                .circleCrop()
                                .into(imgAvatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    tvName.setText("Không thể tải tên");
                    Log.e("ProfileFragment", "Lỗi khi tải tên người dùng: " + error.getMessage());
                }
            });
        }

        // Nút Logout
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        auth.signOut();
                        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getContext(), StartActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });



        // Chuyển đến MyReviewFragment
        LinearLayout layoutReview = view.findViewById(R.id.layoutReview);
        layoutReview.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.main_content, new MyReviewFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Chuyển đến ShippingAddressFragment
        LinearLayout layoutShippingAddress = view.findViewById(R.id.layoutShippingAddress);
        layoutShippingAddress.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.main_content, new ShippingAddressFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Chuyển đến BankAccountFragment
        LinearLayout layoutBankAccount = view.findViewById(R.id.layoutBankAccount);
        layoutBankAccount.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.main_content, new BankAccountFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Chuyển đến UserSettingsFragment
        LinearLayout layoutSettings = view.findViewById(R.id.layoutSettings);
        layoutSettings.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content, new UserSettingsFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
