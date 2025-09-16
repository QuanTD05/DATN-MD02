package com.example.datn_md02.Fragment;

import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.Collections;
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

    // Filter & Sort
    private Spinner spinnerSortPrice;
    private LinearLayout layoutFilterPrice;

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

        new TabLayoutMediator(bannerIndicator, bannerViewPager,
                (tab, position) -> {}).attach();

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

        // Filter & Sort
        spinnerSortPrice = view.findViewById(R.id.spinnerSortPrice);
        layoutFilterPrice = view.findViewById(R.id.layoutFilterPrice);
        setupSortSpinner();
        setupFilterClick();

        // Load banners + products
        loadBannerFromFirebase();
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

                if (!bannerList.isEmpty()) {
                    bannerCurrentIndex = 0;
                    bannerViewPager.setCurrentItem(0, false);
                    startAutoSlide();
                } else {
                    bannerHandler.removeCallbacks(autoSlideRunnable);
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
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProductList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = parseProduct(data);
                    if (product != null) {
                        allProductList.add(product);
                    }
                }

                // sort newest
                Collections.sort(allProductList, (p1, p2) -> {
                    if (p1.getCreated() == null || p2.getCreated() == null) return 0;
                    return p2.getCreated().compareTo(p1.getCreated());
                });

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

            // 🔥 Lấy created hoặc updatedAt thay vì luôn new Date()
            Object createdObj = data.child("created").getValue();
            if (createdObj == null) {
                createdObj = data.child("updatedAt").getValue(); // fallback
            }

            if (createdObj instanceof Long) {
                product.setCreated(new Date((Long) createdObj));
            } else if (createdObj instanceof Double) {
                product.setCreated(new Date(((Double) createdObj).longValue()));
            } else {
                product.setCreated(null); // để null nếu không có
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
        TextView edtSearch = view.findViewById(R.id.edtSearch);
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
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId);
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                updateCartBadge(count);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ===== FILTER & SORT =====

    private void setupSortSpinner() {
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Mặc định", "Giá tăng dần", "Giá giảm dần"}
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortPrice.setAdapter(sortAdapter);

        spinnerSortPrice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    sortProductByPrice(true);
                } else if (position == 2) {
                    sortProductByPrice(false);
                } else {
                    resetList();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupFilterClick() {
        layoutFilterPrice.setOnClickListener(v -> showFilterPriceDialog());
    }

    private void showFilterPriceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter_price, null);
        builder.setView(dialogView);

        ListView listView = dialogView.findViewById(R.id.listPriceRange);

        String[] priceRanges = {
                "100.000 - 500.000",
                "500.000 - 1.000.000",
                "1.000.000 - 3.000.000",
                "3.000.000 - 5.000.000",
                "5.000.000 - 10.000.000",
                "Trên 10.000.000"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                priceRanges
        );
        listView.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            double min = 0, max = Double.MAX_VALUE;

            switch (position) {
                case 0: min = 100000; max = 500000; break;
                case 1: min = 500000; max = 1000000; break;
                case 2: min = 1000000; max = 3000000; break;
                case 3: min = 3000000; max = 5000000; break;
                case 4: min = 5000000; max = 10000000; break;
                case 5: min = 10000000; max = Double.MAX_VALUE; break;
            }

            filterProductByPrice(min, max);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void filterProductByPrice(double min, double max) {
        filteredList.clear();
        for (Product p : allProductList) {
            double price = p.getMinPrice();
            if (price >= min && price <= max) {
                filteredList.add(p);
            }
        }
        allProductAdapter.notifyDataSetChanged();
    }

    private void sortProductByPrice(boolean ascending) {
        Collections.sort(filteredList, (p1, p2) -> {
            double price1 = p1.getMinPrice();
            double price2 = p2.getMinPrice();
            return ascending ? Double.compare(price1, price2) : Double.compare(price2, price1);
        });
        allProductAdapter.notifyDataSetChanged();
    }

    private void resetList() {
        filteredList.clear();
        filteredList.addAll(allProductList);
        allProductAdapter.notifyDataSetChanged();
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
