package com.example.datn_md02.Fragment;

import android.app.AlertDialog;
import android.content.Context;
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
import com.pusher.pushnotifications.PushNotifications;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView btnLogout, imgAvatar;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private ValueEventListener userListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        imgAvatar = view.findViewById(R.id.imgAvatar);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            tvEmail.setText(firebaseUser.getEmail());

            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Kiểm tra fragment đã attach chưa
                    if (!isAdded()) return;

                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String imageUrl = snapshot.child("avatar").getValue(String.class);

                    tvName.setText((fullName != null && !fullName.isEmpty()) ? fullName : "Người dùng");

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_user)
                                .circleCrop()
                                .into(imgAvatar);
                    } else {
                        imgAvatar.setImageResource(R.drawable.ic_user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (!isAdded()) return;
                    tvName.setText("Không thể tải tên");
                    Log.e("ProfileFragment", "Lỗi khi tải tên người dùng: " + error.getMessage());
                }
            };
            userRef.addListenerForSingleValueEvent(userListener);
        }

        btnLogout.setOnClickListener(v -> {
            Context ctx = getContext();
            if (ctx == null) return;

            new AlertDialog.Builder(ctx)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        PushNotifications.clearDeviceInterests();
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(ctx, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ctx, StartActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        setClickNavigation(view, R.id.layoutReview, new MyReviewFragment());
        setClickNavigation(view, R.id.layoutShippingAddress, new ShippingAddressFragment());
        setClickNavigation(view, R.id.layoutBankAccount, new BankAccountFragment());
        setClickNavigation(view, R.id.Oderhistory, new OrderHistoryFragment());
        setClickNavigation(view, R.id.layoutSettings, new UserSettingsFragment());

        return view;
    }

    private void setClickNavigation(View parent, int layoutId, Fragment fragment) {
        LinearLayout layout = parent.findViewById(layoutId);
        layout.setOnClickListener(v -> {
            if (!isAdded()) return;
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listener để tránh callback sau khi fragment bị hủy
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }
}
