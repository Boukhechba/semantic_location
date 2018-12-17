/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic_location;

import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author mob3f
 */
public class FindNearestPOI_google {

    Connection c = null;
    Statement stmt = null;
    Statement stmt2 = null;
    long TotalDuration = 0;
    String type = "";
    String description = "";
    String poiclass = "";
    String lastpoiclass = "";

    public FindNearestPOI_google() {

        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5433/postgis-Hobby",
                            "postgres", "admin");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            stmt2 = c.createStatement();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        // Close the file once all data has been read.
        // End the printout with a blank line.
        System.out.println("fin");

    }

    public void addbatch(double IDpoi, double Latitude, double Longitude, double startDate, double Duration, String PID) throws SQLException, MalformedURLException, IOException, JSONException {

        String GET_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + Latitude + "," + Longitude + "&type=&rankby=distance&key=AIzaSyCOz3kaTy269qNUWQGowNRTq6MFpkGxQ1s";
        String USER_AGENT = "Mozilla/5.0";
        URL obj = new URL(GET_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        //System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                  //System.out.println(inputLine); 
            }
            boolean test = true;
            JSONObject output = new JSONObject(response.toString());

            JSONArray docs = output.getJSONArray("results");
            // for (int j = 0; j < docs.length(); j++) {}
            
            String types = "";
            int i=0;
            while (test) { 
            types = ((JSONObject) docs.get(i)).get("types").toString();
            if(types.contains("bus_station")){
            i++;
            }else{
            test = false;
            }   
            }
            JSONObject jsonrow = (JSONObject) docs.get(i);
            
            JSONObject location = jsonrow.getJSONObject("geometry").getJSONObject("location");
            String x = location.get("lng").toString();
            String y = location.get("lat").toString();
            String name = jsonrow.get("name").toString();
            String vicinity = jsonrow.get("vicinity").toString();
            int dist = (int) (new Distance().getdistance(Latitude, Longitude, Double.valueOf(y), Double.valueOf(x), 'K') * 1000);
           System.out.println(" ( '" + x + "', '" + y + "','" + Longitude + "', '" + Latitude + "', '" + (long) startDate + "', '" + Duration + "','" + TotalDuration + "','" + poiclass + "','" + types.replace(",","|").replace("\"","").replace("[","").replace("]","") + "', '" + name.replace("'", " ") + "','" + vicinity.replace("'", " ")+ "','" + dist + "', '" + PID + "','" + lastpoiclass + "', '" + new SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy").format(new Date((long) startDate)) + "');");

            stmt2.addBatch("INSERT INTO semantic_location( longitude, latitude,longitude_cluster, latitude_cluster, startdatepoi, durationpoi,totalduration,class,type,description,vicinity,distance,pid,lastclass,date)VALUES ( '" + x + "', '" + y + "','" + Longitude + "', '" + Latitude + "', '" + (long) startDate + "', '" + Duration + "','" + TotalDuration + "','" + poiclass + "','" + types.replace(",","|").replace("\"","").replace("[","").replace("]","") + "', '" + name.replace("'", " ") + "','" + vicinity.replace("'", " ")+ "','" + dist + "', '" + PID + "','" + lastpoiclass + "', '" + new SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy").format(new Date((long) startDate)) + "');");
            in.close();

        } else {
            System.out.println("GET request not worked");
        }

    }

    public void comit() throws SQLException {
        stmt2.executeBatch();
        c.commit();
        c.close();
    }

    private void classifyPOI(String building, String name, String leisure, String amenity, String uva_layer, String shop, String tourism, String office, String source) {
        if (Objects.equals(null, building) || Objects.equals("yes", building) || Objects.equals("no", building)) {
            //System.out.println("Universiy of Virginia ; "+ source);
            if (Objects.equals("UVA_FACILITIES", uva_layer) || Objects.equals("University of Virginia", source)) {
                poiclass = "education";
                type = "university";
                if (Objects.equals(null, name)) {
                    description = "uva facilities";
                } else {
                    description = name;
                }
            } else {
                if (Objects.equals(null, amenity)) {
                    if (Objects.equals("yes", shop)) {
                        shop = "shop";
                    }
                    if (Objects.equals("yes", office)) {
                        office = "office";
                    }
                    if (Objects.equals("yes", leisure)) {
                        leisure = "leisure";
                    }
                    if (Objects.equals("yes", tourism)) {
                        tourism = "tourism";
                    }

                    type = Stream.of(leisure, office, tourism, shop).filter(s -> s != null).collect(Collectors.joining());
                    if (!Objects.equals(null, shop)) {
                        poiclass = "shop";
                    }
                    if (!Objects.equals(null, office)) {
                        poiclass = "service";
                    }
                    if (!Objects.equals(null, leisure)) {
                        poiclass = "leisure";
                    }
                    if (!Objects.equals(null, tourism)) {
                        poiclass = "tourism";
                    }

                } else {
                    poiclass = amenity;
                    type = amenity;
                }
                description = name;
            }

        } else {

            poiclass = building;
            type = building;
            description = name;

        }

        if (Objects.equals("school", poiclass) || Objects.equals("university", poiclass) || Objects.equals("library", poiclass) || Objects.equals("college", poiclass)) {
            poiclass = "education";
        }
        if (Objects.equals("stadium", poiclass) || Objects.equals("pub", poiclass) || Objects.equals("hotel", poiclass) || Objects.equals("food_court", poiclass) || Objects.equals("events_centre", poiclass) || Objects.equals("arts_centre", poiclass) || Objects.equals("tourism", poiclass) || Objects.equals("theatre", poiclass) || Objects.equals("cinema", poiclass) || Objects.equals("cafe", poiclass)) {
            poiclass = "leisure";
        }
        if (Objects.equals("hospital", poiclass) || Objects.equals("pharmacy", poiclass)) {
            poiclass = "Health";
        }
        if (Objects.equals("restaurant", poiclass) || Objects.equals("fast_food", poiclass)) {
            poiclass = "Food";
        }

        if (Objects.equals("shop", poiclass) || Objects.equals("commercial", poiclass) || Objects.equals("retail", poiclass)) {
            poiclass = "Shopping";
        }
        if (Objects.equals("service", poiclass) || Objects.equals("office", poiclass) || Objects.equals("fuel", poiclass) || Objects.equals("bank", poiclass) || Objects.equals("train_station", poiclass) || Objects.equals("construction", poiclass) || Objects.equals("grave_yard", poiclass) || Objects.equals("fuel", poiclass) || Objects.equals("bank", poiclass)) {
            poiclass = "Service";
        }
        if (Objects.equals("place_of_worship", poiclass)) {
            poiclass = "Religious";
        }
        if (Objects.equals("supermarket", type)) {
            poiclass = "Supermarket";
        }

        if (!Objects.equals(null, description)) {
            description = description.replace("'", "");
        }

    }

    public long getTotalDuration(String filePath) throws FileNotFoundException, IOException {
        BufferedReader CSVFile = new BufferedReader(new FileReader(filePath));
        String dataRow = CSVFile.readLine();
        CSVFile.readLine();
        String[] currentRow = CSVFile.readLine().split(",");
        long startDate = Double.valueOf(currentRow[2]).longValue() / 1000;
        long currentDate = 0;
        long timeDifference = 0;
        int i = 0;
        double ch;
        double POIclass = 0;
        while ((dataRow = CSVFile.readLine()) != null) {
            currentRow = dataRow.split(",");
            timeDifference = Double.valueOf(currentRow[6]).longValue();
            POIclass = Double.valueOf(currentRow[7]).longValue();
            ch = Double.parseDouble(currentRow[1].split(" ")[3].split(":")[0]);
            if (ch > 7 && ch < 24) {

                currentDate = Double.valueOf(currentRow[2]).longValue() / 1000;

                if (timeDifference > 1800 && POIclass == 0) {
                    startDate = startDate + timeDifference;
                    i++;
                }

            } else {
                startDate = startDate + timeDifference;
            }

        }
        CSVFile.close();

        return currentDate - startDate;
    }

}
