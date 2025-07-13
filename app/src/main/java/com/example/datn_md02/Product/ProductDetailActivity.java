package com.example.datn_md02.Product;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Adapter.VariantAdapter;
import com.example.datn_md02.Model.Product;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.Model.Variant;
import com.example.datn_md02.Model.VariantDisplay;
import com.example.datn_md02.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProduct, btnBack, btnCart, btnFavorite, btnIncrease, btnDecrease;
    private TextView tvName, tvPrice, tvDescription, tvRating, tvQuantity, tvTotal, tvCreated;
    private Button btnAddToCart;
    private RecyclerView recyclerVariants;

    private int quantity = 1;
    private double unitPrice = 0.0;
    private Product product;
    private List<VariantDisplay> variantDisplayList = new ArrayList<>();
    private VariantAdapter variantAdapter;
    private String currentImageUrl = "";

    // ✅ THÊM PHƯƠNG THỨC newIntent() để gọi từ nơi khác
    public static Intent newIntent(Context context, Product product) {
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtra("product", product);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();

        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null) {
            Log.e("ProductDetail", "❌ Không nhận được sản phẩm từ Intent");
            finish();
            return;
        }

        showProductDetails();
        setEventHandlers();
    }

    private void initViews() {
        imgProduct = findViewById(R.id.imgProduct);
        btnBack = findViewById(R.id.btnBack);
        btnCart = findViewById(R.id.btnCart);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvRating = findViewById(R.id.tvRating);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotal = findViewById(R.id.tvTotal);
        tvCreated = findViewById(R.id.tvCreated);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        recyclerVariants = findViewById(R.id.recyclerVariants);

        recyclerVariants.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        variantAdapter = new VariantAdapter(this, variantDisplayList, selectedVariant -> {
            unitPrice = selectedVariant.price;
            tvPrice.setText(String.format(Locale.getDefault(), "%.0f₫", unitPrice));
            updateTotal();

            currentImageUrl = selectedVariant.imageUrl;
            Glide.with(ProductDetailActivity.this)
                    .load(currentImageUrl)
                    .placeholder(R.drawable.haha)
                    .into(imgProduct);
        });

        recyclerVariants.setAdapter(variantAdapter);
    }

    private void showProductDetails() {
        currentImageUrl = product.getImageUrl();
        Glide.with(this)
                .load(currentImageUrl)
                .placeholder(R.drawable.haha)
                .into(imgProduct);

        tvName.setText(product.getName());
        tvDescription.setText(product.getDescription());
        tvQuantity.setText(String.valueOf(quantity));

        Variant firstVariant = getFirstVariant(product.getVariants());
        if (firstVariant != null) {
            unitPrice = firstVariant.getPrice();
            tvPrice.setText(String.format(Locale.getDefault(), "%.0f₫", unitPrice));
        } else {
            unitPrice = 0.0;
            tvPrice.setText("N/A");
        }

        updateTotal();
        showCreatedTime(product.getCreated());
        showRating(product.getReviews());
        displayVariants(product.getVariants());
    }

    private void updateTotal() {
        double total = unitPrice * quantity;
        tvTotal.setText(String.format(Locale.getDefault(), "Tổng tiền: %.0f₫", total));
    }

    private void showCreatedTime(Date created) {
        if (created == null) {
            tvCreated.setText("Không rõ thời gian");
            return;
        }

        long now = System.currentTimeMillis();
        long diff = now - created.getTime();

        long minutes = diff / (1000 * 60);
        long hours = minutes / 60;
        long days = hours / 24;

        String result;
        if (minutes < 1) result = "Vừa xong";
        else if (minutes < 60) result = minutes + " phút trước";
        else if (hours < 24) result = hours + " giờ trước";
        else result = days + " ngày trước";

        tvCreated.setText(result);
    }

    private void showRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            tvRating.setText("Chưa có đánh giá");
            return;
        }

        double totalRating = 0;
        for (Review r : reviews) {
            totalRating += r.getRating();
        }
        double avg = totalRating / reviews.size();
        tvRating.setText(String.format(Locale.getDefault(), "%.1f/5 (%d đánh giá)", avg, reviews.size()));
    }

    private Variant getFirstVariant(Map<String, Map<String, Variant>> variantsMap) {
        if (variantsMap == null || variantsMap.isEmpty()) return null;
        for (Map<String, Variant> colorMap : variantsMap.values()) {
            for (Variant variant : colorMap.values()) {
                return variant;
            }
        }
        return null;
    }

    private void displayVariants(Map<String, Map<String, Variant>> variantsMap) {
        variantDisplayList.clear();
        if (variantsMap != null) {
            for (String size : variantsMap.keySet()) {
                Map<String, Variant> colorMap = variantsMap.get(size);
                if (colorMap != null) {
                    for (String color : colorMap.keySet()) {
                        Variant variant = colorMap.get(color);
                        if (variant != null) {
                            variantDisplayList.add(new VariantDisplay(color, size, variant.getPrice(), variant.getQuantity(), variant.getImageUrl()));
                        }
                    }
                }
            }
        }
        variantAdapter.notifyDataSetChanged();
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

        btnAddToCart.setOnClickListener(v -> {
            Log.d("ProductDetail", "🛒 Thêm vào giỏ: " + product.getName() + ", SL: " + quantity);
            // TODO: xử lý lưu vào giỏ hàng
        });

        imgProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, FullscreenImageActivity.class);
            intent.putExtra("imageUrl", currentImageUrl);
            startActivity(intent);
        });
    }
}
