package com.mobiconnect;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

class M3UStatic {

    private static final String EXT_M3U = "#EXTM3U";
    private static final String EXT_INF = "#EXTINF:";
    private static final String EXT_PLAYLIST_NAME = "#PLAYLIST";
    private static final String EXT_LOGO = "tvg-logo";
    private static final String EXT_URL = "http://";

    private String convertStreamToString(InputStream is) {
        try {
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public M3UPlaylist parseFile(InputStream inputStream) {
        M3UPlaylist m3UPlaylist = new M3UPlaylist();

        List<M3UItem> playlistItems = new ArrayList<>();
        M3UItem item = new M3UItem();


        m3UPlaylist.setPlaylistItems(playlistItems);
        return m3UPlaylist;
    }
}
