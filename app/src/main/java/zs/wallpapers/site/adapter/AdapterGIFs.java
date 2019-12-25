package zs.wallpapers.site.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.like.LikeButton;
import com.like.OnLikeListener;

import zs.wallpapers.site.R;
import zs.wallpapers.site.interfaces.RecyclerViewClickListener;
import zs.wallpapers.site.items.ItemGIF;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.Methods;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;


public class AdapterGIFs extends RecyclerView.Adapter {

    private ArrayList<ItemGIF> arrayList;
    private Context context;
    private RecyclerViewClickListener recyclerViewClickListener;
    private DBHelper dbHelper;
    private Methods methods;
    private int columnWidth = 0, columnHeight = 0;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private class MyViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rootlayout;
        private LikeButton likeButton;
        private SimpleDraweeView my_image_view;
        private TextView textView_cat;
        private View vieww;

        private MyViewHolder(View view) {
            super(view);
            textView_cat = view.findViewById(R.id.tv_wall_cat);
            vieww = view.findViewById(R.id.view_wall);
            rootlayout = view.findViewById(R.id.rootlayout);
            likeButton = view.findViewById(R.id.button_wall_fav);
            my_image_view = view.findViewById(R.id.my_image_view);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private static CircularProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    public AdapterGIFs(Context context, ArrayList<ItemGIF> arrayList, RecyclerViewClickListener recyclerViewClickListener) {
        this.arrayList = arrayList;
        this.context = context;
        dbHelper = new DBHelper(context);
        methods = new Methods(context);
        this.recyclerViewClickListener = recyclerViewClickListener;
        columnWidth = methods.getColumnWidth(3, 3);
        columnHeight = (int) (columnWidth * 1.55);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image_wall, parent, false);
            return new MyViewHolder(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).likeButton.setLiked(dbHelper.isFavGIF(arrayList.get(position).getId()));
            ((MyViewHolder) holder).textView_cat.setVisibility(View.INVISIBLE);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(columnWidth, (int) (columnHeight * 0.4));
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            ((MyViewHolder) holder).vieww.setLayoutParams(params);
            ((MyViewHolder) holder).my_image_view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ((MyViewHolder) holder).my_image_view.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight));

            ((MyViewHolder) holder).my_image_view.setImageURI(Uri.parse(arrayList.get(position).getImage()));

            ((MyViewHolder) holder).likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    dbHelper.addtoFavoriteGIF(arrayList.get(holder.getAdapterPosition()));
                    methods.showSnackBar(((MyViewHolder) holder).rootlayout, context.getString(R.string.added_to_fav));
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    dbHelper.removeFavGIF(arrayList.get(holder.getAdapterPosition()).getId());
                    methods.showSnackBar(((MyViewHolder) holder).rootlayout, context.getString(R.string.removed_from_fav));
                }
            });

            ((MyViewHolder) holder).my_image_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewClickListener.onClick(holder.getAdapterPosition());
                }
            });
        } else {
            if (getItemCount() == 1) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    public boolean isHeader(int position) {
        return position == arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? VIEW_PROG : VIEW_ITEM;
    }
}