package com.mobiconnect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
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

    private RewardedVideoAd mRewardedVideoAd;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private M3UItem rewardedLink;
    private boolean rewarded;
    M3UPlaylist playlist;
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
                    Intent intent = new Intent(ctx, playerExo.class);
                    intent.putExtra("Name", rewardedLink.getItemName());
                    intent.putExtra("Url", rewardedLink.getItemUrl());
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
                        Intent intent = new Intent(ctx, playerExo.class);
                        intent.putExtra("Name", rewardedLink.getItemName());
                        intent.putExtra("Url", rewardedLink.getItemUrl());
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


        mPlaylistParams = findViewById(R.id.playlist_params);
        mPlaylistList = findViewById(R.id.playlist_recycler);
        spinner = findViewById(R.id.login_progress);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mPlaylistList.setLayoutManager(layoutManager);
        mAdapter = new PlaylistAdapter(this);
        mPlaylistList.setAdapter(mAdapter);
        //loader(filepath.getPath());
        //new downloadFile().execute("https://lookaside.fbsbx.com/file/today.m3u?token=AWyka0Nr7nrupPz8muaU3opKSAMbyAofP6_EubLCp5LvKO4Hk3K4LT7ndRyl_rDRIrp8uZgJv_c6NPExmfkZ1R4viW0V00pi6n4Rq2mv2dx9i2UZQx6AYKOpxur2tXbxty-icYP8-CvtnfFpFhcaR9A76AiCABT64_5pUXapMQZp1w"); // this will read direct channels from url
        new downloadFile().execute("https://cdn.fbsbx.com/v/t59.3654-21/60778134_452602911980059_8564121043982090240_n.m3u/tv_channels_75641_plus.m3u?_nc_cat=107&_nc_ht=cdn.fbsbx.com&oh=ea59bab343440a852c2e5fd6c2695710&oe=5CFF5343&dl=1&fbclid=IwAR1f8N2fWmnzxPtmty9erK-iudOFsu4FnLTOvNYiYgv9Ushaat3zUynARlM");
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
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
            case R.id.app_bar_search:
                setContentView(R.layout.searchable);
                break;
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }


    private boolean filter(final String newText) {
        if (mAdapter != null) {
            if (newText.isEmpty()) {
                new _loadFile().execute();
            } else {
                mAdapter.getFilter().filter(newText);
            }
            return true;
        } else {
            new downloadFile().execute("https://cdn.fbsbx.com/v/t59.3654-21/60778134_452602911980059_8564121043982090240_n.m3u/tv_channels_75641_plus.m3u?_nc_cat=107&_nc_ht=cdn.fbsbx.com&oh=ea59bab343440a852c2e5fd6c2695710&oe=5CFF5343&dl=1&fbclid=IwAR1f8N2fWmnzxPtmty9erK-iudOFsu4FnLTOvNYiYgv9Ushaat3zUynARlM");
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

        rewardedLink = imm;
        Log.e("hii", "rewaed" + mRewardedVideoAd.isLoaded());

        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            try {
                rewarded = false;
                Intent intent = new Intent(this, playerExo.class);
                intent.putExtra("Name", imm.getItemName());
                intent.putExtra("Url", imm.getItemUrl());
                this.startActivity(intent);
            } catch (Exception ignored) {
            }
        }
        loadRewardedVideoAd();


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
        }
    }
}