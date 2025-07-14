package com.example.datn_md02.Product;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.ProductAdapter;
import com.example.datn_md02.Model.Product;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.Model.Variant;
import com.example.datn_md02.R;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AllProductActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Product> productList;
    private ProductAdapter adapter;
    private DatabaseReference productRef;
    private String keyword;
    private String categoryId;
    private String categoryName;

    private TextView tvTitle;
    private static final String TAG = "AllProductActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_product);

        recyclerView = findViewById(R.id.recyclerViewAll);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        ImageView btnBack = findViewById(R.id.btnBackAll);
        btnBack.setOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tvTitle);

        // Nhận dữ liệu từ Intent
        keyword = getIntent().getStringExtra("keyword");
        if (keyword != null) keyword = keyword.toLowerCase(Locale.ROOT);

        categoryId = getIntent().getStringExtra("categoryId");
        categoryName = getIntent().getStringExtra("categoryName");

        // Cập nhật tiêu đề động
        if (categoryName != null && !categoryName.isEmpty()) {
            tvTitle.setText("Tất cả các loại " + categoryName);
        } else {
            tvTitle.setText("Tất cả nội thất");
        }

        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList, product -> {
            startActivity(ProductDetailActivity.newIntent(this, product));
        });
        recyclerView.setAdapter(adapter);

        productRef = FirebaseDatabase.getInstance().getReference("product");
        loadProducts();
    }

    private void loadProducts() {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Product product = new Product();

                        // ID
                        String productId = data.child("productId").getValue(String.class);
                        if (productId == null) productId = data.getKey();
                        product.setProductId(productId);

                        // Basic
                        product.setName(data.child("name").getValue(String.class));
                        product.setImageUrl(data.child("imageUrl").getValue(String.class));
                        product.setDescription(data.child("description").getValue(String.class));
                        product.setCategoryId(data.child("categoryId").getValue(String.class));

                        // Created
                        Long createdMillis = data.child("created").getValue(Long.class);
                        if (createdMillis != null) {
                            product.setCreated(new Date(createdMillis));
                        }

                        // Variants
                        Map<String, Map<String, Variant>> variantsMap = new HashMap<>();
                        DataSnapshot variantsSnap = data.child("variants");
                        for (DataSnapshot sizeSnap : variantsSnap.getChildren()) {
                            String size = sizeSnap.getKey();
                            Map<String, Variant> colorMap = new HashMap<>();
                            for (DataSnapshot colorSnap : sizeSnap.getChildren()) {
                                String color = colorSnap.getKey();
                                Variant variant = colorSnap.getValue(Variant.class);
                                if (color != null && variant != null) {
                                    colorMap.put(color, variant);
                                }
                            }
                            if (size != null) {
                                variantsMap.put(size, colorMap);
                            }
                        }
                        product.setVariants(variantsMap);

                        // Reviews
                        List<Review> reviewList = new ArrayList<>();
                        DataSnapshot reviewsSnap = data.child("reviews");
                        if (reviewsSnap.exists()) {
                            for (DataSnapshot reviewSnap : reviewsSnap.getChildren()) {
                                Review review = reviewSnap.getValue(Review.class);
                                if (review != null) reviewList.add(review);
                            }
                        }
                        product.setReviews(reviewList);

                        // 👉 Lọc sản phẩm theo categoryId hoặc keyword
                        boolean isMatch = true;

                        if (categoryId != null && !categoryId.isEmpty()) {
                            isMatch = product.getCategoryId() != null &&
                                    product.getCategoryId().equalsIgnoreCase(categoryId);
                        } else if (keyword != null && !keyword.isEmpty()) {
                            String name = product.getName() != null ? product.getName().toLowerCase(Locale.ROOT) : "";
                            String category = product.getCategoryId() != null ? product.getCategoryId().toLowerCase(Locale.ROOT) : "";
                            isMatch = name.contains(keyword) || category.contains(keyword);
                        }

                        if (isMatch) {
                            productList.add(product);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Lỗi parse sản phẩm: " + e.getMessage(), e);
                    }
                }

                adapter.notifyDataSetChanged();
                Log.d(TAG, "✅ Đã tải " + productList.size() + " sản phẩm");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Firebase Error: " + error.getMessage());
            }
        });
    }
}
