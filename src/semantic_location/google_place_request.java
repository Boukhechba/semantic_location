/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic_location;

/**
 *
 * @author mob3f
 */
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import static com.sun.org.apache.xerces.internal.util.FeatureState.is;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class google_place_request {

    public google_place_request() throws MalformedURLException, IOException, JSONException {
        //AIzaSyCOz3kaTy269qNUWQGowNRTq6MFpkGxQ1s
        String GET_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&type=point_of_interest&rankby=distance&key=AIzaSyCOz3kaTy269qNUWQGowNRTq6MFpkGxQ1s";
        String USER_AGENT = "Mozilla/5.0";
        URL obj = new URL(GET_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
              //  System.out.println(inputLine); 
            }
            
              
              JSONObject  output = new JSONObject(response.toString());

                JSONArray docs = output.getJSONArray("results");
                for (int j = 0; j < docs.length(); j++) {
                     JSONObject jsonrow = (JSONObject) docs.get(j);
                    String name = jsonrow.get("name").toString();
                String types = jsonrow.get("types").toString();
                String vicinity = jsonrow.get("vicinity").toString();
                 System.out.println("name : "+name+"  types : "+types+"  vicinity : "+vicinity);
                }
                   in.close();

                
           

                
                 
         
            // print result
           
            //System.out.println(response.toString());
        } else {
            System.out.println("GET request not worked");
        }
    }

}
