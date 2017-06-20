package org.a8vg.sdxessandroid;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kotaro on 6/15/2017.
 */

public class vpnconnect {
    TextView consoleTextView = null;
    Button disconnectBtn = null;
    TextView upgradeLbl = null;

    public vpnconnect(JSONObject userObj, String server ){
        this.start();
        try{
            Console.toast(server+":"+userObj.get("name").toString());
            this.consoleTextView.setText("connecting to " + server + "...");
            this.connectToVPN();
            this.setEvents();
        } catch(JSONException ex){

        }
    }

    private void setEvents(){
        disconnectBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Console.activity.setContentView(R.layout.activity_main);
                Console.activity.disconnect();
            }
        });

        upgradeLbl.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Website.openSDXWebsite();
            }
        });
    }

    public void start(){
        this.consoleTextView = (TextView) Console.activity.findViewById(R.id.consoleTextView);
        this.disconnectBtn = (Button) Console.activity.findViewById(R.id.disconnectBtn);
        this.upgradeLbl = (TextView) Console.activity.findViewById(R.id.upgradeLbl);
        this.upgradeLbl.setVisibility(View.GONE);
    }

    public void connectToVPN(){

    }
}
