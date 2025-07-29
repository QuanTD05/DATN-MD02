package com.example.datn_md02;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Adapter.ReviewImagePreviewAdapter;
import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.Model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.*;

public class ReviewActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_CODE = 1001;

    private ImageView imgProduct;
    private TextView tvProductName, tvVariant;
    private RatingBar ratingBar;
    private EditText edtComment;
    private Button btnSubmit, btnChooseImages;
    private RecyclerView recyclerSelectedImages;

    private CartItem cartItem;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private final List<Uri> selectedImageUris = new ArrayList<>();
    private ReviewImagePreviewAdapter imagePreviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        imgProduct = findViewById(R.id.imgProduct);
        tvProductName = findViewById(R.id.tvProductName);
        tvVariant = findViewById(R.id.tvVariant);
        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtComment);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnChooseImages = findViewById(R.id.btnChooseImages);
        recyclerSelectedImages = findViewById(R.id.recyclerSelectedImages);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("reviews");
        storageReference = FirebaseStorage.getInstance().getReference("review_images");

        // Lấy CartItem từ intent
        Object obj = getIntent().getSerializableExtra("items");
        if (obj instanceof ArrayList) {
            ArrayList<CartItem> cartItems = (ArrayList<CartItem>) obj;
            if (!cartItems.isEmpty()) {
                cartItem = cartItems.get(0);
            }
        }

        if (cartItem == null || cartItem.getProductId() == null) {
            Toast.makeText(this, "Không nhận được dữ liệu sản phẩm!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Hiển thị dữ liệu sản phẩm
        tvProductName.setText(cartItem.getProductName());
        String variantText = cartItem.getVariant() != null ? cartItem.getVariant() : "Không có";
        tvVariant.setText("Phân loại: " + variantText);
        Glide.with(this).load(cartItem.getProductImage()).into(imgProduct);

        // Adapter hiển thị ảnh đã chọn
        imagePreviewAdapter = new ReviewImagePreviewAdapter(this, selectedImageUris);
        recyclerSelectedImages.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerSelectedImages.setAdapter(imagePreviewAdapter);

        btnChooseImages.setOnClickListener(v -> pickImages());
        btnSubmit.setOnClickListener(v -> {
            if (selectedImageUris.isEmpty()) {
                loadUserAndSubmitReview(new ArrayList<>());
            } else {
                uploadImagesAndThenSubmit();
            }
        });
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGES_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_CODE && resultCode == RESULT_OK) {
            selectedImageUris.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }
            imagePreviewAdapter.notifyDataSetChanged();
        }
    }

    private void uploadImagesAndThenSubmit() {
        List<String> uploadedUrls = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imgRef = storageReference.child(fileName);
            imgRef.putFile(uri)
                    .continueWithTask(task -> imgRef.getDownloadUrl())
                    .addOnSuccessListener(downloadUri -> {
                        uploadedUrls.add(downloadUri.toString());
                        if (uploadedUrls.size() == selectedImageUris.size()) {
                            loadUserAndSubmitReview(uploadedUrls);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void loadUserAndSubmitReview(List<String> imageUrls) {
        String uid = firebaseUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("fullName").getValue(String.class);
                String avatar = snapshot.child("avatar").getValue(String.class);
                submitReviewWithUserInfo(imageUrls,
                        name != null ? name : "Người dùng",
                        avatar != null ? avatar : "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReviewActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReviewWithUserInfo(List<String> imageUrls, String userName, String userAvatar) {
        float rating = ratingBar.getRating();
        String comment = edtComment.getText().toString().trim();

        if (rating == 0 || comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng đánh giá và viết nhận xét!", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewId = databaseReference.push().getKey();
        if (reviewId == null) {
            Toast.makeText(this, "Không tạo được mã đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        Review review = new Review(
                firebaseUser.getUid(),
                userName,
                userAvatar,
                comment,
                rating,
                System.currentTimeMillis()
        );
        review.setProductName(cartItem.getProductName());
        review.setProductImage(cartItem.getProductImage());
        review.setImageUrls(imageUrls);
        review.setVariantColor(cartItem.getVariantColor());
        review.setVariantSize(cartItem.getVariantSize());

        databaseReference.child(cartItem.getProductId()).child(reviewId)
                .setValue(review)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
