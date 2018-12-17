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
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Lenovo
 */
public class TimeBasedClusturingAndroid {

    double cx;
    double cy;
    double ct = 0.0,cd = 0.0;
    double[] CC = new double[5];
    double d = 60.0;
    double t = 300.0;
    Map<Double, Double> Results = new HashMap<Double, Double>();
    LinkedList<double[]> plocs = new LinkedList<double[]>();
    LinkedList<double[]> cl = new LinkedList<double[]>();
    LinkedList<String> rows = new LinkedList<String>();
    double i = 0;
    double day = 0;
    double nbplace = 0;

    public TimeBasedClusturingAndroid(Path filePath) throws FileNotFoundException, IOException {
        CC[3] = 1;
        BufferedReader CSVFile = new BufferedReader(new FileReader(filePath.toString()));

        String dataRow = CSVFile.readLine();
        rows.addLast(dataRow);
        String csvOut ="output_GPS_POI\\"+filePath.getFileName().toString();
        CSVWriter writer = new CSVWriter(new FileWriter(csvOut ));
        writer.writeNext(("IDpoi"+","+"Latitude"+","+"Longitude"+","+"startDate"+","+"Duration"+","+"idParticipent").split(","), false);

// Read the number of the lines in .csv file 
// i = row of the .csv file
        String[] lastRow;
        String[] currentRow;
        double ch;

        while ((dataRow = CSVFile.readLine()) != null) {
            i++;

            currentRow = dataRow.split(",");
            rows.addLast(dataRow);
            if (currentRow.length == 7) {
               
                // Current hour
                 ch = Double.parseDouble(currentRow[1].split(" ")[3].split(":")[0]);
               // if (ch >7 && ch < 24){
                     // Current x
                cx = Double.parseDouble(currentRow[4]);
                // Current y
                cy = Double.parseDouble(currentRow[3]);
                // Current durqtion
                ct = Double.parseDouble(currentRow[6]);
                // Current date
                cd = Double.parseDouble(currentRow[2]);
               
                    
                if ((1000 * new Distance().getdistance(CC[0], CC[1], cy, cx, 'K')) < d) {
                        addToCl(cy, cx, ct);
                        clearPlocs();
                    } else {
                        if (plocs.size() > 1) {
                            if (CC[2] > t) {
                               writer.writeNext(String.valueOf(CC[3]+","+CC[0]+","+CC[1]+","+CC[4]+","+CC[2]+","+currentRow[0]).split(","), false);
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
                
                /*
                }else{
                    if (CC[2] > t) {
                               writer.writeNext(String.valueOf(CC[3]+","+CC[0]+","+CC[1]+","+CC[4]+","+CC[2]+","+currentRow[0]).split(","), false);
                                clPlace();   
                               
                    }
                    clearcl();
                    clearPlocs(); 
                    addToClFirst(cy, cx, cd);
                    
                    
                           
                
                }
                */
                    
                
//                writer.writeNext((dataRow + "," + new DecimalFormat("##.##").format(distanceDiff) + "," + new DecimalFormat("##.##").format(timeDiff / 60000)).split(","));
            }
        }

        CSVFile.close();
        writer.close();
        // End the printout with a blank line.
         csvOut = "output_GPS_clustered\\"+filePath.getFileName().toString();
         writer = new CSVWriter(new FileWriter(csvOut));

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

            writer.writeNext((next + "," + toBeAdded).split(","), false);
            io++;

        }
        // Close the file once all data has been read.
        writer.close();
        for (Map.Entry<Double, Double> entrySet : Results.entrySet()) {
            Double key = entrySet.getKey();
            Double value = entrySet.getValue();
            //System.out.println("ID: "+ key + " Class: "+value );
        }



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
    //////////////////////////////////////////////////////////////////////////////////////////


}
