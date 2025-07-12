package com.example.datn_md02;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.MessageAdapter;
import com.example.datn_md02.Model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private TextView tvTitle;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    private String currentUserEmail, staffEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvTitle = findViewById(R.id.tvChatTitle);

        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        staffEmail = getIntent().getStringExtra("staff_email");

        loadStaffInfo();

        messageAdapter = new MessageAdapter(messageList, currentUserEmail);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);

        loadMessages();

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(currentUserEmail, staffEmail, text);
                etMessage.setText("");
            }
        });
    }

    private void loadStaffInfo() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.orderByChild("email").equalTo(staffEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String fullName = ds.child("fullName").getValue(String.class);
                            if (fullName != null) {
                                tvTitle.setText(fullName + " (" + staffEmail + ")");
                            } else {
                                tvTitle.setText("Đang chat với " + staffEmail);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvTitle.setText("Đang chat với " + staffEmail);
                    }
                });
    }

    private void sendMessage(String sender, String receiver, String text) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chats");

        long currentTime = System.currentTimeMillis();  // ⏱️ Thời gian hiện tại

        Message msg = new Message(sender, receiver, text, currentTime);  // ✅ Dùng constructor đầy đủ

        ref.push().setValue(msg);
    }


    private void loadMessages() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message msg = ds.getValue(Message.class);
                    if (msg == null || msg.getSender() == null || msg.getReceiver() == null)
                        continue;

                    boolean isSenderOrReceiver =
                            (msg.getSender().equals(currentUserEmail) && msg.getReceiver().equals(staffEmail)) ||
                                    (msg.getSender().equals(staffEmail) && msg.getReceiver().equals(currentUserEmail));

                    if (isSenderOrReceiver) {
                        messageList.add(msg);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                rvMessages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Không thể tải tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
