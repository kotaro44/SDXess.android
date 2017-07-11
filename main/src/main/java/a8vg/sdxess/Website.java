package a8vg.sdxess;

/**
 * Created by kotaro on 6/13/2017.
 */

import android.content.Intent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.net.Uri;
import android.app.Activity;
import android.os.Looper;

import java.net.URL;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.*;
/**
 *
 * @author kotaro
 */
public class Website implements ajaxReceiver {
    public static String USER_AGENT = "Mozilla/5.0";


    public static void openSDXWebsite(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.sdxess.com"));
        Console.activity.startActivity(browserIntent);
    }

    public static Website restore(File file){
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            Website result = new Website();

            //first line should be ASN
            String line = br.readLine();
            result.name = file.getName();
            result.ASN = line;
            //main IP is the second line
            result.IP = line = br.readLine();
            result.wasRerouted = Boolean.parseBoolean(br.readLine());
            result.Description = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\\\");
                IPRange range = new IPRange( parts[0] , Integer.parseInt( parts[1] ) );
                if( range.isValid )
                    result.ranges.add( range );
            }
            result.isValid = true;
            fileReader.close();
            return result;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Website restore(String DATA){
        System.out.println(DATA);
        Website result = new Website();
        String[] lines = DATA.split("\\r?\\n");

        //first line should be ASN
        result.ASN = lines[0];
        result.name = lines[1];

        //main IP is the second line
        result.IP = lines[2];
        result.wasRerouted = Boolean.parseBoolean(lines[3]);
        result.Description = lines[4];

        String[] parts = null;
        IPRange range = null;
        for( int i = 5 ; i < lines.length ; i++ ){
            parts = lines[i].split("\\\\");
            range = new IPRange( parts[0] , Integer.parseInt( parts[1] ) );
            if( range.isValid )
                result.ranges.add( range );
        }
        result.isValid = true;
        return result;

    }

