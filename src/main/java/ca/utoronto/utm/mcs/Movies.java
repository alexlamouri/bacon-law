package ca.utoronto.utm.mcs;

import java.io.IOException;

import org.neo4j.driver.v1.*;

// import static org.neo4j.driver.Values.parameters;

// import javax.xml.ws.spi.http.HttpExchange;

import org.json.JSONException;

//import java.io.IOException;
//import java.net.InetSocketAddress;
//import com.sun.net.httpserver.HttpServer;

/* public class Movies {

    private static Memory memory;

    public Movies(Memory mem) {
        memory = mem;
    }

    /*
     * String movieID; String name;
     * 
     * public Movies(String movieID, String name) { this.movieID = movieID;
     * this.name = name; }
     

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

} */

public class Movies {
    // Driver objects are thread-safe and are typically made available
    // application-wide.
    Driver driver;

    public SmallExample(String uri, String user, String password)
    {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    private void addPerson(String name) {
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = driver.session()) {
            // Wrapping a Cypher Query in a Managed Transaction provides atomicity
            // and makes handling errors much easier.
            // Use `session.writeTransaction` for writes and `session.readTransaction` for
            // reading data.
            // These methods are also able to handle connection problems and transient
            // errors using an automatic retry mechanism.
            session.writeTransaction(tx -> tx.run("MERGE (a:Person {name: $x})", parameters("x", name)));
        }
    }

    private void printPeople(String initial) {
        try (Session session = driver.session()) {
            // A Managed Transaction transactions are a quick and easy way to wrap a Cypher
            // Query.
            // The `session.run` method will run the specified Query.
            // This simpler method does not use any automatic retry mechanism.
            Result result = session.run("MATCH (a:Person) WHERE a.name STARTS WITH $x RETURN a.name AS name",
                    parameters("x", initial));
            // Each Cypher execution returns a stream of records.
            while (result.hasNext()) {
                Record record = result.next();
                // Values can be extracted from a record by index or name.
                System.out.println(record.get("name").asString());
            }
        }
    }

    public void close() {
        // Closing a driver immediately shuts down all open connections.
        driver.close();
    }

    public static void main(String... args) {
        SmallExample example = new SmallExample("bolt://localhost:7687", "neo4j", "password");
        example.addPerson("Ada");
        example.addPerson("Alice");
        example.addPerson("Bob");
        example.printPeople("A");
        example.close();
    }
}