package org.a8vg.sdxessandroid;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by kotaro on 6/15/2017.
 */

public class vpnconnect {

    public vpnconnect(){
        setEvents();
    }

    private void setEvents(){
        Button disconnectBtn = (Button) Console.activity.findViewById(R.id.disconnectBtn);
        TextView upgradeLbl = (TextView) Console.activity.findViewById(R.id.upgradeLbl);

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
}
