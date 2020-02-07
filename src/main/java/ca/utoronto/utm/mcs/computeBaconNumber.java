package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.JSONObject;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class computeBaconNumber implements HttpHandler {

	private static Session session;

    public computeBaconNumber(Session ses) {
    	session = ses;
    }

    
    public void handle(HttpExchange r) throws IOException {
        
    	try {
        	
        	// 7.7 GET​ ​/api/v1/computeBaconNumber
            if (r.getRequestMethod().equals("GET")) {
                 	
            	try (Transaction tx = session.beginTransaction()) {
            		
            		// Convert HTTP Request to JSON Object
                	JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
                	
                	// Get actorId parameter from HTTP Request
                    String actorId = "";
                    if (body.has("actorId")) actorId = body.getString("actorId");

                    // ERROR : Request body is improperly formatted or missing required information
                    if (actorId.isEmpty()) {
                    	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST
                    	tx.failure();
                    }
                                        
                    // Check if actor is Kevin Bacon
                    if (actorId.equals("nm0000102")) {  
                    	
                    	// Create JSON Body and add the Body Parameters
	                    JSONObject json = new JSONObject();
	                    json.put("baconNumber", 0);
	                  
	                    // Convert the JSON Body to a String and send it as a Response
	                    String response = json.toString();
	                    r.sendResponseHeaders(200, response.length());
	                    OutputStream output = r.getResponseBody();
	                    output.write(response.getBytes());
	                    output.close();
	                    tx.success();
                    }
                    
                    else {
                    
	                    // Check if actor exists for id
	            		StatementResult result = tx.run ("MATCH (a:actor) WHERE a.id = $actorId RETURN a.Name", parameters("actorId", actorId));
	            		
	            		// Get parameters from Database
	            		if (result.hasNext()) {
	            			
	            			result = tx.run("MATCH (a:actor {id: $actorId}), (b:actor {id: $baconId}) MATCH p=(a)-[:ACTED_IN*]-(b) WITH p, reduce(b = 0, r IN rels(p) | b + 1) AS baconNumber RETURN baconNumber", 
	                    		parameters("actorId", actorId, "baconId", "nm0000102"));
	            	            
	            			if (result.hasNext()) {
	            				
	            				// Get Bacon Number
	            				String baconNumber = result.next().get("baconNumber").toString(); 
		            			
			        			// Create JSON Body and add the Body Parameters
			                    JSONObject json = new JSONObject();
			                    json.put("baconNumber", baconNumber);
			                  
			                    // Convert the JSON Body to a String and send it as a Response
			                    String response = json.toString();
			                    r.sendResponseHeaders(200, response.length());
			                    OutputStream output = r.getResponseBody();
			                    output.write(response.getBytes());
			                    output.close();
			                    tx.success();
	            			}
	            			
	            			// ERROR : If path not found
	            			else {
	            				r.sendResponseHeaders(404, -1); // 404 NOT FOUND
		            			tx.failure();
	            			}
	            			
	            		}
	            		
	            		// ERROR : If actor node not found
	            		else {
	            			r.sendResponseHeaders(404, -1); // 404 NOT FOUND
	            			tx.failure();
	            		}
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

