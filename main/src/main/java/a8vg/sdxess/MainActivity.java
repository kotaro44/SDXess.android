package a8vg.sdxess;

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

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.activities.BaseActivity;


public class MainActivity  extends BaseActivity implements ajaxReceiver  {
    TextView passInput = null;
    TextView userInput = null;
    Button loginBtn = null;
    TextView signupLbl = null;
    Spinner serverCombo = null;
    HostEdit hostEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdxess);
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

        this.setEvents();
    }

    public void disconnected(){
        setContentView(R.layout.sdxess);
        this.start();
    }

    private void setEvents(){
        final ajaxReceiver caller = this;
        loginBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
            String user = userInput.getText().toString();
            String password = passInput.getText().toString();

            if( password.length() == 0 ){
                Console.toast("Please enter a password!");
            }else {
                disableWindow();
                StaticRoutes.checkLogin( user , password , caller);
            }
            }
        });

        signupLbl.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Website.openSDXWebsite();
            }
        });
    }

    public void disableWindow(){
        this.passInput.setEnabled(false);
        this.userInput.setEnabled(false);
        this.loginBtn.setEnabled(false);
        this.signupLbl.setEnabled(false);
    }

    public void enableWindow(){
        this.passInput.setEnabled(true);
        this.userInput.setEnabled(true);
        this.loginBtn.setEnabled(true);
        this.signupLbl.setEnabled(true);
    }

    @Override
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
                        setContentView(R.layout.host_edit);
                        Console.activity.hostEdit = new HostEdit(userObj);
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

    @Override
    public void getResponse(String response){

    }

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}
