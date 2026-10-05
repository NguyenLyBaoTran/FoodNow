package com.example.foodnow.utils;

import android.content.Context;
import com.example.foodnow.R;

public class ImageUtils {
    public static int getDrawableId(Context context, String imageKey) {
        if (imageKey == null || imageKey.isEmpty()) {
            return R.drawable.dish;
        }
        
        // Remove file extension if present
        String key = imageKey;
        if (key.contains(".")) {
            key = key.substring(0, key.lastIndexOf("."));
        }
        
        int resId = context.getResources().getIdentifier(key, "drawable", context.getPackageName());
        if (resId == 0) {
            return R.drawable.dish;
        }
        return resId;
    }
}
