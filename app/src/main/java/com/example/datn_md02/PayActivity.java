//// === File: PayActivity.java ===
//package com.example.datn_md02;
//
//import android.app.AlertDialog;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.StrictMode;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.RadioButton;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.datn_md02.Adapter.CartOrderAdapter;
//import com.example.datn_md02.Api.CreateOrder;
//import com.example.datn_md02.Constant.AppInfo;
//import com.example.datn_md02.Model.Cart;
//import com.example.datn_md02.Model.CartItem;
//import com.example.datn_md02.Model.NotificationItem;
//import com.example.datn_md02.Model.Order;
//import com.example.datn_md02.Model.Promotion;     // <- dùng cho tính khuyến mãi
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import org.json.JSONObject;
//
//import java.text.NumberFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashSet;     // <- thêm
//import java.util.List;
//import java.util.Locale;
//import java.util.Set;        // <- thêm
//
//import vn.zalopay.sdk.Environment;
//import vn.zalopay.sdk.ZaloPayError;
//import vn.zalopay.sdk.ZaloPaySDK;
//import vn.zalopay.sdk.listeners.PayOrderListener;
//
//public class PayActivity extends AppCompatActivity {
//    private static final String TAG = "PayActivity";
//
//    private TextView tvReceiverName, tvReceiverAddress, tvCardNumber;
//    private TextView tvSubtotal, tvShipping, tvDiscount, tvTotal, tvCoupon;
//    private RecyclerView rvCartItems;
//    private RadioButton rbCOD, rbCard, rbZaloPay;
//    private Button btnCheckout;
//
//    private FirebaseUser firebaseUser;
//    private DatabaseReference dbRef;
//    private List<Cart> cartItems;
//    private CartOrderAdapter cartOrderAdapter;
//
//    private double shippingFee = 50000;
//    private double subtotal = 0;
//    private double discount = 0;
//    private String appliedCouponCode = "";
//    private ImageView imgEditAddress,btnBack;
//
//    private boolean awaitingZaloPayResult = false;
//    private String lastAppTransID = "";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pay);
//
//        // Cho phép network trên main thread (demo)
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .permitAll()
//                .build());
//
//        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        dbRef = FirebaseDatabase.getInstance().getReference();
//
//        // Khởi tạo ZaloPay SDK
//        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);
//
//        // Bind UI
//        tvReceiverName   = findViewById(R.id.tvReceiverName);
//        tvReceiverAddress= findViewById(R.id.tvReceiverAddress);
//        tvCardNumber     = findViewById(R.id.tvCardNumber);
//        tvSubtotal       = findViewById(R.id.tvSubtotal);
//        tvShipping       = findViewById(R.id.tvShipping);
//        tvDiscount       = findViewById(R.id.tvDiscount);
//        tvTotal          = findViewById(R.id.tvTotal);
//        tvCoupon         = findViewById(R.id.tvCoupon);
//        rvCartItems      = findViewById(R.id.rvCartItems);
//        rbCOD            = findViewById(R.id.rbCOD);
//        rbCard           = findViewById(R.id.rbCard);
//        rbZaloPay        = findViewById(R.id.rbZaloPay);
//        btnCheckout      = findViewById(R.id.btnCheckout);
//        imgEditAddress   = findViewById(R.id.imgEditAddress);
//        View.OnClickListener editAddressListener = v -> showEditAddressDialog();
//
//        imgEditAddress.setOnClickListener(editAddressListener);
//        // Lấy giỏ hàng từ Intent
//        cartItems = (ArrayList<Cart>) getIntent().getSerializableExtra("cartItems");
//        if (cartItems == null) cartItems = new ArrayList<>();
//        cartOrderAdapter = new CartOrderAdapter(cartItems);
//        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
//        rvCartItems.setAdapter(cartOrderAdapter);
//        btnBack      = findViewById(R.id.btnBack);
//
//        btnBack.setOnClickListener(v -> finish());
//
//        calculateSubtotal();
//        loadDefaultAddress();
//        loadDefaultBankCard();
//        updateTotalUI();
//
//        // Áp mã khuyến mãi (lọc theo sản phẩm trong giỏ + chỉ giảm trên item đủ điều kiện)
//        tvCoupon.setOnClickListener(v -> {
//            Set<String> productIdsTrongGio = buildProductIdSetFromCart();
//
//            // Dùng PromotionDialog phiên bản nhận productIds để lọc danh sách hiển thị
//            PromotionDialog dialog = new PromotionDialog(this, productIdsTrongGio, promotion -> {
//                appliedCouponCode = promotion.getCode();
//
//                // Tính giảm giá chỉ trên các item hợp lệ với promotion
//                discount = computeDiscountForPromotion(promotion);
//
//                // Cập nhật UI
//                double percent = promotion.getDiscount(); // % theo model hiện tại của bạn
//                tvCoupon.setText("Áp dụng: " + promotion.getCode() + " (-" + (percent) + "%)");
//                updateTotalUI();
//                Toast.makeText(this, "Đã áp dụng mã " + promotion.getCode(), Toast.LENGTH_SHORT).show();
//            });
//            dialog.show();
//        });
//
//        btnCheckout.setOnClickListener(v -> {
//            if (!rbCOD.isChecked() && !rbCard.isChecked() && !rbZaloPay.isChecked()) {
//                Toast.makeText(this, "Vui lòng chọn hình thức thanh toán", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            long amountLong = (long)(subtotal + shippingFee - discount);
//            String amount = String.valueOf(amountLong);
//
//            if (rbZaloPay.isChecked()) {
//                // Xử lý ZaloPay
//                awaitingZaloPayResult = true;
//                new Thread(() -> {
//                    try {
//                        JSONObject data = new CreateOrder().createOrder(amount);
//                        int returnCode = data.optInt("return_code", -1);
//                        String token     = data.optString("zp_trans_token", "");
//                        String orderUrl  = data.optString("order_url", "");
//                        runOnUiThread(() -> {
//                            if (returnCode == 1) {
//                                if (!token.isEmpty()) {
//                                    showZaloPayDialog(token, amount);
//                                } else if (!orderUrl.isEmpty()) {
//                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl)));
//                                } else {
//                                    Toast.makeText(this, "Không có token/order URL.", Toast.LENGTH_SHORT).show();
//                                }
//                            } else {
//                                String err = data.optString("sub_return_message", data.optString("return_message",""));
//                                new AlertDialog.Builder(this)
//                                        .setTitle("Tạo đơn thất bại")
//                                        .setMessage(err)
//                                        .setPositiveButton("OK",(d,w)->d.dismiss())
//                                        .show();
//                                awaitingZaloPayResult = false;
//                            }
//                        });
//                    } catch (Exception e) {
//                        Log.e(TAG,"CreateOrder exception",e);
//                        runOnUiThread(() -> {
//                            Toast.makeText(this, "Lỗi tạo đơn: "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                            awaitingZaloPayResult = false;
//                        });
//                    }
//                }).start();
//            } else {
//                // COD hoặc Card
//                String method = rbCOD.isChecked() ? "COD" : "Card";
//                createAndSaveOrder(method, amount, false);
//            }
//        });
//    }
//
//    private void showEditAddressDialog() {
//        if (firebaseUser == null) return;
//
//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_address, null);
//        EditText edtName     = dialogView.findViewById(R.id.edtName);
//        EditText edtPhone    = dialogView.findViewById(R.id.edtPhone);
//        EditText edtStreet   = dialogView.findViewById(R.id.edtStreet);
//        EditText edtWard     = dialogView.findViewById(R.id.edtWard);
//        EditText edtDistrict = dialogView.findViewById(R.id.edtDistrict);
//        EditText edtCity     = dialogView.findViewById(R.id.edtCity);
//
//        dbRef.child("shipping_addresses")
//                .child(firebaseUser.getUid())
//                .orderByChild("default").equalTo(true).limitToFirst(1)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot s : snapshot.getChildren()) {
//                            String name     = s.child("name").getValue(String.class);
//                            String phone    = s.child("phone").getValue(String.class);
//                            String street   = s.child("street").getValue(String.class);
//                            String ward     = s.child("ward").getValue(String.class);
//                            String district = s.child("district").getValue(String.class);
//                            String city     = s.child("city").getValue(String.class);
//                            String addrId   = s.child("id").getValue(String.class);
//
//                            edtName.setText(name != null ? name : "");
//                            edtPhone.setText(phone != null ? phone : "");
//                            edtStreet.setText(street != null ? street : "");
//                            edtWard.setText(ward != null ? ward : "");
//                            edtDistrict.setText(district != null ? district : "");
//                            edtCity.setText(city != null ? city : "");
//
//                            new AlertDialog.Builder(PayActivity.this)
//                                    .setTitle("Sửa địa chỉ")
//                                    .setView(dialogView)
//                                    .setPositiveButton("Lưu", (d, w) -> {
//                                        String newName     = edtName.getText().toString().trim();
//                                        String newPhone    = edtPhone.getText().toString().trim();
//                                        String newStreet   = edtStreet.getText().toString().trim();
//                                        String newWard     = edtWard.getText().toString().trim();
//                                        String newDistrict = edtDistrict.getText().toString().trim();
//                                        String newCity     = edtCity.getText().toString().trim();
//
//                                        if (newName.isEmpty() || newPhone.isEmpty() ||
//                                                newStreet.isEmpty() || newWard.isEmpty() ||
//                                                newDistrict.isEmpty() || newCity.isEmpty()) {
//                                            Toast.makeText(PayActivity.this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
//                                            return;
//                                        }
//
//                                        String fullAddr = newStreet + ", " + newWard + ", " + newDistrict + ", " + newCity;
//
//                                        if (addrId != null) {
//                                            DatabaseReference addrRef = dbRef.child("shipping_addresses")
//                                                    .child(firebaseUser.getUid())
//                                                    .child(addrId);
//
//                                            addrRef.child("name").setValue(newName);
//                                            addrRef.child("phone").setValue(newPhone);
//                                            addrRef.child("street").setValue(newStreet);
//                                            addrRef.child("ward").setValue(newWard);
//                                            addrRef.child("district").setValue(newDistrict);
//                                            addrRef.child("city").setValue(newCity);
//                                            addrRef.child("fullAddress").setValue(fullAddr);
//                                        }
//
//                                        tvReceiverName.setText(newName + " | " + newPhone);
//                                        tvReceiverAddress.setText(fullAddr);
//
//                                        Toast.makeText(PayActivity.this, "Đã cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
//                                    })
//                                    .setNegativeButton("Hủy", null)
//                                    .show();
//                            break;
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) { }
//                });
//    }
//
//    // ====== Khuyến mãi theo sản phẩm: helper ======
//
//    private Set<String> buildProductIdSetFromCart() {
//        Set<String> ids = new HashSet<>();
//        for (Cart c : cartItems) {
//            if (c.getProductId() != null) ids.add(c.getProductId());
//        }
//        return ids;
//    }
//
//    /** Tính giảm giá chỉ trên các item thỏa điều kiện của promotion */
//    private double computeDiscountForPromotion(Promotion promotion) {
//        boolean applyAll = promotion.isApply_to_all();
//        List<String> allowIds = promotion.getApply_to_product_ids();
//
//        double eligibleSubtotal = 0;
//        for (Cart c : cartItems) {
//            boolean ok = applyAll;
//            if (!applyAll) {
//                ok = (allowIds != null && allowIds.contains(c.getProductId()));
//            }
//            if (ok) {
//                eligibleSubtotal += c.getPrice() * c.getQuantity();
//            }
//        }
//
//        double percent = promotion.getDiscount(); // theo model hiện tại
//        return eligibleSubtotal * (percent / 100.0);
//    }
//
//    // ==============================================
//
//    private void updateTotalUI() {
//        // Định dạng số với dấu phân nhóm theo chuẩn Việt Nam
//        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
//
//        tvSubtotal.setText("Tổng phụ: " + nf.format(subtotal) + " VND");
//        tvShipping.setText("Phí vận chuyển: " + nf.format(shippingFee) + " VND");
//        tvDiscount.setText("Giảm giá: -" + nf.format(discount) + " VND");
//
//        double total = subtotal + shippingFee - discount;
//        tvTotal.setText("Tổng thanh toán: " + nf.format(total) + " VND");
//    }
//
//    private void showZaloPayDialog(String token, String amount) {
//        new AlertDialog.Builder(this)
//                .setTitle("Xác nhận ZaloPay")
//                .setMessage("Bạn sẽ thanh toán " + amount + " VND?")
//                .setPositiveButton("Thanh toán",(d,w)-> {
//                    ZaloPaySDK.getInstance().payOrder(
//                            PayActivity.this,
//                            token,
//                            "demozpdk://app",
//                            payListener
//                    );
//                })
//                .setNegativeButton("Hủy",(d,w)-> {
//                    d.dismiss();
//                    awaitingZaloPayResult = false;
//                })
//                .show();
//    }
//
//    private final PayOrderListener payListener = new PayOrderListener() {
//        @Override
//        public void onPaymentSucceeded(String transactionId, String zpTransToken, String appTransID) {
//            awaitingZaloPayResult = false;
//            lastAppTransID = appTransID;
//            String amount = String.valueOf((long)(subtotal + shippingFee - discount));
//            createAndSaveOrder("ZaloPay", amount, true, transactionId);
//        }
//        @Override
//        public void onPaymentCanceled(String zpTransToken, String appTransID) {
//            runOnUiThread(() -> Toast.makeText(PayActivity.this, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show());
//            awaitingZaloPayResult = false;
//        }
//        @Override
//        public void onPaymentError(ZaloPayError errorCode, String zpTransToken, String appTransID) {
//            runOnUiThread(() -> Toast.makeText(PayActivity.this, "Lỗi ZaloPay: "+errorCode, Toast.LENGTH_SHORT).show());
//            awaitingZaloPayResult = false;
//        }
//    };
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent);
//        ZaloPaySDK.getInstance().onResult(intent);
//    }
//
//    private void calculateSubtotal() {
//        subtotal = 0;
//        for (Cart c : cartItems) {
//            subtotal += c.getPrice() * c.getQuantity();
//        }
//    }
//
//    private void loadDefaultAddress() {
//        if (firebaseUser == null) return;
//        dbRef.child("shipping_addresses")
//                .child(firebaseUser.getUid())
//                .orderByChild("default").equalTo(true).limitToFirst(1)
//                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
//                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
//                        for (DataSnapshot s : snap.getChildren()) {
//                            String name   = s.child("name").getValue(String.class);
//                            String phone  = s.child("phone").getValue(String.class);
//                            String addr   = s.child("fullAddress").getValue(String.class);
//                            tvReceiverName.setText((name!=null?name:"") + " | " + (phone!=null?phone:""));
//                            tvReceiverAddress.setText(addr!=null?addr:"");
//                        }
//                    }
//                    @Override public void onCancelled(@NonNull DatabaseError e) {}
//                });
//    }
//
//    private void loadDefaultBankCard() {
//        if (firebaseUser == null) return;
//        dbRef.child("users")
//                .child(firebaseUser.getUid())
//                .child("bankAccounts")
//                .orderByChild("default").equalTo(true).limitToFirst(1)
//                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
//                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
//                        for (DataSnapshot s : snap.getChildren()) {
//                            String full = s.child("cardNumber").getValue(String.class);
//                            if (full!=null && full.length()>=4) {
//                                tvCardNumber.setText("**** **** **** " + full.substring(full.length()-4));
//                            }
//                        }
//                    }
//                    @Override public void onCancelled(@NonNull DatabaseError e) {}
//                });
//    }
//
//    private void createAndSaveOrder(String paymentMethod, String amount, boolean isZaloPaySuccess, String transactionId) {
//        String orderId = dbRef.child("orders").push().getKey();
//        if (firebaseUser==null || orderId==null) return;
//        String userId = firebaseUser.getUid();
//
//        // Build CartItem list with variant
//        List<CartItem> items = new ArrayList<>();
//        for (Cart c : cartItems) {
//            String variantStr = "";
//            if (c.getVariantColor()!=null || c.getVariantSize()!=null) {
//                variantStr = "Màu: "+(c.getVariantColor()!=null?c.getVariantColor():"")
//                        +" - Size: "+(c.getVariantSize()!=null?c.getVariantSize():"");
//            }
//            CartItem ci = new CartItem(c.getProductName(), c.getImageUrl(), variantStr, c.getQuantity(), c.getPrice());
//            ci.setProductId(c.getProductId());
//            items.add(ci);
//        }
//
//        double totalAmount = Double.parseDouble(amount);
//        Order order = new Order(
//                orderId, userId,
//                tvReceiverName.getText().toString(),
//                tvReceiverAddress.getText().toString(),
//                paymentMethod,
//                rbCard.isChecked()
//                        ? tvCardNumber.getText().toString().replace("**** **** **** ", "")
//                        : "",
//                items,
//                subtotal,
//                shippingFee,
//                discount,
//                totalAmount,
//                appliedCouponCode,
//                System.currentTimeMillis(),
//                "pending"
//        );
//
//        dbRef.child("orders").child(userId).child(orderId)
//                .setValue(order)
//                .addOnSuccessListener(u -> {
//                    // Xóa giỏ
//                    for (Cart c : cartItems) {
//                        dbRef.child("Cart").child(userId).child(c.getCartId()).removeValue();
//                    }
//                    // Notification
//                    String msg = "🛒 Bạn đã đặt hàng thành công lúc " +
//                            new SimpleDateFormat("HH:mm dd/MM/yyyy",Locale.getDefault()).format(new Date());
//                    dbRef.child("notifications").child(userId)
//                            .push().setValue(new NotificationItem(System.currentTimeMillis(), msg));
//
//                    // Chuyển sang OrderSuccess
//                    new AlertDialog.Builder(PayActivity.this)
//                            .setTitle("Đặt hàng thành công")
//                            .setMessage("Mã đơn: " + orderId + "\nTổng: " +
//                                    NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(totalAmount))
//                            .setPositiveButton("OK", (d,w) -> {
//                                Intent intent = new Intent(PayActivity.this, OrderSuccessActivity.class);
//                                intent.putExtra("orderId", orderId);
//                                intent.putExtra("totalAmount", totalAmount);
//                                intent.putExtra("cartItems", new ArrayList<>(items));
//                                startActivity(intent);
//                                finish();
//                            })
//                            .setCancelable(false)
//                            .show();
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Lỗi lưu đơn:", e);
//                    Toast.makeText(this, "Lỗi lưu đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
//
//    private void createAndSaveOrder(String paymentMethod, String amount, boolean isZaloPaySuccess) {
//        createAndSaveOrder(paymentMethod, amount, isZaloPaySuccess, null);
//    }
//}





