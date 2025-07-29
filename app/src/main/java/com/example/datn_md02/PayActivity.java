package com.example.datn_md02;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.CartOrderAdapter;
import com.example.datn_md02.Model.Cart;
import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.Model.NotificationItem;
import com.example.datn_md02.Model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class PayActivity extends AppCompatActivity {

    private TextView tvReceiverName, tvReceiverAddress, tvCardNumber, tvCoupon;
    private TextView tvSubtotal, tvShipping, tvDiscount, tvTotal;
    private RecyclerView rvCartItems;
    private RadioButton rbCOD, rbCard;
    private Button btnCheckout;

    private FirebaseUser firebaseUser;
    private DatabaseReference dbRef;

    private List<Cart> cartItems = new ArrayList<>();
    private CartOrderAdapter cartOrderAdapter;

    private double shippingFee = 5.0;
    private double subtotal = 0.0;
    private double discount = 0.0;
    private String appliedCouponCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverAddress = findViewById(R.id.tvReceiverAddress);
        tvCardNumber = findViewById(R.id.tvCardNumber);
        tvCoupon = findViewById(R.id.tvCoupon);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvTotal = findViewById(R.id.tvTotal);
        rvCartItems = findViewById(R.id.rvCartItems);
        rbCOD = findViewById(R.id.rbCOD);
        rbCard = findViewById(R.id.rbCard);
        btnCheckout = findViewById(R.id.btnCheckout);

        cartItems = (ArrayList<Cart>) getIntent().getSerializableExtra("cartItems");
        cartOrderAdapter = new CartOrderAdapter(cartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartOrderAdapter);

        calculateSubtotal();
        loadDefaultAddress();
        loadDefaultBankCard();

        tvCoupon.setOnClickListener(v -> loadPromotionsAndApplyBest());
        btnCheckout.setOnClickListener(v -> handleCheckout());
    }

    private void calculateSubtotal() {
        subtotal = 0.0;
        for (Cart item : cartItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        updateTotalUI();
    }

    private void updateTotalUI() {
        tvSubtotal.setText(String.format("Tổng phụ: ₫%,.0f", subtotal));
        tvShipping.setText(String.format("Phí vận chuyển: ₫%,.0f", shippingFee));
        tvDiscount.setText(String.format("Giảm giá: -₫%,.0f", discount));
        double total = subtotal + shippingFee - discount;
        tvTotal.setText(String.format("Tổng thanh toán: ₫%,.0f", total));
    }

    private void loadDefaultAddress() {
        dbRef.child("shipping_addresses").child(firebaseUser.getUid())
                .orderByChild("default").equalTo(true)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String name = snap.child("name").getValue(String.class);
                            String phone = snap.child("phone").getValue(String.class);
                            String address = snap.child("fullAddress").getValue(String.class);
                            tvReceiverName.setText(name + " | " + phone);
                            tvReceiverAddress.setText(address);
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadDefaultBankCard() {
        dbRef.child("users").child(firebaseUser.getUid()).child("bankAccounts")
                .orderByChild("default").equalTo(true)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String cardNumber = snap.child("cardNumber").getValue(String.class);
                            if (cardNumber != null && cardNumber.length() >= 4) {
                                String lastFour = cardNumber.substring(cardNumber.length() - 4);
                                tvCardNumber.setText("**** **** **** " + lastFour);
                            }
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadPromotionsAndApplyBest() {
        dbRef.child("promotions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double maxDiscount = 0;
                String bestCode = "";

                for (DataSnapshot promoSnap : snapshot.getChildren()) {
                    Boolean isActive = promoSnap.child("is_active").getValue(Boolean.class);
                    Boolean applyAll = promoSnap.child("apply_to_all").getValue(Boolean.class);
                    Long discountValue = promoSnap.child("discount").getValue(Long.class);
                    String code = promoSnap.child("code").getValue(String.class);

                    if (isActive != null && isActive && applyAll != null && applyAll && discountValue != null && discountValue > maxDiscount) {
                        maxDiscount = discountValue;
                        bestCode = code;
                    }
                }

                if (maxDiscount > 0) {
                    appliedCouponCode = bestCode;
                    discount = maxDiscount;
                    tvCoupon.setText("Áp dụng: " + bestCode + " (-₫" + discount + ")");
                    updateTotalUI();
                    Toast.makeText(PayActivity.this, "Áp dụng mã " + bestCode, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PayActivity.this, "Không có mã hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void handleCheckout() {
        if (!rbCOD.isChecked() && !rbCard.isChecked()) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = rbCOD.isChecked() ? "COD" : "Card";
        String orderId = dbRef.child("orders").push().getKey();
        String userId = firebaseUser.getUid();

        if (orderId == null) {
            Toast.makeText(this, "Lỗi tạo đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalAmount = subtotal + shippingFee - discount;
        String address = tvReceiverAddress.getText().toString();
        String receiver = tvReceiverName.getText().toString();

        String cardLastFour = "";
        if (rbCard.isChecked()) {
            String cardText = tvCardNumber.getText().toString();
            if (cardText.contains(" ")) {
                cardLastFour = cardText.substring(cardText.lastIndexOf(" ") + 1);
            }
        }

        // ✅ Tạo danh sách CartItem có set productId
        List<CartItem> convertedItems = new ArrayList<>();
        for (Cart cart : cartItems) {
            String variantStr = "";
            if (cart.getVariantColor() != null || cart.getVariantSize() != null) {
                variantStr = "Màu: " + (cart.getVariantColor() != null ? cart.getVariantColor() : "") +
                        " - Size: " + (cart.getVariantSize() != null ? cart.getVariantSize() : "");
            }

            CartItem cartItem = new CartItem(
                    cart.getProductName(),
                    cart.getImageUrl(),
                    variantStr,
                    cart.getQuantity(),
                    cart.getPrice()
            );
            cartItem.setProductId(cart.getProductId()); // ✅ Gán productId

            convertedItems.add(cartItem);
        }

        Order order = new Order(
                orderId,
                userId,
                receiver,
                address,
                paymentMethod,
                cardLastFour,
                convertedItems,
                subtotal,
                shippingFee,
                discount,
                totalAmount,
                appliedCouponCode,
                System.currentTimeMillis(),
                "pending"
        );

        dbRef.child("orders").child(userId).child(orderId).setValue(order)
                .addOnSuccessListener(unused -> {
                    for (Cart item : cartItems) {
                        dbRef.child("Cart").child(userId).child(item.getCartId()).removeValue();
                    }

                    String message = "🛒 Bạn đã đặt hàng thành công lúc " +
                            new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());

                    NotificationItem notification = new NotificationItem(System.currentTimeMillis(), message);
                    dbRef.child("notifications").child(userId).push().setValue(notification);

                    Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
