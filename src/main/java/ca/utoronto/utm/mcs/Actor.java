package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Actor implements HttpHandler
{
    private static Memory memory;

    public Actor(Memory mem) {
        memory = mem;
    }

    public void handle(HttpExchange r) {
        try {
        	
            if (r.getRequestMethod().equals("PUT")) {
                addActor(r);  
            } 
            
            else if (r.getRequestMethod().equals("POST")) {
                getActor(r);
            }  
        } 
        
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addActor(HttpExchange r) throws IOException, JSONException {
    	String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        long name = memory.getValue();
        long actorId = memory.getValue();
        
        // Check if actor already exists
        if (deserialized.has("name")) {
        	r.sendResponseHeaders(400, -1);
        }

        if (deserialized.has("actorId")) {
        	r.sendResponseHeaders(400, -1);
        }

        memory.setValue(name);
        memory.setValue(actorId);

        r.sendResponseHeaders(200, -1);
    }

    public void getActor(HttpExchange r) throws IOException, JSONException{
    	String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        long actorId = memory.getValue();

        if (deserialized.has("actorId")) {
            long name = deserialized.getLong("name");
            String[] movies = null;
            String response = (Long.toString(actorId) + "," + Long.toString(name) + "," + movies);
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
        r.sendResponseHeaders(404, -1);
    }
}
