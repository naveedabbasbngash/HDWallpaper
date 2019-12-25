package zs.wallpapers.site.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import zs.wallpapers.site.R;
import zs.wallpapers.site.interfaces.RecyclerViewClickListener;
import zs.wallpapers.site.items.ItemWallpaper;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.Methods;

import java.util.ArrayList;


public class AdapterImageHome extends RecyclerView.Adapter {

    private ArrayList<ItemWallpaper> arrayList;
    private Context context;
    private RecyclerViewClickListener recyclerViewClickListener;
    private DBHelper dbHelper;
    private Methods methods;
    private String type;

    private class MyViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rootlayout;
        private RoundedImageView imageView;
        private LikeButton likeButton;
        private TextView textView_cat;

        private MyViewHolder(View view) {
            super(view);
            rootlayout = view.findViewById(R.id.rootlayout);
            imageView = view.findViewById(R.id.iv_home_latest);
            likeButton = view.findViewById(R.id.button_home_fav);
            textView_cat = view.findViewById(R.id.tv_home_cat);
        }
    }

    public AdapterImageHome(Context context, String type, ArrayList<ItemWallpaper> arrayList, RecyclerViewClickListener recyclerViewClickListener) {
        this.arrayList = arrayList;
        this.context = context;
        this.type = type;
        dbHelper = new DBHelper(context);
        methods = new Methods(context);
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        if (type.equals(context.getString(R.string.portrait))) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image_home, parent, false);
        } else if (type.equals(context.getString(R.string.landscape))) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image_home_l, parent, false);
        } else if ((type.equals(context.getString(R.string.square)))) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image_home_s, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder) holder).likeButton.setLiked(dbHelper.isFav(arrayList.get(position).getId()));

        ((MyViewHolder) holder).textView_cat.setText(arrayList.get(position).getCName());
        Picasso.get()
                .load(methods.getImageThumbSize(arrayList.get(position).getImageThumb(), type))
                .placeholder(R.drawable.placeholder_wall)
                .into(((MyViewHolder) holder).imageView);

        ((MyViewHolder) holder).likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                dbHelper.addtoFavorite(arrayList.get(holder.getAdapterPosition()));
                methods.showSnackBar(((MyViewHolder) holder).rootlayout, context.getString(R.string.added_to_fav));
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                dbHelper.removeFav(arrayList.get(holder.getAdapterPosition()).getId());
                methods.showSnackBar(((MyViewHolder) holder).rootlayout, context.getString(R.string.removed_from_fav));
            }
        });

        ((MyViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewClickListener.onClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}