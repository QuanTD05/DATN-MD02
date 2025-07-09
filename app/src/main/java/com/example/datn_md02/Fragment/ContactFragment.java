package com.example.datn_md02.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.example.datn_md02.ChatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.StaffAdapter;
import com.example.datn_md02.Model.User;
import com.example.datn_md02.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {

    private RecyclerView rvStaff;
    private EditText etSearch;
    private StaffAdapter adapter;
    private List<User> staffList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        rvStaff = view.findViewById(R.id.rvStaff);
        etSearch = view.findViewById(R.id.etSearch);
        rvStaff.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StaffAdapter(staffList, staff -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("staff_name", staff.getName());
            intent.putExtra("staff_email", staff.getEmail());
            startActivity(intent);
        });
        rvStaff.setAdapter(adapter);

        // 🔍 Xử lý tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.searchByName(s.toString());
            }
        });

        loadStaffFromFirebase();

        return view;
    }

    private void loadStaffFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> newList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    if (user != null && "staff".equalsIgnoreCase(user.getRole())) {
                        newList.add(user);
                    }
                }
                adapter.updateList(newList); // ✅ đảm bảo cập nhật dữ liệu và tìm kiếm vẫn hoạt động
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ContactFragment", "Firebase load error: " + error.getMessage());
            }
        });
    }
}
