package com.appschef.trick;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StickerAdapter extends BaseAdapter {
    private final Context context;
    private final int[] items;

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return 0;
    }

    public StickerAdapter(Context context2, int[] iArr) {
        this.context = context2;
        this.items = iArr;
    }

    public int getCount() {
        return this.items.length;
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listitemView = convertView;
        if (listitemView == null) {
            listitemView = LayoutInflater.from(context).inflate(R.layout.stickeritem, parent, false);
        }
        ImageView courseIV = listitemView.findViewById(R.id.idIVcourse);
        courseIV.setImageResource(this.items[position]);
        return listitemView;
    }
}
