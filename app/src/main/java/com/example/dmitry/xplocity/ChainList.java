package com.example.dmitry.xplocity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import Adapters.ChainsAdapter;
import Classes.Chain;
import misc.VolleySingleton;
import XMLParsers.XMLChainsParser;

public class ChainList extends AppCompatActivity
{

    private ChainsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chain_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewChainActivity.class);
                startActivity(intent);
            }
        });

        download_chains("http://br-on.ru:3003/api/v1/chains?user_id=1");

        ListView newsListView = (ListView) findViewById(R.id.chain_list);
        newsListView.setOnItemClickListener(listListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chain_list, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void download_chains(String urlString) {
        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        chain_list_init(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

    }

    // Chain list
    private void chain_list_init(String xml) {

        ArrayList<Chain> chains;

        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLChainsParser ChainsParser = new XMLChainsParser();

        try {
            chains = ChainsParser.parse(stream);
            // используем адаптер данных
            adapter = new ChainsAdapter(this, chains);

            ListView listView = (ListView)findViewById(R.id.chain_list);
            listView.setAdapter(adapter);
        }
        catch (Throwable e) {
        }
    }


    AdapterView.OnItemClickListener listListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), ChainView.class);
            Chain chain = adapter.getItem(position);
            intent.putExtra("chain_id", chain.id);
            startActivity(intent);
        }
    };



}




