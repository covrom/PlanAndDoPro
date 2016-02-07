/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements EventListFragment.EventListFragmentListener,TrackEventFragment.TrackEventFragmentListener {

    EventListFragment evList;
    long startWithRowId=-1;
    public static final String PREF_ROWID = "pro.tsov.plananddopro.activityrowid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //if (savedInstanceState != null) return;

        if (findViewById(R.id.singleFragment)!=null){
            evList = new EventListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.singleFragment,evList);
            transaction.commit();
        }

        Intent i = getIntent();
        long startWithRowId = i.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);

        if (savedInstanceState!=null && startWithRowId==-1){
//            startWithRowId = savedInstanceState.getLong(PREF_ROWID);
        }

        if(startWithRowId!=-1){onEventSelected(startWithRowId);}

//        TrackAlarmReceiver.sendActionOverAlarm(this,5000,false);

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putLong(PREF_ROWID,startWithRowId);
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
        if (requestCode==1) {
            if (findViewById(R.id.singleFragment) != null) {
                evList.forceLoad();
            }
            else{
                ((EventListFragment) getSupportFragmentManager().findFragmentById(R.id.eventsFragment)).forceLoad();
            }
            if (data!=null) {
                long evId = data.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);
                onEventSelected(evId);
            }
        };
    }

    @Override
    public void onEventSelected(long rowID) {
        startWithRowId = rowID;
        if (findViewById(R.id.singleFragment)!=null){
            displayTrack(rowID,R.id.singleFragment);
        }
        else{
            //планшет

//            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.tracksFragment);
//            if (fragment != null)
//                getSupportFragmentManager().beginTransaction().remove(fragment).commit();

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

