package moneycam.artal.com.moneycam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BillDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MyDatabaseHelper dbHelper;
    private String[] allColumns = {
            MyDatabaseHelper.COLUMN_ID,
            MyDatabaseHelper.COLUMN_SUMM,
            MyDatabaseHelper.COLUMN_CATEGORY,
            MyDatabaseHelper.COLUMN_DATE
    };

    public BillDataSource(Context context) {
        dbHelper = new MyDatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public BillEntity createBill(BillEntity bill) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_SUMM, bill.summ);
        values.put(MyDatabaseHelper.COLUMN_CATEGORY, bill.category);
        values.put(MyDatabaseHelper.COLUMN_DATE, bill.date.getTime());
        long insertId = database.insert(MyDatabaseHelper.TABLE_BILL, null, values);
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_BILL,
                allColumns, MyDatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        BillEntity newBill = cursorToComment(cursor);
        cursor.close();
        return newBill;
    }

    public void deleteComment(BillEntity billEntity) {
        long id = billEntity.id;
        database.delete(MyDatabaseHelper.TABLE_BILL, MyDatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    public List<BillEntity> getAllComments() {
        List<BillEntity> bills = new ArrayList<BillEntity>();

        Cursor cursor = database.query(MyDatabaseHelper.TABLE_BILL,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            BillEntity bill = cursorToComment(cursor);
            bills.add(bill);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return bills;
    }

    private BillEntity cursorToComment(Cursor cursor) {
        BillEntity bill = new BillEntity();
        bill.id = cursor.getLong(0);
        bill.summ = cursor.getInt(1);
        bill.category = cursor.getInt(2);
        Date date = new Date();
        date.setTime(cursor.getLong(3));
        bill.date = date;
        return bill;
    }
} 
