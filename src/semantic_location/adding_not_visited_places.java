/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic_location;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author mob3f
 */
public class adding_not_visited_places {
     Connection c = null;
    Statement stmt = null;
    Statement stmt2 = null;
    String pid="",day="",poiclass="",lastpid="",lastday="",lastpoiclass="";
    List<String> classNames = new ArrayList<String>();
    public adding_not_visited_places() throws ClassNotFoundException, SQLException {
        
        Class.forName("org.postgresql.Driver");
        c = DriverManager
                .getConnection("jdbc:postgresql://localhost:5433/postgis-Hobby",
                        "postgres", "admin");
        c.setAutoCommit(false);
        stmt = c.createStatement();
        stmt2 = c.createStatement();
         classNames.add("education");classNames.add("Shopping");classNames.add("friendshouses");classNames.add("home");classNames.add("Health");classNames.add("Supermarket");classNames.add("Religious");classNames.add("Out of town");classNames.add("Food");classNames.add("leisure");classNames.add("Service");
         ResultSet rs = stmt.executeQuery(" select distinct pid,day,class from semantic_location order by pid,day,class;");
                            
                            while (rs.next()) {
                                pid = rs.getString("pid");
                                day = rs.getString("day");
                                poiclass = rs.getString("class");
     
                                 if(Objects.equals(pid, lastpid) && Objects.equals(day, lastday) ){
                                 
                                  classNames.remove(poiclass);
                                 }else{
                                     if(!Objects.equals("", lastpid)){
                                          for (Iterator<String> iterator = classNames.iterator(); iterator.hasNext();) {
                                        String next = iterator.next();
                                        stmt2.addBatch("INSERT INTO public.semantic_location( pid,durationpoi,class,day)VALUES ('"+lastpid+"', 0, '"+next+"','"+lastday+"');");
                                    System.out.println("INSERT INTO public.semantic_location( pid,durationpoi,class,day)VALUES ('"+lastpid+"', 0, '"+next+"','"+lastday+"');");
                                    }
                                     }
                                    
                                    
                                    
                                 classNames.clear();
                                 classNames.add("education");classNames.add("Shopping");classNames.add("friendshouses");classNames.add("home");classNames.add("Health");classNames.add("Supermarket");classNames.add("Religious");classNames.add("Out of town");classNames.add("Food");classNames.add("leisure");classNames.add("Service");
                                 classNames.remove(poiclass);
                                 
                                 }
                              
                                

                                lastpid=pid;
                                lastday=day;
                                lastpoiclass=poiclass;
                                
                            }
                            stmt2.executeBatch();
                            c.commit();
                            
                            stmt.close();
                            stmt2.close();
                            c.close();
        
        
    }
    
    
    
}
