package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.*;
import org.neo4j.driver.v1.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class addActor implements HttpHandler {
	
	
	private static Session session;

    public addActor(Session ses) {
    	session = ses;
    }

    
    public void handle(HttpExchange r) throws IOException {
    	
        try {
        	
        	// 7.1 PUT​ ​/api/v1/addActor
            if (r.getRequestMethod().equals("PUT")) {
            	
            	try (Transaction tx = session.beginTransaction()) {
            		
            		// Convert HTTP Request to JSON Object
                	JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));

                	// Get name parameter from HTTP Request
                    String name = "";
                    if (body.has("name")) name = body.getString("name");
                    
                    // Get actorId parameter from HTTP Request
                    String actorId = "";
                    if (body.has("actorId")) actorId = body.getString("actorId");

                    // ERROR : Request body is improperly formatted or has missing information
                    if (name.isEmpty() || actorId.isEmpty()) {
                    	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST
                    	tx.failure();
                    }
            		
            		// ERROR : Duplicate actor for actorId
            		if (tx.run ("MATCH (a:actor) WHERE a.id = $actorId RETURN a", parameters("actorId", actorId)).hasNext()) {
            			r.sendResponseHeaders(400, -1); // 400 BAD REQUEST
            			tx.failure();
            		}
            		
            		// Create new actor for actorId
            		else {
            			tx.run("CREATE (a:actor {Name: $name, id: $actorId})", parameters("name", name, "actorId", actorId));
                		r.sendResponseHeaders(200, -1); // 400 BAD REQUEST 
                		tx.success();
                	}
            	}
            }
        } 
        
        // ERROR : Java Exception
        catch (Exception e) {
        	r.sendResponseHeaders(500, -1); // 500 INTERNAL SERVER ERROR
            e.printStackTrace();
        }
    }
}