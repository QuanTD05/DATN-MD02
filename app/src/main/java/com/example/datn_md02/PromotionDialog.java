package com.example.datn_md02;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.PromotionAdapter;
import com.example.datn_md02.Model.Promotion;
import com.example.datn_md02.R;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PromotionDialog extends Dialog {

    public interface OnPromotionSelectedListener {
        void onPromotionSelected(Promotion promotion);
    }

    private OnPromotionSelectedListener listener;
    private Context context;
    private List<Promotion> promotionList;
    private PromotionAdapter adapter;

    public PromotionDialog(@NonNull Context context, OnPromotionSelectedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_dialog);

        RecyclerView recyclerView = findViewById(R.id.rvPromotions);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        promotionList = new ArrayList<>();
        adapter = new PromotionAdapter(promotionList, promo -> {
            listener.onPromotionSelected(promo);
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        loadPromotions();
    }

    private void loadPromotions() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("promotions");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                promotionList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Promotion promo = snap.getValue(Promotion.class);
                    if (promo != null && promo.isIs_active() && promo.isApply_to_all() && isValidDate(promo)) {
                        promotionList.add(promo);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private boolean isValidDate(Promotion promo) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            Date today = new Date();
            Date start = sdf.parse(promo.getStart_date());
            Date end = sdf.parse(promo.getEnd_date());
            return start != null && end != null && !today.before(start) && !today.after(end);
        } catch (ParseException e) {
            return false;
        }
    }
}
