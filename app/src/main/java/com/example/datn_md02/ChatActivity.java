package com.example.datn_md02;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Adapter.MessageAdapter;
import com.example.datn_md02.Model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "chat_user_channel";
    private static final String PREFS = "chat_user_prefs";
    private static final String KEY_LAST_TS = "last_ts";
    private long lastTs = 0L;

    private ImageView ivChatAvatar, btnBack;
    private TextView tvChatTitle;
    private RecyclerView rv;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnImage;

    private MessageAdapter adapter;
    private final List<Message> msgs = new ArrayList<>();

    private String meEmail, partnerEmail, partnerName, partnerAvatar, meUid;
    private DatabaseReference chatsRef, statusMeRef;
    private FirebaseStorage storage;
    private ActivityResultLauncher<String> pickImage;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_chat);

        createNotificationChannel();
        requestNotificationPermission();

        lastTs = getSharedPreferences(PREFS, MODE_PRIVATE).getLong(KEY_LAST_TS, 0L);

        ivChatAvatar = findViewById(R.id.ivChatAvatar);
        tvChatTitle  = findViewById(R.id.tvChatTitle);
        rv           = findViewById(R.id.rvMessages);
        etMessage    = findViewById(R.id.etMessage);
        btnSend      = findViewById(R.id.btnSend);
        btnImage     = findViewById(R.id.btnSelectImage);
        btnBack      = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            meEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            meUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        partnerEmail  = getIntent().getStringExtra("partner_email");
        partnerName   = getIntent().getStringExtra("partner_name");
        partnerAvatar = getIntent().getStringExtra("partner_avatar");

        handleDeepLink(getIntent());

        if (TextUtils.isEmpty(partnerEmail)) {
            Toast.makeText(this, "Thiếu người nhận", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        storage  = FirebaseStorage.getInstance();

        tvChatTitle.setText(!TextUtils.isEmpty(partnerName) ? partnerName : partnerEmail);
        Glide.with(this)
                .load(partnerAvatar)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(ivChatAvatar);

        // 🔹 Lấy trạng thái online từ users
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("email").equalTo(partnerEmail)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            Boolean on = userSnap.child("status").getValue(Boolean.class);
                            String title = !TextUtils.isEmpty(partnerName) ? partnerName : partnerEmail;
                            tvChatTitle.setText(title + (on != null && on ? " (Online)" : " (Offline)"));
                            break;
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });

        adapter = new MessageAdapter(msgs, meEmail);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rv.setLayoutManager(lm);
        rv.setAdapter(adapter);

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                msgs.clear();
                for (DataSnapshot ds : snap.getChildren()) {
                    String s  = ds.child("sender").getValue(String.class);
                    String r  = ds.child("receiver").getValue(String.class);
                    String c  = ds.child("content").getValue(String.class);
                    String legacyMsg = ds.child("message").getValue(String.class);
                    String legacyImg = ds.child("imageUrl").getValue(String.class);
                    Boolean im = ds.child("image").getValue(Boolean.class);
                    Long ts   = ds.child("timestamp").getValue(Long.class);

                    if (s == null || r == null || ts == null) continue;
                    if (meEmail == null || TextUtils.isEmpty(partnerEmail)) continue;

                    boolean conv = (s.equalsIgnoreCase(meEmail) && r.equalsIgnoreCase(partnerEmail))
                            || (s.equalsIgnoreCase(partnerEmail) && r.equalsIgnoreCase(meEmail));
                    if (!conv) continue;

                    if (Boolean.TRUE.equals(im)) {
                        if (TextUtils.isEmpty(c) && !TextUtils.isEmpty(legacyImg)) {
                            c = legacyImg;
                        }
                        msgs.add(new Message(s, r, c, ts, true));
                    } else {
                        if (TextUtils.isEmpty(c) && !TextUtils.isEmpty(legacyMsg)) {
                            c = legacyMsg;
                        }
                        msgs.add(new Message(s, r, c, ts));
                    }

                    if (!s.equalsIgnoreCase(meEmail) && ts > lastTs) {
                        String shown = Boolean.TRUE.equals(im) ? "[Hình ảnh]" : (c != null ? c : "");
                        sendLocalNotification(s, shown);
                        lastTs = ts;
                        getSharedPreferences(PREFS, MODE_PRIVATE)
                                .edit().putLong(KEY_LAST_TS, lastTs).apply();
                    }
                }
                adapter.notifyDataSetChanged();
                if (!msgs.isEmpty()) rv.scrollToPosition(msgs.size() - 1);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        btnSend.setOnClickListener(v -> sendTextMessage());

        pickImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    new AlertDialog.Builder(this)
                            .setTitle("Gửi ảnh")
                            .setMessage("Bạn có muốn gửi ảnh này không?")
                            .setPositiveButton("Gửi", (d, w) -> uploadImage(uri))
                            .setNegativeButton("Hủy", null)
                            .show();
                }
        );
        btnImage.setOnClickListener(v -> pickImage.launch("image/*"));
    }

    private void sendTextMessage() {
        if (meEmail == null || TextUtils.isEmpty(partnerEmail)) return;
        String txt = etMessage.getText().toString().trim();
        if (txt.isEmpty()) return;

        long ts = System.currentTimeMillis();
        HashMap<String, Object> m = new HashMap<>();
        m.put("sender", meEmail);
        m.put("receiver", partnerEmail);
        m.put("content", txt);
        m.put("message", txt);
        m.put("timestamp", ts);
        m.put("image", false);

        chatsRef.push().setValue(m);
        etMessage.setText("");
    }

    private void uploadImage(Uri uri) {
        if (meEmail == null || TextUtils.isEmpty(partnerEmail)) return;

        String name = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference("chat_images/" + name);

        ref.putFile(uri)
                .addOnSuccessListener(task -> ref.getDownloadUrl()
                        .addOnSuccessListener(url -> {
                            long ts = System.currentTimeMillis();
                            HashMap<String, Object> m = new HashMap<>();
                            m.put("sender",    meEmail);
                            m.put("receiver",  partnerEmail);
                            m.put("content",   url.toString());
                            m.put("imageUrl",  url.toString());
                            m.put("image",     true);
                            m.put("timestamp", ts);
                            chatsRef.push().setValue(m);
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gửi ảnh thất bại", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "myapp".equals(data.getScheme()) && "chat".equals(data.getHost())) {
            String peerIdSanitized = data.getQueryParameter("peerId");
            String peerNameParam   = data.getQueryParameter("peerName");
            if (!TextUtils.isEmpty(peerIdSanitized)) {
                partnerEmail = peerIdSanitized.replace(",", ".");
            }
            if (!TextUtils.isEmpty(peerNameParam)) {
                partnerName = peerNameParam;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (meUid != null) {
            statusMeRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(meUid)
                    .child("status");
            statusMeRef.setValue(true);
            statusMeRef.onDisconnect().setValue(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (statusMeRef != null) statusMeRef.setValue(false);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Chat (User)", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Thông báo tin nhắn mới (User)");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{ Manifest.permission.POST_NOTIFICATIONS }, 1101
            );
        }
    }

    private void sendLocalNotification(String sender, String message) {
        String deep = "myapp://chat?peerId=" + sanitizeEmail(sender) +
                "&peerName=" + Uri.encode(getNameFromEmail(sender));
        Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(deep));
        PendingIntent pi = PendingIntent.getActivity(
                this, (int) System.currentTimeMillis(), open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle("Tin nhắn mới từ " + sender)
                .setContentText(message != null ? message : "")
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    private String sanitizeEmail(String e) {
        return e == null ? "" : e.replaceAll("[.#$\\[\\]]", ",");
    }

    private String getNameFromEmail(String email) {
        if (TextUtils.isEmpty(email)) return "";
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
