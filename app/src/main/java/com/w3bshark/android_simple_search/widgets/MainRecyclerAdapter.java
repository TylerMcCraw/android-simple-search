package com.w3bshark.android_simple_search.widgets;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.w3bshark.android_simple_search.R;
import com.w3bshark.android_simple_search.model.CsvItem;

import java.util.ArrayList;
import java.util.List;

public class MainRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    public class TotalsViewHolder extends RecyclerView.ViewHolder {
//        CardView cardView;
        TextView totalDescription;

        TotalsViewHolder(View itemView) {
            super(itemView);
//            cardView = (CardView) itemView.findViewById(R.id.main_fragment_totals_cardview);
            totalDescription = (TextView)itemView.findViewById(R.id.totals_itemDescription);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView itemDescription;

        ItemViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.main_fragment_cardview);
            itemDescription = (TextView)itemView.findViewById(R.id.itemDescription);
        }
    }

    private Context context;
    private List<CsvItem> csvItems;
    private static final int TOTALS_SECTION = 0;
    protected List<Integer> viewsForPositions = new ArrayList<>();

    public MainRecyclerAdapter(Context context, List<CsvItem> csvItems) {
        this.context = context;
        this.csvItems = csvItems;
        viewsForPositions.add(R.layout.recycler_totals_item);
    }

    @Override
    public Filter getFilter() {
        return new SearchFilter(this, csvItems);
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case TOTALS_SECTION:
                return R.layout.recycler_totals_item;
            default:
                return R.layout.recycler_csv_item;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case R.layout.recycler_totals_item:
                View totalsView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recycler_totals_item, viewGroup, false);
                return new TotalsViewHolder(totalsView);
            default:
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recycler_csv_item, viewGroup, false);
                return new ItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case R.layout.recycler_totals_item:
                setUpTotalsSection(viewHolder);
                break;
            default:
                // This needs to be position minus additional views due to reusing cards for
                // other view holders
                if (position - viewsForPositions.size() >= 0
                        && position - viewsForPositions.size() <= csvItems.size()) {
                    setUpItemsSection(viewHolder, position - viewsForPositions.size());
                }
        }
    }

    private void setUpTotalsSection(RecyclerView.ViewHolder viewHolder) {
        TotalsViewHolder holder = (TotalsViewHolder) viewHolder;
        String totalsText = "";
        if (csvItems != null) {
            totalsText = csvItems.size() + context.getString(R.string.items_count);
        }
        else {
            totalsText = "0" + context.getString(R.string.items_count);
        }
        holder.totalDescription.setText(totalsText);
    }

    private void setUpItemsSection(RecyclerView.ViewHolder viewHolder, int position) {
        ItemViewHolder holder = (ItemViewHolder) viewHolder;
        holder.itemDescription.setText(csvItems.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return csvItems == null ? viewsForPositions.size() : viewsForPositions.size() + csvItems.size();
    }

    public static class SearchFilter extends Filter {
        private final MainRecyclerAdapter adapter;
        private final List<CsvItem> originalList;
        private final List<CsvItem> filteredList;

        public SearchFilter(MainRecyclerAdapter adapter, List<CsvItem> originalList) {
            this.adapter = adapter;
            this.originalList = new ArrayList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final CsvItem item : originalList) {
                    if (item.getDescription().toLowerCase().trim().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.csvItems.clear();
            adapter.csvItems.addAll((ArrayList<CsvItem>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
//    public CsvItem removeItem(int position) {
//        final CsvItem item = csvItems.remove(position);
//        notifyItemRemoved(position);
//        return item;
//    }
//
//    public void addItem(int position, CsvItem item) {
//        csvItems.add(position, item);
//        notifyItemInserted(position);
//    }
//
//    public void moveItem(int fromPosition, int toPosition) {
//        final CsvItem item = csvItems.remove(fromPosition);
//        csvItems.add(toPosition, item);
//        notifyItemMoved(fromPosition, toPosition);
//    }

//    public void animateTo(ArrayList<CsvItem> items) {
//        applyAndAnimateRemovals(items);
//        applyAndAnimateAdditions(items);
//        applyAndAnimateMovedItems(items);
//        // Update the totals number displayed
//        notifyItemChanged(TOTALS_SECTION);
//    }

//    private void applyAndAnimateRemovals(ArrayList<CsvItem> newItems) {
//        for (int i = csvItems.size() - 1; i >= 0; i--) {
//            final CsvItem item = csvItems.get(i);
//            if (!newItems.contains(item)) {
//                removeItem(i);
//            }
//        }
//    }
//    private void applyAndAnimateAdditions(ArrayList<CsvItem> newModels) {
//        for (int i = 0, count = newModels.size(); i < count; i++) {
//            final CsvItem item = newModels.get(i);
//            if (!csvItems.contains(item)) {
//                addItem(i, item);
//            }
//        }
//    }
//    private void applyAndAnimateMovedItems(ArrayList<CsvItem> newModels) {
//        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
//            final CsvItem item = newModels.get(toPosition);
//            final int fromPosition = csvItems.indexOf(item);
//            if (fromPosition >= 0 && fromPosition != toPosition) {
//                moveItem(fromPosition, toPosition);
//            }
//        }
//    }
}
