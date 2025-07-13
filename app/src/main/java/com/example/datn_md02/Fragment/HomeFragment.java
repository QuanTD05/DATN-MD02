package com.example.datn_md02.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.PopularProductAdapter;
import com.example.datn_md02.Model.Product;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.Model.Variant;

import com.example.datn_md02.Product.AllProductActivity;
import com.example.datn_md02.Product.ProductDetailActivity;
import com.example.datn_md02.R;

import java.util.*;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerPopular;
    private PopularProductAdapter adapter;
    private List<Product> popularList;
    private DatabaseReference productRef;
    private static final String TAG = "HomeFragment";

    private TextView tvAll;
    private boolean isSearchTriggered = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // RecyclerView phổ biến
        recyclerPopular = view.findViewById(R.id.recyclerPopular);
        recyclerPopular.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularList = new ArrayList<>();
        adapter = new PopularProductAdapter(getContext(), popularList, product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
        recyclerPopular.setAdapter(adapter);

        // Button "Tất cả"
        tvAll = view.findViewById(R.id.tvAll);
        tvAll.setOnClickListener(v -> startActivity(new Intent(getContext(), AllProductActivity.class)));

        // Firebase
        productRef = FirebaseDatabase.getInstance().getReference("product");
        loadPopularProducts();

        // Tìm kiếm
        setupSearchAndCategory(view);

        return view;
    }

    private void setupSearchAndCategory(View view) {
        EditText edtSearch = view.findViewById(R.id.edtSearch);

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (isSearchTriggered) return true;

            String keyword = edtSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                isSearchTriggered = true;
                openAllProductWithSearch(keyword);
            }
            return true;
        });

        view.findViewById(R.id.itemCategoryBan).setOnClickListener(v -> openAllProductWithSearch("bàn"));
        view.findViewById(R.id.itemCategoryGhe).setOnClickListener(v -> openAllProductWithSearch("ghế"));
        view.findViewById(R.id.itemCategoryTu).setOnClickListener(v -> openAllProductWithSearch("tủ"));
        view.findViewById(R.id.itemCategoryGiuong).setOnClickListener(v -> openAllProductWithSearch("giường"));
        view.findViewById(R.id.itemCategoryKe).setOnClickListener(v -> openAllProductWithSearch("kệ"));
    }

    private void openAllProductWithSearch(String keyword) {
        Log.d(TAG, "🔍 Mở AllProductActivity với từ khóa: " + keyword);
        Intent intent = new Intent(getContext(), AllProductActivity.class);
        intent.putExtra("keyword", keyword.toLowerCase(Locale.ROOT));
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        isSearchTriggered = false;
    }

    private void loadPopularProducts() {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                popularList.clear();

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

                        // ✅ Fix lỗi "HashMap to Long"
                        Object createdObj = data.child("created").getValue();
                        if (createdObj instanceof Long) {
                            product.setCreated(new Date((Long) createdObj));
                        } else {
                            Log.w(TAG, "⚠️ Trường 'created' không phải kiểu Long, bỏ qua: " + createdObj);
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

                        // 👉 Thêm vào danh sách phổ biến
                        popularList.add(product);
                        Log.d(TAG, "🔥 Thêm vào phổ biến: " + product.getName());

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Lỗi parse sản phẩm: " + e.getMessage(), e);
                    }
                }

                adapter.notifyDataSetChanged();
                Log.d(TAG, "✅ Đã tải " + popularList.size() + " sản phẩm phổ biến");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Firebase Error: " + error.getMessage());
            }
        });
    }
}
