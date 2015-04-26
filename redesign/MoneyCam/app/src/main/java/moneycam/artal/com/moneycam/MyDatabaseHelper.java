package moneycam.artal.com.moneycam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_BILL = "bills";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_SUMM = "summ";
    public static final String COLUMN_CATEGORY = "category";

    private static final String DATABASE_NAME = "moneycam.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_BILL + "("
            + COLUMN_ID   + " integer primary key autoincrement, "
            + COLUMN_SUMM   + " integer not null, "
            + COLUMN_CATEGORY   + " integer not null, "
            + COLUMN_DATE + " long not null);";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILL);
        onCreate(db);
    }

}
