/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements EventListFragment.EventListFragmentListener,TrackEventFragment.TrackEventFragmentListener {

    EventListFragment evList;
    TrackEventFragment trackEv;
    long startWithRowId=-1;
    private final String FRAG_TAG_EVENTS = "eventsfragmenttag";
    private final String FRAG_TAG_TRACKS = "tracksfragmenttag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        boolean isPortrait = findViewById(R.id.tracksFragment) == null;

        if (savedInstanceState != null) {
            evList = (EventListFragment) getSupportFragmentManager().findFragmentByTag(FRAG_TAG_EVENTS);
            trackEv = (TrackEventFragment) getSupportFragmentManager().findFragmentByTag(FRAG_TAG_TRACKS);
        }
        if (evList==null) evList = new EventListFragment();
        if (trackEv == null) trackEv = new TrackEventFragment();

        if (!evList.isInLayout()) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);//чтобы не осталось открытых фрагментов портретного режима
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.eventsFragment, evList, FRAG_TAG_EVENTS);
            transaction.commit();
        }
        if (!isPortrait){
            if (!trackEv.isInLayout()) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(trackEv);
                trackEv = new TrackEventFragment();
                transaction.replace(R.id.tracksFragment, trackEv, FRAG_TAG_TRACKS);
                transaction.commit();
            }
        }

        Intent i = getIntent();
        long startWithRowId = i.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);

        if(startWithRowId!=-1){
            onEventSelected(startWithRowId);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1) {
            if (data != null) {
                long evId = data.getLongExtra(EditEventActivity.ACTION_EXTRA_EVENTID, -1);
                onEventSelected(evId);
            }
            evList.forceLoad();
        };
    }

    @Override
    public void onEventSelected(long rowID) {
        startWithRowId = rowID;
        if (findViewById(R.id.tracksFragment)==null){
            displayTrack(rowID, R.id.eventsFragment,true, FRAG_TAG_TRACKS);
        }
        else{
            //land
            displayTrack(rowID, R.id.tracksFragment,false,FRAG_TAG_TRACKS);
        }

    }

    private void displayTrack(long rowID, int viewID, boolean putBackStack, String frTag) {
        trackEv = new TrackEventFragment();
        Bundle args = new Bundle();
        args.putLong(TrackEventFragment.PREF_ROWID, rowID);
        trackEv.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, trackEv, frTag);
        if(putBackStack) transaction.addToBackStack(null);
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

