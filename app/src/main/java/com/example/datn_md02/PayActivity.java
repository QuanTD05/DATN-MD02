package com.example.datn_md02;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.CartOrderAdapter;
import com.example.datn_md02.Api.CreateOrder;
import com.example.datn_md02.Constant.AppInfo;
import com.example.datn_md02.Model.Cart;
import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.Model.NotificationItem;
import com.example.datn_md02.Model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PayActivity extends AppCompatActivity {
    private TextView tvReceiverName, tvReceiverAddress, tvCardNumber;
    private TextView tvSubtotal, tvShipping, tvDiscount, tvTotal, tvCoupon;
    private RecyclerView rvCartItems;
    private RadioButton rbCOD, rbCard, rbZaloPay;
    private Button btnCheckout;

    private FirebaseUser firebaseUser;
    private DatabaseReference dbRef;
    private List<Cart> cartItems;
    private CartOrderAdapter cartOrderAdapter;

    private double shippingFee = 50000;
    private double subtotal = 0;
    private double discount = 0;
    private String appliedCouponCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        // Cho phép network trên main thread (demo)
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build());

        // Khởi tạo Firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Init ZaloPay SDK sandbox
        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);

        // Bind UI
        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverAddress = findViewById(R.id.tvReceiverAddress);
        tvCardNumber = findViewById(R.id.tvCardNumber);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvTotal = findViewById(R.id.tvTotal);
        rvCartItems = findViewById(R.id.rvCartItems);
        rbCOD = findViewById(R.id.rbCOD);
        rbCard = findViewById(R.id.rbCard);
        rbZaloPay = findViewById(R.id.rbZaloPay);
        btnCheckout = findViewById(R.id.btnCheckout);
        tvCoupon = findViewById(R.id.tvCoupon);
        tvCoupon.setOnClickListener(v -> {
            PromotionDialog dialog = new PromotionDialog(PayActivity.this, promotion -> {
                appliedCouponCode = promotion.getCode();
                discount = subtotal * promotion.getDiscount() / 100.0;
                tvCoupon.setText("Áp dụng: " + promotion.getCode() + " (-" + promotion.getDiscount() + "%)");
                updateTotalUI();
                Toast.makeText(PayActivity.this, "Đã áp dụng mã " + promotion.getCode(), Toast.LENGTH_SHORT).show();
            });
            dialog.show();
        });

        // Load cart items
        cartItems = (ArrayList<Cart>) getIntent().getSerializableExtra("cartItems");
        cartOrderAdapter = new CartOrderAdapter(cartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartOrderAdapter);

        calculateSubtotal();
        loadDefaultAddress();
        loadDefaultBankCard();

        btnCheckout.setOnClickListener(v -> {
            // Kiểm tra phương thức thanh toán
            if (!rbCOD.isChecked() && !rbCard.isChecked() && !rbZaloPay.isChecked()) {
                Toast.makeText(this, "Vui lòng chọn hình thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            String amount = String.valueOf((long)(subtotal + shippingFee - discount));
            if (rbZaloPay.isChecked()) {
                // Thanh toán ZaloPay
                new Thread(() -> {
                    try {
                        JSONObject data = new CreateOrder().createOrder(amount);
                        int returnCode = data.optInt("return_code", -1);
                        String returnMsg = data.optString("return_message", "");
                        String token = data.optString("zp_trans_token", "");
                        String orderUrl = data.optString("order_url", "");
                        int subReturnCode = data.optInt("sub_return_code", 0);
                        String subReturnMsg = data.optString("sub_return_message", "");

                        runOnUiThread(() -> {
                            if (returnCode == 1) {
                                // App-to-App hoặc Web
                                if (!token.isEmpty()) {
                                    showZaloPayDialog(token, amount);
                                } else if (!orderUrl.isEmpty()) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl)));
                                }
                            } else {
                                String err = !subReturnMsg.isEmpty() ? subReturnMsg : returnMsg;
                                new AlertDialog.Builder(PayActivity.this)
                                        .setTitle("Tạo đơn thất bại")
                                        .setMessage(err + "\nCode: " + returnCode + " Subcode: " + subReturnCode)
                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                        .show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(
                                PayActivity.this, "Lỗi tạo đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else {
                // COD hoặc Card
                String method = rbCOD.isChecked() ? "COD" : "Card";
                saveOrder(method, amount);
                Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void updateTotalUI() {
        tvSubtotal.setText(String.format(Locale.getDefault(), "Tổng phụ: ₫%,.0f", subtotal));
        tvShipping.setText(String.format(Locale.getDefault(), "Phí vận chuyển: ₫%,.0f", shippingFee));
        tvDiscount.setText(String.format(Locale.getDefault(), "Giảm giá: -₫%,.0f", discount));
        double total = subtotal + shippingFee - discount;
        tvTotal.setText(String.format(Locale.getDefault(), "Tổng thanh toán: ₫%,.0f", total));
    }


    private void showZaloPayDialog(String token, String amount) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận ZaloPay")
                .setMessage("Bạn sẽ thanh toán " + amount + " VND?")
                .setPositiveButton("Thanh toán", (dialog, which) -> {
                    // App-to-App flow: chuyển sang ZaloPay App
                    ZaloPaySDK.getInstance().payOrder(
                            PayActivity.this,
                            token,
                            "demozpdk://app",
                            payListener
                    );
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private final PayOrderListener payListener = new PayOrderListener() {
        @Override
        public void onPaymentSucceeded(String transactionId, String zpTransToken, String appTransID) {
            runOnUiThread(() -> {
                String amount = String.valueOf((long)(subtotal + shippingFee - discount));
                saveOrder("ZaloPay", amount);
                new AlertDialog.Builder(PayActivity.this)
                        .setTitle("Thanh toán thành công")
                        .setMessage("Mã giao dịch: " + transactionId)
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        @Override
        public void onPaymentCanceled(String zpTransToken, String appTransID) {
            runOnUiThread(() -> Toast.makeText(
                    PayActivity.this, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onPaymentError(ZaloPayError errorCode, String zpTransToken, String appTransID) {
            runOnUiThread(() -> Toast.makeText(
                    PayActivity.this, "Lỗi ZaloPay: " + errorCode.name(), Toast.LENGTH_SHORT).show());
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    private void calculateSubtotal() {
        subtotal = 0;
        for (Cart c : cartItems) subtotal += c.getPrice() * c.getQuantity();
        tvSubtotal.setText(String.format(Locale.getDefault(), "Tổng phụ: ₫%,.0f", subtotal));
        tvShipping.setText(String.format(Locale.getDefault(), "Phí vận chuyển: ₫%,.0f", shippingFee));
        tvDiscount.setText(String.format(Locale.getDefault(), "Giảm giá: -₫%,.0f", discount));
        tvTotal.setText(String.format(Locale.getDefault(), "Tổng thanh toán: ₫%,.0f", subtotal + shippingFee - discount));
    }

    private void loadDefaultAddress() {
        dbRef.child("shipping_addresses").child(firebaseUser.getUid())
                .orderByChild("default").equalTo(true).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s : snapshot.getChildren()) {
                            String name = s.child("name").getValue(String.class);
                            String phone = s.child("phone").getValue(String.class);
                            String addr = s.child("fullAddress").getValue(String.class);
                            tvReceiverName.setText(name + " | " + phone);
                            tvReceiverAddress.setText(addr);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadDefaultBankCard() {
        dbRef.child("users").child(firebaseUser.getUid()).child("bankAccounts")
                .orderByChild("default").equalTo(true).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s : snapshot.getChildren()) {
                            String full = s.child("cardNumber").getValue(String.class);
                            tvCardNumber.setText("**** **** **** " + full.substring(full.length() - 4));
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void saveOrder(String paymentMethod, String amount) {
        String orderId = dbRef.child("orders").push().getKey();
        String userId = firebaseUser.getUid();
        List<CartItem> items = new ArrayList<>();
        for (Cart c : cartItems) {
            CartItem ci = new CartItem(c.getProductName(), c.getImageUrl(), "", c.getQuantity(), c.getPrice());
            ci.setProductId(c.getProductId());
            items.add(ci);
        }

        Order order = new Order(orderId, userId,
                tvReceiverName.getText().toString(),
                tvReceiverAddress.getText().toString(),
                paymentMethod,
                rbCard.isChecked() ? tvCardNumber.getText().toString().replace("**** **** **** ", "") : "",
                items, subtotal, shippingFee, discount,
                Double.parseDouble(amount), appliedCouponCode,
                System.currentTimeMillis(), "pending");

        dbRef.child("orders").child(userId).child(orderId)
                .setValue(order)
                .addOnSuccessListener(u -> {
                    for (Cart c : cartItems) {
                        dbRef.child("Cart").child(userId).child(c.getCartId()).removeValue();
                    }

                    String msg = "🛒 Bạn đã đặt hàng thành công lúc " +
                            new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());
                    dbRef.child("notifications").child(userId).push()
                            .setValue(new NotificationItem(System.currentTimeMillis(), msg));

                    // 👉 CHUYỂN MÀN HÌNH CẢM ƠN
                    Intent intent = new Intent(PayActivity.this, OrderSuccessActivity.class);
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("totalAmount", order.getTotalAmount());
                    intent.putExtra("items", new ArrayList<>(items));
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this, "Lỗi lưu đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}