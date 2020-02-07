package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.*;
import org.neo4j.driver.v1.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class hasRelationship implements HttpHandler {
	
	private static Session session;

    public hasRelationship(Session ses) {
    	session = ses;
    }
    
    
    public void handle(HttpExchange r) throws IOException {
    	
        try {
            
            // 7.6 GET​ ​/api/v1/hasRelationship
            if (r.getRequestMethod().equals("GET")) {
            	
            	try (Transaction tx = session.beginTransaction()) {
            		
            		// Convert HTTP Request to JSON Object
                	JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
                	
                	// Get actorId parameter
                    String actorId = "";
                    if (body.has("actorId")) actorId = body.getString("actorId");
                    
                    // Get movieId parameter
                    String movieId = "";
                    if (body.has("movieId")) movieId = body.getString("movieId");

                    // Check request body format & information
                    if (actorId.isEmpty() || movieId.isEmpty()) {
                    	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST
                    	tx.failure();;
                    }
                    
                    
                    // Check if actor node exists for id
            		if (!tx.run("MATCH (a:actor) WHERE a.id = $actorId RETURN a", parameters("actorId", actorId)).hasNext()) {
            			r.sendResponseHeaders(404, -1); // 404 NOT FOUND
            			tx.failure();
            		}
            		
            		
            		// Check if movie node exists for id	
            		if (!tx.run ("MATCH (m:movie) WHERE m.id = $movieId RETURN m", parameters("movieId", movieId)).hasNext()) {
            			r.sendResponseHeaders(404, -1); // 404 NOT FOUND 
            			tx.failure();
            		}
            		
            		
                    // Check if existing node for id
            		// https://stackoverflow.com/questions/42022215/test-if-relationship-exists-in-neo4j-spring-data
            		StatementResult result = tx.run ("RETURN EXISTS((:actor {id: $actorId})-[:ACTED_IN]->(:movie {id: $movieId}))", parameters("actorId", actorId.toString(), "movieId", movieId.toString())); 
            		String exists = result.next().get("EXISTS((:actor {id: $actorId})-[:ACTED_IN]->(:movie {id: $movieId}))").toString();
            		boolean hasRelationship;
            		
            		if (exists == "TRUE") {
            			hasRelationship = true;
            		}
            		
            		else {
            			hasRelationship = false;
            		}
            		
            		// Create JSON Body and add the Body Parameters
                    JSONObject json = new JSONObject();
                    json.put("actorId", actorId);
                    json.put("movieId", movieId);
                    json.put("hasRelationship", hasRelationship);
                    
                    // Convert the JSON Body to a String and send it as a Response
                    String response = json.toString();
                    r.sendResponseHeaders(200, response.length());
                    OutputStream output = r.getResponseBody();
                    output.write(response.getBytes());
                    output.close();
                    tx.success();
            	}
            }  
        } 
        
        catch (Exception e) {
        	r.sendResponseHeaders(500, -1); // 500 INTERNAL SERVER ERROR
            e.printStackTrace();
        }
    }
}
