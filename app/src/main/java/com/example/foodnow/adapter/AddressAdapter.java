package com.example.foodnow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.model.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    public interface OnAddressClickListener {
        void onAddressClick(Address address);
        void onEdit(Address address);
        void onDelete(Address address);
    }

    private List<Address> addressList;
    private OnAddressClickListener listener;

    public AddressAdapter(List<Address> addressList, OnAddressClickListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.tvNamePhone.setText(address.getRecipientName() + " | " + address.getPhone());
        holder.tvAddressLine.setText(address.getAddressLine());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onAddressClick(address);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(address);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamePhone, tvAddressLine;
        android.widget.Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamePhone = itemView.findViewById(R.id.tvNamePhone);
            tvAddressLine = itemView.findViewById(R.id.tvAddressLine);
            btnEdit = itemView.findViewById(R.id.btnEditAddress);
            btnDelete = itemView.findViewById(R.id.btnDeleteAddress);
        }
    }
}