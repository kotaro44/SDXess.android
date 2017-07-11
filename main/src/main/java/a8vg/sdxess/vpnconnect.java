package a8vg.sdxess;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.views.LaunchVPN_remote;

import static de.blinkt.openvpn.R.id.domainsLayout;

/**
 * Created by kotaro on 6/15/2017.
 */

public class vpnconnect {
    TextView consoleTextView = null;
    TextView cTimeLabel = null;
    Button disconnectBtn = null;
    TextView upgradeLbl = null;
    TimerTask timetask;
    ScrollView reroutedLayout = null;
    TableLayout sitesLayout = null;
    TextView routedLbl = null;

    private ArrayList<Website> IPlist = null;

    private boolean isConnected = false;

    private int seconds = 0;
    private boolean timerstatus = false;
    private Timer timer = new Timer();
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    public ArrayList<Website> websites = null;

    public vpnconnect( ArrayList<Website> websites ){
        this.websites = websites;
        this.start();
        this.disableWindow();
        this.consoleTextView.setText("connecting to " + HostEdit.server + "...");
        this.connectToVPN();
        this.setEvents();
    }

    private void setEvents(){
        this.disconnectBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
            ExecutorTask.end();
            Console.activity.setContentView(R.layout.activity_main);
            Console.activity.disconnected();
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
        this.cTimeLabel = (TextView) Console.activity.findViewById(R.id.cTimeLabel);
        this.routedLbl = (TextView) Console.activity.findViewById(R.id.reroutedLbl);
        this.reroutedLayout = (ScrollView) Console.activity.findViewById(R.id.reroutedSitesLayout);
        this.sitesLayout = (TableLayout) Console.activity.findViewById(R.id.sitesLayout);

        this.cTimeLabel.setVisibility(View.GONE);
        this.routedLbl.setVisibility(View.GONE);
        this.reroutedLayout.setVisibility(View.GONE);

        switch(HostEdit.accountType){
            case BASIC:
            case STARTER:
                break;
            case ADVANCED:
                this.upgradeLbl.setVisibility(View.GONE);
                break;
        }

        this.setSites();
    }

    public void setSites(){
        for( Website website : this.websites ){
            if( website.wasRerouted ) {
                TableRow row = new TableRow(Console.activity);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(lp);

                TextView domain_name = new TextView(Console.activity);
                domain_name.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                    }
                });

                domain_name.setText(website.name);

                row.addView(domain_name);
                TableRow.LayoutParams lp2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                lp2.weight = 1;
                domain_name.setLayoutParams(lp2);

                this.sitesLayout.addView(row);
            }
        }
    }

    public void connectToVPN(){
        ExecutorTask task = new ExecutorTask(HostEdit.server,this);
    }

    public void disableWindow(){
        this.disconnectBtn.setEnabled(false);
    }

    public void enableWindow(){
        this.disconnectBtn.setEnabled(true);
    }

    public void connected(ArrayList<String> ip2route,ArrayList<String> sites2reroute){
        StaticRoutes.Start();

        this.IPlist = new ArrayList<Website>();
        this.consoleTextView.setText("Connected to " + HostEdit.server + " as " + HostEdit.username );

        //cTimeLabel.setText("Rerouting default websites...");
        this.cTimeLabel.setVisibility(View.VISIBLE);
        this.routedLbl.setVisibility(View.VISIBLE);
        this.reroutedLayout.setVisibility(View.VISIBLE);
        //logginPanel.setVisible(false);

        //this.setSize(this.getWidth(), 200);
        //this.repaint();

        //this.websites = new ArrayList<>();

        /*ArrayList<Website> restored_sites = HostEdit.restoreWebsites();

        if( restored_sites != null ){
            if( !restored_sites.isEmpty() ){
                for( Website website : restored_sites ){
                    cTimeLabel.setText("Restoring " + website.name + "...");
                    this.websites.add(website);
                    if( website.wasRerouted )
                        website.route();
                }
            }

            for( Object w_domain : websites ){
                String domain = (String) w_domain;
                boolean addDefaultSite = true;
                String[] parts = domain.split(":");

                for( Website other : this.websites ){
                    if( other.ASN.compareTo(parts[1]) == 0 ){
                        addDefaultSite = false;
                    }
                }

                if( addDefaultSite ){
                    cTimeLabel.setText("Reading " + parts[0] + " from server... ");
                    Website website = new Website(parts[0]);
                    this.websites.add( website  );
                }
            }


            StaticRoutes.disableAllTrafficReroute();
            if( HostEdit.accountType == ACType.BASIC ){
                this.upgradeLbl.setVisibility(View.VISIBLE);
            }else{
                HostEdit.saveWebsites(this.websites);
            }
        }*/

        StaticRoutes.flushDNS();

        this.startTimer();
        this.isConnected = true;
        disconnectBtn.setEnabled(true);
        //hideBtn.setVisible(true);
        upgradeLbl.setVisibility(View.GONE);
        //this.hideOnTray();
    }

    public void reconnecting(){

    }

    public void updateMessage(String message){

    }

    public void disconnect(boolean byError , Runnable callback ){
        Console.activity.disconnected();
    }

    public void notconnected(String message){
        Console.activity.disconnected();
        Console.toast(message);
        /*consoleLabel.setText("Connection error: " + message);
        userField.setEnabled(true);
        serverCombo.setEnabled(true);
        passField.setEnabled(true);
        connectBtn.setEnabled(true);
        ctimeLbl.setVisible(false);
        ctimeLbl.setText("");*/
    }

    public void startTimer(){
        final vpnconnect _self = this;
        timetask = new TimerTask() {
            @Override
            public void run() {
                seconds++;
                df.setTimeZone(tz);
                String time = df.format(new Date(seconds*1000));
                _self.updateTime(time);
                //Console.log(seconds);
            }
        };
        seconds = 0;
        if (timerstatus == false){
            timerstatus = true;
            timer.scheduleAtFixedRate(timetask, 1000, 1000);
        }
    }

    public void updateTime(String _time){
        final String time = _time;
        Console.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cTimeLabel.setText("Connection time: "+time);
            }
        });

    }
}
