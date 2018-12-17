/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic_location;

import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author mob3f
 */
public class HomeDetector {

    Connection c = null;
    Statement stmt = null;
    String lastId;

    public HomeDetector() {

        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5433/postgis-Hobby",
                            "postgres", "admin");

            stmt = c.createStatement();
            stmt.executeUpdate("update semantic_location set class ='home', class_google='home' where id_poi in (select id_poi from ((select pid,max (ct) as ct from \n" +
"                    (select pid,longitude_google,latitude_google,sum(durationpoi) as ct,type from semantic_location where ((SUBSTRING (date,9,2))::int>20 or (SUBSTRING (date,9,2))::int<8 ) and pid like 'EIM____' group by pid,type,longitude_google,latitude_google order by pid,ct )\n" +
"                    as view group by pid order by pid) as maxV\n" +
"                    natural join \n" +
"                    (select pid,longitude_google,latitude_google,sum(durationpoi) as ct,type from semantic_location where ((SUBSTRING (date,9,2))::int>20 or (SUBSTRING (date,9,2))::int<8 ) and pid like 'EIM____' group by pid,type,longitude_google,latitude_google order by pid,ct ) countV\n" +
"                    ) as pt\n" +
"                    natural join \n" +
"                    semantic_location) ;");
            stmt.close();
            stmt = c.createStatement();
            stmt.executeUpdate("update semantic_location \n"
                    + "set class ='friendshouses'\n"
                    + "where\n"
                    + "class='house'or class='dormitory'or class='apartments' or class='residential'");

            stmt.executeUpdate("update semantic_location\n"
                    + "set class ='home', class_google='home'\n"
                    + "where id_poi in\n"
                    + "(select id_poi from\n"
                    + "(select *,ST_Distance(ST_MakePoint(longitude_cluster, latitude_cluster)::geography,ST_MakePoint(longitude_home, latitude_home)::geography) as distance from \n"
                    + "(select distinct id_poi,pid,longitude,latitude,longitude_cluster,latitude_cluster,class from semantic_location where pid like 'EIM____' order by pid) tab1\n"
                    + "left join\n"
                    + "(select distinct pid,longitude as longitude_home,latitude as latitude_home,class from semantic_location where  class='home'  order by pid)tab2\n"
                    + "on tab1.pid=tab2.pid)tab3\n"
                    + " where distance < 100) ");
            /*   stmt.executeUpdate("DROP VIEW IF EXISTS results;\n"
                    + "CREATE VIEW results AS \n"
                    + "select pid,class,percentage,percentagecalls,percentagesms,count,case when countsms is null then 0 else countsms*(select max(totalduration) from semantic_location)/totalduration end as countsms,case when countcalls is null then 0 else countcalls*(select max(totalduration) from semantic_location)/totalduration end as countcalls,diversity,totalduration,dass,sias,sias_label from \n" +
"(select * \n" +
"from lab natural join\n" +
"(select tab2.pid,percentage,count,class,totalduration,case when percentagecalls is null then 0 else percentagecalls end,case when percentagesms is null then 0 else percentagesms end from \n" +
"((select pid,ROUND((Sum(durationpoi)*100/totalduration)::numeric,2)as percentage,count(class),class,totalduration from semantic_location  group by pid,class,totalduration) \n" +
"union all \n" +
"(select pid,ROUND(((totalduration-sum(durationpoi))*100/totalduration)::numeric,2) as percentage,count(class)-1 as count,'transition' as class,totalduration from semantic_location  where totalduration > 180000 group by pid,totalduration )) as tab2 \n" +
"left join \n" +
"(select pid,location,ROUND(((count(pid))*100/total)::numeric,0) as percentagecalls from calls natural join (select pid,count(pid) as total from calls group by pid) as total group by pid,location,total order by pid) as tab3\n" +
"on tab2.pid=tab3.pid and tab2.class=tab3.location\n" +
"left join \n" +
"(select pid,location,ROUND(((count(pid))*100/total)::numeric,0) as percentagesms from sms natural join (select pid,count(pid) as total from sms group by pid) as total group by pid,location,total order by pid) as tab4\n" +
"on tab2.pid=tab4.pid and tab2.class=tab4.location\n" +
"order by pid) as duration\n" +
")as tab5\n" +
"left join \n" +
"(select count(pid) as countSMS,pid as pidd from sms  group by pid order by pid)as tab6\n" +
"on tab5.pid=tab6.pidd\n" +
"left join \n" +
"(select count(pid) as countCalls,pid as piddd from calls  group by pid order by pid)as tab7\n" +
"on tab5.pid=tab7.piddd\n" +
"natural join \n" +
"(select pid,count(pid) as diversity from(select distinct pid,longitude,latitude  from semantic_location  order by pid) as table1 group by pid) as tab4\n" +
"order by pid");
             */
            stmt.close();

            c.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

}
