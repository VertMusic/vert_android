package com.example.brencodie.vertmusic;

import android.content.Context;
import android.widget.MediaController;

/**
 * Created by Bren Codie on 4/6/2015.
 */
public class MusicController extends MediaController {

    public MusicController(Context c, boolean useFastForward) {
        super(c, useFastForward);
    }

    public void hide() {}
}
