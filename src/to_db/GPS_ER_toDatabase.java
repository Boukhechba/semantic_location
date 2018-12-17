/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package to_db;

import com.github.opendevl.JFlat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author mob3f
 */
public class GPS_ER_toDatabase {

    Connection c = null;
    Statement stmt = null;

    public GPS_ER_toDatabase() throws ClassNotFoundException, SQLException, IOException, JSONException, ParseException {

        Class.forName("org.postgresql.Driver");
        c = DriverManager
                .getConnection("jdbc:postgresql://localhost:5433/postgis-Hobby",
                        "postgres", "admin");
        c.setAutoCommit(false);
        stmt = c.createStatement();
        
        
         File directory = new File("D:\\studies\\Loneliness");
        File[] fList = directory.listFiles();
        String ID="";
        String DataType="";
        JSONObject output;
           
        for (File file : fList) {
         if (file.isDirectory()) {
             ID=file.getName();
        File[] fList2 = file.listFiles();    
             for (File file2 : fList2) {
             if (file2.isDirectory()) {
             DataType=file2.getName();
             File[] fList3 = file2.listFiles();
             for (File file3 : fList3) {
                 //Objects.equals("SmsDatum", DataType)|| Objects.equals("TelephonyDatum", DataType
                 if(Objects.equals("LocationDatum", DataType)){
                 System.out.println(ID+"_"+DataType+"_"+file3.getName());
                  String str = new String(Files.readAllBytes(Paths.get(file3.toString())));
                   System.out.println(str);
                     JFlat flatMe = new JFlat(str);
                     flatMe.json2Sheet().write2csv("D:\\LO_GPS_Data\\"+ID+"_"+DataType+"_"+file3.getName().replace("json", "csv"));
                 BufferedReader CSVFile = new BufferedReader(new FileReader("D:\\LO_GPS_Data\\"+ID+"_"+DataType+"_"+file3.getName().replace("json", "csv")));
                 String dataRow = CSVFile.readLine();
                 DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                   
                    String[] currentRow;
                    DateFormat edtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    edtFormat.setTimeZone(TimeZone.getTimeZone("EST"));
                   while ((dataRow = CSVFile.readLine()) != null) {
                    
                       
                        currentRow = dataRow.replace("LocationDatum, Sensus","LocationDatum_Sensus").replace("\"", "").split(",");
                        //Date date = utcFormat.parse(currentRow[6]);
                        //edtFormat.format(date);
                        
                        Long time = (long) utcFormat.parse(currentRow[6]).getTime();

                        stmt.addBatch("INSERT INTO public.gps(pid,latitude,longitude,date, date_long,accuracy,type) VALUES ('LO_" + ID + "','" + currentRow[1] + "','" + currentRow[2] + "','" + currentRow[6] + "','"  + time + "','" + currentRow[3]+ "','" + currentRow[0].split("LocationDatum_Sensus")[1].replace(".","")+ "');");
                        // System.out.println("INSERT INTO public.gps(pid,latitude,longitude,date, date_long,accuracy,type) VALUES ('ER_" + ID + "','" + currentRow[1] + "','" + currentRow[2] + "','" + currentRow[6] + "','"  + time + "','" + currentRow[3]+ "','" + currentRow[0].split("LocationDatum_Sensus")[1].replace(".","")+ "');");   
                        
                   
                   } 
                 
                 
               
                 }
                 
                  stmt.executeBatch();
                    c.commit();
                 
             }
             
             }    
             }
           // listf(file.getAbsolutePath(), files);
        }
        }
    
        
        
        
        /*
        
        
        Stream<Path> filePathStream;
        filePathStream = Files.walk(Paths.get("C:\\Users\\mob3f\\Documents\\Hobby Project\\Emotion regulation\\Locations_GPS_clean.csv"));
        filePathStream.forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                try {
                    BufferedReader CSVFile = new BufferedReader(new FileReader(filePath.toString()));
                    String dataRow = CSVFile.readLine();
                    int i = 0;

                    String[] currentRow;
                    double lastid = 0;
                    DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                   

                    DateFormat edtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    edtFormat.setTimeZone(TimeZone.getTimeZone("EST"));

                    while ((dataRow = CSVFile.readLine()) != null) {

                        currentRow = dataRow.split(",");
                        if (lastid != Double.valueOf(currentRow[0])) {
                            lastid = Double.valueOf(currentRow[0]);
                            //stmt.execute("INSERT INTO public.users(pid, id_experimentation)VALUES ('"+currentRow[0]+"', 3);");
                        }
                        Date date = utcFormat.parse(currentRow[3]);
                        edtFormat.format(date);
                        
                        Long time = (long) utcFormat.parse(currentRow[3]).getTime();

                        stmt.addBatch("INSERT INTO public.gps(pid,latitude,longitude,date, date_long,accuracy,type) VALUES ('ER_" + currentRow[0] + "','" + currentRow[1] + "','" + currentRow[2] + "','" + currentRow[3] + "','"  + time + "','" + currentRow[4]+ "','" + currentRow[5].split("LocationDatum_Sensus")[1].replace(".","")+ "');");
                         // System.out.println(dataRow);   
                        //System.out.println("INSERT INTO public.gps(pid,latitude,longitude,date, date_long,accuracy,type) VALUES ('ER_" + currentRow[0] + "','" + currentRow[1] + "','" + currentRow[2] + "','" + currentRow[3] + "','" + time + "','" + currentRow[4]+ "','" + currentRow[5].split("LocationDatum_Sensus")[1]+ "');");
                        i++;

                    }
                    //rs.close();

                    CSVFile.close();
                    stmt.executeBatch();
                    c.commit();

                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    System.exit(0);
                }
            }
        });

        stmt.close();
        c.close();
*/
// Read the number of the lines in .csv file 
// i = row of the .csv file
    }

}
