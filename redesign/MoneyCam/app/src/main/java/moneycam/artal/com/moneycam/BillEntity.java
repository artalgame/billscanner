package moneycam.artal.com.moneycam;

import android.app.Activity;

import java.util.Date;

/**
 * Created by Alexander on 26.04.2015.
 */
public class BillEntity  {
    public long id;
    public Date date;
    public int category;
    public int summ;

    public BillEntity(){

    }

    public BillEntity(Date date, int category, int summ) {
        this.date = date;
        this.category = category;
        this.summ = summ;
    }

    public void save(Activity activity) {
        ((MainActivity)activity).datasource.createBill(this);
    }
}
