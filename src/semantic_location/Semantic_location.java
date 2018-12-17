/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic_location;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import org.json.JSONException;
import to_db.GPS_ER_toDatabase;

/**
 *
 * @author mob3f
 */
public class Semantic_location {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, JSONException, ParseException {
        // TODO code application logic here
       // new GPS_ER_toDatabase();
         new ReadGPS();
         new TimeBasedClusturingIOS();
      // new adding_not_visited_places();
      //new google_place_request();
    }
    
}
