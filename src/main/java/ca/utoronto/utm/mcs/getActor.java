package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.*;
import org.neo4j.driver.v1.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class getActor implements HttpHandler {
	
	
	private static Session session;

    public getActor(Session ses) {
    	session = ses;
    }

    
    public void handle(HttpExchange r) throws IOException {
    	
        try {
            
            // 7.4 GET​ ​/api/v1/getActor
            if (r.getRequestMethod().equals("GET")) {
            	
            	try (Transaction tx = session.beginTransaction()) {
            		
            		// 1. Convert HTTP Request to JSON Object
                	JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
                	
                	// 2. Get movieId parameter from HTTP Request
                    String actorId = "";
                    if (body.has("actorId")) actorId = body.getString("actorId");

                    // 3. Check if request body is properly formatted and has required information
                    if (actorId.isEmpty()) {
                    	r.sendResponseHeaders(400, -1); // 400 BAD REQUEST
                    	return;
                    }
                    
                    String name;
                    String movieId;
                    ArrayList<String> movies = new ArrayList<String>(1024);
                    
                    // 4. Check if actor node exists for id
            		StatementResult actorResult = tx.run (
                            "MATCH (a:actor) WHERE a.id = $actorId RETURN a.Name",
                            parameters("actorId", actorId)
                    );
            		
            		// 5. Get parameters from Database
            		if (actorResult.hasNext()) {
            			
            			// 5.1 Get name parameter from Database
            			name = actorResult.next().get("a.Name").asString();
               
            			// 5.2 Accumulate movies parameter from Database
            			StatementResult moviesResult = tx.run(
            					"MATCH (a:actor {id : $actorId})-[r:ACTED_IN]->(m:movie) "
            					+ "RETURN m.id", 
            					parameters("actorId", actorId));
            			
            			while (moviesResult.hasNext()) {
            				Record moviesRecord = moviesResult.next();
            				movieId = moviesRecord.get("m.id").asString();
            				movies.add(movieId);
            			}
            			
            			// 6. Create JSON Body and add the Body Parameters
                        JSONObject json = new JSONObject();
                        json.put("actorId", actorId);
                        json.put("name", name);
                        json.put("movies", movies);
                      
                        // 7. Convert the JSON Body to a String and send it as a Response
                        String response = json.toString();
                        r.sendResponseHeaders(200, response.length());
                        OutputStream output = r.getResponseBody();
                        output.write(response.getBytes());
                        output.close();
                        tx.success();
            		}
            		
            		else {
            			r.sendResponseHeaders(404, -1); // 404 NOT FOUND
            			tx.failure();
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