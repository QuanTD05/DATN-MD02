package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.BankAccountAdapter;
import com.example.datn_md02.Model.BankAccount;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BankAccountFragment extends Fragment {

    private RecyclerView recyclerView;
    private BankAccountAdapter adapter;
    private List<BankAccount> bankList;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private ImageView btnAddBank;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bank_account, container, false);

        recyclerView = view.findViewById(R.id.recyclerBankAccounts);
        btnAddBank = view.findViewById(R.id.btnAddBank);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ImageView btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed(); // hoặc requireActivity().onBackPressed();
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
            loadBankAccounts();
        }

        btnAddBank.setOnClickListener(v -> {
            // Mở màn hình thêm mới
            AddBankAccountFragment addFragment = new AddBankAccountFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_content, addFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadBankAccounts() {
        userRef.child("bankAccounts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bankList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    BankAccount account = data.getValue(BankAccount.class);
                    if (account != null) {
                        account.setId(data.getKey()); // Gán ID để phục vụ sửa/xóa
                        bankList.add(account);
                    }
                }
                adapter = new BankAccountAdapter(getContext(), bankList, userRef, getParentFragmentManager());
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Không thể tải danh sách thẻ ngân hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
