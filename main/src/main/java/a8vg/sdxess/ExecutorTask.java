package a8vg.sdxess;

import android.app.PendingIntent;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.activities.DisconnectVPN;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.LogItem;
import de.blinkt.openvpn.core.ProfileManager;

import static de.blinkt.openvpn.core.OpenVPNService.DISCONNECT_VPN;

/**
 * Created by kotaro on 6/28/2017.
 */

public class ExecutorTask {

    private Process process = null;
    private static boolean connected = false;
    private static boolean abortTimeOut = false;
    private String server = "";

    private static vpnconnect frame = null;
    private static ArrayList<String> ip2route = null;
    private static ArrayList<String> sites2route = null;

    private static Intent openVPN = null;

    public ExecutorTask(String server, vpnconnect frame) {
        //this.frame = frame;
        this.server = server;
        ExecutorTask.frame = frame;
        this.connect(server,frame.websites);
    }

    public static void vpnMessage(LogItem msg){
        if( Console.activity == null )
            return;
        final LogItem log_msg = msg;
        Console.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            Console.log(log_msg);
            ExecutorTask.processMessage(log_msg.toString());
            }
        });
    }

    public void connect(String server , ArrayList<Website> websites ){
        ConfigParser cp = new ConfigParser();
        InputStream inputStream = Console.activity.getApplicationContext().getResources().openRawResource(Console.activity.getResId("sdxess_" + server,R.raw.class));

        String newOpts = "";

        if( websites == null ) {
            newOpts = "redirect-gateway def1 bypass-dhcp";
        }else{
            for( Website website : websites ){
                if( website.wasRerouted ) {
                    for (IPRange range : website.ranges) {
                        newOpts += "\nroute " + range.getIP() + " " + range.getMask();
                    }
                }
            }
        }

        List<InputStream> streams = Arrays.asList(
                inputStream,
                new ByteArrayInputStream(newOpts.getBytes()));
        InputStream finalInputStream = new SequenceInputStream(Collections.enumeration(streams));

        BufferedReader reader = new BufferedReader(new InputStreamReader(finalInputStream));


        try {
            cp.parseConfig(reader);
            VpnProfile mResult = cp.convertProfile(server);
            LaunchVPN.profile = mResult;


            ProfileManager vpl = ProfileManager.getInstance(Console.activity);
            vpl.addProfile(mResult);
            vpl.saveProfile(Console.activity, mResult);
            vpl.saveProfileList(Console.activity);

            ExecutorTask.openVPN = new Intent(Console.activity, LaunchVPN.class);
            ExecutorTask.openVPN.putExtra(LaunchVPN.EXTRA_KEY, LaunchVPN.profile.getUUID().toString());
            ExecutorTask.openVPN.setAction(Intent.ACTION_MAIN);
            Console.activity.startActivity(ExecutorTask.openVPN);

            this.ip2route = new ArrayList<String>();
            this.sites2route = new ArrayList<String>();

            ExecutorTask.abortTimeOut = false;
            ExecutorTask.setTimeout(new Runnable() {
                public void run() {
                    ExecutorTask.connectionTimeout();
                }
            },30000);

        }catch(Exception ex){

        }
    }

    public static void processMessage(String line) {
        if( ExecutorTask.frame == null )
            return;

            //Initialization sequence complete
        if( line.contains("CONNECTED,SUCCESS") && !ExecutorTask.connected   ){
            ExecutorTask.connected = true;
            Console.log("---Connection start---");

            ExecutorTask.setTimeout(new Runnable() {
                public void run() {
                    ExecutorTask.frame.connected( ExecutorTask.ip2route , ExecutorTask.sites2route );
                }
            },1000);


            //RESTARTED CONNECTION
        }else if( line.contains("Connection reset, restarting") || line.contains("Restart pause,")
                || line.contains("[server] Inactivity timeout") ){
            frame.reconnecting();
            ExecutorTask.abortTimeOut = true;
            ExecutorTask.connected = false;
            //Static route ROUTE PARAMETER
        } else if( line.contains("sdxess-site") && line.contains("Unrecognized option") ){
            Pattern pattern = Pattern.compile("sdxess-site:[^\\s(]*");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()){
                String[] parts = matcher.group(0).split(":");
                sites2route.add( (parts[1] + ":" + parts[2]).trim() );
                Console.log("Website added for rerouting: " + parts[1]);
            }else{
                Console.log(line,true);
            }
            //Static route ROUTE PARAMETER
        } else if( line.contains("route.exe ADD") ){
            Pattern pattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()){
                ip2route.add(matcher.group(0));
            }

            pattern = Pattern.compile("ADD\\s(.*)");
            matcher = pattern.matcher(line);
            if (matcher.find()){
                StaticRoutes.addedRoutes.add(matcher.group(1));
            }

            ExecutorTask.frame.updateMessage("Rerouted " + StaticRoutes.addedRoutes.size() + " IP's...");
            Console.log(line,true);

        } else if ( line.contains("[server] Peer Connection Initiated") ){
            ExecutorTask.frame.updateMessage("Peer Connection Initiated...");
            ExecutorTask.abortTimeOut = true;
            Console.log(line,true);
        } else if ( line.contains("Successful ARP Flush on interface") ){
            Pattern pattern = Pattern.compile("\\[(\\d+)\\]");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()){
                StaticRoutes.interf = Integer.parseInt(matcher.group(1));
            }
            Console.log(line,true);
        } else if ( line.contains("Exiting due to fatal error") ){
            ExecutorTask.frame.disconnect(true,null);
            Console.log(line,true);
        } else {
            Console.log(line,true);
        }
    }

    public static void setTimeout(Runnable callback, int time){
        new android.os.Handler().postDelayed(callback,time);
    }

    public static void connectionTimeout(){
        if( !ExecutorTask.connected && !ExecutorTask.abortTimeOut ){
            ExecutorTask.frame.notconnected("Connection timed out");
            ExecutorTask.end();
        }
    }

    public static void end(){
        ExecutorTask.abortTimeOut = true;
        Intent intent = new Intent(Console.activity, DisconnectVPN.class);
        Console.activity.startActivity(intent);


        /*if( process != null ){
            process.destroyForcibly();
            process.destroy();
        }*/
    }
}
