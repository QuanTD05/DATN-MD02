package com.example.datn_md02.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.*;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.datn_md02.R;
import java.util.List;

public class ReviewImagePreviewAdapter extends RecyclerView.Adapter<ReviewImagePreviewAdapter.ImageViewHolder> {
    private final Context context;
    private final List<Uri> imageUris;

    public ReviewImagePreviewAdapter(Context context, List<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Glide.with(context).load(imageUris.get(position)).into(holder.imgPreview);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPreview;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPreview = itemView.findViewById(R.id.imgPreview);
        }
    }
}
