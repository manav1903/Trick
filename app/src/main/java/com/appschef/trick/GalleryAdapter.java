package com.appschef.trick;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.ItemTouchHelper.Callback;

import com.bumptech.glide.Glide;

import java.io.File;

public class GalleryAdapter extends BaseAdapter {
    private Context context;
    private File[] fileItems;
    private int[] items;

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return 0;
    }

    public GalleryAdapter(Context context2, int[] iArr) {
        this.context = context2;
        this.items = iArr;
    }

    public GalleryAdapter(Context context2, File[] fileArr) {
        this.context = context2;
        this.fileItems = fileArr;
    }

    public int getCount() {
        int[] iArr = this.items;
        if (iArr == null) {
            return this.fileItems.length;
        }
        return iArr.length;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView = new ImageView(this.context);
        RelativeLayout relativeLayout = new RelativeLayout(this.context);
        imageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,330));
//        imageView.setLayoutParams(new LayoutParams(Callback.DEFAULT_DRAG_ANIMATION_DURATION, Callback.DEFAULT_DRAG_ANIMATION_DURATION));
//        imageView.setAdjustViewBounds(true);
        relativeLayout.addView(imageView);
        if (this.items == null) {
            if (this.fileItems[i].getAbsolutePath().contains("mp4")) {
                Glide.with(this.context).asBitmap().load(this.fileItems[i]).into(imageView);
imageView.setForeground(AppCompatResources.getDrawable(context,R.drawable.ic_baseline_play_arrow_24));

            } else {
                Glide.with(this.context).asBitmap().load(this.fileItems[i]).into(imageView);
            }
        }else {
            Glide.with(this.context).asBitmap().load(this.items[i]).into(imageView);
        }
        return relativeLayout;
    }
}
