/*
 * Copyright (c) Roman Tsovanyan
 */

package pro.tsov.plananddopro;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class TrackService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TrackFactory(getApplicationContext());
    }
}
