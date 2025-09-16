package com.example.datn_md02.Product;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Adapter.ReviewAdapter;
import com.example.datn_md02.Adapter.VariantAdapter;
import com.example.datn_md02.Cart.CartActivity;
import com.example.datn_md02.Model.Cart;
import com.example.datn_md02.Model.Product;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.Model.Variant;
import com.example.datn_md02.Model.VariantDisplay;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class ProductDetailActivity extends AppCompatActivity {

    private Product product;
    private ImageView imgProduct, btnBack, btnCart, btnIncrease, btnDecrease;
    private Button btnAddToCart;
    private TextView tvName, tvPrice, tvQuantity, tvTotal, tvDescription, tvCreated, tvRating, tvCartBadge;
    private RecyclerView recyclerViewVariant, recyclerReviews;
    private VariantAdapter variantAdapter;
    private ReviewAdapter reviewAdapter;
    private List<VariantDisplay> variantDisplayList = new ArrayList<>();

    private double unitPrice = 0.0;
    private int quantity = 1;
    private String currentImageUrl;
    private VariantDisplay selectedVariant = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initView();

        product = (Product) getIntent().getSerializableExtra("product");
        if (product != null) {
            showProductDetails();
            loadReviewsFromFirebase(product.getProductId());
        }

        setEventHandlers();
        loadCartItemCount();

        btnCart.setOnClickListener(view -> {
            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
            startActivity(intent);
        });
    }

    private void initView() {
        imgProduct = findViewById(R.id.imgProduct);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotal = findViewById(R.id.tvTotal);
        tvDescription = findViewById(R.id.tvDescription);
        tvCreated = findViewById(R.id.tvCreated);
        tvRating = findViewById(R.id.tvRating);
        btnBack = findViewById(R.id.btnBack);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnCart = findViewById(R.id.btnCart);
        tvCartBadge = findViewById(R.id.tvCartBadge);

        recyclerViewVariant = findViewById(R.id.recyclerVariants);
        recyclerViewVariant.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        variantAdapter = new VariantAdapter(variantDisplayList, this::onVariantSelected);
        recyclerViewVariant.setAdapter(variantAdapter);

        recyclerReviews = findViewById(R.id.recyclerReviews);
        reviewAdapter = new ReviewAdapter(this, new ArrayList<>());
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(reviewAdapter);

        // ❌ mặc định disable giỏ khi chưa chọn biến thể
        btnAddToCart.setEnabled(false);
        btnAddToCart.setAlpha(0.5f);
    }

    private void showProductDetails() {
        currentImageUrl = product.getImageUrl();
        unitPrice = product.getPrice();

        Glide.with(this)
                .load(currentImageUrl)
                .placeholder(R.drawable.haha)
                .into(imgProduct);

        tvName.setText(product.getName());
        tvDescription.setText(product.getDescription());
        tvQuantity.setText(String.valueOf(quantity));
        tvPrice.setText(String.format(Locale.getDefault(), "%.0f₫", unitPrice));
        updateTotal();

        if (product.getCreated() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvCreated.setText(sdf.format(product.getCreated()));
        }

        displayVariants(product.getVariants());
    }

    private void displayVariants(Map<String, Map<String, Variant>> variantsMap) {
        variantDisplayList.clear();

        if (variantsMap != null) {
            for (Map.Entry<String, Map<String, Variant>> sizeEntry : variantsMap.entrySet()) {
                String size = sizeEntry.getKey();
                for (Map.Entry<String, Variant> colorEntry : sizeEntry.getValue().entrySet()) {
                    String color = colorEntry.getKey();
                    Variant variant = colorEntry.getValue();
                    variant.setSize(size);
                    variant.setColor(color);
                    variantDisplayList.add(new VariantDisplay(size, color,
                            variant.getPrice(),
                            variant.getQuantity(),
                            variant.getImageUrl()));
                }
            }
        }

        variantAdapter.notifyDataSetChanged();
    }

    private void onVariantSelected(VariantDisplay variant) {
        selectedVariant = variant;
        unitPrice = variant.price;
        currentImageUrl = (variant.imageUrl != null && !variant.imageUrl.isEmpty())
                ? variant.imageUrl
                : product.getImageUrl();

        tvPrice.setText(String.format(Locale.getDefault(), "%.0f₫", unitPrice));
        updateTotal();

        Glide.with(this)
                .load(currentImageUrl)
                .placeholder(R.drawable.haha)
                .into(imgProduct);

        // ✅ nếu hết hàng
        if (variant.getQuantity() <= 0) {
            btnAddToCart.setEnabled(false);
            btnAddToCart.setAlpha(0.5f);
            Toast.makeText(this, "Biến thể này đã hết hàng", Toast.LENGTH_SHORT).show();
        } else {
            btnAddToCart.setEnabled(true);
            btnAddToCart.setAlpha(1f);
        }
    }

    private void setEventHandlers() {
        btnBack.setOnClickListener(v -> finish());

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updateTotal();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateTotal();
            }
        });

        btnAddToCart.setOnClickListener(v -> addToCart());

        imgProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, FullscreenImageActivity.class);
            intent.putExtra("imageUrl", currentImageUrl);
            startActivity(intent);
        });

        tvRating.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, ReviewListActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
    }

    private void loadReviewsFromFirebase(String productId) {
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("reviews").child(productId);

        reviewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double total = 0;
                int count = 0;
                List<Review> reviewList = new ArrayList<>();
                for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                    Review review = reviewSnap.getValue(Review.class);
                    if (review != null && review.getRating() > 0) {
                        total += review.getRating();
                        count++;
                        reviewList.add(review);
                    }
                }

                if (count > 0) {
                    double avg = total / count;
                    tvRating.setText(String.format(Locale.getDefault(), "%.1f/5 (%d đánh giá)", avg, count));
                } else {
                    tvRating.setText("⭐ Chưa có đánh giá");
                }

                reviewAdapter.setData(reviewList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvRating.setText("Không tải được đánh giá");
            }
        });
    }

    private void updateTotal() {
        double total = unitPrice * quantity;
        tvTotal.setText(String.format(Locale.getDefault(), "%.0f₫", total));
    }

    private void addToCart() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ bắt buộc chọn biến thể
        if (selectedVariant == null) {
            Toast.makeText(this, "Vui lòng chọn biến thể", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ nếu hết hàng thì chặn
        if (selectedVariant.getQuantity() <= 0) {
            Toast.makeText(this, "Biến thể này đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child(userId);

        String variantSize = selectedVariant.size;
        String variantColor = selectedVariant.color;

        cartRef.orderByChild("productId").equalTo(product.getProductId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean merged = false;

                        for (DataSnapshot itemSnap : snapshot.getChildren()) {
                            Cart existing = itemSnap.getValue(Cart.class);
                            if (existing == null) continue;

                            boolean sameSize = Objects.equals(existing.getVariantSize(), variantSize);
                            boolean sameColor = Objects.equals(existing.getVariantColor(), variantColor);

                            if (sameSize && sameColor) {
                                int newQty = Math.max(1, existing.getQuantity()) + Math.max(1, quantity);
                                existing.setQuantity(newQty);
                                existing.setImageUrl(currentImageUrl);
                                existing.setPrice(unitPrice);

                                cartRef.child(itemSnap.getKey()).setValue(existing)
                                        .addOnSuccessListener(u -> Toast.makeText(
                                                ProductDetailActivity.this,
                                                "Đã cập nhật số lượng trong giỏ",
                                                Toast.LENGTH_SHORT
                                        ).show())
                                        .addOnFailureListener(e -> Toast.makeText(
                                                ProductDetailActivity.this,
                                                "Lỗi cập nhật giỏ: " + e.getMessage(),
                                                Toast.LENGTH_SHORT
                                        ).show());

                                merged = true;
                                break;
                            }
                        }

                        if (!merged) {
                            String cartId = cartRef.push().getKey();
                            if (cartId == null) {
                                Toast.makeText(ProductDetailActivity.this,
                                        "Không thể tạo cartId", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Cart newItem = new Cart(
                                    cartId,
                                    product.getProductId(),
                                    product.getName(),
                                    currentImageUrl,
                                    Math.max(1, quantity),
                                    unitPrice,
                                    true,
                                    variantSize,
                                    variantColor
                            );

                            cartRef.child(cartId).setValue(newItem)
                                    .addOnSuccessListener(u -> Toast.makeText(
                                            ProductDetailActivity.this,
                                            "Đã thêm vào giỏ hàng",
                                            Toast.LENGTH_SHORT
                                    ).show())
                                    .addOnFailureListener(e -> Toast.makeText(
                                            ProductDetailActivity.this,
                                            "Lỗi khi thêm vào giỏ: " + e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProductDetailActivity.this,
                                "Lỗi giỏ hàng: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCartItemCount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvCartBadge.setVisibility(View.GONE);
            return;
        }

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(user.getUid());
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                if (count > 0) {
                    tvCartBadge.setText(String.valueOf(count));
                    tvCartBadge.setVisibility(View.VISIBLE);
                } else {
                    tvCartBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvCartBadge.setVisibility(View.GONE);
            }
        });
    }

    public static Intent newIntent(Context context, Product product) {
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtra("product", product);
        return intent;
    }
}
