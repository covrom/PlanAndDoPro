package pro.tsov.plananddopro;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;

public class EventListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private PlanDoDBOpenHelper dbhelper;
    private SimpleCursorAdapter scAdapter;
    private ListView lvData;
    private FragmentActivity faActivity;
    private RelativeLayout llLayout;

    public interface EventListFragmentListener{
        void onEventSelected(long rowID);
        void onAddEvent();
        void onEditEvent(long rowID);
    }

    private EventListFragmentListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (EventListFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener=null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        faActivity = super.getActivity();

        dbhelper = PlanDoDBOpenHelper.getInstance(faActivity);
        // формируем столбцы сопоставления
        String[] from = new String[]{"name", "describe", "lasteventdue", "notifyhour", "clockicon", "itemicon"};
        int[] to = new int[]{R.id.itemName, R.id.itemDesc, R.id.itemLastDue, R.id.itemNotifyHour, R.id.clockIcon, R.id.itemIcon};

        // создааем адаптер и настраиваем список
        scAdapter = new SimpleCursorAdapter(faActivity, R.layout.eventitem, null, from, to, 0);
        scAdapter.setViewBinder(new MainListViewBinder(faActivity));

        // создаем лоадер для чтения данных
        LoaderManager lm = getLoaderManager();
        lm.initLoader(0, null, this);

    }

    @Override
    public void onDestroy() {
        if (dbhelper != null) dbhelper.close();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        forceLoad();
    }

    public void forceLoad(){
        Loader ldr = getLoaderManager().getLoader(0);
        if (ldr!=null) ldr.forceLoad();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        llLayout = (RelativeLayout) inflater.inflate(R.layout.event_list, container, false);

        Toolbar toolbar = (Toolbar) llLayout.findViewById(R.id.main_toolbar);

        FloatingActionButton mainFab = (FloatingActionButton) llLayout.findViewById(R.id.mainFAB);
        mainFab.setRippleColor(ContextCompat.getColor(faActivity, R.color.colorAccentBright));
        mainFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewEvent();
            }
        });

        lvData = (ListView) llLayout.findViewById(R.id.listViewData);

        lvData.setAdapter(scAdapter);

        View emptyV = llLayout.findViewById(R.id.emptylist);
        Button btnV = (Button) emptyV.findViewById(R.id.buttonAdd);
        btnV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewEvent();
            }
        });
        lvData.setEmptyView(emptyV);

        // добавляем контекстное меню к списку
        registerForContextMenu(lvData);

        lvData.setOnItemClickListener(this);

        return llLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setRetainInstance(true);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, 0, R.string.edit_menurecord);
        menu.add(1, 2, 2, R.string.delete_record);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // получаем из пункта контекстного меню данные по пункту списка
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        if (item.getItemId() == 1) {
            listener.onEditEvent(acmi.id);
            return true;
        } else if (item.getItemId() == 2) {
            // извлекаем id записи и удаляем соответствующую запись в БД
            dbhelper.delEventRec(acmi.id);
            EditEventActivity.sendRefreshWidget(faActivity);
            // получаем новый курсор с данными
            forceLoad();
            return true;
        }
        return super.onContextItemSelected(item);
    }


    public void addNewEvent() {
        listener.onAddEvent();
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(faActivity, dbhelper);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        scAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listener.onEventSelected(id);
    }

    static class MyCursorLoader extends android.support.v4.content.CursorLoader {

        PlanDoDBOpenHelper db;

        public MyCursorLoader(Context context, PlanDoDBOpenHelper dbhlp) {
            super(context);
            this.db = dbhlp;
        }

        @Override
        public Cursor loadInBackground() {
            return db.getAllEventsData();
        }

    }

}
