package com.ronschka.david.esb;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ronschka.david.esb.databaseHomework.SourceHw;
import com.ronschka.david.esb.helper.Converter;
import com.ronschka.david.esb.helper.Homework;
import com.ronschka.david.esb.helper.Subject;
import com.ronschka.david.esb.helper.Utils;

import java.util.Arrays;
import java.util.Calendar;

import static com.ronschka.david.esb.helper.Converter.toMilliseconds;

public final class AddHomework extends AppCompatActivity{

    /**
     * String array containing the subjects
     */
    private static String[] subjects;

    /**
     * 0 is year, 1 is month and 2 is day
     */
    private static int[] date;

    /**
     * Time in milliseconds
     */
    private static long time;

    /**
     * ID of the homework entry in the database
     */
    private static String ID = null;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_add);
        //back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        subjects = Subject.get(this);
        date = getDate(0);

        setUntilTV(date);
        setSpinner();
        handleIntent(getIntent());
        Utils.setupActionBar(this, false);
    }

    //back arrow
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private int[] getDate(final long time) {
        final Calendar c = Calendar.getInstance();
        if (time != 0)
            c.setTimeInMillis(time);

        final int[] tmpDate = new int[3];

        // E.g "1970"
        tmpDate[0] = c.get(Calendar.YEAR);

        // E.g "01"
        tmpDate[1] = c.get(Calendar.MONTH);

        // Get current day, e.g. "01", plus one day > e.g. "02"
        tmpDate[2] = c.get(Calendar.DAY_OF_MONTH) + 1;

        if (time != 0)
            tmpDate[2] = c.get(Calendar.DAY_OF_MONTH);

        return tmpDate;
    }

    private void setUntilTV(final int[] date) {
        final String until = Converter.toDate(date);
        final TextView untilTV = findViewById(R.id.button_until);
        untilTV.setText(until);
        time = toMilliseconds(date);
    }

    private void setSpinner() {
        final Spinner subSpin = findViewById(R.id.spinner_subject);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subSpin.setAdapter(adapter);
    }

    private void handleIntent(final Intent intent) {
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            // Set ID
            ID = extras.getString(SourceHw.allColumns[0]);

            // Set Title
            final EditText hwEdit = findViewById(R.id.editText_homework);
            hwEdit.setText(extras.getString(SourceHw.allColumns[1]));
            hwEdit.setSelection(hwEdit.getText().length()); //Cursor position on end of text

            // Set Subject
            final String subject = extras.getString(SourceHw.allColumns[2]);
            final Spinner subSpin = findViewById(R.id.spinner_subject);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, subjects);
            // Get position in subject list
            int spinnerPosition = adapter.getPosition(subject);
            // If subject is not in subject list
            if (spinnerPosition == -1) {
                final int size = subjects.length;
                final String[] tmp = new String[size + 1];
                System.arraycopy(subjects, 0, tmp, 0, size);
                tmp[size] = subject;
                Arrays.sort(tmp);

                subjects = tmp;
                setSpinner();
                adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, subjects);
                spinnerPosition = adapter.getPosition(subject);
            }
            subSpin.setSelection(spinnerPosition);

            // Set Info
            final EditText infoEdit = findViewById(R.id.editText_info);
            infoEdit.setText(extras.getString(SourceHw.allColumns[3]));

            // Set Urgent
            if (!extras.getString(SourceHw.allColumns[4]).equals("")) {
                final CheckBox checkBox = findViewById(R.id.checkBox_urgent);
                checkBox.setChecked(true);
            }

            // Set Until
            time = Long.valueOf(extras.getString(SourceHw.allColumns[5])).longValue();
            date = getDate(time);
            setUntilTV(date);

            // Change the "Add" button to "Save"
            final Button mAdd = findViewById(R.id.button_add);
            mAdd.setText(R.string.homework_save);
            setTitle("Hausaufgabe bearbeiten");
        }
    }

    public final void setUntil(final View v) {
        final DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public final void onDateSet(final DatePicker view, final int year,
                                                final int monthOfYear, final int dayOfMonth) {
                        date[0] = year;
                        date[1] = monthOfYear;
                        date[2] = dayOfMonth;
                        setUntilTV(date);

                    }

                }, date[0], date[1], date[2]);

        dpd.show();
    }

    public final void addData(final View v) {
        final Spinner subSpin = findViewById(R.id.spinner_subject);
        final EditText hwEdit = findViewById(R.id.editText_homework);
        final EditText infoEdit = findViewById(R.id.editText_info);

        // Close keyboard
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(hwEdit.getWindowToken(), 0);

        // If nothing filled in -> cancel
        if (hwEdit.getText().toString().trim().length() == 0) {
            hwEdit.setError(getString(R.string.please_enter_something));
            return;
        }

        // Urgent?
        String urgent;
        final CheckBox urgentCheck = findViewById(R.id.checkBox_urgent);
        if (urgentCheck.isChecked())
            urgent = getString(R.string.important);
        else
            urgent = "";

        // Get filled in data
        final String subject = subSpin.getSelectedItem().toString();
        final String homework = hwEdit.getText().toString().trim();
        final String info = infoEdit.getText().toString().trim();

        //TODO change to dynamic color
        final String color;

        if(subject.equals("Mathe")){
            color = getApplicationContext().getResources().getString(0+ R.color.MaterialIndigo2);
        }
        else if(subject.equals("Deutsch")){
            color = getApplicationContext().getResources().getString(0+ R.color.MaterialRed1);
        }
        else if(subject.equals("Englisch")){
            color = getApplicationContext().getResources().getString(0+ R.color.MaterialGreen1);
        }
        else if(subject.equals("Informatik")){
            color = getApplicationContext().getResources().getString(0+ R.color.MaterialAmber);
        }
        else if(subject.equals("Elektrotechnik")){
            color = getApplicationContext().getResources().getString(0+ R.color.MaterialRed3);
        }
        else if(subject.equals("Physik")){
            color = getApplicationContext().getResources().getString(0+ R.color.MaterialTeal1);
        }
        else if(subject.equals("Wirtschaft")){
            color = getApplicationContext().getResources().getString(0+ R.color.MaterialBlue2);
        }
        else if(subject.equals("Technische Informatik")){
            color = getApplicationContext().getResources().getString(0+ R.color.MaterialBlue1);
        }
        else if(subject.equals("Gesellschaftslehre")){
            color = getApplicationContext().getResources().getString(0+ R.color.MaterialPink3);
        }
        else if(subject.equals("Religion")){
            color = getApplicationContext().getResources().getString(0+ R.color.MaterialPurple2);
        }
        else{
            color = getApplicationContext().getResources().getString(0+ R.color.Grey);
        }

        // Entry in database
        Homework.add(this, ID, homework, subject, time, info, urgent, color, "false");

        finish();
    }

}


