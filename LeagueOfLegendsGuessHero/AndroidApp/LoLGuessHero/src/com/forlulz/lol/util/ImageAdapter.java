package com.forlulz.lol.util;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private GameResourceLoader loader;
    private Map<String, SoftReference<Drawable>> cache;

    public ImageAdapter(Context context, GameResourceLoader loader) {
        this.context = context;
        this.loader = loader;
        this.cache = new ConcurrentHashMap<String, SoftReference<Drawable>>();
    }

    public int getCount() {// TODO: actually depends on filtering of heroes
                           // (male|female)
        return loader.getHeroes().size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) { // if it's not recycled, initialize some
                                   // attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(80, 80));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        String assetPath = loader.getHeroes().get(position).getImageAssetPath();
        // first check the cache
        SoftReference<Drawable> soft = cache.get(assetPath);         
        Drawable resource = null;
        if (soft != null) {
            resource = soft.get();
        }
        
        if (resource == null) {
            AssetFileDescriptor descriptor = loader.resolveAssetPath(assetPath);
            AsyncImageLoadTask loadTask = new AsyncImageLoadTask();
            loadTask.execute(imageView, descriptor, assetPath);
        } else {
            imageView.setImageDrawable(resource);    
        }
        
        return imageView;
    }

    private class AsyncImageLoadTask extends AsyncTask<Object, Void, Drawable> {

        private ImageView targetView;
        
        @Override
        protected Drawable doInBackground(Object... params) {
            targetView = (ImageView) params[0];
            AssetFileDescriptor descriptor = (AssetFileDescriptor) params[1];
            String assetPath = (String) params[2];
            
            try {
                Drawable resource = Drawable.createFromStream(descriptor.createInputStream(), null);
                cache.put(assetPath, new SoftReference<Drawable>(resource));
                descriptor.close();
                return resource;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Problem with loading game resources");
            }
        };

        @Override
        protected void onPostExecute(Drawable result) {
            if (targetView != null && result != null) {
                targetView.setImageDrawable(result);
            }
        }
    };
}