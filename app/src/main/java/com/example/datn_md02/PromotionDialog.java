package com.example.datn_md02;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.PromotionAdapter;
import com.example.datn_md02.Model.Promotion;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PromotionDialog extends Dialog {

    public interface OnPromotionSelectedListener {
        void onPromotionSelected(Promotion promotion);
    }

    private final Set<String> productIdsTrongGio;
    private final OnPromotionSelectedListener listener;

    private RecyclerView rv;
    private Button btnClose;

    private final List<Promotion> promotionList = new ArrayList<>();
    private PromotionAdapter adapter;

    public PromotionDialog(@NonNull Context context,
                           Set<String> productIdsTrongGio,
                           OnPromotionSelectedListener listener) {
        super(context);
        this.productIdsTrongGio = productIdsTrongGio != null ? productIdsTrongGio : new HashSet<>();
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ---- UI đơn giản dựng bằng code ----
        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (12 * getContext().getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        rv = new RecyclerView(getContext());
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        root.addView(rv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        btnClose = new Button(getContext());
        btnClose.setText("Đóng");
        root.addView(btnClose, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setTitle("Chọn mã khuyến mãi");

        adapter = new PromotionAdapter(promotionList, promo -> {
            if (listener != null) listener.onPromotionSelected(promo);
            dismiss();
        });
        rv.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dismiss());

        loadPromotions();
    }

    private void loadPromotions() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("promotions");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                promotionList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Promotion promo = snap.getValue(Promotion.class);
                    if (promo == null) continue;

                    boolean active = isActive(promo);
                    boolean dateOk = isWithinDate(promo);
                    boolean matchProducts = isApplicableToCart(promo, productIdsTrongGio);

                    if (active && dateOk && matchProducts) {
                        promotionList.add(promo);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private boolean isActive(Promotion promo) {
        // Tùy model bạn: is_active hoặc getIs_active
        try {
            // nếu Promotion có getter boolean is_active
            return promo.isIs_active();
        } catch (Throwable ignore) {
            // fallback nếu khác tên
            try { return (boolean) Promotion.class.getMethod("getIs_active").invoke(promo); }
            catch (Throwable e) { return true; } // nếu không có field, coi như active
        }
    }

    private boolean isWithinDate(Promotion promo) {
        String start = promo.getStart_date();
        String end   = promo.getEnd_date();
        if (start == null || end == null) return false;

        String[] fmts = new String[]{"yyyy-MM-dd", "yyyy-M-d"};
        Date today = new Date();
        for (String f : fmts) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(f, Locale.getDefault());
                sdf.setLenient(false);
                Date s = sdf.parse(start);
                Date e = sdf.parse(end);
                if (s != null && e != null && !today.before(s) && !today.after(e)) {
                    return true;
                }
            } catch (ParseException ignore) {}
        }
        return false;
    }

    private boolean isApplicableToCart(Promotion promo, Set<String> productIdsTrongGio) {
        boolean applyAll = promo.isApply_to_all();
        if (applyAll) return true;

        List<String> ids = promo.getApply_to_product_ids();
        if (ids == null || ids.isEmpty()) return false;

        for (String id : ids) {
            if (productIdsTrongGio.contains(id)) return true;
        }
        return false;
    }
}