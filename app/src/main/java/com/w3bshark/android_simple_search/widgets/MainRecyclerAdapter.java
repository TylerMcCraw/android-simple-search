package com.w3bshark.android_simple_search.widgets;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.w3bshark.android_simple_search.R;
import com.w3bshark.android_simple_search.model.CsvItem;

import java.util.ArrayList;
import java.util.List;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainRecyclerAdapter.ItemViewHolder> {
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView itemDescription;

        ItemViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.main_fragment_cardview);
            itemDescription = (TextView)itemView.findViewById(R.id.itemDescription);
        }
    }

    Context context;
    List<CsvItem> csvItems;

    public MainRecyclerAdapter(Context context, List<CsvItem> csvItems) {
        this.context = context;
        this.csvItems = csvItems;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return 1;
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_item, viewGroup, false);
        if (i == 0) {
            ItemViewHolder itemViewHolder = new ItemViewHolder(v);
            itemViewHolder.itemDescription.setTextColor(ContextCompat.getColor(context,R.color.smashing_pink));
        }
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder ItemViewHolder, int i) {
        ItemViewHolder.itemDescription.setText(csvItems.get(i).getDescription());
    }

    @Override
    public int getItemCount() {
        return csvItems == null ? 0 : csvItems.size();
    }

    //TODO: Clean up these helper methods
    public CsvItem removeItem(int position) {
        final CsvItem item = csvItems.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    public void addItem(int position, CsvItem item) {
        csvItems.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final CsvItem item = csvItems.remove(fromPosition);
        csvItems.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(ArrayList<CsvItem> items) {
        applyAndAnimateRemovals(items);
        applyAndAnimateAdditions(items);
        applyAndAnimateMovedItems(items);
    }

    private void applyAndAnimateRemovals(ArrayList<CsvItem> newItems) {
        for (int i = csvItems.size() - 1; i >= 0; i--) {
            final CsvItem item = csvItems.get(i);
            if (!newItems.contains(item)) {
                removeItem(i);
            }
        }
    }
    private void applyAndAnimateAdditions(ArrayList<CsvItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final CsvItem item = newModels.get(i);
            if (!csvItems.contains(item)) {
                addItem(i, item);
            }
        }
    }
    private void applyAndAnimateMovedItems(ArrayList<CsvItem> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final CsvItem item = newModels.get(toPosition);
            final int fromPosition = csvItems.indexOf(item);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }
}
