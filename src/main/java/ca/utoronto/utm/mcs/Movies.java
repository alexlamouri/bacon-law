package ca.utoronto.utm.mcs;

import java.io.IOException;

import javax.xml.ws.spi.http.HttpExchange;

import org.json.JSONException;

//import java.io.IOException;
//import java.net.InetSocketAddress;
//import com.sun.net.httpserver.HttpServer;

public class Movies {

    private static Memory memory;

    public Movies(Memory mem) {
        memory = mem;
    }

    /*
     * String movieID; String name;
     * 
     * public Movies(String movieID, String name) { this.movieID = movieID;
     * this.name = name; }
     */

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("GET")) {
                addMovie(r);
            } else if (r.getRequestMethod().equals("POST")) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMovie(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        long movieID = memory.getValue();
        long name = memory.getValue();

        if (deserialized.has("movieID"))
            r.sendResponseHeaders(400, -1);
        else if (deserialized.has("name"))
            r.sendResponseHeaders(400, -1);
        else {
            // ADD TO DATABASE
        }
    }

    public void getMovie(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        long movieID = memory.getValue();

        if (deserialized.has("movieID")) {
            // GET FROM DATABASE
            long name = deserialized.getLong("name");
            String[] actors = deserialized.getLong("actors");
            memory.setValue(movieID, name, actors);
        } else {
            r.sendResponseHeaders(404, -1);
        }

    }

}