package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.NotificationAdapter;
import com.example.datn_md02.Model.NotificationItem;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class PromotionNotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private final List<NotificationItem> promoList = new ArrayList<>();
    private ValueEventListener listener;
    private final SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private OnNotificationCountChangeListener countListener;
    private final int tabPosition = 0;

    public static PromotionNotificationFragment newInstance(OnNotificationCountChangeListener listener) {
        PromotionNotificationFragment fragment = new PromotionNotificationFragment();
        fragment.countListener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(getContext(), promoList);
        recyclerView.setAdapter(adapter);

        loadPromotions();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) {
            FirebaseDatabase.getInstance().getReference("notifications").removeEventListener(listener);
        }
    }

    private void loadPromotions() {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference("notifications");
        String uid = FirebaseAuth.getInstance().getUid();

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                promoList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    boolean isBroadcast = child.hasChild("title") || child.hasChild("message") || child.hasChild("type");
                    if (isBroadcast) {
                        NotificationItem item = mapToNotification(child);
                        if (item != null && isPromoMessage(item)) {
                            item.id = child.getKey();
                            // broadcast thường không có trạng thái read -> mặc định false
                            item.read = false;
                            ensureMessageFromStructuredFields(item);
                            promoList.add(item);
                        }
                        continue;
                    }

                    if (uid != null && uid.equals(child.getKey())) {
                        for (DataSnapshot n : child.getChildren()) {
                            NotificationItem item = mapToNotification(n);
                            if (item != null && isPromoMessage(item)) {
                                item.id = n.getKey();
                                Boolean readFlag = n.child("read").getValue(Boolean.class);
                                item.read = readFlag != null && readFlag;
                                ensureMessageFromStructuredFields(item);
                                promoList.add(item);
                            }
                        }
                    }
                }

                promoList.sort((a, b) -> Long.compare(getTsMillis(a.getTimestamp()), getTsMillis(b.getTimestamp())));
                Collections.reverse(promoList);

                adapter.notifyDataSetChanged();

                if (countListener != null) {
                    // gửi số lượng thông báo chưa đọc
                    int unreadCount = 0;
                    for (NotificationItem ni : promoList) {
                        if (!ni.read) unreadCount++;
                    }
                    countListener.onCountChanged(tabPosition, unreadCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải thông báo khuyến mãi", Toast.LENGTH_SHORT).show();
                }
            }
        };

        root.addValueEventListener(listener);
    }

    @Nullable
    private NotificationItem mapToNotification(@NonNull DataSnapshot snap) {
        NotificationItem item = null;
        try {
            item = snap.getValue(NotificationItem.class);
        } catch (Exception ignore) {}

        if (item == null) item = new NotificationItem();

        try {
            Object title = getOrNull(snap, "title");
            Object message = getOrNull(snap, "message");
            Object timestamp = getOrNull(snap, "timestamp");
            Object type = getOrNull(snap, "type");

            Object code = getOrNull(snap, "code");
            Object discount = getOrNull(snap, "discount");
            Object applyAll = getOrNull(snap, "apply_to_all");
            Object startDate = getOrNull(snap, "start_date");
            Object endDate = getOrNull(snap, "end_date");
            Object productNamesText = getOrNull(snap, "product_names_text");

            List<String> applyIds = new ArrayList<>();
            try {
                for (DataSnapshot idSnap : snap.child("apply_to_product_ids").getChildren()) {
                    String pid = idSnap.getValue(String.class);
                    if (pid != null) applyIds.add(pid);
                }
            } catch (Exception ignore) {}

            if (item.title == null) item.title = title != null ? String.valueOf(title) : "🎁 Khuyến mãi";
            if (item.message == null) item.message = message != null ? String.valueOf(message) : null;
            if (type != null) item.type = String.valueOf(type);
            item.setTimestamp(timestamp);

            putIfFieldExists(item, "code", code);
            putIfFieldExists(item, "discount", discount);
            putIfFieldExists(item, "apply_to_all", applyAll);
            putIfFieldExists(item, "start_date", startDate);
            putIfFieldExists(item, "end_date", endDate);
            putIfFieldExists(item, "product_names_text", productNamesText);
            putIfFieldExists(item, "apply_to_product_ids", applyIds);

            return item;
        } catch (Exception e) {
            return null;
        }
    }

    private void ensureMessageFromStructuredFields(@NonNull NotificationItem item) {
        if (!TextUtils.isEmpty(item.message) && item.message.contains("Hiệu lực")) return;

        String code = safeString(getField(item, "code"));
        String desc = "";
        String discount = safeString(getField(item, "discount"));
        String start = safeString(getField(item, "start_date"));
        String end = safeString(getField(item, "end_date"));
        String applyAllText = "TẤT CẢ sản phẩm";
        Object applyAllObj = getField(item, "apply_to_all");
        if (applyAllObj instanceof Boolean && !((Boolean) applyAllObj)) {
            String namesText = safeString(getField(item, "product_names_text"));
            if (TextUtils.isEmpty(namesText)) {
                namesText = "Một số sản phẩm";
            }
            applyAllText = namesText;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Mã: ").append(code);
        if (!TextUtils.isEmpty(desc)) sb.append(" - ").append(desc);
        if (!TextUtils.isEmpty(discount)) sb.append(" (").append(discount).append("%)");
        sb.append("\nÁp dụng: ").append(applyAllText);
        if (!TextUtils.isEmpty(start) || !TextUtils.isEmpty(end)) {
            sb.append("\nHiệu lực: ").append(start).append(" → ").append(end);
        }

        item.message = sb.toString();
        if (TextUtils.isEmpty(item.title)) item.title = "🎁 Khuyến mãi mới!";
        if (TextUtils.isEmpty(item.type)) item.type = "promo";
    }

    private boolean isPromoMessage(@NonNull NotificationItem item) {
        try {
            if (item.type != null) {
                String t = item.type.toLowerCase(Locale.getDefault());
                if (t.equals("promo") || t.equals("promotion")) return true;
            }
        } catch (Exception ignore) {}

        String t = item.title != null ? item.title : "";
        String m = item.message != null ? item.message : "";
        String content = (t + " " + m).toLowerCase(Locale.getDefault());
        return content.contains("mã")
                || content.contains("giảm giá")
                || content.contains("voucher")
                || content.contains("khuyến mãi")
                || content.contains("khuyen mai");
    }

    private long getTsMillis(Object ts) {
        if (ts == null) return 0L;
        try {
            if (ts instanceof Long) return (Long) ts;
            if (ts instanceof Integer) return ((Integer) ts).longValue();
            if (ts instanceof Double) return ((Double) ts).longValue();
            if (ts instanceof String) {
                String s = (String) ts;
                try {
                    return Instant.parse(s).toEpochMilli();
                } catch (Exception ignore) {
                    String[] patterns = new String[] {
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                            "yyyy-MM-dd'T'HH:mm:ss'Z'",
                            "yyyy-MM-dd'T'HH:mm:ss.SSS",
                            "yyyy-MM-dd'T'HH:mm:ss"
                    };
                    for (String p : patterns) {
                        try {
                            return new SimpleDateFormat(p, Locale.getDefault()).parse(s).getTime();
                        } catch (ParseException ignored) {}
                    }
                }
            }
        } catch (Exception ignore) {}
        return 0L;
    }

    private Object getOrNull(DataSnapshot snap, String key) {
        DataSnapshot c = snap.child(key);
        return c.exists() ? c.getValue() : null;
    }

    private void putIfFieldExists(NotificationItem item, String field, Object value) {
        if (value == null) return;
        try {
            java.lang.reflect.Field f = item.getClass().getField(field);
            f.set(item, value);
        } catch (NoSuchFieldException nsf) {
            String mName = "set" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
            try {
                item.getClass().getMethod(mName, value.getClass()).invoke(item, value);
            } catch (Exception ignore) {}
        } catch (Exception ignore) {}
    }

    private Object getField(NotificationItem item, String field) {
        try {
            java.lang.reflect.Field f = item.getClass().getField(field);
            return f.get(item);
        } catch (Exception ignore) { return null; }
    }

    private String safeString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
