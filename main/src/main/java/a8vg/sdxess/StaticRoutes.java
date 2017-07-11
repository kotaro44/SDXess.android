package a8vg.sdxess;

/**
 * Created by kotaro on 6/13/2017.
 */

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.CIDRIP;
import de.blinkt.openvpn.core.NetworkSpace;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVpnManagementThread;

import static a8vg.sdxess.Console.context;

public class StaticRoutes {


    public static ArrayList<String> addedRoutes = new ArrayList<>();
    public static String sdxessGateway = "";
    public static String sdxessIP = "";
    public static int interf = -1;

    public static OpenVpnManagementThread ovpnManagementThread;

    public static String localIp = null;

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static boolean isAdmin(){
        Preferences prefs = Preferences.systemRoot();
        PrintStream systemErr = System.err;
        synchronized(systemErr){    // better synchroize to avoid problems with other threads that access System.err
            System.setErr(null);
            try{
                prefs.put("foo", "bar"); // SecurityException on Windows
                prefs.remove("foo");
                prefs.flush(); // BackingStoreException on Linux
                return true;
            }catch(Exception e){
                return false;
            }finally{
                System.setErr(systemErr);
            }
        }
    }

    public static void Start(){
        //WE ADD THE X.X.X.1 TO THE GATEWAY SINCE THE CONNECTION IS BY UDP
        /*String gatewayip;
        try {
            StaticRoutes.sdxessIP = StaticRoutes.GetTAPInfo(3);
            gatewayip = StaticRoutes.GetTAPInfo(6);
            Console.log("Original Gateway IP: " + gatewayip );
            String[] parts = gatewayip.split("\\.");
            StaticRoutes.sdxessGateway = parts[0] + "." + parts[1] + "." + parts[2] + ".1";

        } catch (IOException ex) {
            Logger.getLogger(StaticRoutes.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        String[] parts = ip.split("\\.");
        StaticRoutes.localIp = parts[0] + "." + parts[1] + "." + parts[2] + "1";

    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void flushDNS(){
        /*ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "ipconfig /flushdns");
        builder.redirectErrorStream(true);
        Process p = null;
        try {
            p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line =  r.readLine();
            while (line != null) {
                Console.log(line);
                line = r.readLine();
            }

        } catch (IOException ex) {
            Logger.getLogger(StaticRoutes.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void checkLogin(String user, String pass, ajaxReceiver caller ){
        try {
            MessageDigest m;
            m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(pass.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            String hashtext = bigInt.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while(hashtext.length() < 32 ){
                hashtext = "0"+hashtext;
            }
            pass = hashtext;

            JSONObject obj = new JSONObject( "{\"authentication_data\":{\"user\":\"" + user + "\",\"password\":\"" + pass +"\"}}" );
            Website.ajaxPOST( "http://inet99.ji8.net/SDXess-WS/login.php" , obj , caller );

        } catch (NoSuchAlgorithmException ex) {

        } catch (JSONException ex) {

        }
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void disableAllTrafficReroute(){
        //StaticRoutes.deleteStaticRoute("0.0.0.0","128.0.0.0",StaticRoutes.sdxessGateway);
        //StaticRoutes.deleteStaticRoute("128.0.0.0","128.0.0.0",StaticRoutes.sdxessGateway);
        //Console.log("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+StaticRoutes.ovpnManagementThread);
        //OpenVPNService.mRoutes.deleteRoute("0.0.0.0");
        StaticRoutes.deleteStaticRouteCMD("0.0.0.0/1","tun0");
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void enableAllTrafficReroute(){
        //StaticRoutes.AddStaticRoute("0.0.0.0","128.0.0.0",StaticRoutes.sdxessGateway);
        //StaticRoutes.AddStaticRoute("128.0.0.0","128.0.0.0",StaticRoutes.sdxessGateway);
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static String NSLookup(String domainname)  {
        try {
            InetAddress inetHost = InetAddress.getByName(domainname);
            String ip = inetHost.getHostAddress();
            return ip;
        } catch(UnknownHostException ex) {
            return "Unrecognized host";
        }
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void AddStaticRoute(String destination_ip, String subnetmask, String gatewayip){
        /*try{
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "route ADD "
                    + destination_ip + " MASK " + subnetmask + " " + gatewayip + " if " + interf);
            Console.log("Rerouting " + destination_ip + " " + subnetmask + " through " +
                    gatewayip + " interface:" + interf);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            int flag=0;
            while (flag == 0) {
                line = r.readLine();
                if (line == null) {
                    flag = 1;
                    break;
                }
                Console.log(line);
            }
        } catch( IOException ex ){
            Logger.getLogger(StaticRoutes.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && !OpenVPNService.mProfile.mAllowLocalLAN) {
            OpenVPNService.mRoutes.addIPSplit(new CIDRIP(destination_ip, subnetmask), true);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && OpenVPNService.mProfile.mAllowLocalLAN)
            OpenVPNService.mRoutes.addIP(new CIDRIP(destination_ip, subnetmask), false);
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void AddStaticRoute(String destination_ip){
        String subnetmask = "255.255.0.0";
        StaticRoutes.AddStaticRoute(destination_ip,subnetmask,StaticRoutes.sdxessGateway);
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void AddStaticRoute(String destination_ip, String subnetmask){
        StaticRoutes.AddStaticRoute(destination_ip,subnetmask,StaticRoutes.sdxessGateway);
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void flushAddedRoutes(){
        for( int i = 0 ; i < StaticRoutes.addedRoutes.size() ; i++ ){
            //StaticRoutes.deleteStaticRouteCMD(StaticRoutes.addedRoutes.get(i));
        }
        StaticRoutes.addedRoutes = new ArrayList<>();
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    private static void deleteStaticRouteCMD( String route , String adapter ) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("ip route delete " + route + " dev " + adapter);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            Console.log("Disable");
            while( (line = bufferedReader.readLine() ) != null ){

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "route DELETE " + route);
            builder.redirectErrorStream(true);
            Console.log( "Unrouting " + route );
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            int flag=0;
            while (flag == 0) {
                line = r.readLine();
                if (line == null) {
                    flag = 1;
                    break;
                }
                Console.log(line);
            }
        } catch( IOException ex ){
            Logger.getLogger(StaticRoutes.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void deleteStaticRoute( String route , String mask , String gateway) {
        //deleteStaticRouteCMD( route + " MASK " + mask + " " + gateway );
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void deleteStaticRoute( String route , String mask ) {
        //deleteStaticRouteCMD( route + " MASK " + mask  );
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static void deleteStaticRoute( String route  ) {
        //deleteStaticRouteCMD( route );
    }

    /***************************************************************************
     ***  brief                                                               ***
     ***  serial number ????                                                  ***
     ***  parameter out <none>                                                ***
     ***  parameter in  <none>                                                ***
     ***  return <none>                                                       ***
     *** @param                                                               ***
     ***************************************************************************/
    public static String GetTAPInfo(int tapindex) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "\"openvpn/openvpn.exe\" --show-net");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        int x =0,y=-1;
        String[] tapinfo = new String[10];
        while (x == 0) {
            line = r.readLine();
            if (line == null) {
                x = 1;
                //break;
            }else{
                if(line.contains("TAP-Windows Adapter")){
                    y=0;
                }
                if(y!=-1 && y<=9){
                    tapinfo[y] = line.toString();
                    //Console.log("tapinfo "+y+" "+tapinfo[y]);
                    y++;
                }
            }
        }
        String gettapconfig = "";
        String replacetext = "";
        String startvalue;
        int flag = 0;
        int index = 0;
        switch (tapindex) {
            case 1: case 2: case 4: case 7: case 8: //1 index, 2 guid, 4 mac, 7 dhcp lease obtained, 8,dhcp lease expires
                if(tapindex==1){replacetext="Index = ";}
                if(tapindex==2){replacetext="GUID = ";}
                if(tapindex==4){replacetext="MAC = ";}
                if(tapindex==7){replacetext="DHCP LEASE OBTAINED = ";}
                if(tapindex==8){replacetext="DHCP LEASE EXPIRES  = ";}
                gettapconfig = (tapinfo[tapindex].replace(replacetext,"")).trim();
                break;
            case 3: case 5: case 6: //3 ip, 5 gateway ip, 6 dhcp server ip
                if(tapindex==3){replacetext="IP = ";}
                if(tapindex==5){replacetext="GATEWAY = ";}
                if(tapindex==6){replacetext="DHCP SERV = ";}
                String ip = (tapinfo[tapindex].replace(replacetext,"")).trim();
                for(x=0; x<ip.length(); x++){
                    if(Character.isDigit(ip.charAt(x))){
                        gettapconfig = gettapconfig + ip.charAt(x);
                    }else{
                        if(ip.charAt(x)=='.'){
                            gettapconfig = gettapconfig + ip.charAt(x);
                        }
                        if(ip.charAt(x)=='/'){
                            break;
                        }
                    }
                }
                break;
            case 9: case 10: case 11: //9 ip mask, 10 gateway ip mask, 11 dhcp server ip mask
                if(tapindex==9){replacetext="IP = "; index=3;}
                if(tapindex==10){replacetext="GATEWAY = ";index=5;}
                if(tapindex==11){replacetext="DHCP SERV = ";index=6;}
                startvalue = (tapinfo[index].replace(replacetext,"")).trim();
                for(x=0; x<startvalue.length(); x++){
                    if(startvalue.charAt(x)=='/'){
                        for(y=(x+1); y<startvalue.length(); y++){
                            if(Character.isDigit(startvalue.charAt(y))){
                                gettapconfig = gettapconfig + startvalue.charAt(y);
                            }else{
                                if(startvalue.charAt(y)=='.'){
                                    gettapconfig = gettapconfig + startvalue.charAt(y);
                                }else{
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            case 12: //12 dns server1 ip,
                index=9;
                startvalue = (tapinfo[index].replace("DNS SERV = ","")).trim();
                flag = 0;
                for(x=0; x<startvalue.length(); x++){
                    if(Character.isDigit(startvalue.charAt(x))){
                        gettapconfig = gettapconfig + startvalue.charAt(x);
                    }else{
                        if(startvalue.charAt(x)=='.'){
                            gettapconfig = gettapconfig + startvalue.charAt(x);
                        }
                        if(startvalue.charAt(x)=='/'){
                            break;
                        }
                    }
                }
                if (flag==1){
                    break;
                }
                break;
            case 13: // 13 dns server1 nm
                index=9;
                startvalue = (tapinfo[index].replace("DNS SERV = ","")).trim();
                flag = 0;
                for(x=0; x<startvalue.length(); x++){
                    if(startvalue.charAt(x)=='/'){
                        flag = 1;
                        for(y=(x+1); y<startvalue.length(); y++){
                            if(Character.isDigit(startvalue.charAt(y))){
                                gettapconfig = gettapconfig + startvalue.charAt(y);
                            }else{
                                if(startvalue.charAt(y)=='.'){
                                    gettapconfig = gettapconfig + startvalue.charAt(y);
                                }else{
                                    break;
                                }
                            }
                        }
                    }
                    if (flag==1){
                        break;
                    }
                }
                break;
            case 14://DNS Server 2 ip
                index = 9;
                startvalue = (tapinfo[index].replace("DNS SERV = ","")).trim();
                for(x=0; x<startvalue.length(); x++){
                    if(startvalue.charAt(x)==' '){
                        for(y=(x+1); y<startvalue.length(); y++){
                            if(Character.isDigit(startvalue.charAt(y))){
                                gettapconfig = gettapconfig + startvalue.charAt(y);
                            }else{
                                if(startvalue.charAt(y)=='.'){
                                    gettapconfig = gettapconfig + startvalue.charAt(y);
                                }
                                if(startvalue.charAt(y)=='/'){
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            case 15://DNS Server 2 nm
                index = 9;
                startvalue= (tapinfo[index].replace("DNS SERV = ","")).trim();
                for(x=0; x<startvalue.length(); x++){
                    if(startvalue.charAt(x)==' '){
                        for(y=(x+1); y<startvalue.length(); y++){
                            if(startvalue.charAt(y)=='/'){
                                for(int z=(y+1); z<startvalue.length(); z++){
                                    if(Character.isDigit(startvalue.charAt(z))){
                                        gettapconfig = gettapconfig + startvalue.charAt(z);
                                    }else{
                                        if(startvalue.charAt(z)=='.'){
                                            gettapconfig = gettapconfig + startvalue.charAt(z);
                                        }else{
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            default:
                gettapconfig = null;
                break;
        }
        return gettapconfig;
    }

}
