package a8vg.sdxess;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import de.blinkt.openvpn.R;

/**
 * Created by kotaro on 7/10/2017.
 */

public class details {
    public Website website = null;
    public TextView domainLbl = null;
    public TextView ipLbl = null;
    public TextView asnLbl = null;
    public TextView rangesLbl = null;
    public TextView descLbl = null;
    public TableLayout ipTable = null;
    public Button backBtn = null;

    public details(Website website) {
        this.website = website;
        this.start();

        this.domainLbl.setText(website.name);
        this.ipLbl.setText(website.IP);
        this.asnLbl.setText(website.ASN);
        this.rangesLbl.setText(website.ranges.size()+"");
        this.descLbl.setText(website.Description);

        this.showIps();
        this.setEvents();
    }

    public void start(){
        this.domainLbl = (TextView) Console.activity.findViewById(R.id.domainLbl);
        this.ipLbl = (TextView) Console.activity.findViewById(R.id.ipLbl);
        this.asnLbl = (TextView) Console.activity.findViewById(R.id.asnLbl);
        this.rangesLbl = (TextView) Console.activity.findViewById(R.id.rangesLbl);
        this.descLbl = (TextView) Console.activity.findViewById(R.id.descLbl);
        this.ipTable = (TableLayout) Console.activity.findViewById(R.id.ipTable);
        this.backBtn = (Button) Console.activity.findViewById(R.id.backBtn);
    }

    public void setEvents(){
        this.backBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Console.activity.setContentView(R.layout.host_edit);
                Console.activity.hostEdit.start();
            }
        });
    }

    public void showIps(){
        for( IPRange range : this.website.ranges ) {
            TableRow row = new TableRow(Console.activity);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);

            TextView subnet = new TextView(Console.activity);
            TextView ip_subnet = new TextView(Console.activity);

            subnet.setText(range.getMask());
            ip_subnet.setText(range.toString(true));

            row.addView(subnet);
            row.addView(ip_subnet);

            TableRow.LayoutParams lp2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            lp2.weight = 1;
            subnet.setLayoutParams(lp2);
            ip_subnet.setLayoutParams(lp2);
            ipTable.addView(row);
        }
    }
}
