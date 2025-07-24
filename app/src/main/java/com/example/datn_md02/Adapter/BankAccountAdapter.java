// BankAccountAdapter.java
package com.example.datn_md02.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentManager;

import com.example.datn_md02.Fragment.AddBankAccountFragment;
import com.example.datn_md02.Model.BankAccount;
import com.example.datn_md02.R;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class BankAccountAdapter extends RecyclerView.Adapter<BankAccountAdapter.ViewHolder> {

    private final List<BankAccount> bankList;
    private final Context context;
    private final DatabaseReference userRef;
    private final FragmentManager fragmentManager;

    public BankAccountAdapter(Context context, List<BankAccount> bankList, DatabaseReference userRef, FragmentManager fragmentManager) {
        this.context = context;
        this.bankList = bankList;
        this.userRef = userRef;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bank_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BankAccount bank = bankList.get(position);
        holder.txtBankName.setText(bank.getBankName());
        holder.txtCardHolder.setText("Chủ thẻ: " + bank.getCardHolderName());
        holder.txtCardNumber.setText("Số tài khoản: " + bank.getCardNumber());
        holder.txtExpiry.setText("Hết hạn: " + bank.getExpiryDate());
        holder.radioDefault.setChecked(bank.isDefault());

        holder.radioDefault.setOnClickListener(v -> {
            for (BankAccount b : bankList) {
                b.setDefault(b.getId().equals(bank.getId()));
            }
            notifyDataSetChanged();
            for (BankAccount b : bankList) {
                userRef.child("bankAccounts").child(b.getId()).child("default").setValue(b.isDefault());
            }
            Toast.makeText(context, "Đã chọn thẻ mặc định", Toast.LENGTH_SHORT).show();
        });

        holder.imgEdit.setOnClickListener(v -> {
            AddBankAccountFragment fragment = AddBankAccountFragment.newInstance(bank);
            fragmentManager.beginTransaction()
                    .replace(R.id.main_content, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        holder.imgDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa thẻ")
                    .setMessage("Bạn có chắc muốn xóa thẻ này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        userRef.child("bankAccounts").child(bank.getId()).removeValue();
                        Toast.makeText(context, "Đã xóa thẻ", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return bankList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtBankName, txtCardHolder, txtCardNumber, txtExpiry;
        RadioButton radioDefault;
        ImageView imgEdit, imgDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBankName = itemView.findViewById(R.id.txtBankName);
            txtCardHolder = itemView.findViewById(R.id.txtCardHolder);
            txtCardNumber = itemView.findViewById(R.id.txtCardNumber);
            txtExpiry = itemView.findViewById(R.id.txtExpiry);
            radioDefault = itemView.findViewById(R.id.radioDefault);
            imgEdit = itemView.findViewById(R.id.btnEdit);
            imgDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
