package com.example.phonestore.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonestore.databinding.ItemCollectionCardBinding;

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder> {

    public static class CollectionItem {
        public String name;
        public int iconResId;

        public CollectionItem(String name, int iconResId) {
            this.name = name;
            this.iconResId = iconResId;
        }
    }

    private final List<CollectionItem> collectionList;

    public CollectionAdapter(List<CollectionItem> collectionList) {
        this.collectionList = collectionList;
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCollectionCardBinding binding = ItemCollectionCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CollectionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        CollectionItem item = collectionList.get(position);
        holder.binding.tvCollectionName.setText(item.name);
        holder.binding.imgCollectionIcon.setImageResource(item.iconResId);
    }

    @Override
    public int getItemCount() {
        return collectionList != null ? collectionList.size() : 0;
    }

    static class CollectionViewHolder extends RecyclerView.ViewHolder {
        final ItemCollectionCardBinding binding;

        public CollectionViewHolder(ItemCollectionCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
