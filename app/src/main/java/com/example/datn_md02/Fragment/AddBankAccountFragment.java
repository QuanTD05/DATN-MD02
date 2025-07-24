package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.datn_md02.Model.BankAccount;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddBankAccountFragment extends Fragment {

    private EditText edtBankName, edtCardNumber, edtExpiryDate, edtCardHolder;
    private CheckBox checkboxDefault;
    private Button btnSave;
    private TextView tvCardNumberPreview, tvCardHolderPreview, tvExpiryPreview;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;

    private boolean isEditMode = false;
    private String currentId = null;

    public static AddBankAccountFragment newInstance(BankAccount account) {
        AddBankAccountFragment fragment = new AddBankAccountFragment();
        Bundle args = new Bundle();
        args.putString("id", account.getId());
        args.putString("bankName", account.getBankName());
        args.putString("cardNumber", account.getCardNumber());
        args.putString("expiryDate", account.getExpiryDate());
        args.putString("cardHolderName", account.getCardHolderName());
        args.putBoolean("isDefault", account.isDefault());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_bank_account, container, false);

        edtBankName = view.findViewById(R.id.edtBankName);
        edtCardNumber = view.findViewById(R.id.edtCardNumber);
        edtExpiryDate = view.findViewById(R.id.edtExpiryDate);
        edtCardHolder = view.findViewById(R.id.edtCardHolder);
        checkboxDefault = view.findViewById(R.id.checkboxDefault);
        btnSave = view.findViewById(R.id.btnSaveBankAccount);
        tvCardNumberPreview = view.findViewById(R.id.tvCardNumberPreview);
        tvCardHolderPreview = view.findViewById(R.id.tvCardHolderPreview);
        tvExpiryPreview = view.findViewById(R.id.tvExpiryPreview);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
        }

        // Nếu sửa
        if (getArguments() != null) {
            currentId = getArguments().getString("id");
            edtBankName.setText(getArguments().getString("bankName"));
            edtCardNumber.setText(getArguments().getString("cardNumber"));
            edtExpiryDate.setText(getArguments().getString("expiryDate"));
            edtCardHolder.setText(getArguments().getString("cardHolderName"));
            checkboxDefault.setChecked(getArguments().getBoolean("isDefault"));
            isEditMode = true;

            // Gán preview ban đầu
            updatePreviews();
        }

        btnSave.setOnClickListener(v -> saveBankAccount());

        setupPreviewWatchers();
        updatePreviews();

        return view;
    }

    private void setupPreviewWatchers() {
        edtCardNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String number = s.toString();
                if (number.length() >= 4) {
                    String lastFour = number.substring(number.length() - 4);
                    tvCardNumberPreview.setText("**** **** **** " + lastFour);
                } else {
                    tvCardNumberPreview.setText("**** **** **** XXXX");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        edtCardHolder.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCardHolderPreview.setText("Card Holder Name\n" + s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        edtExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvExpiryPreview.setText("Expiry Date\n" + s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updatePreviews() {
        String number = edtCardNumber.getText().toString();
        if (number.length() >= 4) {
            tvCardNumberPreview.setText("**** **** **** " + number.substring(number.length() - 4));
        } else {
            tvCardNumberPreview.setText("**** **** **** XXXX");
        }
        tvCardHolderPreview.setText("Card Holder Name\n" + edtCardHolder.getText().toString());
        tvExpiryPreview.setText("Expiry Date\n" + edtExpiryDate.getText().toString());
    }

    private void saveBankAccount() {
        String bankName = edtBankName.getText().toString().trim();
        String cardNumber = edtCardNumber.getText().toString().trim();
        String expiryDate = edtExpiryDate.getText().toString().trim();
        String cardHolder = edtCardHolder.getText().toString().trim();
        boolean isDefault = checkboxDefault.isChecked();

        if (TextUtils.isEmpty(bankName) || TextUtils.isEmpty(cardNumber)
                || TextUtils.isEmpty(expiryDate) || TextUtils.isEmpty(cardHolder)) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = isEditMode ? currentId : userRef.child("bankAccounts").push().getKey();

        // ✅ SỬA: ĐÚNG THỨ TỰ constructor
        BankAccount account = new BankAccount(id, bankName, cardHolder, cardNumber, expiryDate, isDefault);

        if (isDefault) {
            userRef.child("bankAccounts").get().addOnSuccessListener(snapshot -> {
                for (DataSnapshot data : snapshot.getChildren()) {
                    userRef.child("bankAccounts").child(data.getKey()).child("default").setValue(false);
                }
                saveToFirebase(id, account);
            });
        } else {
            saveToFirebase(id, account);
        }
    }

    private void saveToFirebase(String id, BankAccount account) {
        userRef.child("bankAccounts").child(id).setValue(account)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), isEditMode ? "Đã cập nhật thẻ" : "Đã thêm thẻ", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi khi lưu thẻ", Toast.LENGTH_SHORT).show());
    }
}