package com.example.datn_md02;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.datn_md02.Model.Promotion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
    private ImageView imgEditAddress, btnBack;
    private CheckBox checkboxShipping;
    private TextView tvShippingFee;


    private boolean awaitingZaloPayResult = false;
    private String lastAppTransID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Khởi tạo ZaloPay SDK
        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);

        // Bind UI
        tvReceiverName   = findViewById(R.id.tvReceiverName);
        tvReceiverAddress= findViewById(R.id.tvReceiverAddress);
        tvCardNumber     = findViewById(R.id.tvCardNumber);
        tvSubtotal       = findViewById(R.id.tvSubtotal);
        tvShipping       = findViewById(R.id.tvShipping);
        tvDiscount       = findViewById(R.id.tvDiscount);
        tvTotal          = findViewById(R.id.tvTotal);
        tvCoupon         = findViewById(R.id.tvCoupon);
        rvCartItems      = findViewById(R.id.rvCartItems);
        rbCOD            = findViewById(R.id.rbCOD);
        rbCard           = findViewById(R.id.rbCard);
        rbZaloPay        = findViewById(R.id.rbZaloPay);
        btnCheckout      = findViewById(R.id.btnCheckout);
        imgEditAddress   = findViewById(R.id.imgEditAddress);
        btnBack          = findViewById(R.id.btnBack);
        checkboxShipping = findViewById(R.id.checkboxShipping);
        tvShippingFee = findViewById(R.id.tvShippingFee);


        imgEditAddress.setOnClickListener(v -> showEditAddressDialog());
        btnBack.setOnClickListener(v -> finish());

        // Lấy giỏ hàng từ Intent
        cartItems = (ArrayList<Cart>) getIntent().getSerializableExtra("cartItems");
        if (cartItems == null) cartItems = new ArrayList<>();
        cartOrderAdapter = new CartOrderAdapter(cartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartOrderAdapter);

        calculateSubtotal();
        loadDefaultAddress();
        loadDefaultBankCard();
        updateTotalUI();

        // Áp mã khuyến mãi
        tvCoupon.setOnClickListener(v -> {
            Set<String> productIdsTrongGio = buildProductIdSetFromCart();
            PromotionDialog dialog = new PromotionDialog(this, productIdsTrongGio, promotion -> {
                appliedCouponCode = promotion.getCode();
                discount = computeDiscountForPromotion(promotion);
                double percent = promotion.getDiscount();
                tvCoupon.setText("Áp dụng: " + promotion.getCode() + " (-" + percent + "%)");
                updateTotalUI();
                Toast.makeText(this, "Đã áp dụng mã " + promotion.getCode(), Toast.LENGTH_SHORT).show();
            });
            dialog.show();
        });

        btnCheckout.setOnClickListener(v -> {
            if (!rbCOD.isChecked() && !rbCard.isChecked() && !rbZaloPay.isChecked()) {
                Toast.makeText(this, "Vui lòng chọn hình thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            long amountLong = (long)(subtotal + shippingFee - discount);
            String amount = String.valueOf(amountLong);

            if (rbZaloPay.isChecked()) {
                handleZaloPay(amount);
            } else {
                String method = rbCOD.isChecked() ? "COD" : "Card";
                createAndSaveOrder(method, amount, false);
            }
        });
    }

    // ====== TÍNH PHÍ SHIP ======
    private double calculateShippingFee(String city, double subtotal) {
        // Free ship nếu đơn trên 7 triệu
        if (subtotal >= 7000000) {
            return 0;
        }

        if (city == null) city = "";
        city = city.toLowerCase();

        if (city.contains("hà nội") || city.contains("hanoi")) {
            return 200000;
        } else if (city.contains("hồ chí minh") || city.contains("tphcm") || city.contains("sài gòn")) {
            return 250000;
        } else {
            return 500000;
        }
    }

    // ====== Khuyến mãi theo sản phẩm ======
    private Set<String> buildProductIdSetFromCart() {
        Set<String> ids = new HashSet<>();
        for (Cart c : cartItems) {
            if (c.getProductId() != null) ids.add(c.getProductId());
        }
        return ids;
    }

    private double computeDiscountForPromotion(Promotion promotion) {
        boolean applyAll = promotion.isApply_to_all();
        List<String> allowIds = promotion.getApply_to_product_ids();

        double eligibleSubtotal = 0;
        for (Cart c : cartItems) {
            boolean ok = applyAll;
            if (!applyAll) {
                ok = (allowIds != null && allowIds.contains(c.getProductId()));
            }
            if (ok) {
                eligibleSubtotal += c.getPrice() * c.getQuantity();
            }
        }

        double percent = promotion.getDiscount();
        return eligibleSubtotal * (percent / 100.0);
    }

    private void updateTotalUI() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Nếu checkbox không chọn thì free ship
        double appliedShipping = checkboxShipping.isChecked() ? shippingFee : 0;

        tvSubtotal.setText("Tổng phụ: " + nf.format(subtotal));
        tvShipping.setText("Phí vận chuyển: " + nf.format(appliedShipping));
        tvDiscount.setText("Giảm giá: -" + nf.format(discount));

        double total = subtotal + appliedShipping - discount;
        tvTotal.setText("Tổng thanh toán: " + nf.format(total));

        // Hiển thị riêng tại khu vực "Phương pháp vận chuyển"
        tvShippingFee.setText(nf.format(appliedShipping));
    }

    private void showEditAddressDialog() {
        if (firebaseUser == null) return;

        dbRef.child("shipping_addresses")
                .child(firebaseUser.getUid())
                .orderByChild("default").equalTo(true).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Inflate view mỗi lần mở dialog
                        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_address, null);
                        EditText edtName     = dialogView.findViewById(R.id.edtName);
                        EditText edtPhone    = dialogView.findViewById(R.id.edtPhone);
                        EditText edtStreet   = dialogView.findViewById(R.id.edtStreet);
                        EditText edtWard     = dialogView.findViewById(R.id.edtWard);
                        EditText edtDistrict = dialogView.findViewById(R.id.edtDistrict);
                        EditText edtCity     = dialogView.findViewById(R.id.edtCity);

                        String addrId = null;
                        if (snapshot.exists()) {
                            for (DataSnapshot s : snapshot.getChildren()) {
                                addrId = s.child("id").getValue(String.class);
                                edtName.setText(s.child("name").getValue(String.class));
                                edtPhone.setText(s.child("phone").getValue(String.class));
                                edtStreet.setText(s.child("street").getValue(String.class));
                                edtWard.setText(s.child("ward").getValue(String.class));
                                edtDistrict.setText(s.child("district").getValue(String.class));
                                edtCity.setText(s.child("city").getValue(String.class));
                                break;
                            }
                        }

                        String finalAddrId = addrId;
                        new AlertDialog.Builder(PayActivity.this)
                                .setTitle(snapshot.exists() ? "Sửa địa chỉ" : "Thêm địa chỉ")
                                .setView(dialogView)
                                .setPositiveButton("Lưu", (d, w) -> {
                                    String newName     = edtName.getText().toString().trim();
                                    String newPhone    = edtPhone.getText().toString().trim();
                                    String newStreet   = edtStreet.getText().toString().trim();
                                    String newWard     = edtWard.getText().toString().trim();
                                    String newDistrict = edtDistrict.getText().toString().trim();
                                    String newCity     = edtCity.getText().toString().trim();

                                    if (newName.isEmpty() || newPhone.isEmpty() ||
                                            newStreet.isEmpty() || newWard.isEmpty() ||
                                            newDistrict.isEmpty() || newCity.isEmpty()) {
                                        Toast.makeText(PayActivity.this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String fullAddr = newStreet + ", " + newWard + ", " + newDistrict + ", " + newCity;

                                    DatabaseReference userAddrRef = dbRef.child("shipping_addresses").child(firebaseUser.getUid());
                                    if (finalAddrId != null) {
                                        // update
                                        DatabaseReference addrRef = userAddrRef.child(finalAddrId);
                                        addrRef.child("name").setValue(newName);
                                        addrRef.child("phone").setValue(newPhone);
                                        addrRef.child("street").setValue(newStreet);
                                        addrRef.child("ward").setValue(newWard);
                                        addrRef.child("district").setValue(newDistrict);
                                        addrRef.child("city").setValue(newCity);
                                        addrRef.child("fullAddress").setValue(fullAddr);
                                    } else {
                                        // thêm mới
                                        String newId = userAddrRef.push().getKey();
                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("id", newId);
                                        map.put("name", newName);
                                        map.put("phone", newPhone);
                                        map.put("street", newStreet);
                                        map.put("ward", newWard);
                                        map.put("district", newDistrict);
                                        map.put("city", newCity);
                                        map.put("fullAddress", fullAddr);
                                        map.put("default", true);
                                        userAddrRef.child(newId).setValue(map);
                                    }

                                    // cập nhật UI
                                    tvReceiverName.setText(newName + " | " + newPhone);
                                    tvReceiverAddress.setText(fullAddr);

                                    shippingFee = calculateShippingFee(newCity, subtotal);
                                    updateTotalUI();
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void openAddressDialog(String addrId, String name, String phone, String street, String ward, String district, String city) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_address, null);
        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtPhone = dialogView.findViewById(R.id.edtPhone);
        EditText edtStreet = dialogView.findViewById(R.id.edtStreet);
        EditText edtWard = dialogView.findViewById(R.id.edtWard);
        EditText edtDistrict = dialogView.findViewById(R.id.edtDistrict);
        EditText edtCity = dialogView.findViewById(R.id.edtCity);


        edtName.setText(name != null ? name : "");
        edtPhone.setText(phone != null ? phone : "");
        edtStreet.setText(street != null ? street : "");
        edtWard.setText(ward != null ? ward : "");
        edtDistrict.setText(district != null ? district : "");
        edtCity.setText(city != null ? city : "");


        new AlertDialog.Builder(PayActivity.this)
                .setTitle(addrId == null ? "Thêm địa chỉ mới" : "Sửa địa chỉ")
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, w) -> {
                    String newName = edtName.getText().toString().trim();
                    String newPhone = edtPhone.getText().toString().trim();
                    String newStreet = edtStreet.getText().toString().trim();
                    String newWard = edtWard.getText().toString().trim();
                    String newDistrict = edtDistrict.getText().toString().trim();
                    String newCity = edtCity.getText().toString().trim();


                    if (newName.isEmpty() || newPhone.isEmpty() || newStreet.isEmpty() ||
                            newWard.isEmpty() || newDistrict.isEmpty() || newCity.isEmpty()) {
                        Toast.makeText(PayActivity.this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    String fullAddr = newStreet + ", " + newWard + ", " + newDistrict + ", " + newCity;


                    DatabaseReference userAddrRef = dbRef.child("shipping_addresses").child(firebaseUser.getUid());
                    if (addrId == null) {
// thêm mới
                        String newId = userAddrRef.push().getKey();
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", newId);
                        map.put("name", newName);
                        map.put("phone", newPhone);
                        map.put("street", newStreet);
                        map.put("ward", newWard);
                        map.put("district", newDistrict);
                        map.put("city", newCity);
                        map.put("fullAddress", fullAddr);
                        map.put("default", true);
                        userAddrRef.child(newId).setValue(map);
                    } else {
// cập nhật
                        DatabaseReference addrRef = userAddrRef.child(addrId);
                        addrRef.child("name").setValue(newName);
                        addrRef.child("phone").setValue(newPhone);
                        addrRef.child("street").setValue(newStreet);
                        addrRef.child("ward").setValue(newWard);
                        addrRef.child("district").setValue(newDistrict);
                        addrRef.child("city").setValue(newCity);
                        addrRef.child("fullAddress").setValue(fullAddr);
                    }


                    tvReceiverName.setText(newName + " | " + newPhone);
                    tvReceiverAddress.setText(fullAddr);


                    shippingFee = calculateShippingFee(newCity, subtotal);
                    updateTotalUI();


                    Toast.makeText(PayActivity.this, "Đã lưu địa chỉ", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    private void handleZaloPay(String amount) {
        awaitingZaloPayResult = true;
        new Thread(() -> {
            try {
                JSONObject data = new CreateOrder().createOrder(amount);
                int returnCode = data.optInt("return_code", -1);
                String token     = data.optString("zp_trans_token", "");
                String orderUrl  = data.optString("order_url", "");
                runOnUiThread(() -> {
                    if (returnCode == 1) {
                        if (!token.isEmpty()) {
                            showZaloPayDialog(token, amount);
                        } else if (!orderUrl.isEmpty()) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(orderUrl)));
                        } else {
                            Toast.makeText(this, "Không có token/order URL.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String err = data.optString("sub_return_message", data.optString("return_message",""));
                        new AlertDialog.Builder(this)
                                .setTitle("Tạo đơn thất bại")
                                .setMessage(err)
                                .setPositiveButton("OK",(d,w)->d.dismiss())
                                .show();
                        awaitingZaloPayResult = false;
                    }
                });
            } catch (Exception e) {
                Log.e(TAG,"CreateOrder exception",e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi tạo đơn: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    awaitingZaloPayResult = false;
                });
            }
        }).start();
    }

    private void showZaloPayDialog(String token, String amount) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận ZaloPay")
                .setMessage("Bạn sẽ thanh toán " + amount + " VND?")
                .setPositiveButton("Thanh toán",(d,w)-> {
                    ZaloPaySDK.getInstance().payOrder(
                            PayActivity.this,
                            token,
                            "demozpdk://app",
                            payListener
                    );
                })
                .setNegativeButton("Hủy",(d,w)-> {
                    d.dismiss();
                    awaitingZaloPayResult = false;
                })
                .show();
    }

    private final PayOrderListener payListener = new PayOrderListener() {
        @Override
        public void onPaymentSucceeded(String transactionId, String zpTransToken, String appTransID) {
            awaitingZaloPayResult = false;
            lastAppTransID = appTransID;
            String amount = String.valueOf((long)(subtotal + shippingFee - discount));
            createAndSaveOrder("ZaloPay", amount, true, transactionId);
        }
        @Override
        public void onPaymentCanceled(String zpTransToken, String appTransID) {
            runOnUiThread(() -> Toast.makeText(PayActivity.this, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show());
            awaitingZaloPayResult = false;
        }
        @Override
        public void onPaymentError(ZaloPayError errorCode, String zpTransToken, String appTransID) {
            runOnUiThread(() -> Toast.makeText(PayActivity.this, "Lỗi ZaloPay: "+errorCode, Toast.LENGTH_SHORT).show());
            awaitingZaloPayResult = false;
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    private void calculateSubtotal() {
        subtotal = 0;
        for (Cart c : cartItems) {
            subtotal += c.getPrice() * c.getQuantity();
        }
    }

    private void loadDefaultAddress() {
        if (firebaseUser == null) return;
        dbRef.child("shipping_addresses")
                .child(firebaseUser.getUid())
                .orderByChild("default").equalTo(true).limitToFirst(1)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        for (DataSnapshot s : snap.getChildren()) {
                            String name   = s.child("name").getValue(String.class);
                            String phone  = s.child("phone").getValue(String.class);
                            String addr   = s.child("fullAddress").getValue(String.class);
                            String city   = s.child("city").getValue(String.class);

                            tvReceiverName.setText((name!=null?name:"") + " | " + (phone!=null?phone:""));
                            tvReceiverAddress.setText(addr!=null?addr:"");

                            // Tính phí ship
                            shippingFee = calculateShippingFee(city, subtotal);
                            updateTotalUI();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void loadDefaultBankCard() {
        if (firebaseUser == null) return;
        dbRef.child("users")
                .child(firebaseUser.getUid())
                .child("bankAccounts")
                .orderByChild("default").equalTo(true).limitToFirst(1)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        for (DataSnapshot s : snap.getChildren()) {
                            String full = s.child("cardNumber").getValue(String.class);
                            if (full!=null && full.length()>=4) {
                                tvCardNumber.setText("**** **** **** " + full.substring(full.length()-4));
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void createAndSaveOrder(String paymentMethod, String amount, boolean isZaloPaySuccess, String transactionId) {
        String orderId = dbRef.child("orders").push().getKey();
        if (firebaseUser==null || orderId==null) return;
        String userId = firebaseUser.getUid();

        List<CartItem> items = new ArrayList<>();
        for (Cart c : cartItems) {
            String variantStr = "";
            if (c.getVariantColor()!=null || c.getVariantSize()!=null) {
                variantStr = "Màu: "+(c.getVariantColor()!=null?c.getVariantColor():"")
                        +" - Size: "+(c.getVariantSize()!=null?c.getVariantSize():"");
            }
            CartItem ci = new CartItem(c.getProductName(), c.getImageUrl(), variantStr, c.getQuantity(), c.getPrice());
            ci.setProductId(c.getProductId());
            items.add(ci);
        }

        double totalAmount = Double.parseDouble(amount);
        Order order = new Order(
                orderId, userId,
                tvReceiverName.getText().toString(),
                tvReceiverAddress.getText().toString(),
                paymentMethod,
                rbCard.isChecked()
                        ? tvCardNumber.getText().toString().replace("**** **** **** ", "")
                        : "",
                items,
                subtotal,
                shippingFee,
                discount,
                totalAmount,
                appliedCouponCode,
                System.currentTimeMillis(),
                "pending"
        );

        dbRef.child("orders").child(userId).child(orderId)
                .setValue(order)
                .addOnSuccessListener(u -> {
                    for (Cart c : cartItems) {
                        dbRef.child("Cart").child(userId).child(c.getCartId()).removeValue();
                    }
                    String msg = "🛒 Bạn đã đặt hàng thành công lúc " +
                            new SimpleDateFormat("HH:mm dd/MM/yyyy",Locale.getDefault()).format(new Date());
                    dbRef.child("notifications").child(userId)
                            .push().setValue(new NotificationItem(System.currentTimeMillis(), msg));

                    new AlertDialog.Builder(PayActivity.this)
                            .setTitle("Đặt hàng thành công")
                            .setMessage("Mã đơn: " + orderId + "\nTổng: " +
                                    NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(totalAmount))
                            .setPositiveButton("OK", (d,w) -> {
                                Intent intent = new Intent(PayActivity.this, OrderSuccessActivity.class);
                                intent.putExtra("orderId", orderId);
                                intent.putExtra("totalAmount", totalAmount);
                                intent.putExtra("cartItems", new ArrayList<>(items));
                                startActivity(intent);
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi lưu đơn:", e);
                    Toast.makeText(this, "Lỗi lưu đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createAndSaveOrder(String paymentMethod, String amount, boolean isZaloPaySuccess) {
        createAndSaveOrder(paymentMethod, amount, isZaloPaySuccess, null);
    }

}

