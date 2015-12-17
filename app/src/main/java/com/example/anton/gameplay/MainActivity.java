package com.example.anton.gameplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity  implements OnTaskCompleted{

    //private static final String CARD_API = "http://192.168.2.8/CardApi/";
    private static final String CARD_API = "http://46.105.120.168/cardapi/";
    //persistent data to store user-info, used to identify user
    private String password;
    private String name;
    private String id;

    private final Context context = this;//to find resources etc
    private final OnTaskCompleted onTaskCompleted = this;//enables call-back methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //button save name
        final Button savenamebutton = (Button) findViewById(R.id.button_savename);
        savenamebutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.v("button", "save");
                //save persistent data
                EditText mEdit = (EditText) findViewById(R.id.id_text);
                Log.v("EditText", mEdit.getText().toString());
                id = ((EditText) findViewById(R.id.id_text)).getText().toString();
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString(getString(R.string.id_text), id).commit();
                name = ((EditText) findViewById(R.id.name_text)).getText().toString();
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString(getString(R.string.name_text), name).commit();
                password = ((EditText) findViewById(R.id.password_text)).getText().toString();
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString(getString(R.string.password_text), password).commit();
            }
        });
        //button send player
        final Button listgamesbutton = (Button) findViewById(R.id.button_listgames);
        listgamesbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String urlString = CARD_API +String.format("games/%s/starting",id);
                final int a=5;
                new CallAPI(onTaskCompleted, getString(R.string.startinggames))
                {
                    protected void onPostExecute(String result) {
                        int b=a+8;
                    }
                    }.execute(urlString, getString(R.string.GET));
            }
        });
        //button_startgame
        final Button startgamebutton = (Button) findViewById(R.id.button_startgame);
        startgamebutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String gamenr = ((EditText) findViewById(R.id.id_gamenumber)).getText().toString();
                String urlString = CARD_API +String.format("games/%s/%s/start",id,gamenr);
                new CallAPI(onTaskCompleted, getString(R.string.startgame)).execute(urlString, getString(R.string.POST),
                        CallAPI.createQueryStringForParameters(HashMapBuilder.build("password", password)));
            }
        });
        //button list games
        final Button sendplayerbutton = (Button) findViewById(R.id.button_sendplayer);
        sendplayerbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String urlString = CARD_API +String.format("players/%s/add/%s",id,name);
                new CallAPI(onTaskCompleted, getString(R.string.sendplayer)).execute(
                        urlString,
                        getString(R.string.POST),
                        CallAPI.createQueryStringForParameters(HashMapBuilder.build("password", password)));
              }
        });
        //get saved values for id,name and password
        id = PreferenceManager.getDefaultSharedPreferences(context).getString(getString(R.string.id_text), "");
        ((EditText) findViewById(R.id.id_text)).setText(id);
        name = PreferenceManager.getDefaultSharedPreferences(context).getString(getString(R.string.name_text), "");
        ((EditText) findViewById(R.id.name_text)).setText(name);
        password = PreferenceManager.getDefaultSharedPreferences(context).getString(getString(R.string.password_text), "");
        ((EditText) findViewById(R.id.password_text)).setText(password);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_settings:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set title
                alertDialogBuilder.setTitle("About");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Test app created to demonstrate the use of REST service CardApi\nBy Anton Bil")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                // current dialog
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;
        }
        return false;
    }    /**
     * displays result of call to create new player
     * @param result = json-string
     */
    private void sendPlayer(String result){
        //display result in textview
        TextView tView = (TextView) findViewById(R.id.sendplayerview);
        tView.setText(result);
    }

    /**
     * processes result of start-game request
     * @param result = json-string
     */
    private void startGame(String result){
        Log.v("display", result);//display in android logcat
        try {
            JSONObject jsonResultStartGameObject=new JSONObject(result);
            String error=jsonResultStartGameObject.getString("error");
            //display error message. Only reached when error part of json-message.
            Toast.makeText(getApplicationContext(), "Error\n"+error, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            //no error
            //do whatever you must do with result if error not part of message
        }
        //display raw result in textview
        TextView tView = (TextView) findViewById(R.id.startgameview);
        tView.setText(result);
    }
    /**
     * displays result of starting games
     * @param result  = json-string
     */
    private void startingGames(String result){
        //example how to parse json-string in java

        TextView tView = (TextView) findViewById(R.id.listgamesview);
        try {
            JSONObject jsonObject=new JSONObject(result);
            JSONArray jsonStartingGamesArray = jsonObject.getJSONArray("games");
            String s="";
            if (jsonStartingGamesArray != null) {
                for(int i = 0 ; i < jsonStartingGamesArray.length(); i++) {
                    try {
                        String id;
                        String player;
                        JSONObject jsonStartGameObject=jsonStartingGamesArray.getJSONObject(i);
                        id = jsonStartGameObject.getString("id");
                        Log.v("display", id);//display in android logcat
                        player = jsonStartGameObject.getString("player");
                        s+=String.format(";id:%s,player:%s",id,player);
                    } catch (JSONException e) {
                        Log.e("JSON Parser", "Error parsing data " + e.toString());
                    }
                }
            }
            //display processed result in textview
            tView.setText(s);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            tView.setText(result);
        }

    }

    /**
     * this method is called when get or post-requests are finished
     * @param result = the result of the request, string in JSON-format
     * @param call = the request-call that has finished
     */
    @Override
    public void onTaskCompleted(String result, String call) {
        if (call.equals(getString(R.string.startinggames))) startingGames(result);else
        if (call.equals(getString(R.string.startgame))) startGame(result);else
        if (call.equals(getString(R.string.sendplayer))) sendPlayer(result);

    }

}

