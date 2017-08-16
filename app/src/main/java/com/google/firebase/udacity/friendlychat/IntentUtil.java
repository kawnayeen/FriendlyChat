package com.google.firebase.udacity.friendlychat;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

/**
 * Created by kawnayeen on 8/16/17.
 */
public class IntentUtil {

    public static String getAbsolutePathFromUri(Uri uri, Context context) {
        String result = "";
        String documentID;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String[] pathParts = uri.getPath().split("/");
            documentID = pathParts[pathParts.length - 1];
        } else {
            String pathSegments[] = uri.getLastPathSegment().split(":");
            documentID = pathSegments[pathSegments.length - 1];
        }
        String mediaPath = MediaStore.Images.Media.DATA;
        Cursor imageCursor = context.getContentResolver().query(uri, new String[]{mediaPath}, MediaStore.Images.Media._ID + "=" + documentID, null, null);
        if (imageCursor != null) {
            if (imageCursor.moveToFirst()) {
                result = imageCursor.getString(imageCursor.getColumnIndex(mediaPath));
            }
            imageCursor.close();
        }
        return result;
    }

}
