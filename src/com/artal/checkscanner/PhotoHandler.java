package com.artal.checkscanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.widget.Toast;
import ch.boye.httpclientandroidlib.HttpVersion;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.mime.MIME;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.params.CoreProtocolPNames;
import ch.boye.httpclientandroidlib.util.EntityUtils;

@SuppressLint("SimpleDateFormat")
public class PhotoHandler implements PictureCallback {

	private final Context context;
	private int templateId;
	private boolean isFrontCamera;

	public PhotoHandler(Context context, int templateId, boolean isFrontCamera) {
		this.context = context;
		this.templateId = templateId;
		this.isFrontCamera = isFrontCamera;
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO save picture
		CombinePhotoAsyncTask combinedTask = new CombinePhotoAsyncTask(data,
				templateId, isFrontCamera);
		combinedTask.execute();
	}

	private File getDir() {
		File sdDir = context.getExternalFilesDir(null);
		return new File(sdDir, "Prepared");
	}

	public class CombinePhotoAsyncTask extends AsyncTask<Void, Integer, String> {

		private byte[] makedPictureData;
		private int templateID;
		private boolean isFrontCamera;

		public CombinePhotoAsyncTask(byte[] makedPictureData, int templateID,
				boolean isFrontCamera) {
			this.makedPictureData = makedPictureData;
			this.templateID = templateID;
			this.isFrontCamera = isFrontCamera;
		}

		@Override
		protected String doInBackground(Void... params) {
			Options options = new Options();
			options.inMutable = true;

			Bitmap makedPictureBitmap = BitmapFactory.decodeByteArray(
					makedPictureData, 0, makedPictureData.length, options);
			if (isFrontCamera) {
				makedPictureBitmap = BitmapHelper
						.flipBitmap(makedPictureBitmap);
			}
			// Bitmap templateBitmap =
			// BitmapHelper.decodeSampledBitmapFromAsset(
			// context, templateID, makedPictureBitmap.getWidth(),
			// makedPictureBitmap.getHeight());
			//
			// Bitmap sizedTemplateBitmap = null;
			// if (templateBitmap.getHeight() != makedPictureBitmap.getHeight())
			// {
			// sizedTemplateBitmap = Bitmap.createScaledBitmap(templateBitmap,
			// makedPictureBitmap.getWidth(),
			// makedPictureBitmap.getHeight(), false);
			// }
			//
			// templateBitmap.recycle();
			// Bitmap combinedBitmap = BitmapHelper.combineBitmaps(
			// makedPictureBitmap, sizedTemplateBitmap);

			/*
			 * Bitmap makedPictureBitmap =
			 * BitmapFactory.decodeByteArray(makedPictureData, 0,
			 * makedPictureData.length); Bitmap combinedBitmap =
			 * makedPictureBitmap;
			 */

			File pictureFileDir = getDir();

			if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

				Toast.makeText(context,
						"Can't create directory to save image.",
						Toast.LENGTH_LONG).show();
				return null;

			}

			String photoFile = templateID + ".jpg";

			String filename = pictureFileDir.getPath() + File.separator
					+ photoFile;

			File pictureFile = new File(filename);

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				// combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
				makedPictureBitmap
						.compress(Bitmap.CompressFormat.JPEG, 90, fos);
				/*
				 * int bytes = combinedBitmap.getByteCount();
				 * 
				 * ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a
				 * new buffer combinedBitmap.copyPixelsToBuffer(buffer); //Move
				 * the byte data to the buffer
				 * 
				 * byte[] array = buffer.array(); //Get the underlying array
				 * containing the data.
				 * 
				 * fos.write(array);
				 */
				// combinedBitmap.recycle();
				makedPictureBitmap.recycle();
				fos.close();

			} catch (Exception error) {

				Toast.makeText(context, "Image could not be saved.",
						Toast.LENGTH_LONG).show();
			}

			String url = "http://vsem_kopyt.ngrok.com/";
			File file = new File(filename);
			try {
				
				HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

				Map<String, String> map = new HashMap<String, String>();
			
				HttpPost post = new HttpPost(url);
				post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setCharset(MIME.UTF8_CHARSET);

				if (file != null)
				    builder.addBinaryBody("filearg", file, ContentType.MULTIPART_FORM_DATA, file.getAbsolutePath());

				post.setEntity(builder.build());

				try {
				    String responseBody = EntityUtils.toString(client.execute(post).getEntity(), "UTF-8");
				//  System.out.println("Response from Server ==> " + responseBody);

				    JSONObject object = new JSONObject(responseBody);
				    Boolean success = object.optBoolean("success");
				    String message = object.optString("error");

				    if (!success) {
				        responseBody = message;
				    } else {
				        responseBody = "success";
				    }

				} catch (Exception e) {
				    e.printStackTrace();
				} finally {
				    client.getConnectionManager().shutdown();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return photoFile;
		}

		public JSONObject getJSON(InputStream iis) {
			String json = null;
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(iis, "UTF-8"), 1024);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				json = sb.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}

			JSONObject jObj = null;
			try {
				jObj = new JSONObject(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return jObj;
		}

		@Override
		protected void onPostExecute(String photoFile) {
			super.onPostExecute(photoFile);

		}
	}
}
