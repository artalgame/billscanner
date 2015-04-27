package moneycam.artal.com.moneycam;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;

import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ch.boye.httpclientandroidlib.HttpVersion;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.mime.MIME;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.params.CoreProtocolPNames;
import ch.boye.httpclientandroidlib.util.EntityUtils;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final int PRODUCT_ID = 0;
    private static final int WEAR_ID = 1;
    private static final int OTHER_ID = 2;

    private static final String EXTRA_CHECK_ID = "check_id";
    private static final int REQUEST_CODE_PHOTO = 1221;
    private static final int REQUEST_CODE_GALLERY = 3022;
    private static final String SAVE_DIALOG_TAG = "save_dialog_tag";


    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private File directory;
    private int lastBillCategory = OTHER_ID;
    public static BillDataSource datasource;
    private View loadingScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(getResources().getDrawable(R.mipmap.ic_launcher));
        loadingScreen = findViewById(R.id.loadingScreen);
        createDirectory();
        setupViewPager();
        setupButtons();

        setupDatabase();
    }

    private void setupDatabase() {
        datasource = new BillDataSource(this);
        datasource.open();
    }

    private void setupButtons() {
        findViewById(R.id.IB_product).setOnClickListener(this);
        findViewById(R.id.IB_wear).setOnClickListener(this);
        findViewById(R.id.IB_other).setOnClickListener(this);
    }

    private void setupViewPager() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.IB_product) {
            startScanCheck(PRODUCT_ID);
        } else if (v.getId() == R.id.IB_wear) {
            startScanCheck(WEAR_ID);
        } else if (v.getId() == R.id.IB_other) {
            startScanCheck(OTHER_ID);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), generateFileUri());
                    //bitmap = crupAndScale(bitmap, 300); // if you mind scaling
                    File file = new File("/sdcard/photo.jpg");
                    startLoadToServerTask(file);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_CODE_GALLERY) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri selectedImageUri = intent.getData();

                    String[] projection = {MediaStore.MediaColumns.DATA};
                    Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();

                    String selectedImagePath = cursor.getString(column_index);

//                Bitmap bm;
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(selectedImagePath, options);
//                final int REQUIRED_SIZE = 200;
//                int scale = 1;
//                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
//                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
//                    scale *= 2;
//                options.inSampleSize = scale;
//                options.inJustDecodeBounds = false;
//                bm = BitmapFactory.decodeFile(selectedImagePath, options);

                    if (selectedImagePath != null)
                        startLoadToServerTask(new File(selectedImagePath));
                    else throw new Exception();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, getString(R.string.cannot_load_picture_from_gallery), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    private void startLoadToServerTask(final File file) {
        loadingScreen.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String HOST = "https://money_cam.ngrok.com/";
                int total = -1;
                try {

                    HttpClient client = new DefaultHttpClient();
                    client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

                    HttpPost post = new HttpPost(HOST);
                    post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setCharset(MIME.UTF8_CHARSET);

                    builder.addBinaryBody("filearg", file, ContentType.MULTIPART_FORM_DATA, file.getAbsolutePath());

                    post.setEntity(builder.build());

                    try {
                        String responseBody = EntityUtils.toString(client.execute(post).getEntity(), "UTF-8");
                        //  System.out.println("Response from Server ==> " + responseBody);

                        JSONObject object = new JSONObject(responseBody);
                        try {
                            total = object.optInt("total");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            total = 0;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, getString(R.string.not_recognize), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        client.getConnectionManager().shutdown();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (total == -1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                            }
                        });
                        total = 100500;
                    }
                    startSaveItemDialog(lastBillCategory, new Date(), total);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingScreen.setVisibility(View.GONE);
                        }
                    });

                }
            }
        }).start();
    }

    public static Bitmap crupAndScale(Bitmap source, int scale) {
        int factor = source.getHeight() <= source.getWidth() ? source.getHeight() : source.getWidth();
        int longer = source.getHeight() >= source.getWidth() ? source.getHeight() : source.getWidth();
        int x = source.getHeight() >= source.getWidth() ? 0 : (longer - factor) / 2;
        int y = source.getHeight() <= source.getWidth() ? 0 : (longer - factor) / 2;
        source = Bitmap.createBitmap(source, x, y, factor, factor);
        source = Bitmap.createScaledBitmap(source, scale, scale, false);
        return source;
    }

    private void startSaveItemDialog(int billCategory, Date date, int summ) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        android.app.Fragment prev = getFragmentManager().findFragmentByTag(SAVE_DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = SaveBillDialogFragment.newInstance(billCategory, date, summ);
        newFragment.show(ft, SAVE_DIALOG_TAG);
    }

    private void startScanCheck(int checkId) {
        lastBillCategory = checkId;

        final CharSequence[] items = {getString(R.string.TakePhoto), getString(R.string.LoadFromGallery)};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.ChooserTitle));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(R.string.TakePhoto))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri());
                    startActivityForResult(intent, REQUEST_CODE_PHOTO);
                } else if (items[item].equals(getString(R.string.LoadFromGallery))) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_CODE_GALLERY);
                }
            }
        });
        builder.show();
        //lastBillCategory = checkId;
        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri());
        //intent.putExtra(EXTRA_CHECK_ID, checkId);
