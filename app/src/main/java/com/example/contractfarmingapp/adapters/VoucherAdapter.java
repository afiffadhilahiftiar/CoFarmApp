package com.example.contractfarmingapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.VoucherModel;

import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    public interface OnVoucherClickListener {
        void onVoucherClick(VoucherModel voucher);
    }

    private List<VoucherModel> voucherList;
    private OnVoucherClickListener listener;

    public VoucherAdapter(List<VoucherModel> voucherList, OnVoucherClickListener listener) {
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherAdapter.ViewHolder holder, int position) {
        VoucherModel voucher = voucherList.get(position);
        holder.txtVoucherTitle.setText(voucher.getTitle());
        holder.txtVoucherDesc.setText(voucher.getDescription());
        holder.txtVoucherTime.setText("Hingga: " + voucher.getExpiryDate());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVoucherClick(voucher);
            }
        });
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtVoucherTitle, txtVoucherDesc, txtVoucherTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtVoucherTitle = itemView.findViewById(R.id.txtVoucherTitle);
            txtVoucherDesc = itemView.findViewById(R.id.txtVoucherDesc);
            txtVoucherTime = itemView.findViewById(R.id.txtVoucherTime);
        }
    }
}
