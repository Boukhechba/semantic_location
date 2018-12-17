/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic_location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.stream.Stream;

/**
 *
 * @author mob3f
 */
public class ReadGPS {

    Connection c = null;
    Statement stmt = null;
    Statement stmt2 = null;
    Statement stmt3 = null;

    public ReadGPS() throws ClassNotFoundException, SQLException {

        Class.forName("org.postgresql.Driver");
        c = DriverManager
                .getConnection("jdbc:postgresql://localhost:5433/postgis-Hobby",
                        "postgres", "admin");
        c.setAutoCommit(false);

        stmt = c.createStatement();
        stmt2 = c.createStatement();
        stmt3 = c.createStatement();
          
        ResultSet rs = stmt.executeQuery("select distinct pid,type from gps where pid LIKE 'EIM____'  order by pid");
        while (rs.next()) {
            String pid = rs.getString("pid");
            String type = rs.getString("type");
            
            double cx;
            double cy;
            double last_x = 0;
            double last_y = 0;
            long ct;
            long last_t = 0;
            double distanceDiff = 0.0;
            double timeDiff = 0.0;
            //System.out.println("select * from gps where pid='" + pid + "' ORDER BY date_long;");
            ResultSet rs2 = stmt2.executeQuery("select * from gps where pid='" + pid + "' ORDER BY date_long;");
            while (rs2.next()) {
                cx = rs2.getDouble("longitude");
                cy = rs2.getDouble("latitude");
                ct = rs2.getLong("date_long");
                if(last_t==0){
                distanceDiff = 0;
                timeDiff = 0;
                }else{
                distanceDiff = 1000 * new Distance().getdistance(last_y, last_x, cy, cx, 'K');
                timeDiff = (ct - last_t);
                }
                if(Double.isNaN(distanceDiff)){
                    distanceDiff=0;
                }
                //System.err.println("x : " + cx + "   y  " + cy+ " lastx : " + last_x + "   lasty  " + last_y);
                System.out.println("time diff : " + timeDiff + "   distance_diff  " + distanceDiff);
              stmt3.addBatch("update gps set time_diff="+timeDiff+" , distance_diff= "+String.format("%.2f", distanceDiff)+" where id_gps="+rs2.getString("id_gps"));
                last_x = cx;
                last_y = cy;
                last_t = ct;
            }
        }
        
         stmt3.executeBatch();
                    c.commit();
        
        
        
        // End the printout with a blank line.

    }

}
