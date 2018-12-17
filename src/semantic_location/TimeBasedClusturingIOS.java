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
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import org.json.JSONException;

/**
 *
 * @author Lenovo
 */
public class TimeBasedClusturingIOS {

    double cx;
    double cy;
    double ct = 0.0,cd = 0.0;
    double[] CC = new double[5];
    double d = 200.0;
    double t = 1200.0;
    Map<Double, Double> Results = new HashMap<Double, Double>();
    LinkedList<double[]> plocs = new LinkedList<double[]>();
    LinkedList<double[]> cl = new LinkedList<double[]>();
    LinkedList<String> rows = new LinkedList<String>();
    double i = 0;
    double day = 0;
    double nbplace = 0;
     Connection c = null;
    Statement stmt = null;
    Statement stmt2 = null;

    public TimeBasedClusturingIOS() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, MalformedURLException, JSONException {
        CC[3] = 1;
        
        Class.forName("org.postgresql.Driver");
        c = DriverManager
                .getConnection("jdbc:postgresql://localhost:5433/postgis-Hobby",
                        "postgres", "admin");
        c.setAutoCommit(false);

        stmt = c.createStatement();
        stmt2 = c.createStatement();
        //FindNearestPOI_google poi = new FindNearestPOI_google();
        FindNearestPOI poi = new FindNearestPOI();
        ResultSet rs = stmt.executeQuery("select distinct pid from gps where pid like 'EIM____'  order by pid");
         while (rs.next()) {
         String PID= rs.getString("pid");
         
         ResultSet rs2 = stmt2.executeQuery("select * from  gps where pid= '"+PID+"'  order by date_long");
          String[] lastRow;
        String[] currentRow;
        double ch;
         while (rs2.next()) {
             i++;
                cx = rs2.getDouble("longitude");
                // Current y
                cy = rs2.getDouble("latitude");
                // Current durqtion
                ct = rs2.getDouble("time_diff");
                // Current date
                cd = rs2.getDouble("date_long");
               
                    
                if ((1000 * new Distance().getdistance(CC[0], CC[1], cy, cx, 'K')) < d) {
                        addToCl(cy, cx, ct);
                        clearPlocs();
                    } else {
                        if (plocs.size() > 1) {
                            if (CC[2] > t) {   
                               poi.addbatch(CC[3], CC[0], CC[1], CC[4], CC[2], PID);
                               // System.out.println(CC[3]+" "+CC[0]+" "+ CC[1]+" "+ CC[4]+" "+ CC[2]+" "+ PID);
                                clPlace();     
                            }
                            clearcl();
                            double[] last = plocs.getLast();
                            addToClFirst(last[1], last[2],last[4]);
                            
                            clearPlocs();
                            if (new Distance().getdistance(CC[0], CC[1], ct, cx, 'k') < d) {
                                addToCl(cy, cx, ct);
                            } else {
                                addToPlocs();
                            }

                        } else {
                            addToPlocs();
                        }
                    }

            
         }
         
         
         }
        
       
        double io = 0.0;
        String toBeAdded = "";
        for (Iterator<String> iterator = rows.iterator(); iterator.hasNext();) {
            String next = iterator.next();
            if (io > 0) {
                if (Results.containsKey(io)) {

                    toBeAdded = Results.get(io).toString();
                } else {
                    toBeAdded = "0";
                }
            } else {
                toBeAdded = "class";
            }

           // writer.writeNext((next + "," + toBeAdded).split(","), false);
            io++;

        }
        // Close the file once all data has been read.

        for (Map.Entry<Double, Double> entrySet : Results.entrySet()) {
            Double key = entrySet.getKey();
            Double value = entrySet.getValue();
            //System.out.println("ID: "+ key + " Class: "+value );
        }

        poi.comit();
        review_clusters();
        new HomeDetector();
    }

    private void addToCl(double y, double x, double duration) {
        CC[0] = (CC[0] + y) / 2;
        CC[1] = (CC[1] + x) / 2;
        CC[2] = CC[2] + duration;
        double[] c = {i, cy, cx, ct};
        cl.add(c);
    }

    private void addToClFirst(double y, double x, double cd) {
        CC[0] = y;
        CC[1] = x;
        CC[2] = 0;
        CC[4] = cd;
        

        double[] c = {i, cy, cx, 0};
        cl.add(c);
    }

    private void clearPlocs() {

        for (Iterator<double[]> iterator = plocs.iterator(); iterator.hasNext();) {
            double[] next = iterator.next();

            //Results.put(next[0], 0.0);
        }
        plocs.clear();
    }

    private void addToPlocs() {
        double[] ploc = {i, cy, cx, ct,cd};
        plocs.add(ploc);
    }

    private void clearcl() {

        for (Iterator<double[]> iterator = cl.iterator(); iterator.hasNext();) {
            double[] next = iterator.next();
            //System.out.println("ID: "+ next[0] + " Class: "+ 0.0 );
            //Results.put(next[0], 0.0);
        }
        cl.clear();

    }

    private void clPlace() {
        for (double io = cl.getFirst()[0]; io < cl.getLast()[0]; io++) {

            Results.put(io, CC[3]);
        }
        /* for (Iterator<double[]> iterator = cl.iterator(); iterator.hasNext();) {
         double[] next = iterator.next();
         Results.put(next[0], CC[3]);
         //System.out.println("ID: "+ next[0] + " cluster "+CC[3] );
         }*/
        CC[2] = 0;
        CC[3] = CC[3] + 1;
        nbplace++;
        // plocs.clear();
    }
    private void review_clusters() throws SQLException{
          int i = 0;
            String[] currentRow;
            double lx = 0;
            double ly = 0;
            double cduration;
            double lduration = 0;
            long ct = 0, lt = 0;
            double ld = 0;
            String lid = "";
            ResultSet rs = stmt.executeQuery(" select * from semantic_location where pid like 'EIM____' order by pid,startdatepoi;");
            while (rs.next()) {
                ct = (long) rs.getDouble("startdatepoi") / 1000;
                double x = rs.getDouble("longitude");
                double y = rs.getDouble("latitude");
                double timediff = ct - (lt + (ld));
                if(Objects.equals(lid, rs.getString("pid")) ){
                
                if (lx == x && ly == y ) {
                    System.err.println("err");
                    if (timediff < 1800) {
                        stmt2.executeUpdate("update semantic_location set durationpoi='" + (rs.getDouble("durationpoi") + (ct - lt)) + "' where id_poi='" + ((long) rs.getDouble("id_poi") - 1) + "'");
                        stmt2.executeUpdate("delete from semantic_location where id_poi='" + rs.getString("id_poi") + "'");
                    }
                    
                }
               // stmt2.addBatch("INSERT INTO visitedpoi( longitude, latitude, startdatepoi, durationpoi,totalduration,class,type,description,distance,id,lastclass,date)VALUES ( '" + lx + "', '" + ly + "', '" + (long)((lt+ld) * 1000) + "', '" + timediff  + "','" + rs.getString("totalduration") + "','transition','transition', 'transition','0', '" + rs.getString("id") + "','" + lastpoiclass + "', '" + new SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy").format(new Date(Double.valueOf((lt+ld) * 1000).longValue())) + "');");

                }
                lx = x;
                ly = y;
                lt = ct;
                ld = rs.getDouble("durationpoi");
                lid = rs.getString("pid");

            }
            //stmt2.executeBatch();
            c.commit();
            stmt2.close();
            stmt.close();
            c.close();
    }
    //////////////////////////////////////////////////////////////////////////////////////////


}
