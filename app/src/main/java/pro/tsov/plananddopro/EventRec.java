/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.ContentValues;

/**
 * Created by nmlh on 31.12.2015.
 * In any activity just pass the context and use the singleton method
 * PlanDoDBOpenHelper helper = PlanDoDBOpenHelper.getInstance(this);
 */

public class EventRec {
    public String name;
    public String describe;
    public String icon;
    public String notifyhour;

    public ContentValues ContentValues(){
        ContentValues values = new ContentValues();
        values.put("name", this.name);
        values.put("describe", this.describe);
        values.put("icon", this.icon);
        values.put("notifyhour", this.notifyhour);
        return values;
    }

    public String getTabbedText(long eventId){
        return "Event ID\tName\tDescribe\tNotifyHour\n\r"+
                String.valueOf(eventId)+"\t"+name+"\t"+describe+"\t"+notifyhour+"\n\r";

    }

}
