package com.example.countdowntimer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.example.countdowntimer.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ImageHandler {

    private final String TAG = "IMAGE_HANDLER";

    public Bitmap uriToBitmap(ImageView imgView) {
        try {
            Log.d(TAG, "Converting URI to Bitmap");
            if (imgView.getDrawable() == null) {
                throw new IllegalArgumentException("ImageView does not contain an image.");
            }
            BitmapDrawable drawable = (BitmapDrawable) imgView.getDrawable();
            return drawable.getBitmap();
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in uriToBitmap function: " + e);
        }

        return null;
    }

    public byte[] bitmapToByteArray(Bitmap bitmap) {

        try {
            Log.d(TAG, "Converting Bitmap to ByteArray");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in bitmapToByteArray: " + e);
        }

        return new byte[]{};
    }

    public Bitmap byteArrayToBitmap(byte[] byteArray) {

        try {
            Log.d(TAG, "Converting ByteArray to Bitmap");
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in byteArrayToBitmap: " + e);
        }

        return null;
    }

    public String postAndGetImagePath(ImageView eventImage, String fileName, File folderName) {
        File imgFile = new File(folderName, fileName + ".png");

        try (FileOutputStream stream = new FileOutputStream(imgFile)) {
            Bitmap bitImage = uriToBitmap(eventImage);
            bitImage.compress(Bitmap.CompressFormat.PNG, 50, stream);
            String filePath = imgFile.getAbsolutePath();
            Log.d(TAG, "Image saved successfully at path: " + filePath);

            return filePath;
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in postAndGetImagePath: " + e);
        }

        return "";
    }

    public Bitmap getImageFromPath(Context context, String filePath) {
        try {
            File imgFile = new File(filePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                Log.d(TAG, "Image loaded successfully from path: " + filePath);
                return bitmap;
            } else {
                Log.e(TAG, "File does not exist at path: " + filePath+" . So we are going with default image");
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.event);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in getImageFromPath: " + e);
        }

        return null;
    }

    public void deleteImageFromPath(String file){
        try {
            File deleteFile = new File(file);

            if (deleteFile.exists()){
                Log.d(TAG, "Image is exist for: "+file+". Deleting it.");
                if (deleteFile.delete()){
                    Log.d(TAG, "Image deleted from folder");
                }
                else{
                    Log.d(TAG, "Image deletion failed..");
                }
            }
            else{
                Log.d(TAG, "Image is not exist for: "+file);
            }
        }
        catch (Exception e){
            Log.e(TAG, "Error in deleting image from folder for: "+file+" with error: "+e);
        }
    }

}
