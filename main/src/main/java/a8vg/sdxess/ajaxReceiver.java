package a8vg.sdxess;

import org.json.JSONObject;

/**
 * Created by kotaro on 6/19/2017.
 */

public interface ajaxReceiver {
    public void postResponse(JSONObject repsonse);
    public void getResponse(String repsonse);
}
