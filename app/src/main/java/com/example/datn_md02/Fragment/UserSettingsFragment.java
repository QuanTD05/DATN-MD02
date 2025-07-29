package com.example.datn_md02.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserSettingsFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone;
    private ImageView avatar, btnBack;
    private LinearLayout rowName, rowPhone;

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();
         btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().onBackPressed(); // quay lại màn hình trước
        });
        // Bind views
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        avatar = view.findViewById(R.id.imgAvatar);
        rowName = view.findViewById(R.id.rowName);
        rowPhone = view.findViewById(R.id.rowPhone);

        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            tvEmail.setText(firebaseUser.getEmail());

            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String currentName = snapshot.child("fullName").getValue(String.class);
                    String currentPhone = snapshot.child("phone").getValue(String.class);
                    String imageUrl = snapshot.child("avatar").getValue(String.class);

                    if (currentName != null) tvName.setText("Tên\n" + currentName);
                    if (currentPhone != null) tvPhone.setText("Số điện thoại\n" + currentPhone);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_user)
                                .circleCrop()
                                .into(avatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Không thể tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        }

        rowName.setOnClickListener(v -> showEditDialog("Tên", tvName.getText().toString().replace("Tên\n", ""), "fullName", tvName, "Tên"));
        rowPhone.setOnClickListener(v -> showEditDialog("Số điện thoại", tvPhone.getText().toString().replace("Số điện thoại\n", ""), "phone", tvPhone, "Số điện thoại"));

        avatar.setOnClickListener(v -> openImagePicker());
        setupImagePickerLauncher();

        return view;
    }

    private void showEditDialog(String title, String currentValue, String fieldKey, @Nullable TextView targetView, @Nullable String labelPrefix) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chỉnh sửa " + title);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentValue);
        input.setSelection(currentValue.length());
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);

        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            if (!newValue.isEmpty() && firebaseUser != null) {
                userRef.child(fieldKey).setValue(newValue);

                if (targetView != null && labelPrefix != null) {
                    targetView.setText(labelPrefix + "\n" + newValue);
                }

                Toast.makeText(getContext(), title + " đã được cập nhật", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadImageToFirebase(imageUri);
                    }
                }
        );
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (firebaseUser == null || imageUri == null) return;

        String uid = firebaseUser.getUid();
        StorageReference avatarRef = storageRef.child("avatars/" + uid + ".jpg");

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    userRef.child("avatar").setValue(downloadUrl);
                    Glide.with(requireContext())
                            .load(downloadUrl)
                            .placeholder(R.drawable.ic_user)
                            .circleCrop()
                            .into(avatar);
                    Toast.makeText(getContext(), "Ảnh đại diện đã cập nhật", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Tải ảnh thất bại", Toast.LENGTH_SHORT).show());
    }
}
