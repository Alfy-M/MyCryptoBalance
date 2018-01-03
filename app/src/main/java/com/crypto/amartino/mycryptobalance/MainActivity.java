package com.crypto.amartino.mycryptobalance;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
        String myCoins = "[{'name':'Bitcoin','quantity':'11'},{'name':'OmiseGo','quantity':'13'}]";
        editor.putString("myCoins", myCoins);
        editor.putString("myCurrency", "EUR");
        editor.commit();
        prepareList();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                },
                1500);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
                final AlertDialog.Builder coinDialog = new AlertDialog.Builder(MainActivity.this);
                final View coinView = getLayoutInflater().inflate(R.layout.dialog, null);
                Spinner spinner = (Spinner) coinView.findViewById(R.id.coins_spinner);
                Button cancel_button = (Button) coinView.findViewById(R.id.cancel_button);
                Button confirm_button = (Button) coinView.findViewById(R.id.confirm_button);
                coinDialog.setView(coinView);
                final AlertDialog showDialog = coinDialog.create();
                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialog.dismiss();
                    }
                });
                confirm_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        
                    }
                });
                String[] myResArray = getResources().getStringArray(R.array.coins_array);
                List<String> myResArrayList = Arrays.asList(myResArray);
                List<String> myResMutableList = new ArrayList<String>(myResArrayList);
                //Remove HODL Coins from list of all available coins
                SharedPreferences settings = getPreferences(0);
                String coins = settings.getString("myCoins", "Not Found");
                JSONArray jArray = null;
                if (!coins.isEmpty()) {
                    try {
                        jArray = new JSONArray(coins);
                        for (int i = 0; i < jArray.length(); i++) {
                            Log.d("LOOP", "Test");
                            JSONObject oneObject = jArray.getJSONObject(i);
                            // Pulling items from the array
                            String oneObjectsItem = oneObject.getString("name");
                            myResMutableList.remove(oneObjectsItem);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item, myResMutableList);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinner.setAdapter(adapter);



                showDialog.show();


            }
        });
    }

    public void clear() {
        int size = coinsList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                coinsList.remove(0);
            }

            mAdapter.notifyItemRangeRemoved(0, size);
        }
    }

    private void prepareList() {
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
                    for (int i = 0; i < jArray.length(); i++) {
                        String price = "";
                        Log.d("LOOP", "Test");
                        JSONObject oneObject = jArray.getJSONObject(i);
                        // Pulling items from the array
                        String oneObjectsItem = oneObject.getString("name");
                        String oneObjectsItem2 = oneObject.getString("quantity");
                        // Create URL
                        URL coinMarketCap = new URL("https://api.coinmarketcap.com/v1/ticker/" + oneObjectsItem + "/");
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
                        float value = Float.parseFloat(oneObjectsItem2) * Float.parseFloat(price);
                        Coin coin = new Coin(oneObjectsItem, oneObjectsItem2, Float.toString(value));
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
            Log.d("listSize", String.valueOf(coinsList.size()));
            /*if (coinsList.size()>0) {
                Log.d("Inside","1stTime");
                coinsList.clear();
                mAdapter.notifyDataSetChanged();
            }*/
            //coinsList.clear();
            /*if (coinsList.size()>0) {
                mAdapter.setCoinsList(new ArrayList<Coin>());
                mAdapter.notifyDataSetChanged();
            }*/
            coinsList.clear();
            mAdapter.notifyDataSetChanged();
            prepareList();
            //mAdapter.setCoinsList(coinsList);
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    },
                    3000);
            //mAdapter.notifyItemRangeInserted(0,coinsList.size());
        }
        return super.onOptionsItemSelected(item);
    }
}
