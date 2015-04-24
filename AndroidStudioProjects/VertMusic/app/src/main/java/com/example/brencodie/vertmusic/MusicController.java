package com.example.brencodie.vertmusic;

import android.content.Context;
import android.widget.MediaController;

/**
 * Created by Bren Codie on 4/6/2015.
 * Extending MediaController allows you to control/modify the built in class MediaController.
 */
public class MusicController extends MediaController {

    public MusicController(Context c, boolean useFastForward) {
        super(c, useFastForward);
    }

    /**
     * Overrides the hide function of MediaController
     */
    public void hide() {}


}
