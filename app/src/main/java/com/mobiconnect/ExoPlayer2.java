package com.mobiconnect;


import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;



/*
Created by: Ayal Fieldust
Date: 8/2017
Description:
This Example app was created to show a simple example of ExoPlayer Version 2.8.4.
There is an option to play mp4 files or live stream content.
Exoplayer provides options to play many different formats, so the code can easily be tweaked to play the requested format.
Scroll down to "ADJUST HERE:" I & II to change between sources.
Keep in mind that m3u8 files might be stale and you would need new sources.
 */

public class ExoPlayer2 extends AppCompatActivity {


    private PlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_exo2);
        Bundle bundle = getIntent().getExtras();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        simpleExoPlayerView.requestFocus();
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        simpleExoPlayerView.setPlayer(player);

        player.setPlayWhenReady(true);

        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();


        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(bundle.getString("Url")),
                new DefaultDataSourceFactory(this, bundle.getString("Agent", "mediaPlayerSample")), extractorsFactory, null, null);

        player.prepare(mediaSource);
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.stop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        player.stop();

    }
}