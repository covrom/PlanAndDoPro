/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements EventListFragment.EventListFragmentListener,TrackEventFragment.TrackEventFragmentListener {

    EventListFragment evList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) return;

        if (findViewById(R.id.singleFragment)!=null){
            evList = new EventListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.singleFragment,evList);
            transaction.commit();
        }

        Intent i = getIntent();
        long startWithRowId = i.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);
        if(startWithRowId!=-1){onEventSelected(startWithRowId);}

//        TrackAlarmReceiver.sendActionOverAlarm(this,5000,false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (evList==null){
            evList = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.singleFragment);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1) getLoaderManager().getLoader(0).forceLoad();
    }

    @Override
    public void onEventSelected(long rowID) {
        if (findViewById(R.id.singleFragment)!=null){
            displayTrack(rowID,R.id.singleFragment);
        }
        else{
            //планшет
            getSupportFragmentManager().popBackStack();
            displayTrack(rowID,R.id.tracksFragment);
        }
//        Intent i = new Intent(faActivity, TrackEventFragment.class);
//        i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, id);
//        startActivityForResult(i, 1);

    }

    private void displayTrack(long rowID, int viewID) {
        TrackEventFragment trFragment = new TrackEventFragment();
        Bundle args = new Bundle();
        args.putLong(TrackEventFragment.PREF_ROWID, rowID);
        trFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID,trFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void onAddEvent() {
        Intent i = new Intent(this, EditEventActivity.class);
        long newrec = -1;
        i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, newrec);//новый
        startActivityForResult(i, 1);
    }

    @Override
    public void onEditEvent(long rowID) {
        Intent i = new Intent(this, EditEventActivity.class);
        i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, rowID);//уже есть
        startActivityForResult(i, 1);
    }
}

