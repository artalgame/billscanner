package moneycam.artal.com.moneycam;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.datetimepicker.date.DatePickerDialog;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Alexander on 26.04.2015.
 */
public class SaveBillDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final String BILL_CATEGORY = "bill_category";
    private static final String DATE = "date";
    private static final String SUMM = "summ";
    static int billCategory;
    private Date date;
    private int summ;
    private SparseArray<Group> groups = new SparseArray<Group>();
    private MyExpandableListAdapter adapter;
    private EditText dateET;
    private EditText billET;

    static SaveBillDialogFragment newInstance(int billCategory, Date date, int summ) {
        SaveBillDialogFragment f = new SaveBillDialogFragment();

        Bundle args = new Bundle();
        args.putInt(BILL_CATEGORY, billCategory);
        args.putLong(DATE, date.getTime());
        args.putInt(SUMM, summ);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        billCategory = getArguments().getInt(BILL_CATEGORY);
        date = new Date();
        date.setTime(getArguments().getLong(DATE));
        summ = getArguments().getInt(SUMM);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Добавить чек");

        View v = inflater.inflate(R.layout.save_bill_dialog, container, false);
        dateET = (EditText) v.findViewById(R.id.billDate);
        dateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewDatePickerDialog(date);
            }
        });
        billET = (EditText) v.findViewById(R.id.billSumm);
        updateTextFields();
        createData();
        final ExpandableListView listView = (ExpandableListView) v.findViewById(R.id.expandableListView);
        adapter = new MyExpandableListAdapter(getActivity(), groups, listView);
        listView.setAdapter(adapter);
//        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                billCategory = childPosition;
//                adapter.notifyDataSetChanged();
//                return false;
//            }
//        });

        Button saveButton = (Button)v.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillEntity bill = new BillEntity( date, billCategory, summ);
                bill.save(getActivity());
                SaveBillDialogFragment.this.dismiss();

            }
        });
        return v;
    }

    private void updateTextFields() {
        dateET.setText(getDateString(date));
        billET.setText(String.valueOf(summ));
    }

    public void showNewDatePickerDialog(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DatePickerDialog dialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        calendar.setTime(new Date(System.currentTimeMillis()));
        dialog.setYearRange(1900, calendar.get(Calendar.YEAR));
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        dialog.show(getFragmentManager(), "DATE_PICKER");

    }

    public void createData() {
        for (int j = 0; j < 1; j++) {
            Group group = new Group("Test " + j);
            for (int i = 0; i < 3; i++) {
                group.children.add(getTitleForCategory(i));
            }
            groups.append(j, group);
        }
    }

    private String getTitleForCategory(int billCategory) {
        switch (billCategory) {
            case 0:
                return "Продукты";
            case 1:
                return "Одежда";
            default:
                return "Другое";
        }
    }

    private String getDateString(Date date) {
        return DateFormat.getDateInstance().format(date);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        Calendar selectedTime = Calendar.getInstance();
        selectedTime.set(year, month, day);
        date = selectedTime.getTime();
        updateTextFields();
    }

    public class Group {

        public String string;
        public final List<String> children = new ArrayList<String>();

        public Group(String string) {
            this.string = string;
        }

    }

    public class MyExpandableListAdapter extends BaseExpandableListAdapter {

        private final SparseArray<Group> groups;
        public LayoutInflater inflater;
        public Activity activity;
        private ExpandableListView listView;

        public MyExpandableListAdapter(Activity act, SparseArray<Group> groups, ExpandableListView listView) {
            this.listView = listView;
            activity = act;
            this.groups = groups;
            inflater = act.getLayoutInflater();
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return groups.get(groupPosition).children.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final String children = (String) getChild(groupPosition, childPosition);
            TextView text = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.expand_list_row, null);
            }
            convertView.setTag(childPosition);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    billCategory = (int) v.getTag();
                    notifyDataSetChanged();
                    listView.collapseGroup(0);
                }
            });
            text = (TextView) convertView.findViewById(R.id.row_title);
            text.setText(children);
            text.setCompoundDrawablesWithIntrinsicBounds(getIconIdForCategory(childPosition), 0, 0, 0);
            return convertView;
        }

        private int getIconIdForCategory(int childPosition) {
            switch (childPosition) {
                case 0:
                    return R.drawable.ic_product;
                case 1:
                    return R.drawable.ic_wear;
                default:
                    return R.drawable.ic_other;
            }
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return groups.get(groupPosition).children.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groups.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return groups.size();
        }

        @Override
        public void onGroupCollapsed(int groupPosition) {
            super.onGroupCollapsed(groupPosition);
        }

        @Override
        public void onGroupExpanded(int groupPosition) {
            super.onGroupExpanded(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.expand_list_group, null);
            }
            CheckedTextView groupTitle = (CheckedTextView) convertView.findViewById(R.id.group_title);
            groupTitle.setText(getTitleForCategory(SaveBillDialogFragment.billCategory));
            groupTitle.setChecked(isExpanded);

            ImageView group_image = (ImageView) convertView.findViewById(R.id.group_image);
            group_image.setImageResource(getIconIdForCategory(SaveBillDialogFragment.billCategory));

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
