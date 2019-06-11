package com.mobiconnect;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ItemHolder> implements Filterable {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private List<M3UItem> mItem = new ArrayList<>();
    private List<M3UItem> mItemSaved = new ArrayList<>();
    private final List<M3UItem> res = new ArrayList<>();

    private final ColorGenerator generator = ColorGenerator.MATERIAL;

    public PlaylistAdapter(Context c) {
        mContext = c;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View sView = mInflater.inflate(R.layout.item_playlist, parent, false);
        return new ItemHolder(sView);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, final int position) {
        final M3UItem item = mItem.get(position);
        if (item != null) {
            holder.update(item);
        }
    }

    @Override
    public int getItemCount() {
        return mItem.size();
    }

    void update(List<M3UItem> _list) {
        this.mItem = new ArrayList<>();
        mItem.addAll(_list);
        this.mItemSaved = new ArrayList<>();
        mItemSaved.addAll(_list);

    }

    @Override
    public Filter getFilter() {
        return new Filter() { //TODO search it on github
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mItem.clear();
                mItem.addAll(res);
                notifyDataSetChanged();
            }
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                if (!(constraint.length() == 0)){
                    res.clear();
                    String filtePatt = constraint.toString().toLowerCase().trim();
                    for (M3UItem itm : mItemSaved) {
                        if (itm.getItemName().toLowerCase().contains(filtePatt)){
                            res.add(itm);
                        }
                    }
                }

                return null;
            }
        };
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final PackageManager pm = mContext.getPackageManager();
        final boolean isApp = Utils.getInstance().isPackageInstalled(pm);
        TextView name;
        ImageView cImg;

        ItemHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            name = view.findViewById(R.id.item_name);
            cImg = view.findViewById(R.id.cimg);
        }

        void update(final M3UItem item) {
            try {
                name.setText(item.getItemName());
                int color = generator.getRandomColor();
                TextDrawable textDrawable;
                if (item.getItemIcon().isEmpty()) {
                    textDrawable = TextDrawable.builder()
                            .buildRoundRect(String.valueOf(item.getItemName().charAt(0)), color, 100);
                    cImg.setImageDrawable(textDrawable);
                } else {
                    if (Utils.getInstance().isNetworkAvailable(mContext)) {
                        Picasso.with(mContext).load(item.getItemIcon()).into(cImg);
                    } else {
                        textDrawable = TextDrawable.builder()
                                .buildRoundRect(String.valueOf(item.getItemName().charAt(0)), color, 100);
                        cImg.setImageDrawable(textDrawable);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        public void onClick(View v) {
            MainActivity main = (MainActivity) mContext;
            M3UItem imm = mItem.get(getLayoutPosition());
            main.runReward(imm);

        }

    }
}
