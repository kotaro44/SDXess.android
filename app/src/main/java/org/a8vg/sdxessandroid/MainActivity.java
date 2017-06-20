package org.a8vg.sdxessandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;


public class MainActivity  extends AppCompatActivity  {
    TextView passInput = null;
    TextView userInput = null;
    Button loginBtn = null;
    TextView signupLbl = null;
    Spinner serverCombo = null;

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
        this.passInput = (TextView) findViewById(R.id.passVal);
        this.userInput = (TextView) findViewById(R.id.userVal);
        this.loginBtn = (Button) findViewById(R.id.loginBtn);
        this.signupLbl = (TextView) findViewById(R.id.signupLbl);
        this.serverCombo = (Spinner) findViewById(R.id.serverCombo);

        this.getConfs();
        this.setEvents();
    }

    public void disconnect(){
        this.start();
    }

    private void setEvents(){
        loginBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String user = userInput.getText().toString();
                String password = passInput.getText().toString();

                if( password.length() == 0 ){
                    Console.toast("Please enter a pssword!");
                }else {
                    disableWindow();
                    StaticRoutes.checkLogin( user , password );
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

    public void postResponse(JSONObject resp){
        final JSONObject response = resp;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if( response == null ){
                        Console.toast("Network Error!");
                        enableWindow();
                    }else{
                        int status = Integer.parseInt( (String) response.get("status") );
                        if( status == 200 ){
                            JSONObject userObj = (JSONObject) response.get("data");
                            setContentView(R.layout.vpnconnect);
                            vpnconnect x = new vpnconnect(userObj , serverCombo.getSelectedItem().toString() );
                        }else{
                            Console.toast("Incorrect user/password!");
                            passInput.setText("");
                            enableWindow();
                        }
                    }
                }catch( JSONException ex ){
                    Console.toast("Unhandled Error!");
                }
            }
        });


    }

    public void disableWindow(){
        this.passInput.setEnabled(false);
        this.userInput.setEnabled(false);
        this.loginBtn.setEnabled(false);
        this.signupLbl.setEnabled(false);
        this.serverCombo.setEnabled(false);
    }

    public void enableWindow(){
        this.passInput.setEnabled(true);
        this.userInput.setEnabled(true);
        this.loginBtn.setEnabled(true);
        this.signupLbl.setEnabled(true);
        this.serverCombo.setEnabled(true);
    }

}
