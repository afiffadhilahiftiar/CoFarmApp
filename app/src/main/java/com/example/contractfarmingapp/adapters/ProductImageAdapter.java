package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.R;

import java.util.List;

public class ProductImageAdapter extends PagerAdapter {
    private Context context;
    private List<String> mediaUrls; // bisa gambar atau video
    private LayoutInflater inflater;

    public ProductImageAdapter(Context context, List<String> mediaUrls) {
        this.context = context;
        this.mediaUrls = mediaUrls;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mediaUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = inflater.inflate(R.layout.item_product_image, container, false);
        ImageView imageView = itemView.findViewById(R.id.imageProduct);
        WebView webView = itemView.findViewById(R.id.webViewVideo);

        String url = mediaUrls.get(position);

        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            // Tampilkan WebView untuk YouTube
            imageView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            // Format embed untuk YouTube
            String videoId = extractYoutubeId(url);
            String html = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" + videoId + "\" frameborder=\"0\" allowfullscreen></iframe>";

            webView.loadData(html, "text/html", "utf-8");
        } else {
            // Tampilkan gambar
            webView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(url)
                    .centerCrop()
                    .into(imageView);
        }

        container.addView(itemView);
        return itemView;
    }

    private String extractYoutubeId(String url) {
        if (url.contains("youtu.be/")) {
            return url.substring(url.lastIndexOf("/") + 1);
        } else if (url.contains("v=")) {
            return url.substring(url.indexOf("v=") + 2);
        }
        return url; // fallback
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
