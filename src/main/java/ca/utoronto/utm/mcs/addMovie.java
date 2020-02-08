package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.*;
import org.neo4j.driver.v1.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class addMovie implements HttpHandler {
	
	
	private static Session session;

    public addMovie(Session ses) {
    	session = ses;
    }

    
    public void handle(HttpExchange r) throws IOException {
    	
        try {
        	
        	// 7.2 PUT​ ​/api/v1/addMovie
            if (r.getRequestMethod().equals("PUT")) {
            	
            	try (Transaction tx = session.beginTransaction()) {
            		
            		// 1. Convert HTTP Request to JSON Object
                	JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));

                	// 2. Get name parameter from HTTP Request
                    String name = "";
                    if (body.has("name")) name = body.getString("name");
                    
                    // 3. Get movieId parameter from HTTP Request
                    String movieId = "";
                    if (body.has("movieId")) movieId = body.getString("movieId");

                    // 4. Check if request body is properly formatted and has required information
                    if (name.isEmpty() || movieId.isEmpty()) {
                    	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST 
                    	tx.failure();
                    }
            		
            		// 5. Check if existing movie for movieId
            		if (tx.run ("MATCH (m:movie) WHERE m.id = $movieId RETURN m.id", parameters("movieId", movieId)).hasNext()) {
            			r.sendResponseHeaders(400, -1); // 400 BAD REQUEST 
            			tx.failure();
            		}
            		
            		// 6. Create new movie for movieId
            		else {
            			tx.run("CREATE (m:movie {Name: $name, id: $movieId})", parameters("name", name, "movieId", movieId));		
                		r.sendResponseHeaders(200, -1); // 200 OK
                		tx.success();
                	}
            	} 
            }     
        } 
        
        catch (JSONException e) {
        	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST
            e.printStackTrace();
        }
    	
    	catch (IOException e) {
        	r.sendResponseHeaders(500, -1); // 500 INTERNAL SERVER ERROR
            e.printStackTrace();
        }
    }	
}