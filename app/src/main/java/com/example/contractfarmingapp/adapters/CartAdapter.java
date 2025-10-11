package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;
    private final List<CartItem> cartItemList;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position, CartItem item);
    }

    public interface OnItemCheckedChangeListener {
        void onItemCheckedChanged(List<CartItem> selectedItems, double totalPrice);
    }

    private OnDeleteClickListener deleteClickListener;
    private OnItemCheckedChangeListener itemCheckedChangeListener;

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setOnItemCheckedChangeListener(OnItemCheckedChangeListener listener) {
        this.itemCheckedChangeListener = listener;
    }

    public CartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo;
        TextView txtNamaProduk, txtNamaPerusahaan, txtVariasi, txtHarga;
        EditText txtQuantity;
        ImageButton btnDelete, btnIncrease, btnDecrease;
        CheckBox checkBox;
        Handler debounceHandler = new Handler(Looper.getMainLooper());
        Runnable debounceRunnable;

        public ViewHolder(View view) {
            super(view);
            imgLogo = view.findViewById(R.id.imgLogo);
            txtNamaProduk = view.findViewById(R.id.txtNamaProduk);
            txtNamaPerusahaan = view.findViewById(R.id.txtNamaPerusahaan);
            txtVariasi = view.findViewById(R.id.txtVariasi);
            txtHarga = view.findViewById(R.id.txtHarga);
            txtQuantity = view.findViewById(R.id.txtQuantity);
            btnDelete = view.findViewById(R.id.btnDelete);
            btnIncrease = view.findViewById(R.id.btnIncrease);
            btnDecrease = view.findViewById(R.id.btnDecrease);
            checkBox = view.findViewById(R.id.checkboxSelect);
        }
    }

    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CartAdapter.ViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        holder.txtNamaProduk.setText(item.getNamaProduk());
        holder.txtNamaPerusahaan.setText(item.getNamaPerusahaan());
        holder.txtVariasi.setText(item.getNamaVariasi());

        String existingText = holder.txtQuantity.getText().toString();
        String newText = String.valueOf(item.getQuantity());
        if (!existingText.equals(newText)) {
            holder.txtQuantity.setText(newText);
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        holder.txtHarga.setText(formatter.format(item.getTotalPrice()));

        Glide.with(context)
                .load(item.getLogoProduk())
                .placeholder(R.drawable.kotakabu)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgLogo);

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position, item);
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            item.increaseQuantity();
            notifyItemChanged(position);
            updateQuantityToServer(item);
            notifyCheckedItemsChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            item.decreaseQuantity();
            notifyItemChanged(position);
            updateQuantityToServer(item);
            notifyCheckedItemsChanged();
        });

        if (holder.txtQuantity.getTag() instanceof TextWatcher) {
            holder.txtQuantity.removeTextChangedListener((TextWatcher) holder.txtQuantity.getTag());
        }

        TextWatcher quantityWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();

                if (input.isEmpty()) return;

                try {
                    int newQty = Integer.parseInt(input);
                    if (newQty < 1) {
                        newQty = 1;
                        holder.txtQuantity.setText(String.valueOf(newQty));
                        holder.txtQuantity.setSelection(holder.txtQuantity.length());
                    }

                    if (newQty != item.getQuantity()) {
                        item.updateQuantity(newQty);
                        holder.txtHarga.setText(formatter.format(item.getTotalPrice()));

                        if (holder.debounceRunnable != null) {
                            holder.debounceHandler.removeCallbacks(holder.debounceRunnable);
                        }

                        holder.debounceRunnable = () -> {
                            updateQuantityToServer(item);
                            notifyCheckedItemsChanged();
                        };
                        holder.debounceHandler.postDelayed(holder.debounceRunnable, 2000);
                    }

                } catch (NumberFormatException e) {
                    holder.txtQuantity.setText("1");
                    holder.txtQuantity.setSelection(holder.txtQuantity.length());
                    item.updateQuantity(1);
                    holder.txtHarga.setText(formatter.format(item.getTotalPrice()));
                    holder.debounceHandler.postDelayed(() -> {
                        updateQuantityToServer(item);
                        notifyCheckedItemsChanged();
                    }, 500);
                }
            }

        };

        holder.txtQuantity.addTextChangedListener(quantityWatcher);
        holder.txtQuantity.setTag(quantityWatcher);

        // Checkbox setup
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
            notifyCheckedItemsChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }
    public List<CartItem> getSelectedItems() {
        List<CartItem> selected = new ArrayList<>();
        for (CartItem item : cartItemList) {
            if (item.isChecked()) {
                selected.add(item);
            }
        }
        return selected;
    }


    private void notifyCheckedItemsChanged() {
        List<CartItem> selectedItems = new java.util.ArrayList<>();
        double total = 0;
        for (CartItem item : cartItemList) {
            if (item.isChecked()) {
                selectedItems.add(item);
                total += item.getTotalPrice();
            }
        }

        if (itemCheckedChangeListener != null) {
            itemCheckedChangeListener.onItemCheckedChanged(selectedItems, total);
        }
    }

    private void updateQuantityToServer(CartItem item) {
        String url = "http://192.168.1.27:8080/contractfarming/update_cart_quantity.php";

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Berhasil
                },
                error -> {
                    // Gagal
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("cart_id", String.valueOf(item.getId()));
                params.put("quantity", String.valueOf(item.getQuantity()));
                params.put("total_price", String.valueOf(item.getTotalPrice()));
                return params;
            }
        };

        queue.add(request);
    }
}