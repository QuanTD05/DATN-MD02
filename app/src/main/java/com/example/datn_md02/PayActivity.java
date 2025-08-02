package com.example.datn_md02;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
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
    private static final String TAG = "PayActivity";

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

    // Track if we are waiting for ZaloPay result (optional fallback logic)
    private boolean awaitingZaloPayResult = false;
    private String lastZpTransToken = "";
    private String lastAppTransID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        // Cho phép network trên main thread (chỉ demo; production nên dùng async properly)
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build());

        // Firebase
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

        // Load cart items passed from previous screen
        cartItems = (ArrayList<Cart>) getIntent().getSerializableExtra("cartItems");
        if (cartItems == null) cartItems = new ArrayList<>();
        cartOrderAdapter = new CartOrderAdapter(cartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartOrderAdapter);

        calculateSubtotal();
        loadDefaultAddress();
        loadDefaultBankCard();
        updateTotalUI();

        btnCheckout.setOnClickListener(v -> {
            if (!rbCOD.isChecked() && !rbCard.isChecked() && !rbZaloPay.isChecked()) {
                Toast.makeText(this, "Vui lòng chọn hình thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            long amountLong = (long) (subtotal + shippingFee - discount);
            String amount = String.valueOf(amountLong);

            if (rbZaloPay.isChecked()) {
                // Thanh toán ZaloPay
                awaitingZaloPayResult = true; // set flag
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
                                if (!token.isEmpty()) {
                                    lastZpTransToken = token;
                                    showZaloPayDialog(token, amount);
                                } else if (!orderUrl.isEmpty()) {
                                    // Fallback web payment
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl)));
                                } else {
                                    Toast.makeText(PayActivity.this, "Không có token/order URL.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String err = !subReturnMsg.isEmpty() ? subReturnMsg : returnMsg;
                                new AlertDialog.Builder(PayActivity.this)
                                        .setTitle("Tạo đơn thất bại")
                                        .setMessage(err + "\nCode: " + returnCode + " Subcode: " + subReturnCode)
                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                        .show();
                                awaitingZaloPayResult = false;
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "CreateOrder exception", e);
                        runOnUiThread(() -> {
                            Toast.makeText(
                                    PayActivity.this, "Lỗi tạo đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            awaitingZaloPayResult = false;
                        });
                    }
                }).start();
            } else {
                // COD hoặc Card: trạng thái pending
                String method = rbCOD.isChecked() ? "COD" : "Card";
                createAndSaveOrder(method, amount, false);
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
                    // App-to-App flow
                    ZaloPaySDK.getInstance().payOrder(
                            PayActivity.this,
                            token,
                            "demozpdk://app",
                            payListener
                    );
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                    awaitingZaloPayResult = false;
                })
                .show();
    }

    private final PayOrderListener payListener = new PayOrderListener() {
        @Override
        public void onPaymentSucceeded(String transactionId, String zpTransToken, String appTransID) {
            Log.d(TAG, "ZaloPay success: transactionId=" + transactionId + " appTransID=" + appTransID);
            runOnUiThread(() -> {
                awaitingZaloPayResult = false;
                lastAppTransID = appTransID;
                String amount = String.valueOf((long) (subtotal + shippingFee - discount));
                // Lưu đơn với trạng thái hoàn tất cho ZaloPay
                createAndSaveOrder("ZaloPay", amount, true, transactionId);
            });
        }

        @Override
        public void onPaymentCanceled(String zpTransToken, String appTransID) {
            Log.d(TAG, "ZaloPay canceled appTransID=" + appTransID);
            runOnUiThread(() -> {
                awaitingZaloPayResult = false;
                Toast.makeText(
                        PayActivity.this, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onPaymentError(ZaloPayError errorCode, String zpTransToken, String appTransID) {
            Log.d(TAG, "ZaloPay error: " + errorCode.name() + " appTransID=" + appTransID);
            runOnUiThread(() -> {
                awaitingZaloPayResult = false;
                Toast.makeText(
                        PayActivity.this, "Lỗi ZaloPay: " + errorCode.name(), Toast.LENGTH_SHORT).show();
            });
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // quan trọng để giữ intent hiện tại
        Log.d(TAG, "onNewIntent received: " + intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // (Optional) fallback: nếu đang chờ và không nhận được callback, có thể trigger verify ở đây
        if (awaitingZaloPayResult) {
            Log.d(TAG, "Still awaiting ZaloPay result in onResume");
            // TODO: nếu bạn có API verify giao dịch, gọi ở đây để kiểm tra rồi gọi createAndSaveOrder nếu thành công.
        }
    }

    private void calculateSubtotal() {
        subtotal = 0;
        for (Cart c : cartItems) {
            subtotal += c.getPrice() * c.getQuantity();
        }
        updateTotalUI();
    }

    private void loadDefaultAddress() {
        if (firebaseUser == null) return;
        dbRef.child("shipping_addresses").child(firebaseUser.getUid())
                .orderByChild("default").equalTo(true).limitToFirst(1)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s : snapshot.getChildren()) {
                            String name = s.child("name").getValue(String.class);
                            String phone = s.child("phone").getValue(String.class);
                            String addr = s.child("fullAddress").getValue(String.class);
                            tvReceiverName.setText((name != null ? name : "") + " | " + (phone != null ? phone : ""));
                            tvReceiverAddress.setText(addr != null ? addr : "");
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadDefaultBankCard() {
        if (firebaseUser == null) return;
        dbRef.child("users").child(firebaseUser.getUid()).child("bankAccounts")
                .orderByChild("default").equalTo(true).limitToFirst(1)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s : snapshot.getChildren()) {
                            String full = s.child("cardNumber").getValue(String.class);
                            if (full != null && full.length() >= 4) {
                                tvCardNumber.setText("**** **** **** " + full.substring(full.length() - 4));
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // Overload: ZaloPay success includes transactionId to show in dialog
    private void createAndSaveOrder(String paymentMethod, String amount, boolean isZaloPaySuccess, String transactionId) {
        String orderId = dbRef.child("orders").push().getKey();
        if (firebaseUser == null || orderId == null) return;
        String userId = firebaseUser.getUid();
        List<CartItem> items = new ArrayList<>();
        for (Cart c : cartItems) {
            CartItem ci = new CartItem(c.getProductName(), c.getImageUrl(), "", c.getQuantity(), c.getPrice());
            ci.setProductId(c.getProductId());
            items.add(ci);
        }

        String cardInfo = rbCard.isChecked() ? tvCardNumber.getText().toString().replace("**** **** **** ", "") : "";
        String status = isZaloPaySuccess ? "pending" : (paymentMethod.equals("ZaloPay") ? "pending" : "pending");

        Order order = new Order(orderId, userId,
                tvReceiverName.getText().toString(),
                tvReceiverAddress.getText().toString(),
                paymentMethod,
                cardInfo,
                items, subtotal, shippingFee, discount,
                Double.parseDouble(amount), appliedCouponCode,
                System.currentTimeMillis(), status);

        dbRef.child("orders").child(userId).child(orderId)
                .setValue(order)
                .addOnSuccessListener(u -> {
                    // Xoá giỏ
                    for (Cart c : cartItems) {
                        dbRef.child("Cart").child(userId).child(c.getCartId()).removeValue();
                    }

                    // Thông báo
                    String msg = "🛒 Bạn đã đặt hàng "
                            + (isZaloPaySuccess ? "thành công" : "với phương thức " + paymentMethod)
                            + " lúc " +
                            new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());
                    dbRef.child("notifications").child(userId).push()
                            .setValue(new NotificationItem(System.currentTimeMillis(), msg));

                    // Hiển thị dialog (với ZaloPay đã có transactionId)
                    String title = isZaloPaySuccess ? "Thanh toán thành công" : "Đặt hàng thành công";
                    String message = "Mã đơn: " + orderId + "\nTổng: ₫" + String.format(Locale.getDefault(), "%, .0f", order.getTotalAmount());
                    if (isZaloPaySuccess && transactionId != null) {
                        message += "\nMã giao dịch ZaloPay: " + transactionId;
                    }

                    new AlertDialog.Builder(PayActivity.this)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton("OK", (dialog, which) -> {
                                // Chuyển màn hình cảm ơn
                                Intent intent = new Intent(PayActivity.this, OrderSuccessActivity.class);
                                intent.putExtra("orderId", orderId);
                                intent.putExtra("totalAmount", order.getTotalAmount());
                                intent.putExtra("items", new ArrayList<>(items));
                                startActivity(intent);
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi lưu đơn:", e);
                    Toast.makeText(
                            PayActivity.this, "Lỗi lưu đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Wrapper cho COD/Card hoặc ZaloPay khi không có transactionId cụ thể (chưa dùng transactionId)
    private void createAndSaveOrder(String paymentMethod, String amount, boolean isZaloPaySuccess) {
        createAndSaveOrder(paymentMethod, amount, isZaloPaySuccess, null);
    }
}
