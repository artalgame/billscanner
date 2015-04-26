package moneycam.artal.com.moneycam;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final int PRODUCT_ID = 0;
    private static final int WEAR_ID = 1;
    private static final int OTHER_ID = 2;

    private static final String EXTRA_CHECK_ID = "check_id";
    private static final int REQUEST_CODE_PHOTO = 1221;
    private static final String SAVE_DIALOG_TAG = "save_dialog_tag";

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private File directory;
    private int lastBillCategory = OTHER_ID;
    public BillDataSource datasource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDirectory();
        setupViewPager();
        setupButtons();

        setupDatabase();
    }

    private void setupDatabase() {
        datasource = new BillDataSource(this);
        datasource.open();

        List<BillEntity> values = datasource.getAllComments();
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
                    //TODO send picture to server
                    startSaveItemDialog(lastBillCategory, new Date(), 100500);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
        }
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
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, generateFileUri());
        intent.putExtra(EXTRA_CHECK_ID, checkId);
        startActivityForResult(intent, REQUEST_CODE_PHOTO);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            return 3;
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
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            List<BillEntity> values = ((MainActivity)getActivity()).datasource.getAllComments();
            int productSumm = 0;
            int wearSumm = 0;
            int otherSumm = 0;
            for(BillEntity bill : values){
                if(bill.category == 0){
                    productSumm+=bill.summ;
                }
                if(bill.category == 1){
                    wearSumm+=bill.summ;
                }
                if(bill.category == 2){
                    otherSumm+=bill.summ;
                }
            }

            final PieGraph pg = (PieGraph) rootView.findViewById(R.id.piegraph);

            PieSlice slice1 = new PieSlice();
            slice1.setColor(Color.GREEN);
            slice1.setSelectedColor(Color.BLACK);
            slice1.setValue(productSumm);
            slice1.setTitle("Продукты");
            pg.addSlice(slice1);

            PieSlice slice2 = new PieSlice();
            slice2.setColor(Color.BLUE);
            slice2.setSelectedColor(Color.BLACK);
            slice2.setValue(wearSumm);
            slice2.setTitle("Одежда");
            pg.addSlice(slice2);

            PieSlice slice3 = new PieSlice();
            slice3.setColor(Color.YELLOW);
            slice3.setSelectedColor(Color.BLACK);
            slice3.setValue(otherSumm);
            slice3.setTitle("Другое");
            pg.addSlice(slice3);

            return rootView;
        }
    }

}