    public static void ajaxPOST(String url, JSONObject obj_param , ajaxReceiver main_caller ){
        final String url_string = url;
        final JSONObject obj = obj_param;
        final ajaxReceiver caller = main_caller;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost request = new HttpPost(url_string);
                    StringEntity params = new StringEntity( obj.toString() );
                    request.addHeader("content-type", "application/json");
                    request.setEntity(params);
                    HttpResponse response = client.execute(request);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    Console.activity.postResponse( new JSONObject(responseString) );
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
                    Console.activity.postResponse(null);
                } catch (IOException ex) {
                    Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
                    Console.activity.postResponse(null);
                } catch ( Exception ex ){
                    Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
                    caller.postResponse(null);
                }
                Looper.loop();
            }
        });
        thread.start();
    }

    public static void ajaxGET(String url, ajaxReceiver main_caller){
        final String url_string = url;
        final ajaxReceiver caller = main_caller;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(url_string);
                    HttpResponse response = client.execute(request);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    caller.getResponse(responseString);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
                    Console.activity.postResponse(null);
                } catch (IOException ex) {
                    Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
                    Console.activity.postResponse(null);
                } catch ( Exception ex ){
                    Logger.getLogger(Website.class.getName()).log(Level.SEVERE, null, ex);
                    caller.postResponse(null);
                }
                Looper.loop();
            }
        });
        thread.start();
    }

    private void log(String msg){
        final String message = msg;
        Console.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Console.activity.hostEdit.consoleView.setText(message);
            }
        });
    }

    public static String URLtoASN(String url){
        /*String IP = StaticRoutes.NSLookup( url );
        if( IP.compareTo("Unrecognized host") == 0 )
            return null;
        return Website.IPtoASN( IP );*/
        return null;
    }

    public static String IPtoASN(String IP){
        /*String json = Website.ajaxGET("https://ipinfo.io/" + IP + "/json");
        JSONObject obj = new JSONObject(  json  );
        return obj.get("org").toString().split(" ")[0];*/
        return null;
    }

    public static boolean isIP(String text){
        return text.matches(  "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$" );
    }

    private static Pattern pDomainNameOnly;
    private static Pattern ASNOnly;
    private static final String DOMAIN_NAME_PATTERN = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
    private static final String ASN_PATTERN = "^AS[0-9]+$";

    static {
        pDomainNameOnly = Pattern.compile(DOMAIN_NAME_PATTERN);
        ASNOnly = Pattern.compile(ASN_PATTERN);
    }

    public static boolean isValidDomainName(String domainName) {
        return pDomainNameOnly.matcher(domainName).find();
    }

    public static boolean isASNNumber(String domainName) {
        return ASNOnly.matcher(domainName).find();
    }


    /******** Class Starts ***********/
    public ArrayList<IPRange> ranges = new ArrayList<IPRange>();
    public String IP = "";
    public String name = "";
    public String ASN = "";
    public String Description = "";
    public HostEdit callbacker = null;
    public boolean isValid = true;
    public boolean wasRerouted = true;
    private boolean routed = false;

    public boolean _shouldbereouted = false;
    //private HostEdit callbacker = null;

    public Website(){

    }

    public Website(String name) {
        WebsiteConstruct2( name );
    }

    public Website(String name,HostEdit callbacker) {
        this.callbacker = callbacker;
        WebsiteConstruct2( name );
    }

    public void WebsiteConstruct2(String name){
        this.name = name;
        Website.ajaxGET("http://inet99.ji8.net/IPCrawler/getips.php?a=" + name, this);
    }


    public static String getRadnURL(String ASN){
        return "http://www.radb.net/query/?advanced_query=1%091%091%091%091%0" +
                "91%091%091%091%091%091&keywords=" + ASN + "&query=Query&-K=1" +
                "&-T=1&-T+option=route&ip_lookup=1&ip_option=-L&-i=1&-i+optio" +
                "n=origin&-r=1";
    }

    public static ArrayList<IPRange> processRanges(String HTML){
        Pattern pattern = Pattern.compile("route:\\s*(([^\\<])*)");
        Matcher matcher = pattern.matcher(HTML);
        ArrayList<IPRange> ranges = new ArrayList<IPRange>();
        String[] parts;

        while(matcher.find()){
            parts = matcher.group(1).split("/");
            ranges.add( new IPRange(parts[0], Integer.parseInt(parts[1])) );
        }

        return ranges;
    }

    public String[][] toRangesArray(){
        String[][] result = new String[this.ranges.size()][3];
        for( int i = 0 ; i < this.ranges.size() ; i++ ){
            IPRange range = this.ranges.get(i);
            result[i][0] = range.getIP();
            result[i][1] = range.getMask();
            result[i][2] = range.toString(true);
        }
        return result;
    }

    private void reduceRanges(){
        if( this.routed )
            return;

        ArrayList<IPRange> ranges2remove = new ArrayList<IPRange>();
        for( IPRange range : this.ranges ){
            for( IPRange range2 : this.ranges ){
                if( range != range2 ){
                    if( range.contains(range2) ){
                        ranges2remove.add(range2);
                    }
                }
            }
        }
        this.ranges.removeAll(ranges2remove);
    }

    public void deleteRouting(){
        this.deleteRouting(null);
    }

    public void route(){
        this.route(null);
    }

    public void route(HostEdit routingTable){
        /*if( !this.routed ){
            this.routed = true;

            IPRange range = null;
            for( int i = 0 ; i < this.ranges.size() ; i++ ){
                range = this.ranges.get(i);
                StaticRoutes.AddStaticRoute( range.getIP() , range.getMask() );
                if( routingTable != null ){
                    routingTable.message("Routing " + this.name + " " + Math.floor(100*((float)i/this.ranges.size())) + "% ...");
                }
            }
            StaticRoutes.flushDNS();
        }*/
    }

    public boolean isRouted(){
        return this.routed;
    }

    public void deleteRouting(HostEdit routingTable){
        /*if( this.routed ){
            this.routed = false;
            IPRange range = null;
            for( int i = 0 ; i < this.ranges.size() ; i++ ){
                range = this.ranges.get(i);
                StaticRoutes.deleteStaticRoute(range.getIP() , range.getMask() ,
                        StaticRoutes.sdxessGateway );
                if( routingTable != null ){
                    routingTable.message("Unrouting " + this.name + " " + Math.floor(100*((float)i/this.ranges.size())) + "% ...");
                }
            }
            StaticRoutes.flushDNS();
        }*/
    }

    @Override
    public String toString(){
        return this.name + " (" + this.IP + ")";
    }

    @Override
    public void postResponse(JSONObject repsonse) {

    }

    @Override
    public void getResponse(String HTML) {
        Console.log(">>>"+HTML+"<<<");

        Pattern pattern = Pattern.compile("\\<p\\>([^\\<]+)");
        Matcher matcher = pattern.matcher(HTML);



        //domain name
        if( matcher.find() && HTML.compareTo("ERROR") != 0){
            this.log( "Found " + matcher.group(1).split(":")[1] );
        }else{
            this.isValid = false;
            return;
        }

        //IP
        if( matcher.find() ){
            this.IP = matcher.group(1).split(":")[1];
        }

        //ASN
        if( matcher.find() ){
            this.ASN = matcher.group(1).split(":")[1];
        }

        //all the IP ranges
        while(matcher.find()){
            String[] parts = matcher.group(1).split("/");
            this.ranges.add( new IPRange(parts[0], Integer.parseInt(parts[1])) );
        }

        final Website website = this;
        if( this.callbacker != null ) {
            Console.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    website.callbacker.websiteReady(website);
                }
            });
        }
    }
}
