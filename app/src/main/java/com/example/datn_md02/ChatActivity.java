    package com.example.datn_md02;

    import android.net.Uri;
    import android.os.Bundle;
    import android.util.Log;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
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
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.UUID;

    public class ChatActivity extends AppCompatActivity {

        private RecyclerView rvMessages;
        private EditText etMessage;
        private Button btnSend;
        private ImageButton btnSendImage;
        private TextView tvTitle;

        private MessageAdapter messageAdapter;
        private final List<Message> messageList = new ArrayList<>();

        private String currentUserEmail, staffEmail;

        private final FirebaseDatabase database = FirebaseDatabase.getInstance();
        private final FirebaseStorage storage = FirebaseStorage.getInstance();

        private Uri selectedImageUri;

        private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        confirmSendImage(uri);
                    }
                }
        );

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat);

            rvMessages = findViewById(R.id.rvMessages);
            etMessage = findViewById(R.id.etMessage);
            btnSend = findViewById(R.id.btnSend);
            btnSendImage = findViewById(R.id.btnSelectImage);
            tvTitle = findViewById(R.id.tvChatTitle);

            currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            staffEmail = getIntent().getStringExtra("staff_email");

            Log.d("ChatActivity", "User: " + currentUserEmail + " | Staff: " + staffEmail);

            loadStaffInfo();

            messageAdapter = new MessageAdapter(messageList, currentUserEmail);
            rvMessages.setLayoutManager(new LinearLayoutManager(this));
            rvMessages.setAdapter(messageAdapter);

            loadMessages();

            btnSend.setOnClickListener(v -> {
                String text = etMessage.getText().toString().trim();
                if (!text.isEmpty()) {
                    sendTextMessage(text);
                    etMessage.setText("");
                }
            });

            btnSendImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        private void confirmSendImage(Uri imageUri) {
            new android.app.AlertDialog.Builder(ChatActivity.this)
                    .setTitle("Gửi ảnh")
                    .setMessage("Bạn có chắc chắn muốn gửi ảnh này không?")
                    .setPositiveButton("Gửi", (dialog, which) -> uploadImageAndSend(imageUri))
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        private void loadStaffInfo() {
            DatabaseReference userRef = database.getReference("users");
            userRef.orderByChild("email").equalTo(staffEmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String fullName = ds.child("fullName").getValue(String.class);
                                if (fullName != null) {
                                    tvTitle.setText(fullName); // chỉ hiển thị tên
                                } else {
                                    tvTitle.setText("Đang chat");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            tvTitle.setText("Đang chat");
                        }
                    });
        }

        private void sendTextMessage(String text) {
            long timestamp = System.currentTimeMillis();
            Message msg = new Message(currentUserEmail, staffEmail, text, timestamp);
            database.getReference("chats").push().setValue(msg);
        }

        private void uploadImageAndSend(Uri uri) {
            StorageReference storageRef = storage.getReference("chat_images/" + UUID.randomUUID() + ".jpg");
            storageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot ->
                            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri ->
                                    sendImageMessage(downloadUri.toString())
                            ).addOnFailureListener(e ->
                                    Toast.makeText(ChatActivity.this, "Lỗi lấy URL ảnh", Toast.LENGTH_SHORT).show()
                            )
                    ).addOnFailureListener(e ->
                            Toast.makeText(ChatActivity.this, "Tải ảnh thất bại", Toast.LENGTH_SHORT).show()
                    );
        }

        private void sendImageMessage(String imageUrl) {
            long timestamp = System.currentTimeMillis();
            Message msg = new Message(currentUserEmail, staffEmail, imageUrl, timestamp, true);
            database.getReference("chats").push().setValue(msg);
        }

        private void loadMessages() {
            DatabaseReference ref = database.getReference("chats");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    messageList.clear();
                    List<String> keysToMarkSeen = new ArrayList<>();

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Message msg = ds.getValue(Message.class);
                        if (msg == null || msg.getSender() == null || msg.getReceiver() == null) continue;

                        boolean isConversation =
                                (msg.getSender().equalsIgnoreCase(currentUserEmail) && msg.getReceiver().equalsIgnoreCase(staffEmail)) ||
                                        (msg.getSender().equalsIgnoreCase(staffEmail) && msg.getReceiver().equalsIgnoreCase(currentUserEmail));

                        if (isConversation) {
                            messageList.add(msg);

                            // Nếu tin nhắn gửi tới mình mà chưa seen thì lưu key để đánh dấu seen
                            if (msg.getReceiver().equalsIgnoreCase(currentUserEmail) && !msg.isSeen()) {
                                keysToMarkSeen.add(ds.getKey());
                            }
                        }
                    }

                    // Đánh dấu tất cả tin nhắn chưa seen là đã seen
                    for (String key : keysToMarkSeen) {
                        database.getReference("chats").child(key).child("seen").setValue(true);
                    }

                    messageAdapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) {
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ChatActivity.this, "Không thể tải tin nhắn", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
