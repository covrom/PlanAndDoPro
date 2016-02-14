/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TrackRec {
    public long eventId;
    public HashMap<Date,Integer> trackdates;
    public HashMap<Date,String> trackcomments;

    public TrackRec (long eventId){
        this.eventId = eventId;
        trackdates = new HashMap<>();
        trackcomments = new HashMap<>();
    }

    public String getTabbedText(PlanDoDBOpenHelper helper){
        EventRec ev = helper.getEventData(eventId);
        helper.readTrackToRec(this);
        String Result = "Plan and Do Pro (event track)\n\rhttps://play.google.com/store/apps/details?id=pro.tsov.plananddopro\n\r"+ev.getTabbedText(eventId)+"\n\r\n\r"+"Date\tStatus\tComment\n\r";
        List keys = new ArrayList(trackdates.keySet());
        Collections.sort(keys);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < keys.size(); i++) {
            Date dt = (Date)keys.get(i);
            int et = trackdates.get(dt);
            String ets = "";
            if (et == 1) ets = "plan";
            if (et == 2) ets = "do";
            if (et == 3) ets = "cancel";
            String comments = trackcomments.get(EventCalendar.roundDate(dt));
            if (comments==null) comments="";
            Result = Result+ iso8601Format.format(dt)+"\t"+ets+"\t"+comments+"\n\r";
        }

        Result = Result+"\n\r(end of event track)\n\r";
        return Result;
    }
}
