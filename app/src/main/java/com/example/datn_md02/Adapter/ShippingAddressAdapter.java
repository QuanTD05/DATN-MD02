package com.example.datn_md02.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.datn_md02.Model.ShippingAddress;
import com.example.datn_md02.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ShippingAddressAdapter {

    public interface OnAddressActionListener {
        void onEdit(ShippingAddress address);
        void onDelete(ShippingAddress address);
    }

    private Context context;
    private List<ShippingAddress> addressList;
    private LinearLayout container;
    private OnAddressActionListener listener;
    private String uid;

    public ShippingAddressAdapter(Context context,
                                  List<ShippingAddress> addressList,
                                  LinearLayout container,
                                  OnAddressActionListener listener,
                                  String uid) {
        this.context = context;
        this.addressList = addressList;
        this.container = container;
        this.listener = listener;
        this.uid = uid;
        renderItems();
    }

    private void renderItems() {
        container.removeAllViews();
        for (ShippingAddress address : addressList) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_shipping_address, container, false);

            CheckBox checkBox = view.findViewById(R.id.checkboxDefault);
            TextView tvName = view.findViewById(R.id.tvName);
            TextView tvPhone = view.findViewById(R.id.tvPhone);
            TextView tvAddress = view.findViewById(R.id.tvAddress);
            ImageView btnEdit = view.findViewById(R.id.btnEdit);
            ImageView btnDelete = view.findViewById(R.id.btnDelete);

            tvName.setText(address.getName());
            tvPhone.setText(address.getPhone());
            tvAddress.setText(address.getFullAddress());
            checkBox.setChecked(address.isDefault());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Cập nhật tất cả địa chỉ
                    for (ShippingAddress addr : addressList) {
                        boolean isDefault = addr.getId().equals(address.getId());
                        addr.setDefault(isDefault);

                        // Gửi lên Firebase
                        FirebaseDatabase.getInstance()
                                .getReference("shipping_addresses")
                                .child(uid)
                                .child(addr.getId())
                                .child("default")
                                .setValue(isDefault);
                    }

                    // Cập nhật lại giao diện
                    renderItems();
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(address);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(address);
            });

            container.addView(view);
        }
    }
}
