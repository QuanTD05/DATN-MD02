package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.datn_md02.Adapter.ProductAdapter;
import com.example.datn_md02.Cart.CartActivity;
import com.example.datn_md02.Model.*;
import com.example.datn_md02.Product.AllProductActivity;
import com.example.datn_md02.Product.ProductDetailActivity;
import com.example.datn_md02.R;
import com.google.firebase.database.*;

import java.util.*;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerAllProduct;
    private ProductAdapter allProductAdapter;
    private List<Product> allProductList;
    private List<Product> filteredList;
    private DatabaseReference productRef;

    private TextView tvAll;
    private EditText edtSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        productRef = FirebaseDatabase.getInstance().getReference("product");

        // Setup RecyclerView
        recyclerAllProduct = view.findViewById(R.id.recyclerAllProduct);
        recyclerAllProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));

        allProductList = new ArrayList<>();
        filteredList = new ArrayList<>();

        allProductAdapter = new ProductAdapter(getContext(), filteredList, product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
        recyclerAllProduct.setAdapter(allProductAdapter);

        // Giỏ hàng
        ImageView ivCart = view.findViewById(R.id.ic_cart);
        ivCart.setOnClickListener(v -> startActivity(new Intent(getContext(), CartActivity.class)));

        // "Tất cả" chuyển sang AllProductActivity
        tvAll = view.findViewById(R.id.tvAll);
        tvAll.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AllProductActivity.class);
            intent.putExtra("categoryName", "sản phẩm");
            startActivity(intent);
        });

        // Load dữ liệu ban đầu
        loadAllProducts();

        // Tìm kiếm và danh mục
        setupSearchAndCategory(view);

        return view;
    }

    private void loadAllProducts() {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProductList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = parseProduct(data);
                    if (product != null) {
                        allProductList.add(product);
                    }
                }
                // Ban đầu hiển thị tất cả
                filteredList.clear();
                filteredList.addAll(allProductList);
                allProductAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Firebase Error: " + error.getMessage());
            }
        });
    }

    private Product parseProduct(DataSnapshot data) {
        try {
            Product product = new Product();

            String productId = data.child("productId").getValue(String.class);
            if (productId == null) productId = data.getKey();
            product.setProductId(productId);

            product.setName(data.child("name").getValue(String.class));
            product.setImageUrl(data.child("imageUrl").getValue(String.class));
            product.setDescription(data.child("description").getValue(String.class));
            product.setCategoryId(data.child("categoryId").getValue(String.class));

            Object createdObj = data.child("created").getValue();
            if (createdObj instanceof Long) {
                product.setCreated(new Date((Long) createdObj));
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

            return product;

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi parse sản phẩm: " + e.getMessage(), e);
            return null;
        }
    }

    private void setupSearchAndCategory(View view) {
        edtSearch = view.findViewById(R.id.edtSearch);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim().toLowerCase(Locale.ROOT);
                filteredList.clear();
                if (keyword.isEmpty()) {
                    filteredList.addAll(allProductList);
                } else {
                    for (Product p : allProductList) {
                        if (p.getName() != null && p.getName().toLowerCase().contains(keyword)) {
                            filteredList.add(p);
                        }
                    }
                }
                allProductAdapter.notifyDataSetChanged();
            }
        });

        view.findViewById(R.id.itemCategoryBan).setOnClickListener(v -> openAllProductWithCategory("ban", "bàn"));
        view.findViewById(R.id.itemCategoryGhe).setOnClickListener(v -> openAllProductWithCategory("ghe", "ghế"));
        view.findViewById(R.id.itemCategoryTu).setOnClickListener(v -> openAllProductWithCategory("tu", "tủ"));
        view.findViewById(R.id.itemCategoryGiuong).setOnClickListener(v -> openAllProductWithCategory("giuong", "giường"));
        view.findViewById(R.id.itemCategoryKe).setOnClickListener(v -> openAllProductWithCategory("ke", "kệ"));
    }

    private void openAllProductWithCategory(String categoryId, String categoryName) {
        Log.d(TAG, "📂 Mở AllProductActivity với loại: " + categoryName);
        Intent intent = new Intent(getContext(), AllProductActivity.class);
        intent.putExtra("categoryId", categoryId);
        intent.putExtra("categoryName", categoryName);
        startActivity(intent);
    }
}
