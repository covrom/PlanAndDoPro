/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,AdapterView.OnItemClickListener {

    private PlanDoDBOpenHelper dbhelper;
    private SimpleCursorAdapter scAdapter;
    private ListView lvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ActionBar ab = getActionBar();

        dbhelper = PlanDoDBOpenHelper.getInstance(this);
        // формируем столбцы сопоставления
        String[] from = new String[] { "name", "describe", "lasteventdue", "notifyhour", "clockicon", "itemicon" };
        int[] to = new int[] { R.id.itemName, R.id.itemDesc, R.id.itemLastDue, R.id.itemNotifyHour, R.id.clockIcon, R.id.itemIcon };

        // создааем адаптер и настраиваем список
        scAdapter = new SimpleCursorAdapter(this, R.layout.eventitem, null, from, to, 0);
        scAdapter.setViewBinder(new MainListViewBinder(this));

        setContentView(R.layout.activity_main);
        lvData = (ListView) findViewById(R.id.listViewData);

//        lvData = new ListView(this);
        lvData.setAdapter(scAdapter);

        View emptyV = findViewById(R.id.emptylist);
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

        // создаем лоадер для чтения данных
        LoaderManager lm = getLoaderManager();
        lm.initLoader(0, null, this);

        lvData.setOnItemClickListener(this);

//        setContentView(lvData);

        TrackAlarmReceiver.sendActionOverAlarm(this,5000,false);

    }

    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        if (dbhelper!=null) dbhelper.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //сюда вернется результат открытия активности по редактированию
        //надо обновить список
        getLoaderManager().getLoader(0).forceLoad();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void addNewEvent(){
        Intent i = new Intent(MainActivity.this, EditEventActivity.class);
        long newrec = -1;
        i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, newrec);//новый
        startActivityForResult(i, 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        } else
        if (id == R.id.action_add){
            addNewEvent();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, 0, R.string.edit_menurecord);
        menu.add(1, 2, 2, R.string.delete_record);
    }

    public boolean onContextItemSelected(MenuItem item) {
        // получаем из пункта контекстного меню данные по пункту списка
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        if (item.getItemId() == 1) {
            Intent i = new Intent(MainActivity.this,EditEventActivity.class);
            i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, acmi.id);//уже есть
            startActivityForResult(i,1);
            return true;
        }
        else if (item.getItemId() == 2){
            // извлекаем id записи и удаляем соответствующую запись в БД
            dbhelper.delEventRec(acmi.id);
            EditEventActivity.sendRefreshWidget(this);
            // получаем новый курсор с данными
            getLoaderManager().getLoader(0).forceLoad();
            return true;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this,dbhelper);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        scAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(MainActivity.this,TrackEventActivity.class);
        i.putExtra(EditEventActivity.ACTION_EXTRA_EVENTID, id);
        startActivityForResult(i,1);
    }

    static class MyCursorLoader extends CursorLoader {

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

