package org.a8vg.sdxessandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Console.context = getApplicationContext();
        Console.activity = this;
        Console.log("SDXess App Started!");


        this.start();
    }

    public void start(){
        this.getConfs();
        this.setEvents();
    }

    public void disconnect(){
        this.start();
    }

    private void setEvents(){
        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        TextView signupLbl = (TextView) findViewById(R.id.signupLbl);

        loginBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                TextView passInput = (TextView) findViewById(R.id.passVal);
                TextView userInput = (TextView) findViewById(R.id.userVal);
                Button loginBtn = (Button) findViewById(R.id.loginBtn);
                TextView signupLbl = (TextView) findViewById(R.id.signupLbl);
                Spinner serverCombo = (Spinner) findViewById(R.id.serverCombo);

                String user = userInput.getText().toString();
                String password = passInput.getText().toString();

                if( password.length() == 0 ){
                    Console.toast("Please enter a pssword!");
                }else {


                    passInput.setEnabled(false);
                    userInput.setEnabled(false);
                    loginBtn.setEnabled(false);
                    signupLbl.setEnabled(false);
                    serverCombo.setEnabled(false);

                    JSONObject userObj = StaticRoutes.checkLogin( user , password );
                    if(  userObj == null ) {
                        passInput.setEnabled(true);
                        userInput.setEnabled(true);
                        loginBtn.setEnabled(true);
                        signupLbl.setEnabled(true);
                        serverCombo.setEnabled(true);

                        Console.toast("Incorrect user/password!");

                        return;
                    }

                    setContentView(R.layout.vpnconnect);
                    vpnconnect x = new vpnconnect();
                }
            }
        });

        signupLbl.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Website.openSDXWebsite();
            }
        });
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    private void getConfs(){
        ArrayList<String> confs = new ArrayList<>();
        Field[] fields=R.raw.class.getFields();
        for(int count=0; count < fields.length; count++){
            confs.add(fields[count].getName());
        }

        Spinner sp = (Spinner) findViewById(R.id.serverCombo);
        ArrayAdapter<String> adp= new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,confs);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adp);
    }
}
