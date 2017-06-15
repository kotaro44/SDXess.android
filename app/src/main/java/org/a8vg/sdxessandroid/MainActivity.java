package org.a8vg.sdxessandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Console.context = getApplicationContext();
        Console.log("SDXess App Started!");

        this.getConfs();
        this.setEvents();

    }

    private void setEvents(){
        Button button = (Button) findViewById(R.id.loginBtn);

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Console.toast("Hello");
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
