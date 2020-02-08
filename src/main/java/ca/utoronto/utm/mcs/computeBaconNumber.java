package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.JSONException;
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
	            		StatementResult actorResult = tx.run ("MATCH (a:actor) WHERE a.id = $actorId RETURN a.id", parameters("actorId", actorId));
	            		
	            		// Get parameters from Database
	            		if (actorResult.hasNext()) {
	            			
	            			// https://blog.knutwalker.de/bacon-number/?fbclid=IwAR3YcUGqDvg3lRx6MoQHdC5qdcFcKI2OYon-N6GXlTj8F6JBK0HGSVc-jg4
	            			// https://neo4j.com/docs/cypher-manual/current/clauses/match/
	            			StatementResult baconNumberResult = tx.run(
	            					"MATCH p=shortestPath((a:actor)-[r:ACTED_IN*]-(b:actor))"
	            					+ "WHERE a.id = $actorId AND b.id = $baconId "
	            					+ "RETURN length([m in nodes(p) WHERE m:movie]) as BaconNumber", 
	            					parameters("actorId", actorId, "baconId", "nm0000102"));
	            	            
	            			if (baconNumberResult.hasNext()) {
	            				
	            				// Get Bacon Number
	            				String baconNumber = baconNumberResult.next().get("BaconNumber").toString(); 
		            			
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
	            				
	            				// https://piazza.com/class/k4xb0djrq1x1ei?cid=137
	            				
	            				// Create JSON Body and add the Body Parameters
			                    JSONObject json = new JSONObject();
			                    json.put("baconNumber", "undefined");
			                  
			                    // Convert the JSON Body to a String and send it as a Response
			                    String response = json.toString();
			                    r.sendResponseHeaders(200, response.length());
			                    OutputStream output = r.getResponseBody();
			                    output.write(response.getBytes());
			                    output.close();
	            				tx.success();
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
