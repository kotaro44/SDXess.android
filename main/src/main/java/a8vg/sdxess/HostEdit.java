package a8vg.sdxess;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

import de.blinkt.openvpn.R;

/**
 * Created by kotaro on 7/4/2017.
 */

public class HostEdit {

    public static ACType accountType = ACType.NOTYPE;
    public static String server = "";
    public static String username = "";
    public ArrayList<Website> websites = null;


    public TextView consoleView = null;
    Spinner serverCombo = null;
    Button addDomainBtn = null;
    Button connectBtn = null;
    TextView upgrade_lbl = null;
    CheckBox allTrafficChb = null;
    TableLayout domainsLayout = null;
    ImageView loader = null;

    public static ArrayList<Website> restoreWebsites(){
        ArrayList<Website> result = new ArrayList<Website>();

        int fname = R.raw.dat_a;
        if( HostEdit.accountType == ACType.STARTER )
            fname = R.raw.dat_b;
        if( HostEdit.accountType == ACType.ADVANCED )
            fname = R.raw.dat_c;

        InputStream inputStream = Console.activity.getApplicationContext().getResources().openRawResource(fname);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        try{
            String line = null;
            String DATA = null;
            while ((line = br.readLine()) != null) {
                if( Website.isASNNumber(line) ){
                    if( DATA != null ){
                        Website website = Website.restore(DATA);
                        result.add(website);
                        Console.log("restored " + website.name + "...");
                    }
                    DATA = line + "\n";
                    for( int i = 0 ; i < 4 ; i++ ){
                        line = br.readLine();
                        DATA += line + "\n";
                    }
                }else{
                    DATA += line + "\n";
                }
            }
            Website website = Website.restore(DATA);
            result.add(website);
            Console.log("restored " + website.name + "...");
        }  catch (IOException ex) {

        }

        return result;
    }

    public static void saveWebsites(ArrayList<Website> websites){

    }

    public HostEdit(JSONObject userObj ){
        try {
            HostEdit.username = (String) userObj.get("name");
            HostEdit.accountType = ACType.NOTYPE;

            switch( Integer.parseInt((String) userObj.get("data_plan"))){
                case 1:
                    HostEdit.accountType = ACType.BASIC;
                    break;
                case 2:
                    HostEdit.accountType = ACType.STARTER;
                    break;
                case 3:
                    HostEdit.accountType = ACType.ADVANCED;
                    break;
            }

            this.websites = this.restoreWebsites();

            if( HostEdit.accountType == ACType.NOTYPE){
                Console.toast("Your account is not active, please visit http://sdxess.com to check the status of your account.");
                Console.activity.setContentView(R.layout.activity_main);
                Console.activity.disconnected();
            }else{
                this.start();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        this.serverCombo = (Spinner) Console.activity.findViewById(R.id.serverCombo);
        this.addDomainBtn = (Button) Console.activity.findViewById(R.id.addDomainBtn);
        this.connectBtn = (Button) Console.activity.findViewById(R.id.connectBtn);
        this.upgrade_lbl = (TextView) Console.activity.findViewById(R.id.upgrade_lbl);
        this.allTrafficChb = (CheckBox) Console.activity.findViewById(R.id.allTrafficChb);
        this.domainsLayout = (TableLayout) Console.activity.findViewById(R.id.domainsLayout);
        this.loader = (ImageView) Console.activity.findViewById(R.id.loader);
        this.consoleView = (TextView) Console.activity.findViewById(R.id.consoleView);

        this.loader.setVisibility(View.GONE);

        switch(HostEdit.accountType){
            case BASIC:
            case STARTER:
                this.allTrafficChb.setVisibility(View.GONE);
                this.addDomainBtn.setEnabled(false);
                break;
            case ADVANCED:
                this.upgrade_lbl.setVisibility(View.GONE);
                break;
        }

        this.getConfs();

        this.setEvents();
        this.updateTable();
    }

    public void enableWindow(){
        this.loader.setVisibility(View.GONE);
        this.serverCombo.setEnabled(true);
        this.addDomainBtn.setEnabled(true);
        this.connectBtn.setEnabled(true);
        this.allTrafficChb.setEnabled(true);
        this.domainsLayout.setEnabled(true);

        for(int i = 0 ; i < this.domainsLayout.getChildCount() ; i++ ){
            TableRow row = (TableRow) this.domainsLayout.getChildAt(i);
            for( int j = 0 ; j < row.getChildCount(); j++ ){
                ((View) row.getChildAt(j)).setEnabled(true);
            }
        }
    }

    public void disableWindow(){
        this.loader.setVisibility(View.VISIBLE);
        this.serverCombo.setEnabled(false);
        this.addDomainBtn.setEnabled(false);
        this.connectBtn.setEnabled(false);
        this.allTrafficChb.setEnabled(false);
        this.domainsLayout.setEnabled(false);

        for(int i = 0 ; i < this.domainsLayout.getChildCount() ; i++ ){
            TableRow row = (TableRow) this.domainsLayout.getChildAt(i);
            for( int j = 0 ; j < row.getChildCount(); j++ ){
                ((View) row.getChildAt(j)).setEnabled(false);
            }
        }

    }

    public void setEvents(){
        this.connectBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
            HostEdit.server = (String) serverCombo.getSelectedItem();
            Console.activity.setContentView(R.layout.vpnconnect);
            vpnconnect x = null;
            if( Console.activity.hostEdit.allTrafficChb.isChecked() ){
                x = new vpnconnect(null);
            }
            else{
                x = new vpnconnect(Console.activity.hostEdit.websites);
            }
            }
        });

