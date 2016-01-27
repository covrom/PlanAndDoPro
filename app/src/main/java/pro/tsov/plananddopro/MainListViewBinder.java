/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MainListViewBinder implements SimpleCursorAdapter.ViewBinder {
    private Context context;
    private Typeface font1,font2;

    public MainListViewBinder(Context ctx){
        super();
        context = ctx;
        font1 = Typeface.createFromAsset(context.getResources().getAssets(), "fontawesome-webfont.ttf");
        font2 = Typeface.createFromAsset(context.getResources().getAssets(), "flaticon.ttf");
    }
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (cursor.getColumnName(columnIndex).equalsIgnoreCase("clockicon")){
            ((TextView) view).setText(R.string.clockIconSym);
            ((TextView) view).setTypeface(font1);
            return true;
        }
        if (cursor.getColumnName(columnIndex).equalsIgnoreCase("itemicon")) {
            String st = cursor.getString(columnIndex);
            if(!st.equals("--")){
                ((TextView) view).setText(st);
                ((TextView) view).setTypeface(font2);
                return true;
            }
            else
            {
                ((TextView) view).setText("    ");
                return true;
            }
        }
        return false;
    }
}
