package zs.wallpapers.site.adapter;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.like.LikeButton;
import com.like.OnLikeListener;

import zs.wallpapers.site.R;
import zs.wallpapers.site.interfaces.RecyclerViewClickListener;
import zs.wallpapers.site.items.ItemWallpaper;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.Methods;

import java.util.ArrayList;


public class AdapterWallpaperFav extends RecyclerView.Adapter {

    private ArrayList<ItemWallpaper> arrayList;
    private ArrayList<ItemWallpaper> filteredArrayList;
    private NameFilter filter;
    private Context context;
    private RecyclerViewClickListener recyclerViewClickListener;
    private DBHelper dbHelper;
    private Methods methods;
    private String type;
    private int columnWidth = 0, columnHeight = 0;

    private final int VIEW_ITEM = 1;

    private class MyViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rootlayout;
        private LikeButton likeButton;
        private TextView textView_cat;
        private View vieww;
        private SimpleDraweeView my_image_view;

        private MyViewHolder(View view) {
            super(view);
            textView_cat = view.findViewById(R.id.tv_wall_cat);
            vieww = view.findViewById(R.id.view_wall);
            rootlayout = view.findViewById(R.id.rootlayout);
            likeButton = view.findViewById(R.id.button_wall_fav);
            my_image_view = view.findViewById(R.id.my_image_view);
        }
    }

    public AdapterWallpaperFav(Context context, String type, ArrayList<ItemWallpaper> arrayList, RecyclerViewClickListener recyclerViewClickListener) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        this.type = type;
        dbHelper = new DBHelper(context);
        methods = new Methods(context);
        setColumnWidthHeight(type);
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image_wall, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder) holder).likeButton.setLiked(dbHelper.isFav(arrayList.get(position).getId()));
        ((MyViewHolder) holder).textView_cat.setText(arrayList.get(position).getCName());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(columnWidth, (int) (columnHeight * 0.4));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ((MyViewHolder) holder).vieww.setLayoutParams(params);
        ((MyViewHolder) holder).my_image_view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ((MyViewHolder) holder).my_image_view.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight));

        ((MyViewHolder) holder).my_image_view.setImageURI(Uri.parse(methods.getImageThumbSize(arrayList.get(position).getImageThumb(), type)));

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

        ((MyViewHolder) holder).my_image_view.setOnClickListener(new View.OnClickListener() {
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

    public String getID(int position) {
        return arrayList.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_ITEM;
    }

    public void setType(String type) {
        this.type = type;
        setColumnWidthHeight(type);
    }

    private void setColumnWidthHeight(String type) {
        if (type.equals("") || type.equals(context.getString(R.string.portrait))) {
            columnWidth = methods.getColumnWidth(3, 3);
            columnHeight = (int) (columnWidth * 1.55);
        } else if (type.equals(context.getString(R.string.landscape))) {
            columnWidth = methods.getColumnWidth(2, 3);
            columnHeight = (int) (columnWidth * 0.54);
        } else {
            columnWidth = methods.getColumnWidth(3, 3);
            columnHeight = columnWidth;
        }
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new NameFilter();
        }
        return filter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<ItemWallpaper> filteredItems = new ArrayList<>();

                for (int i = 0, l = filteredArrayList.size(); i < l; i++) {
                    String nameList = filteredArrayList.get(i).getTags();
                    if (nameList.toLowerCase().contains(constraint))
                        filteredItems.add(filteredArrayList.get(i));
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = filteredArrayList;
                    result.count = filteredArrayList.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            arrayList = (ArrayList<ItemWallpaper>) results.values;
            notifyDataSetChanged();
        }
    }
}