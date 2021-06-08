package zs.wallpapers.site.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



import java.util.ArrayList;

import zs.wallpapers.site.R;


public class AdapterTags extends RecyclerView.Adapter {

    private ArrayList<String> arrayList;

    private class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textView_tag;

        private MyViewHolder(View view) {
            super(view);
            textView_tag = view.findViewById(R.id.tv_tags);
        }
    }

    public AdapterTags(ArrayList<String> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tags, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        ((MyViewHolder) holder).textView_tag.setText(arrayList.get(position));
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