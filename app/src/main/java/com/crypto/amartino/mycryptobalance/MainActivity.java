package com.crypto.amartino.mycryptobalance;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private List<Coin> coinsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CoinsAdapter mAdapter;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Adversting https://developer.android.com/distribute/best-practices/earn/show-ads-admob.html
        MobileAds.initialize(this, "ca-app-pub-3224473877526605~5449948090");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);


        //Improve the recycle: https://code.tutsplus.com/tutorials/getting-started-with-recyclerview-and-cardview-on-android--cms-23465
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new CoinsAdapter(coinsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        String myCoins = "[{'name':'Bitcoin','value':'11'},{'name':'OmiseGo','value':'13'}]";
        editor.putString("myCoins", myCoins);
        editor.commit();
        prepareList();
        mAdapter.notifyDataSetChanged();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void prepareList()  {
        //Eseguo la lettura dei valori in un thread


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // All your networking logic
                // should be here
                SharedPreferences settings = getPreferences(0);
                String coins = settings.getString("myCoins", "Not Found");
                JSONArray jArray = null;
                try {
                    jArray = new JSONArray(coins);
                    for (int i=0; i < jArray.length(); i++)
                    {
                        String price = "";
                        Log.d("LOOP","Test");
                        JSONObject oneObject = jArray.getJSONObject(i);
                        // Pulling items from the array
                        String oneObjectsItem = oneObject.getString("name");
                        String oneObjectsItem2 = oneObject.getString("value");


                        // Create URL

                        URL coinMarketCap = new URL("https://api.coinmarketcap.com/v1/ticker/"+oneObjectsItem+"/");
                        // Create connection
                        HttpsURLConnection myConnection =
                                (HttpsURLConnection) coinMarketCap.openConnection();
                        if (myConnection.getResponseCode() == 200) {
                            InputStream responseBody = myConnection.getInputStream();
                            InputStreamReader responseBodyReader =
                                    new InputStreamReader(responseBody, "UTF-8");
                            JsonReader jsonReader = new JsonReader(responseBodyReader);
                            jsonReader.beginArray();
                            while (jsonReader.hasNext()) {
                                jsonReader.beginObject();
                                while (jsonReader.hasNext()) {
                                    String value = jsonReader.nextName();

                                    //Log.d("Value",jsonReader.nextString());
                                    if (value.equals("price_usd")) {

                                        //Log.d("price",jsonReader.nextString());
                                         price = jsonReader.nextString();



                                        // price = jsonReader.nextString();

                                    } else {
                                        jsonReader.skipValue();
                                    }

                                }
                                jsonReader.endObject();

                            }
                            jsonReader.endArray();







                        } else {
                            // Error handling code goes here
                        }

                        Coin coin = new Coin(oneObjectsItem, price, oneObjectsItem2);
                        coinsList.add(coin);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
