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
import java.util.HashMap;
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
public class FindNearestPOI {

    Connection c = null;
    Statement stmt = null;
    Statement stmt2 = null;
    long TotalDuration = 0;
    String type = "";
    String description = "";
    String poiclass = "";
    String lastpoiclass = "";
    HashMap<String, String> hmap  = new HashMap<String, String>();

    public FindNearestPOI()  {

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
    public void addbatch(double IDpoi, double Latitude,double Longitude,double startDate,double Duration, String PID) throws SQLException, JSONException, IOException{
      ResultSet rs = stmt.executeQuery(" select building,shop,amenity,tourism,leisure,name,\"uva:layer\",office,source,ST_X(ST_Centroid(geometry)) as longitude,ST_Y(ST_Centroid(geometry)) as latitude  from(select * from buildingsandpoints ORDER BY geometry <-> ST_GeomFromText('POINT(" + Longitude + " " + Latitude + ")',4326) LIMIT 1)as bAndp ;");
                             System.err.println(Longitude+" "+Latitude+" "+ PID );
                            while (rs.next()) {
                                double x = rs.getDouble("longitude");
                                double y = rs.getDouble("latitude");
                                System.err.println(Longitude+" "+Latitude+" "+ PID +" "+x+" "+y);
                                int dist = (int) (new Distance().getdistance(Latitude, Longitude, y, x, 'K') * 1000);

                                if (dist > 500) {
                                    type = "Out of town";
                                    poiclass = "Out of town";
                                } else {

                                    String building = rs.getString("building");
                                    String name = rs.getString("name");
                                    String leisure = rs.getString("leisure");
                                    String amenity = rs.getString("amenity");
                                    String uva_layer = rs.getString("uva:layer");
                                    String shop = rs.getString("shop");
                                    String tourism = rs.getString("tourism");
                                    String office = rs.getString("office");
                                    String source = rs.getString("source");

                                    classifyPOI(building, name, leisure, amenity, uva_layer, shop, tourism, office, source);
                                    

                                }
                                findpoigoogle(Latitude, Longitude);
                                
                               
                                //System.out.println("INSERT INTO visitedpoi( longitude, latitude, startdatepoi, durationpoi,totalduration, type, description,distance,id)VALUES ( '" + currentRow[2] + "', '" + currentRow[1] + "', '" + Double.valueOf(currentRow[3]).longValue() + "', '" + currentRow[4] + "','" + TotalDuration+ "', '" + type + "', '" + description + "','" + dist + "', '" + currentRow[5] + "');");

                                stmt2.addBatch("INSERT INTO semantic_location( longitude, latitude,longitude_cluster, latitude_cluster, startdatepoi, durationpoi,totalduration,class,type,description,distance,pid,lastclass,date,class_google,type_google,distance_google,longitude_google,latitude_google,description_google)VALUES ( '" + x + "', '" + y + "','" + Longitude + "', '" + Latitude + "', '" + (long)startDate + "', '" + Duration + "','" + TotalDuration + "','" + poiclass + "','" + type + "', '" + description + "','" + dist + "', '" + PID + "','" + lastpoiclass + "', '" + new SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy").format(new Date((long)startDate*1000)) + "','"+hmap.get("types")+"','"+hmap.get("name")+"','"+hmap.get("distance")+"','"+hmap.get("longitude")+"','"+hmap.get("latitude")+"','"+hmap.get("description")+"');");
                            }

    }
    public void comit() throws SQLException{
    stmt2.executeBatch();
             c.commit();
             c.close();
    }
    
     public HashMap<String, String> findpoigoogle( double Latitude, double Longitude) throws SQLException, MalformedURLException, IOException, JSONException {
        String vicinity = "";
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
            types = jsonrow.get("types").toString();
            String description = jsonrow.get("vicinity").toString();
            int dist = (int) (new Distance().getdistance(Latitude, Longitude, Double.valueOf(y), Double.valueOf(x), 'K') * 1000);
          
            hmap.put("name",name.replace("'", " ").replace(",", " "));
            hmap.put("types",types.replace("'", " ").replace(",", " "));
            hmap.put("distance",String.valueOf(dist));
            hmap.put("longitude",String.valueOf(x));
            hmap.put("latitude",String.valueOf(y));
            hmap.put("description",description.replace("'", " ").replace(",", " "));
            
            

        } else {
            System.out.println("GET request not worked");
        }
        return hmap;

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
        if (Objects.equals("stadium", poiclass) || Objects.equals("bar", poiclass) ||Objects.equals("hangar", poiclass)|| Objects.equals("swimming_pool", poiclass)|| Objects.equals("pub", poiclass) || Objects.equals("hotel", poiclass) || Objects.equals("food_court", poiclass) || Objects.equals("events_centre", poiclass)|| Objects.equals("arts_centre", poiclass) || Objects.equals("tourism", poiclass) || Objects.equals("theatre", poiclass) || Objects.equals("cinema", poiclass) || Objects.equals("cafe", poiclass)) {
            poiclass = "leisure";
        }
        if (Objects.equals("hospital", poiclass) || Objects.equals("pharmacy", poiclass)|| Objects.equals("clinic", poiclass)) {
            poiclass = "Health";
        }
        if (Objects.equals("restaurant", poiclass) || Objects.equals("fast_food", poiclass)) {
            poiclass = "Food";
        }

        if (Objects.equals("shop", poiclass) || Objects.equals("commercial", poiclass) || Objects.equals("retail", poiclass) ||Objects.equals("ice_cream", poiclass)) {
            poiclass = "Shopping";
        }
        if (Objects.equals("service", poiclass) || Objects.equals("civic", poiclass) ||Objects.equals("office", poiclass) || Objects.equals("fuel", poiclass) || Objects.equals("bank", poiclass) || Objects.equals("train_station", poiclass)|| Objects.equals("construction", poiclass)|| Objects.equals("grave_yard", poiclass)|| Objects.equals("fuel", poiclass) || Objects.equals("bank", poiclass)) {
            poiclass = "Service";
        }
        if (Objects.equals("place_of_worship", poiclass) || Objects.equals("church", poiclass)) {
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
