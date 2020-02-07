package com.example.twittersearches;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class SearchesAdapter extends RecyclerView.Adapter<SearchesAdapter.SearchViewHolder> {

    private List<String> tags;
    private View.OnClickListener itemClickListener;
    private View.OnLongClickListener itemLongClickListener;

    public SearchesAdapter(List<String> tags, View.OnClickListener itemClickListener, View.OnLongClickListener itemLongClickListener) {
        this.tags = tags;
        this.itemClickListener = itemClickListener;
        this.itemLongClickListener = itemLongClickListener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        ((TextView) holder.itemView).setText(tags.get(position));
    }

    @Override
    public int getItemCount() {
        if (tags != null) {
            return tags.size();
        }
        return 0;
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(itemClickListener);
            itemView.setOnLongClickListener(itemLongClickListener);
        }
    }
}
