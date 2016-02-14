/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TrackEventFragment extends Fragment implements View.OnClickListener, EventCalendar.EventCalendarListener,TextWatcher {

    public long currentRowId;
    public Date currentDay;

    public static final String PREF_ROWID = "pro.tsov.plananddopro.trackactivityrowid";
    public static final String PREF_MONTH = "pro.tsov.plananddopro.trackactivitymonth";
    public static final String PREF_YEAR = "pro.tsov.plananddopro.trackactivityyear";
    private RelativeLayout llLayout;
    private TrackEventFragmentListener listener;
    PlanDoDBOpenHelper helper;


    @Override
    public void onEventCalendarChanged() {
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");

        EventCalendar rv = (EventCalendar) llLayout.findViewById(R.id.calendar);
        Date selectedDate = rv.getSelectedDate();
        EditText edtx = (EditText) llLayout.findViewById(R.id.comment);
        TextView txondt = (TextView) llLayout.findViewById(R.id.commentOnDate);
        if (selectedDate != null) {
            edtx.setText(helper.getTrackCommentOnDate(currentRowId, selectedDate));
            txondt.setText(iso8601Format.format(selectedDate) + "   " + getString(R.string.commentstr));
            edtx.setVisibility(View.VISIBLE);
            txondt.setVisibility(View.VISIBLE);
        }
        else
        {
            edtx.setVisibility(View.INVISIBLE);
            txondt.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        EventCalendar rv = (EventCalendar) llLayout.findViewById(R.id.calendar);
        Date onDate = rv.getSelectedDate();
        TrackRec trc = new TrackRec(currentRowId);
        helper.readTrackToRec(trc);
        int currType;
        if(trc.trackdates.get(onDate)==null) currType = 0;
        else currType = trc.trackdates.get(onDate);
        helper.updateTrackEventOnDate(currentRowId, onDate, currType, s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface TrackEventFragmentListener {
        public void onEditEvent(long rowID);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (TrackEventFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDay = Calendar.getInstance().getTime();
        helper = PlanDoDBOpenHelper.getInstance(super.getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

//        setRetainInstance(true);

        llLayout = (RelativeLayout) inflater.inflate(R.layout.track_event, container, false);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
        sp.edit().remove(PREF_MONTH).remove(PREF_YEAR).apply();

        currentRowId = -1;
        if (savedInstanceState!=null){
            currentRowId = savedInstanceState.getLong(PREF_ROWID);
        }else{
            Bundle args = getArguments();
            if (args!=null) currentRowId = args.getLong(PREF_ROWID);
        }

        FragmentActivity faActivity = (FragmentActivity) super.getActivity();

        Toolbar toolbar = (Toolbar) llLayout.findViewById(R.id.main_toolbar);
        toolbar.setTitle("");

        FloatingActionButton editFAB = (FloatingActionButton) llLayout.findViewById(R.id.editFAB);
        editFAB.setRippleColor(ContextCompat.getColor(faActivity, R.color.colorAccentBright));
        editFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editEvent();
            }
        });

        FloatingActionButton sendFAB = (FloatingActionButton) llLayout.findViewById(R.id.sendFAB);
        sendFAB.setRippleColor(ContextCompat.getColor(faActivity, R.color.colorAccentBright));
        sendFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendEvent();
            }
        });

        Button btnPrev = (Button) llLayout.findViewById(R.id.prev_month_button);
        btnPrev.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "fontawesome-webfont.ttf"));
        btnPrev.setOnClickListener(this);

        Button btnNext = (Button) llLayout.findViewById(R.id.next_month_button);
        btnNext.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "fontawesome-webfont.ttf"));
        btnNext.setOnClickListener(this);

        Button btnTitl = (Button) llLayout.findViewById(R.id.month_label);
        btnTitl.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentDay));
        btnTitl.setOnClickListener(this);

        EditText edtx = ((EditText) llLayout.findViewById(R.id.comment));
        edtx.addTextChangedListener(this);

        refreshFromDB();

        return llLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFromDB();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(PREF_ROWID, currentRowId);
    }


    private void editEvent(){
        if (currentRowId==-1) return;
        listener.onEditEvent(currentRowId);
//        Intent i = new Intent(super.getActivity(), EditEventActivity.class);
//        i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, currentRowId);//новый
//        startActivityForResult(i, 2);
    }

    private void sendEvent() {
        if (currentRowId==-1) return;
        TrackRec tr = new TrackRec(currentRowId);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, tr.getTabbedText(helper));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.prev_month_button) prevMonth();
        if (vid == R.id.next_month_button) nextMonth();
        if (vid == R.id.month_label) resetMonth();
    }

    private void prevMonth(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
        Calendar cal = Calendar.getInstance();
        int thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
        int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, thisMonth);
        cal.set(Calendar.YEAR, thisYear);
        cal.add(Calendar.MONTH, -1);
        sp.edit()
                .putInt(PREF_MONTH, cal.get(Calendar.MONTH))
                .putInt(PREF_YEAR, cal.get(Calendar.YEAR))
                .putInt(TrackTextView.PREF_SELYEAR, 2000)
                .putInt(TrackTextView.PREF_SELDAY, 1)
                .apply();
        refreshFromDB();
    }

    private void nextMonth() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
        Calendar cal = Calendar.getInstance();
        int thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
        int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, thisMonth);
        cal.set(Calendar.YEAR, thisYear);
        cal.add(Calendar.MONTH, 1);
        sp.edit()
                .putInt(PREF_MONTH, cal.get(Calendar.MONTH))
                .putInt(PREF_YEAR, cal.get(Calendar.YEAR))
                .putInt(TrackTextView.PREF_SELYEAR, 2000)
                .putInt(TrackTextView.PREF_SELDAY, 1)
                .apply();
        refreshFromDB();
    }

    private void resetMonth() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
        sp.edit().remove(PREF_MONTH).remove(PREF_YEAR).apply();
        refreshFromDB();
    }

    public void refreshFromDB(){
        if (currentRowId != -1) {
            //считываем из базы

            EventRec rec = helper.getEventData(currentRowId);
            TrackRec trrec = new TrackRec(currentRowId);
            helper.readTrackToRec(trrec);
            if (rec != null) {

                ////////////////////////////////////////////
                ((TextView) llLayout.findViewById(R.id.textViewName)).setText(rec.name);
                ((TextView) llLayout.findViewById(R.id.textViewDesc)).setText(rec.describe);

                EventCalendar rv = (EventCalendar) llLayout.findViewById(R.id.calendar);

                Date selectedDate = rv.getSelectedDate();

                SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
                EditText edtx = ((EditText) llLayout.findViewById(R.id.comment));
                TextView txondt = (TextView) llLayout.findViewById(R.id.commentOnDate);
                if (selectedDate!=null){
                    edtx.setText(trrec.trackcomments.get(EventCalendar.roundDate(selectedDate)));
                    txondt.setText(iso8601Format.format(selectedDate)+"   "+getString(R.string.commentstr));
                    edtx.setVisibility(View.VISIBLE);
                    txondt.setVisibility(View.VISIBLE);
                }else{
                    edtx.setVisibility(View.INVISIBLE);
                    txondt.setVisibility(View.INVISIBLE);
                }

                if (rec.icon.equals("--")) {
                    ((TextView) llLayout.findViewById(R.id.textViewIcon)).setText("    ");
                } else {
                    TextView v = ((TextView) llLayout.findViewById(R.id.textViewIcon));
                    v.setText(rec.icon);
                    v.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "flaticon.ttf"));
                }

            }
            refreshDecorators();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==2) refreshFromDB();
    }

    public void refreshDecorators(){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
        EventCalendar rv = (EventCalendar) llLayout.findViewById(R.id.calendar);
        rv.setListener(this);

        Calendar cal = Calendar.getInstance();
        int thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
        int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, thisMonth);
        cal.set(Calendar.YEAR, thisYear);

        ((Button) llLayout.findViewById(R.id.month_label)).setText(DateFormat.format("MMMM yyyy", cal));

        cal.set(Calendar.DAY_OF_MONTH, 1);
        int monthStartDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (monthStartDayOfWeek == Calendar.SUNDAY)
                cal.add(Calendar.DAY_OF_MONTH, -6);
            else cal.add(Calendar.DAY_OF_MONTH, 2-monthStartDayOfWeek);

        rv.setCurrentRowId(currentRowId);
        rv.build();
        rv.invalidate();

    }

}
