package com.example.datn_md02;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.MessageAdapter;
import com.example.datn_md02.Model.Message;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private ImageView ivChatAvatar;
    private TextView tvChatTitle;
    private RecyclerView rv;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnImage;

    private MessageAdapter adapter;
    private final List<Message> msgs = new ArrayList<>();

    private String meEmail, partnerEmail, partnerName, partnerAvatar;
    private DatabaseReference chatsRef, statusMeRef, statusPartnerRef;
    private FirebaseStorage storage;
    private ActivityResultLauncher<String> pickImage;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_chat);

        ivChatAvatar = findViewById(R.id.ivChatAvatar);
        tvChatTitle  = findViewById(R.id.tvChatTitle);
        rv           = findViewById(R.id.rvMessages);
        etMessage    = findViewById(R.id.etMessage);
        btnSend      = findViewById(R.id.btnSend);
        btnImage     = findViewById(R.id.btnSelectImage);

        meEmail       = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        partnerEmail  = getIntent().getStringExtra("partner_email");
        partnerName   = getIntent().getStringExtra("partner_name");
        partnerAvatar = getIntent().getStringExtra("partner_avatar");

        chatsRef      = FirebaseDatabase.getInstance().getReference("chats");
        storage       = FirebaseStorage.getInstance();

        // Header
        tvChatTitle.setText(partnerName);
        Glide.with(this)
                .load(partnerAvatar)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(ivChatAvatar);

        // Listen partner presence
        statusPartnerRef = FirebaseDatabase.getInstance()
                .getReference("status")
                .child(sanitizeEmail(partnerEmail));
        statusPartnerRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                Boolean on = snap.getValue(Boolean.class);
                tvChatTitle.setText(partnerName + (on != null && on ? " (Online)" : " (Offline)"));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        // Recycler + adapter
        adapter = new MessageAdapter(msgs, meEmail);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Load messages
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                msgs.clear();
                for (DataSnapshot ds : snap.getChildren()) {
                    Message m = ds.getValue(Message.class);
                    if (m == null) continue;
                    boolean conv = (m.getSender().equalsIgnoreCase(meEmail) && m.getReceiver().equalsIgnoreCase(partnerEmail))
                            || (m.getSender().equalsIgnoreCase(partnerEmail) && m.getReceiver().equalsIgnoreCase(meEmail));
                    if (conv) msgs.add(m);
                }
                adapter.notifyDataSetChanged();
                rv.scrollToPosition(msgs.size()-1);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        // Send text
        btnSend.setOnClickListener(v -> {
            String txt = etMessage.getText().toString().trim();
            if (txt.isEmpty()) return;
            long ts = System.currentTimeMillis();
            Message m = new Message(meEmail, partnerEmail, txt, ts);
            chatsRef.push().setValue(m);
            etMessage.setText("");
        });

        // Pick/send image
        pickImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    StorageReference ref = storage.getReference("chat_images/" + UUID.randomUUID());
                    ref.putFile(uri).addOnSuccessListener(t -> ref.getDownloadUrl()
                            .addOnSuccessListener(url -> {
                                long ts = System.currentTimeMillis();
                                Message m = new Message(meEmail, partnerEmail,
                                        url.toString(), ts, true);
                                chatsRef.push().setValue(m);
                            }));
                }
        );
        btnImage.setOnClickListener(v -> pickImage.launch("image/*"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Mark me online
        statusMeRef = FirebaseDatabase.getInstance()
                .getReference("status")
                .child(sanitizeEmail(meEmail));
        statusMeRef.setValue(true);
        statusMeRef.onDisconnect().setValue(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (statusMeRef != null) statusMeRef.setValue(false);
    }

    private String sanitizeEmail(String e) {
        return e.replaceAll("[.#\\$\\[\\]]", ",");
    }
}
