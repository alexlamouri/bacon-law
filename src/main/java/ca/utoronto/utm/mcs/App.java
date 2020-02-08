package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import org.neo4j.driver.v1.*;
import static org.neo4j.driver.v1.Values.parameters;


public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
 
    	// New neo4j server to graph results
    	Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "1234"));
        
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        // 7.1 PUT​ ​/api/v1/addActor
        server.createContext("/api/v1/addActor", new addActor(driver.session()));
        
        // 7.2 PUT​ ​/api/v1/addMovie
        server.createContext("/api/v1/addMovie", new addMovie(driver.session()));
        
        // 7.3 PUT​ ​/api/v1/addRelationship
        server.createContext("/api/v1/addRelationship", new addRelationship(driver.session()));
        
        // 7.4 GET​ ​/api/v1/getActor
        server.createContext("/api/v1/getActor", new getActor(driver.session()));
        
        // 7.5 GET​ ​/api/v1/getMovie
        server.createContext("/api/v1/getMovie", new getMovie(driver.session()));
        
        // 7.6 GET​ ​/api/v1/hasRelationship
        server.createContext("/api/v1/hasRelationship", new hasRelationship(driver.session()));
        
        // 7.7 GET​ ​/api/v1/computeBaconNumber
        server.createContext("/api/v1/computeBaconNumber", new computeBaconNumber(driver.session()));
        
        // 7.8 GET​ ​/api/v1/computerBaconPath
        server.createContext("/api/v1/computeBaconPath", new computeBaconPath(driver.session()));
        
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
