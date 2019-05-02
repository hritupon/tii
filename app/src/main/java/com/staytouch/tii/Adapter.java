package com.staytouch.tii;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.staytouch.tii.models.Article;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    List<Article> articles;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public Adapter(List<Article> articles, Context context) {
        this.articles = articles;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, viewGroup,  false);
        return new MyViewHolder(view, onItemClickListener) ;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
        final MyViewHolder holder = viewHolder;
        Article model = articles.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(Utils.getRandomDrawbleColor());
        requestOptions.error(Utils.getRandomDrawbleColor());
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        requestOptions.centerCrop();
        requestOptions.timeout(3000);

        Glide.with(context)
                .applyDefaultRequestOptions(new RequestOptions()
                .placeholder(R.drawable.icon_news)
                .error(R.drawable.icon_news))
                .load(model.getUrlToImage())
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(holder.imageView);

        holder.title.setText(model.getTitle());
        holder.desc.setText(model.getDescription());
        holder.source.setText(model.getSource().getName());
        holder.time.setText("\u2022"+Utils.DateToTimeFormat(model.getPublishedAt()));
        holder.publishedAt.setText(Utils.DateToTimeFormat(model.getPublishedAt()));
        holder.author.setText(model.getAuthor());

        if(!isVerifiedNews()){
            holder.verifiedNews.setVisibility(View.GONE);
        }else {
            holder.verifiedNews.setVisibility(View.VISIBLE);
        }

    }

    private boolean isVerifiedNews(){
        int s=(int)(Math.random()*1000)+1;
        return ((s%2)==0);
    }
    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, desc, author, publishedAt, source, time;
        ImageView imageView, verifiedNews;
        ProgressBar progressBar;
        OnItemClickListener onItemClickListener;

        public MyViewHolder(View itemView, OnItemClickListener onItemClickListener){
            super(itemView);

            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.title);
            desc =  itemView.findViewById(R.id.desc);
            author =  itemView.findViewById(R.id.author);
            publishedAt =  itemView.findViewById(R.id.publishedAt);
            source =  itemView.findViewById(R.id.source);
            time =  itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.img);
            verifiedNews = itemView.findViewById(R.id.verifiedNews);
            progressBar = itemView.findViewById(R.id.progress_load_photo);
            this.onItemClickListener = onItemClickListener;

        }
        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, getAdapterPosition() );
        }
    }
}
