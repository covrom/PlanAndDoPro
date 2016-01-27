/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Arrays;

public class EditEventActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ACTION_EXTRA_EVENTID = "pro.tsov.plananddopro.eventId";

    public static String[] spindata = {"--","06:00","07:00","08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00","19:00","20:00","21:00","22:00","23:00","00:00","01:00","02:00","03:00","04:00","05:00"};

    public long currentRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) return;

        setContentView(R.layout.activity_edit_event);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spindata);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.spinnerNotifyHour);
        spinner.setAdapter(adapter);
        spinner.setPrompt(getResources().getString(R.string.notifyhourname));

        //icon spinner
        Spinner spinnerIcon = (Spinner) findViewById(R.id.spinnerIcon);
        spinnerIcon.setAdapter(new IconAdapter(this));
        spinnerIcon.setPrompt(getResources().getString(R.string.habicon));

        Intent i = getIntent();
        currentRowId = i.getLongExtra(ACTION_EXTRA_EVENTID, -1);
        if (currentRowId != -1){
            //считываем из базы
            PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(this);
            EventRec rec = helper.getEventData(currentRowId);
            if(rec != null){

                ////////////////////////////////////////////
                ((EditText) findViewById(R.id.editTextName)).setText(rec.name);
                ((EditText) findViewById(R.id.editTextDesc)).setText(rec.describe);
                spinner.setSelection(Arrays.asList(spindata).indexOf(rec.notifyhour));
                spinnerIcon.setSelection(Arrays.asList(getResources().getStringArray(R.array.all_icons)).indexOf(rec.icon));

            }
        }

        ((Button) findViewById(R.id.buttonSave)).setOnClickListener(this);

    }

    static public void sendRefreshWidget(Context ctx){
        Intent intent = new Intent(ctx, TrackWidget.class);
        intent.setAction(TrackWidget.ACTION_REFRESH);
        ctx.sendBroadcast(intent);
        TrackAlarmReceiver.sendActionOverAlarm(ctx,5000,false);
    }

    @Override
    public void onClick(View v) {
        PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(this);

        EventRec newrec = new EventRec();

        //////////////////////////////////////////////////
        newrec.name = ((EditText) findViewById(R.id.editTextName)).getText().toString();
        newrec.describe = ((EditText) findViewById(R.id.editTextDesc)).getText().toString();
        newrec.icon = ((Spinner) findViewById(R.id.spinnerIcon)).getSelectedItem().toString();
        newrec.notifyhour = ((Spinner) findViewById(R.id.spinnerNotifyHour)).getSelectedItem().toString();

        if (currentRowId == -1) {
            currentRowId = helper.addEventRec(newrec);
        } else {
            helper.updateEventRec(newrec, currentRowId);
        }

        sendRefreshWidget(this);

        Intent i = new Intent();
        i.putExtra(ACTION_EXTRA_EVENTID, currentRowId);
        setResult(RESULT_OK, i);
        finish();

    }
}
