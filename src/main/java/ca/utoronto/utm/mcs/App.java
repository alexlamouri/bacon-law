package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        
        Memory mem = new Memory();
        
        // 7.1 PUT​ ​/api/v1/addActor
        server.createContext("/api/v1/addActor", new Actor(mem));
        
        // 7.2 PUT​ ​/api/v1/addMovie
        server.createContext("​/api/v1/addMovie", new Movie(mem));
        
        // 7.3 PUT​ ​/api/v1/addRelationship
        server.createContext("​/api/v1/addRelationship", new Relationship(mem));
        
        // 7.4 GET​ ​/api/v1/getActor
        server.createContext("​/api/v1/getActor", new Actor(mem));
        
        // 7.5 GET​ ​/api/v1/getMovie
        server.createContext("​/api/v1/getMovie", new Movie(mem));
        
        // 7.6 GET​ ​/api/v1/hasRelationship
        server.createContext("​/api/v1/hasRelationship", new Relationship(mem));
        
        // 7.7 GET​ ​/api/v1/computeBaconNumber
        server.createContext("​/api/v1/computeBaconNumber", new BaconNumber(mem));
        
        // 7.8 GET​ ​/api/v1/computeBaconPath
        server.createContext("​/api/v1/computeBaconPath", new BaconPath(mem));
        
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