        if( HostEdit.accountType == ACType.ADVANCED ) {
            this.addDomainBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    addDomain();
                }
            });
        }

    }

    public void addDomain(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Console.activity);
        builder.setTitle("Enter Domain, IP or ASN number");

        final EditText input = new EditText(Console.activity);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            final String domain = input.getText().toString();
            if( domain != null ){
                if ( Website.isValidDomainName(domain) || Website.isASNNumber(domain) ||
                        Website.isIP(domain)) {

                    AlertDialog.Builder builderDesc = new AlertDialog.Builder(Console.activity);
                    builderDesc.setTitle("Enter a description for " + domain);

                    final EditText input = new EditText(Console.activity);

                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builderDesc.setView(input);

                    builderDesc.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String description = input.getText().toString();
                            HostEdit.addWebsite(domain,description);
                        }
                    });

                    builderDesc.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builderDesc.show();


                } else {
                    Console.toast( domain + " is not a valid Domain name, IP or ASN number");
                }
            }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    public static void addWebsite(String domain, String description){
        Console.activity.hostEdit.consoleView.setText("getting info from " + domain + "...");
        Console.activity.hostEdit.disableWindow();
        Website website = new Website(domain,Console.activity.hostEdit);
        website.Description = description;
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
            String name = fields[count].getName();
            if( name.startsWith("sdxess_") ) {
                confs.add(name.substring(7,name.length()));
            }
        }


        Spinner sp = (Spinner) Console.activity.findViewById(R.id.serverCombo);
        ArrayAdapter<String> adp= new ArrayAdapter<String>(Console.activity,
                android.R.layout.simple_list_item_1,confs);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adp);
    }

    public void updateTable(){
        domainsLayout.removeAllViews();
        int id = 0;
        for( Website website : this.websites ) {
            TableRow row = new TableRow(Console.activity);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);

            TextView domain_name = new TextView(Console.activity);
            CheckBox routed_domain = new CheckBox(Console.activity);
            Button details_domain = new Button(Console.activity);

            domain_name.setText(website.name);
            routed_domain.setText("routed");
            details_domain.setText("details");
            domain_name.setId(id);
            routed_domain.setId(id);
            details_domain.setId(id++);

            details_domain.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    Console.activity.hostEdit.openDetails(v.getId());
                }
            });

            routed_domain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                    if( Console.activity.hostEdit != null ) {
                        Website website = Console.activity.hostEdit.websites.get(buttonView.getId());
                        website.wasRerouted = isChecked;
                    }
                }
            });

            row.addView(domain_name);
            row.addView(routed_domain);
            row.addView(details_domain);


            routed_domain.setChecked(website.wasRerouted);

            TableRow.LayoutParams lp2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            lp2.weight = 1;
            domain_name.setLayoutParams(lp2);
            routed_domain.setLayoutParams(lp2);
            details_domain.setLayoutParams(lp2);
            domainsLayout.addView(row);
        }
    }

    public void saveWebsites(){

    }

    public void websiteReady(Website website){
        if( website.isValid ){
            boolean addNewWebsite = true;
            //check if the website was added before
            for( Website other : Console.activity.hostEdit.websites ){
                if( other.ASN.compareTo(website.ASN) == 0 ){
                    Console.toast( "\"" +  website.name +
                            "\" IP's are already included in \"" + other.name +
                            "\" (" + other.ASN + ")");
                    addNewWebsite = false;
                }
            }

            if( addNewWebsite ){
                Console.activity.hostEdit.websites.add(website);
                Console.activity.hostEdit.updateTable();
                Console.activity.hostEdit.saveWebsites();
            }
        }else{
            Console.toast( website.toString() + " not found!" );
        }
        Console.activity.hostEdit.consoleView.setText("");
        Console.activity.hostEdit.enableWindow();
    }

    public void openDetails(int index){
        Website website = this.websites.get(index);
        Console.activity.setContentView(R.layout.details);
        details det = new details(website);
    }
}
