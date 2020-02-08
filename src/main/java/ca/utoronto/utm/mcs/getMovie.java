package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.*;
import org.neo4j.driver.v1.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class getMovie implements HttpHandler {
	
	
	private static Session session;

    public getMovie(Session ses) {
    	session = ses;
    }

    
    public void handle(HttpExchange r) throws IOException {
    	
        try {
            
            // 7.5 GET​ ​​/api/v1/getMovie
            if (r.getRequestMethod().equals("GET")) {
            
            	try (Transaction tx = session.beginTransaction()) {
                    
                    // 1. Convert HTTP Request to JSON Object
                    JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
                    
                    // 2. Get movieId parameter from HTTP Request
                    String movieId = "";
                    if (body.has("movieId")) movieId = body.getString("movieId");

                    // 3. Check if request body is properly formatted and has required information
                    if (movieId.isEmpty()) {
                        r.sendResponseHeaders(400, -1); // 400 BAD REQUEST
                        tx.failure();
                    }
                    
                    String name;
                    String actorId;
                    ArrayList<String> actors = new ArrayList<String>(1024);
                    
                    // 4. Check if movie node exists for id
                    StatementResult movieResult = tx.run (
                    		"MATCH (m:movie) WHERE m.id = $movieId "
                    		+ "RETURN m.Name", 
                    		parameters("movieId", movieId));
                    
                    // 5. Get parameters from Database
                    if (movieResult.hasNext()) {
                        
                        // 5.1 Get name parameter from Database
                        Record movieRecord = movieResult.next();
                        name = movieRecord.get("m.Name").asString();
                        
                        // 5.2 Accumulate actors parameter from Database
                        StatementResult actorsResult = tx.run(
                        		"MATCH (m:movie {id: $movieId})<-[r:ACTED_IN]-(a:actor) "
                        		+ "RETURN a.id", 
                        		parameters("movieId", movieId));
                        
                        while (actorsResult.hasNext()) {
                            Record actorsRecord = actorsResult.next();
                            actorId = actorsRecord.get("a.id").asString();
                            actors.add(actorId);
                        }
                        
                        // 6. Create JSON Body and add the Body Parameters
                        JSONObject json = new JSONObject();
                        json.put("movieId", movieId);
                        json.put("name", name);
                        json.put("actors", actors);
                          
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