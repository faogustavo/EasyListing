package com.gustavofao.easylisting.sample;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gustavofao.easylisting.EasyListing;
import com.gustavofao.easylisting.sample.Models.Author;
import com.gustavofao.easylisting.sample.Models.Book;
import com.squareup.picasso.Picasso;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * Set a custom format for dates
         */
        EasyListing.getInstance().setDateFormat("dd/MM/yyyy HH:mm");

        /**
         * Set the image downloader from URL
         */
        EasyListing.getInstance().setImageLoader(new EasyListing.ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(imageView.getContext())
                        .load(url)
                        .error(-1)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(imageView);
            }
        });

        /**
         * Set a custom view creator for an specific class
         */
        EasyListing.getInstance().setCustomViewHandler(Book.class, new EasyListing.CustomViewCreator<Book>() {
            @Override
            public View withoutViewCreated(View convertView, int position, ViewGroup parent, Context context, Book data) {
                convertView = EasyListing.normalCreation(convertView, parent, context, data);

                if (position % 2 == 0)
                    convertView.setBackgroundColor(Color.rgb(225,225,225));

                return convertView;
            }
        });
    }
}