//        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(Intent.createChooser(intent, getString(R.string.ChooserTitle)), REQUEST_CODE_GALLERY);
        //startActivityForResult(intent, REQUEST_CODE_PHOTO);
    }

    private Uri generateFileUri() {
        return Uri.parse("file:///sdcard/photo.jpg");
//        File file = null;
//        file = new File(directory.getPath() + "/last_check.jpg");
//        return Uri.fromFile(file);
    }

    private void createDirectory() {
        directory = new File(getFilesDir(), "Checks");
        if (!directory.exists())
            directory.mkdirs();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    public void updateStatistic() {

        PlaceholderFragment fragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
        if (fragment != null)  // could be null if not instantiated yet
        {
            if (fragment.getView() != null) {
                // no need to call if fragment's onDestroyView()
                //has since been called.
                fragment.updateStatistic(); // do what updates are required
            }
        }
//        int index = mViewPager.getCurrentItem();
//        SectionsPagerAdapter adapter = ((SectionsPagerAdapter) mViewPager.getAdapter());
//        PlaceholderFragment fragment = (PlaceholderFragment) adapter.getItem(mViewPager.getCurrentItem());
//        fragment.updateStatistic();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private TextView allSummTV;
        private PieGraph pg;
        private Context context;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            allSummTV = (TextView) rootView.findViewById(R.id.allSumm);
            pg = (PieGraph) rootView.findViewById(R.id.piegraph);
            context = getActivity().getApplication().getApplicationContext();
            updateStatistic();
            return rootView;
        }

        public void updateStatistic() {
            List<BillEntity> values = MainActivity.datasource.getAllComments();
            int productSumm = 0;
            int wearSumm = 0;
            int otherSumm = 0;
            for (BillEntity bill : values) {
                if (bill.category == 0) {
                    productSumm += bill.summ;
                }
                if (bill.category == 1) {
                    wearSumm += bill.summ;
                }
                if (bill.category == 2) {
                    otherSumm += bill.summ;
                }
            }

            int allSumm = productSumm + wearSumm + otherSumm;
            if (allSumm == 0) {
                allSummTV.setText(context.getString(R.string.no_records));
            } else {
                DecimalFormat df = new DecimalFormat("###,###,###"); // or pattern "###,###.##$"
                String summString = df.format(allSumm);
                allSummTV.setText(context.getString(R.string.total_amount) + " " + summString);
                pg.removeSlices();

                PieSlice slice1 = new PieSlice();
                slice1.setColor(context.getResources().getColor(R.color.FOOD_COLOR));
                slice1.setValue(productSumm);
                slice1.setTitle(context.getString(R.string.Food));
                pg.addSlice(slice1);

                PieSlice slice2 = new PieSlice();
                slice2.setColor(context.getResources().getColor(R.color.WEAR_COLOR));
                slice2.setValue(wearSumm);
                slice2.setTitle(getString(R.string.Wear));
                pg.addSlice(slice2);

                PieSlice slice3 = new PieSlice();
                slice3.setColor(context.getResources().getColor(R.color.OTHER_COLOR));
                slice3.setValue(otherSumm);
                slice3.setTitle(context.getString(R.string.Other));
                pg.addSlice(slice3);
            }
        }
    }

}
