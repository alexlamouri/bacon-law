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

public class computerBaconPath implements HttpHandler {

	private static Session session;

    public computerBaconPath(Session ses) {
    	session = ses;
    }

    
    public void handle(HttpExchange r) throws IOException {
        
    	try {
        	
        	// 7.7 GET​ ​/api/v1/computeBaconNumber
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
                    	tx.failure();
                    }
                    
//                    String name;
//                    String movieId;
//                    ArrayList<String> movies = new ArrayList<String>(1024);
//                    
//                    // 4. Check if movie node exists for id
//            		StatementResult result = tx.run (
//                            "MATCH (a:actor) WHERE a.id = $actorId RETURN a.Name",
//                            parameters("actorId", actorId)
//                    );
//            		
//            		// 5. Get parameters from Database
//            		if (result.hasNext()) {
//            			
//            			// 5.1 Get name parameter from Database
//            			Record record = result.next();
//            			name = record.get("a.Name").asString();
//               
//            			// 5.2 Accumulate movies parameter from Database
//            			result = tx.run("MATCH (a:actor {id : $actorId})-[r:ACTED_IN]->(m:movie) RETURN m.id", parameters("actorId", actorId));
//            			while (result.hasNext()) {
//            				record = result.next();
//            				movieId = record.get("m.id").asString();
//            				movies.add(movieId);
//            			}
            			
            			// 6. Create JSON Body and add the Body Parameters
                        JSONObject json = new JSONObject();
                        json.put("actorId", actorId);
                        json.put("name", name);
                        json.put("movies", movies);
                      
                        // 7. Convert the JSON Body to a String and send it as a Response
                        String response = json.toString();
                        System.out.println(response);
                        r.sendResponseHeaders(200, response.length());
                        OutputStream output = r.getResponseBody();
                        output.write(response.getBytes());
                        output.close();
            		}
            		
            		else {
            			r.sendResponseHeaders(404, -1); // 404 NOT FOUND
            			System.out.print("actor not found\n");  
            			tx.failure();
            		}
            	}
            } 
    	} 
        
        catch (Exception e) {
        	r.sendResponseHeaders(500, -1); // 500 INTERNAL SERVER ERROR
            e.printStackTrace();
        }
    }
}

