package com.mobiconnect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.io.InputStream;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private final M3UParser parser = new M3UParser();
    private ProgressBar spinner;
    private TextView mPlaylistParams;
    private RecyclerView mPlaylistList;
    private InputStream is;
    private PlaylistAdapter mAdapter;
    MenuItem search;
    private RewardedVideoAd mRewardedVideoAd;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private M3UItem rewardedLink;
    private boolean rewarded;
    M3UPlaylist playlist;
    GridLayoutManager layoutManager;
    private Context ctx = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawable(null);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8265484806809068/6415668371");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                try {
                    rewarded = false;
                    Intent intent = new Intent(ctx, ExoPlayer2.class);
                    intent.putExtra("Name", rewardedLink.getItemName());
                    intent.putExtra("Url", rewardedLink.getItemUrl());
                    intent.putExtra("Agent", rewardedLink.getItemUserAgent());

                    ctx.startActivity(intent);
                } catch (Exception ignored) {
                }
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });


        MobileAds.initialize(this, "ca-app-pub-8265484806809068~6467870777");
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewarded(RewardItem reward) {

                rewarded = true;
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                rewarded = false;

            }

            @Override
            public void onRewardedVideoAdClosed() {
                loadRewardedVideoAd();

                if (rewarded) {
                    try {
                        rewarded = false;
                        Intent intent = new Intent(ctx, ExoPlayer2.class);
                        intent.putExtra("Name", rewardedLink.getItemName());
                        intent.putExtra("Url", rewardedLink.getItemUrl());
                        intent.putExtra("Agent", rewardedLink.getItemUserAgent());

                        ctx.startActivity(intent);
                    } catch (Exception ignored) {
                    }
                }

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorCode) {
                loadRewardedVideoAd();


            }

            @Override
            public void onRewardedVideoAdLoaded() {
                rewarded = false;
            }

            @Override
            public void onRewardedVideoAdOpened() {
            }

            @Override
            public void onRewardedVideoStarted() {
            }

            @Override
            public void onRewardedVideoCompleted() {
            }

        });
        loadRewardedVideoAd();


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);





        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.playlist_recycler);
        recyclerView.getItemAnimator().setChangeDuration(100);
        mPlaylistParams = findViewById(R.id.playlist_params);
        mPlaylistList = findViewById(R.id.playlist_recycler);
        spinner = findViewById(R.id.login_progress);
        layoutManager = new GridLayoutManager(this,3);
        mPlaylistList.setLayoutManager(layoutManager);
        mAdapter = new PlaylistAdapter(this);
        mPlaylistList.setAdapter(mAdapter);
        //loader(filepath.getPath());
        //new downloadFile().execute("http://m-iptv.net:6204/get.php?username=AFEOdvNlBf&password=3zrFg7Q96M&type=m3u_plus&output=ts");
        new downloadFile().execute("https://deepapp.000webhostapp.com/datas.m3u");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        search = menu.findItem(R.id.app_bar_search);
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem search = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setQueryHint("Search channel name");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return filter(query);
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                //TODO here changes the search text)
                return filter(newText);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_list_to_grid:
                if (!((Animatable) item.getIcon()).isRunning()) {
                    if (layoutManager.getSpanCount() == 1) {
                        item.setIcon(AnimatedVectorDrawableCompat.create(MainActivity.this, R.drawable.avd_grid_to_list));
                        layoutManager.setSpanCount(3);
                        mAdapter.setGroup(true);
                        mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());

                    }
                }
                break;
            default: {
                return super.onOptionsItemSelected(item);
            }
        }


        return super.onOptionsItemSelected(item);
    }

    void loader() {

        try { //new FileInputStream (new File(name)
            is = getAssets().open("data.m3u"); // if u r trying to open file from asstes InputStream is = getassets.open(); InputStream
            M3UPlaylist playlist = parser.parseFile(is);
            mAdapter.update(playlist.getPlaylistItems());
        } catch (Exception e) {
            Log.d("Google", "" + e.toString());
        }
    }

    protected void onResume() {
        super.onResume();
        mRewardedVideoAd.resume(this);

    }



    private boolean filter(final String newText) {
        if (mAdapter != null) {

            if (newText.isEmpty()) {
                layoutManager.setSpanCount(3);
                mAdapter.setGroup(true);
                new _loadFile().execute();
            } else {
                layoutManager.setSpanCount(1);
                mAdapter.setGroup(false);
                mAdapter.getFilter().filter(newText);
            }
            mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());

            return true;
        } else {
            new downloadFile().execute("https://deepapp.000webhostapp.com/datas.m3u");
            return false;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return filter(query);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return filter(newText);
    }


    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getResources().getString(R.string.reward_ad_unit_id_test),
                new AdRequest.Builder().build());
    }

    public void runReward(M3UItem imm) {

        if(layoutManager.getSpanCount()==1){
            rewardedLink = imm;
            Log.e("hii", "rewaed" + mRewardedVideoAd.isLoaded());

            if (!mRewardedVideoAd.isLoaded()) {
                mRewardedVideoAd.show();
            } else if (!mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                try {
                    rewarded = false;
                    Intent intent = new Intent(this, ExoPlayer2.class);
                    intent.putExtra("Name", imm.getItemName());
                    intent.putExtra("Url", imm.getItemUrl());
                    intent.putExtra("Agent", imm.getItemUserAgent());

                    this.startActivity(intent);
                } catch (Exception ignored) {
                }
            }
            loadRewardedVideoAd();
        }
        else{
            layoutManager.setSpanCount(1);
            mAdapter.setGroup(false);
            mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());
        }


    }


    @SuppressLint("StaticFieldLeak")
    class downloadFile extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            spinner.setVisibility(View.VISIBLE);
        }

        @SuppressLint("WrongThread")
        @Override
        protected Boolean doInBackground(String... strings) {
            try {

                URL yahoo = new URL(strings[0]);
                is = yahoo.openStream();
                playlist = parser.parseFile(is);
                mAdapter.update(playlist.getPlaylistItems());

                return true;
            } catch (Exception e) {
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mAdapter.notifyDataSetChanged();

            spinner.setVisibility(View.GONE);
            search.setVisible(true);

        }
    }

    @SuppressLint("StaticFieldLeak")
    class _loadFile extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {

                mAdapter.update(playlist.getPlaylistItems());

                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            spinner.setVisibility(View.GONE);
            search.setVisible(true);

        }
    }
}