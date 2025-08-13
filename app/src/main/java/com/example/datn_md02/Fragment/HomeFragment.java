package com.example.datn_md02.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.datn_md02.Adapter.BannerAdapter;
import com.example.datn_md02.Adapter.ProductAdapter;
import com.example.datn_md02.Cart.CartActivity;
import com.example.datn_md02.Model.Product;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.Model.Variant;
import com.example.datn_md02.Product.AllProductActivity;
import com.example.datn_md02.Product.ProductDetailActivity;
import com.example.datn_md02.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // Product list
    private RecyclerView recyclerAllProduct;
    private ProductAdapter allProductAdapter;
    private List<Product> allProductList;
    private List<Product> filteredList;
    private DatabaseReference productRef;

    private TextView tvAll;
    private EditText edtSearch;
    private TextView tvCartBadge;

    // Banner
    private ViewPager2 bannerViewPager;
    private TabLayout bannerIndicator;
    private BannerAdapter bannerAdapter;
    private List<String> bannerList;
    private Handler bannerHandler;
    private Runnable autoSlideRunnable;
    private final int bannerIntervalMs = 5000;
    private int bannerCurrentIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        productRef = FirebaseDatabase.getInstance().getReference("product");

        // RecyclerView sản phẩm
        recyclerAllProduct = view.findViewById(R.id.recyclerAllProduct);
        recyclerAllProduct.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        allProductList = new ArrayList<>();
        filteredList = new ArrayList<>();

        allProductAdapter = new ProductAdapter(requireContext(), filteredList, product -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
        recyclerAllProduct.setAdapter(allProductAdapter);

        // Giỏ hàng
        ImageView ivCart = view.findViewById(R.id.ic_cart);
        tvCartBadge = view.findViewById(R.id.cart_badge);
        ivCart.setOnClickListener(v -> startActivity(new Intent(requireContext(), CartActivity.class)));

        // "Tất cả"
        tvAll = view.findViewById(R.id.tvAll);
        tvAll.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AllProductActivity.class);
            intent.putExtra("categoryName", "sản phẩm");
            startActivity(intent);
        });

        // Banner setup
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        bannerIndicator = view.findViewById(R.id.bannerIndicator);
        bannerList = new ArrayList<>();
        bannerAdapter = new BannerAdapter(requireContext(), bannerList);
        bannerViewPager.setAdapter(bannerAdapter);

        // TabLayout indicator
        new TabLayoutMediator(bannerIndicator, bannerViewPager,
                (tab, position) -> {
                    // no action needed, just indicator
                }).attach();

        // Handler & runnable
        bannerHandler = new Handler(Looper.getMainLooper());
        autoSlideRunnable = () -> {
            if (!bannerList.isEmpty() && bannerAdapter.getItemCount() > 0) {
                bannerCurrentIndex = (bannerCurrentIndex + 1) % bannerAdapter.getItemCount();
                bannerViewPager.setCurrentItem(bannerCurrentIndex, true);
                bannerHandler.postDelayed(autoSlideRunnable, bannerIntervalMs);
            }
        };

        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bannerCurrentIndex = position;
                super.onPageSelected(position);
            }
        });

        // Load banners from Firebase
        loadBannerFromFirebase();

        // Load sản phẩm
        loadAllProducts();

        // Search + Category
        setupSearchAndCategory(view);

        // Cập nhật badge giỏ hàng
        updateCartBadgeFromFirebase();

        return view;
    }

    private void loadBannerFromFirebase() {
        DatabaseReference bannerRef = FirebaseDatabase.getInstance().getReference("banners");
        bannerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bannerList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String url = null;
                    Object value = child.getValue();

                    if (value instanceof String) {
                        url = ((String) value).trim();
                    } else if (value instanceof Map) {
                        Object imageUrlObj = ((Map<?, ?>) value).get("imageUrl");
                        if (imageUrlObj instanceof String) {
                            url = ((String) imageUrlObj).trim();
                        }
                    }

                    if (url != null && !url.isEmpty()) {
                        bannerList.add(url);
                    }
                }
                bannerAdapter.notifyDataSetChanged();

                if (bannerList.isEmpty()) {
                    bannerHandler.removeCallbacks(autoSlideRunnable);
                } else {
                    bannerCurrentIndex = 0;
                    bannerViewPager.setCurrentItem(0, false);
                    startAutoSlide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi load banner: " + error.getMessage());
            }
        });
    }

    private void startAutoSlide() {
        bannerHandler.removeCallbacks(autoSlideRunnable);
        bannerHandler.postDelayed(autoSlideRunnable, bannerIntervalMs);
    }

    private void stopAutoSlide() {
        if (bannerHandler != null) bannerHandler.removeCallbacks(autoSlideRunnable);
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
                filteredList.clear();
                filteredList.addAll(allProductList);
                allProductAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase Error: " + error.getMessage());
            }
        });
    }

    private Product parseProduct(DataSnapshot data) {
        try {
            Product product = new Product();

            String productId = safeGetString(data.child("productId"));
            if (productId == null) productId = data.getKey();
            product.setProductId(productId);

            product.setName(safeGetString(data.child("name")));
            product.setImageUrl(safeGetString(data.child("imageUrl")));
            product.setDescription(safeGetString(data.child("description")));
            product.setCategoryId(safeGetString(data.child("categoryId")));

            Object createdObj = data.child("created").getValue();
            if (createdObj instanceof Long) {
                product.setCreated(new Date((Long) createdObj));
            }

            // Variants
            Map<String, Map<String, Variant>> variantsMap = new HashMap<>();
            DataSnapshot variantsSnap = data.child("variants");
            if (variantsSnap.exists()) {
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
            Log.e(TAG, "Lỗi parse sản phẩm: " + e.getMessage(), e);
            return null;
        }
    }

    private String safeGetString(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value instanceof String) {
            return ((String) value).trim();
        }
        return null;
    }

    private void setupSearchAndCategory(View view) {
        edtSearch = view.findViewById(R.id.edtSearch);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

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

        view.findViewById(R.id.itemCategoryBan)
                .setOnClickListener(v -> openAllProductWithCategory("ban", "bàn"));
        view.findViewById(R.id.itemCategoryGhe)
                .setOnClickListener(v -> openAllProductWithCategory("ghe", "ghế"));
        view.findViewById(R.id.itemCategoryTu)
                .setOnClickListener(v -> openAllProductWithCategory("tu", "tủ"));
        view.findViewById(R.id.itemCategoryGiuong)
                .setOnClickListener(v -> openAllProductWithCategory("giuong", "giường"));
        view.findViewById(R.id.itemCategoryKe)
                .setOnClickListener(v -> openAllProductWithCategory("ke", "kệ"));
    }

    private void openAllProductWithCategory(String categoryId, String categoryName) {
        Log.d(TAG, "Mở AllProductActivity với loại: " + categoryName);
        Intent intent = new Intent(requireContext(), AllProductActivity.class);
        intent.putExtra("categoryId", categoryId);
        intent.putExtra("categoryName", categoryName);
        startActivity(intent);
    }

    private void updateCartBadge(int cartCount) {
        if (tvCartBadge == null) return;

        if (cartCount > 0) {
            tvCartBadge.setText(String.valueOf(cartCount));
            tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void updateCartBadgeFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            updateCartBadge(0);
            return;
        }

        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child(userId);

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                updateCartBadge(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi load giỏ hàng: " + error.getMessage());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoSlide();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerList != null && !bannerList.isEmpty()) {
            startAutoSlide();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoSlide();
    }
}
