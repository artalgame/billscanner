package com.artal.checkscanner;

import java.io.FileInputStream;
import java.io.InputStream;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;

public class BitmapHelper {
	public static Bitmap decodeSampledBitmapFromAsset(Context context, int pos, int reqWidth, int reqHeight){
		InputStream ims = null;
		try{
			// get input stream
	        ims = context.getAssets().open("template/"+pos+".png");
	        
	       // First decode with inJustDecodeBounds=true to check dimensions
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(ims,null, options);
	        ims.close();
	        ims = context.getAssets().open("template/"+pos+".png");
	        
	        // Calculate inSampleSize
	        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	
	        // Decode bitmap with inSampleSize set
	        options.inJustDecodeBounds = false;
	        Bitmap bitmap =  BitmapFactory.decodeStream(ims,null, options);
	        ims.close();
	        return bitmap;
		}catch(Exception ex){
			Log.e("BitmapHelper", ex.getMessage(), ex);
		}
		return null;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	    return inSampleSize;
	}

	public static Point getTemplateBitmapSize(Context context, int templateId) {
		InputStream ims = null;
		try{
			// get input stream
	        ims = context.getAssets().open("template/"+templateId+".png");
	        
	       // First decode with inJustDecodeBounds=true to check dimensions
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(ims,null, options);
	        return new Point(options.outWidth, options.outHeight);
		}catch(Exception ex){
			Log.e("BitmapHelper", ex.getMessage(), ex);
			return new Point(0,0);
		}
	}

	public static Bitmap combineBitmaps(Bitmap makedPictureBitmap,
			Bitmap sizedTemplateBitmap) {		
		for(int i=0; i < makedPictureBitmap.getWidth(); i++){
			for(int j=0; j < makedPictureBitmap.getHeight(); j++){
				//int templatePixelAlpha = Color.alpha(sizedTemplateBitmap.getPixel(i, j));
				
				Bitmap bmOverlay = Bitmap.createBitmap(makedPictureBitmap.getWidth(), makedPictureBitmap.getHeight(), makedPictureBitmap.getConfig());
		        Canvas canvas = new Canvas(bmOverlay);
		        canvas.drawBitmap(makedPictureBitmap, new Matrix(), null);
		        canvas.drawBitmap(sizedTemplateBitmap, new Matrix(), null);
		        return bmOverlay;
		        
				/*if(templatePixelAlpha!=0){
					makedPictureBitmap.setPixel(i, j, sizedTemplateBitmap.getPixel(i, j));
				}*/
			}
		}
		return makedPictureBitmap;
	}

	public static Bitmap loadSizedBitmap(String photoPath, int widthPixels,int heightPixels)
	{
		InputStream ims = null;
		try{
			// get input stream
	        ims = new FileInputStream(photoPath);
	        
	       // First decode with inJustDecodeBounds=true to check dimensions
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(ims,null, options);
	        ims.close();
	          
	        // Calculate inSampleSize
	        options.inSampleSize = calculateInSampleSize(options, widthPixels, heightPixels);
	
	        // Decode bitmap with inSampleSize set
	        ims = new FileInputStream(photoPath);
	        options.inJustDecodeBounds = false;
	        Bitmap bitmap =  BitmapFactory.decodeStream(ims,null, options);
	        ims.close();
	        return bitmap;
		}catch(Exception ex){
			Log.e("BitmapHelper", ex.getMessage(), ex);
	}
	return null;
	}

	public static Bitmap[] loadBitmapsForTemplate(String[] photoPathsForTemplate, int reqWidth, int reqHeight) {
		if(photoPathsForTemplate!=null){
			Bitmap[] photos = new Bitmap[photoPathsForTemplate.length];
			int i=0;
			for(String photoPath:photoPathsForTemplate){
				photos[i] = loadSizedBitmap(photoPath, reqWidth, reqHeight);
				i++;
			}
			return photos;
		}
		return null;
	}

	public static Bitmap flipBitmap(Bitmap src) {
		Matrix m = new Matrix();
	    m.preScale(-1, 1);
	    Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
	    dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
	    return dst;
	}

//	public static Bitmap[] loadTemplatesBitmaps(Context context, int reqWidth, int reqHeight) {
//		Bitmap[] bitmaps = new Bitmap[ProjectConstants.TEMPLATES_COUNT];
//		
//		for(int i=0; i<ProjectConstants.TEMPLATES_COUNT; i++){
//			bitmaps[i] = BitmapHelper.decodeSampledBitmapFromAsset(context, i, reqWidth, reqHeight);
//		}
//		return bitmaps;
//		
//	}
}
