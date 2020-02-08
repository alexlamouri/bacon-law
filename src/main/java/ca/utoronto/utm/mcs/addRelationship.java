package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.*;
import org.neo4j.driver.v1.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class addRelationship implements HttpHandler {
	
	private static Session session;

    public addRelationship(Session ses) {
    	session = ses;
    }

    
    public void handle(HttpExchange r) throws IOException {
        
    	try {
        	
        	// 7.3 PUT​ ​/api/v1/addRelationship
            if (r.getRequestMethod().equals("PUT")) {
                
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
                    	tx.failure();
                    }
                    
                    // Check if actor node exists for id
            		if (!tx.run ("MATCH (a:actor) WHERE a.id = $actorId RETURN a", parameters("actorId", actorId)).hasNext()) {
            			r.sendResponseHeaders(404, -1); // 404 NOT FOUND
            			tx.failure();
            		}
            		
            		
            		// Check if movie node exists for id
            		if (!tx.run("MATCH (m:movie) WHERE m.id = $movieId RETURN m", parameters("movieId", movieId)).hasNext()) {
            			r.sendResponseHeaders(404, -1); // 404 NOT FOUND
            			tx.failure();
            		}

                    // Check if existing relationship node for actor and movie
            		// https://stackoverflow.com/questions/42022215/test-if-relationship-exists-in-neo4j-spring-data
            		StatementResult relationshipResult = tx.run("RETURN EXISTS((:actor {id: $actorId})-[:ACTED_IN]->(:movie {id: $movieId}))", parameters("actorId", actorId.toString(), "movieId", movieId.toString())); 
            		boolean hasRelationship = relationshipResult.next().get("EXISTS((:actor {id: $actorId})-[:ACTED_IN]->(:movie {id: $movieId}))").asBoolean();
            		
            		if (hasRelationship) {
            			r.sendResponseHeaders(400, -1); // 400 BAD REQUEST
            			tx.failure();
            		}
            		
            		else {
            			tx.run("MATCH (a:actor),(m:movie) WHERE a.id = $actorId AND m.id = $movieId CREATE (a)-[r:ACTED_IN]->(m)", parameters("actorId", actorId, "movieId", movieId));
                		r.sendResponseHeaders(200, -1); // 400 BAD REQUEST
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
