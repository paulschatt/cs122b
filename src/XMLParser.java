import jakarta.annotation.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import static java.lang.Integer.parseInt;
import static java.util.Map.entry;


public class XMLParser {
    @Resource(name = "jdbc/moviedb")
    private static DataSource dataSource;

    private static final String MOVIEFILE = "/home/ubuntu/xml/mains243.xml";
    private static final String CASTFILE = "/home/ubuntu/xml/casts124.xml";
    private static final String ACTORFILE = "/home/ubuntu/xml/actors63.xml";

    public static void main(String[] args) throws IOException, XMLStreamException, SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        Connection connect = DriverManager
                .getConnection("jdbc:mysql://localhost/moviedb?"
                        + "user=mytestuser&password=My6$Password");

        connect.setAutoCommit(false);

        DAO dao = new DAO(connect);

        MovieParser mp = new MovieParser(MOVIEFILE);
        mp.parseDocument();
        mp.insertRecords(dao);
        mp.printInconsistencies();

        ActorParser ap = new ActorParser(ACTORFILE);
        ap.parseDocument();
        ap.insertRecords(dao);
        ap.printInconsistencies();
        Map<String, Integer> actormap = ap.getActorMap();

        CastParser cp = new CastParser(CASTFILE);
        cp.parseDocument();
        cp.cleanse();
        cp.insertRecords(dao, actormap);
        cp.printInconsistencies();

        connect.setAutoCommit(true);
    }
}

class DAO{
    private static final int BATCH_SIZE = 1000; // Define batch size
    private Connection connection;
    private PreparedStatement ps;
    private int batchCount = 0;

    public DAO(Connection connection) {
        this.connection = connection;
    }

    public void setPreparedStatement(String query) throws SQLException {
        this.ps = connection.prepareStatement(query);
    }

    public PreparedStatement getStatement() {
        return ps;
    }

    public int getMaxKey(String tableName) throws SQLException{
        String sql = "select max(id) from " + tableName;
        try {
            this.ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            connection.commit();
            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    public void addQueryToBatch() throws SQLException {
        try {
            ps.addBatch();
            batchCount++;

            if (batchCount % BATCH_SIZE == 0) {
                executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    protected void executeBatch() {
        try {
            ps.executeBatch();
            connection.commit(); // Commit each batch for better performance
            batchCount = 0; // Reset batch count
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback(); // Rollback if an error occurs
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }
    protected int readInt(String sql, String param){
        try {
            this.ps = connection.prepareStatement(sql);
            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            connection.commit();
            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }
}

class MovieParser extends DefaultHandler {
    private ArrayList<Director> directors = new ArrayList<>();
    HashMap<String, Integer> genres = new HashMap<>();

    private Director tempDirector = new Director();
    private Movie tempMovie;
    private String tempVal;
    private String tempDir = "";
    private String path;
    private int badMovieRows = 0;
    private int badGenre = 0;

    Map<String, Integer> genreMap = Map.ofEntries(
            entry("ACTN", 1),
            entry("ADVT", 3),
            entry("BIOP", 5),
            entry("CART", 4),
            entry("COMD", 6),
            entry("CNRB", 7),
            entry("DOCU", 8),
            entry("DRAM", 9),
            entry("EPIC", 24),
            entry("FAML", 10),
            entry("FANT", 11),
            entry("HIST", 12),
            entry("HORR", 13),
            entry("MUSC", 15),
            entry("MYST", 16),
            entry("PORN", 2),
            entry("ROMT", 18),
            entry("SCIFI", 19),
            entry("SUSP", 21),
            entry("WEST", 23)
    );

    public MovieParser(String path){
        this.path = path;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of movie
            tempMovie = new Movie();
        }
    }

    public void printData() {
        Iterator<Director> it = directors.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
        System.out.println("Genres: " + genres.toString());
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("film")) {
            tempDirector.movies.add(tempMovie);
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setId(tempVal);
        } else if (qName.equalsIgnoreCase("cat")) {
            tempVal = tempVal.replaceAll("\\s+","").toUpperCase();
            tempMovie.addGenre(tempVal);
            genres.merge(tempVal, 1, Integer::sum);
        } else if (qName.equalsIgnoreCase("year")) {
            tempMovie.setYear(tempVal);
        } else if (qName.equalsIgnoreCase("dirname")) {
            if(!tempDir.equals(tempVal)){
                if(tempDir.toUpperCase().contains("UNKNOWN") || tempDir.toUpperCase().contains("UNYEAR")){
                    badMovieRows++;
                    tempDir = "";
                    directors.add(tempDirector);
                    tempDirector = new Director();
                }
                else {
                    tempDir = tempVal;
                    directors.add(tempDirector);
                    tempDirector = new Director();
                    tempDirector.setName(tempVal);
                }
            }
        }
    }

    public void parseDocument() {
        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse(this.path, this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }


    public void insertRecords(DAO dao) throws SQLException {
        String insertDirectorQuery = "INSERT INTO stars (name) VALUES (?);";
        String insertMovieQuery = "INSERT INTO movies (id, title, year, director, price) VALUES (?, ?, ?, ?, 1) ON DUPLICATE KEY UPDATE id = id;";
        String insertMovieDirectorRelationQuery = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?) ON DUPLICATE KEY UPDATE starId = starId;";
        String insertMovieGenreRelationQuery = "INSERT INTO genres_in_movies (genreId, movieId) VALUES(?, ?) ON DUPLICATE KEY UPDATE movieId = movieId;";



        // Track successfully inserted movie IDs
        Set<String> successfullyInsertedMovies = new HashSet<>();

        // Step 1: Insert Movies and collect successful IDs
        dao.setPreparedStatement(insertMovieQuery);
        for(Director tempDirector : directors){
            String directorName = tempDirector.getName();

            for (Movie tempMovie : tempDirector.movies) {
                if (tempMovie.getId() == null || tempMovie.getTitle() == null || directorName == null || directorName.isEmpty() || tempMovie.getTitle().isEmpty()) {
                    badMovieRows++;
                    continue;  // Skip invalid entries
                }
                try {
                    dao.getStatement().setString(1, tempMovie.getId());
                    dao.getStatement().setString(2, tempMovie.getTitle());
                    if(tempMovie.getYear().isEmpty()){
                        dao.getStatement().setNull(3, Types.INTEGER);
                    }
                    else {
                        dao.getStatement().setInt(3, parseInt(tempMovie.getYear()));
                    }
                    dao.getStatement().setString(4, directorName);
                    dao.addQueryToBatch();

                    // Add ID to Set if no errors
                    successfullyInsertedMovies.add(tempMovie.getId());
                } catch (NumberFormatException e) {
                    continue;  // Skip if year parsing fails
                }
            }
        }
        dao.executeBatch();  // Commit movies batch

        int maxStarId = dao.getMaxKey("stars") + 1;
        // Step 2: Insert Directors
        /*dao.setPreparedStatement(insertDirectorQuery);
        for (Director tempDirector : directors) {
            if(tempDirector.getName() != null) {
                dao.getStatement().setString(1, tempDirector.getName());
                dao.addQueryToBatch();
                tempDirector.setID(maxStarId);
                maxStarId++;
            }
        }
        dao.executeBatch();  // Commit directors batch

        // Step 3: Insert Director-Movie Relations only for successfully inserted movies
        dao.setPreparedStatement(insertMovieDirectorRelationQuery);
        for (Director tempDirector : directors) {
            for (Movie tempMovie : tempDirector.movies) {
                if (successfullyInsertedMovies.contains(tempMovie.getId())) {  // Check if movie was inserted
                    dao.getStatement().setInt(1, tempDirector.getID());
                    dao.getStatement().setString(2, tempMovie.getId());
                    dao.addQueryToBatch();
                }
            }
        }
        dao.executeBatch();*/  // Commit relationships batch
        //Insert genres in movies
        dao.setPreparedStatement(insertMovieGenreRelationQuery);
        for (Director tempDirector : directors) {
            for (Movie tempMovie : tempDirector.movies) {
                if (successfullyInsertedMovies.contains(tempMovie.getId())) {
                    for(String genre : tempMovie.getGenres()){
                        if(genreMap.containsKey(genre)) {
                            dao.getStatement().setString(2, tempMovie.getId());
                            dao.getStatement().setInt(1, genreMap.get(genre));
                            dao.addQueryToBatch();
                        }
                        else{
                            badGenre++;
                        }
                    }
                }
            }
        }
        dao.executeBatch();
    }
    public void printInconsistencies(){
        System.out.println(badMovieRows + " movies skipped due to bad or no names");
        System.out.println(badGenre + " genres skipped due to bad names");
    }
}



class ActorParser extends DefaultHandler {
    private ArrayList<Actor> actors = new ArrayList<>();

    private Actor tempActor;
    private String tempVal;
    private String path;

    private Map<String, Integer> actorMap = new HashMap<>();
    private int badRows;


    public ActorParser(String path){
        this.path = path;
    }

    public Map<String, Integer> getActorMap() {
        return actorMap;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            tempActor = new Actor();
        }
    }

    public void printData() {
        Iterator<Actor> it = actors.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            actors.add(tempActor);
        } else if (qName.equalsIgnoreCase("stagename")) {
            if(tempVal.toUpperCase().contains("UNKNOWN") || tempVal.toUpperCase().contains("UNYEAR") || tempVal.length() <= 3) {
                tempActor.setName(null);
            }
            else{
                tempActor.setName(tempVal);
            }
        } else if (qName.equalsIgnoreCase("dob")) {
            tempActor.setBirthYear(tempVal);
        }
    }

    public void parseDocument() {
        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse(this.path, this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }
    public void insertRecords(DAO dao) throws SQLException{
        String insertStarQuery = "INSERT INTO stars (name, birthYear) VALUES(?, ?);";

        Set<String> successfullyInserted = new HashSet<>();
        Iterator<Actor> it = actors.iterator();


        int maxId = dao.getMaxKey("stars") + 1;
        dao.setPreparedStatement(insertStarQuery);
        it = actors.iterator();

        String name;
        int birthYear;
        while (it.hasNext()) {
            tempActor = it.next();
            try {
                name = tempActor.getName();
                if(name != null) {
                    dao.getStatement().setString(1, name);
                    if(tempActor.getBirthYear() == null || tempActor.getBirthYear().isEmpty()){
                        dao.getStatement().setNull(2, Types.INTEGER);
                    }
                    else {
                        dao.getStatement().setInt(2, parseInt(tempActor.getBirthYear()));
                    }
                    dao.addQueryToBatch();
                    tempActor.setId(maxId);
                    actorMap.put(name, maxId);
                    maxId++;
                }
                else{
                    badRows++;
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        dao.executeBatch();
    }
    public void printInconsistencies(){
        System.out.println(badRows + " actors skipped due to bad or no actor names");
    }
}

class CastParser extends DefaultHandler {
    private ArrayList<Cast> casts = new ArrayList<>();

    private Cast tempCast;
    private String tempVal;
    private String path;
    private int undetectedActors;

    public CastParser(String path){
        this.path = path;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("filmc")) {
            tempCast = new Cast();
        }
    }

    public void printData() {
        Iterator<Cast> it = casts.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("filmc")) {
            casts.add(tempCast);
        } else if (qName.equalsIgnoreCase("f")) {
            tempCast.setId(tempVal);
        } else if (qName.equalsIgnoreCase("a")) {
            if(!tempVal.equals("s a")){
                tempCast.addActor(tempVal);
            }
        }
    }

    public void parseDocument() {
        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse(this.path, this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }
    public void cleanse(){
        Iterator<Cast> it = casts.iterator();
        while (it.hasNext()) {
            Cast cast = it.next();
            Set<String> set = new HashSet<>(cast.actors);
            cast.actors.clear();
            cast.actors.addAll(set);
        }
    }
    public void insertRecords(DAO dao, Map<String, Integer> actormap) throws SQLException {
        String insertQuery = "INSERT IGNORE into stars_in_movies(starId, movieId) VALUES (?, ?) ON DUPLICATE KEY UPDATE starId = starId;";
        String insertStarQuery = "INSERT INTO stars(name) VALUES (?);";

        Map<Actor, List<String>> actorsToAdd = new HashMap<Actor, List<String>>();

        String readQuery = "SELECT id FROM stars WHERE name = ?;";

        Set<String> successfullyInserted = new HashSet<>();
        Iterator<Cast> it = casts.iterator();
        while (it.hasNext()) {
            Cast tempCast = it.next();
            for (String actor : tempCast.actors) {
                if(!actormap.containsKey(actor)){
                    undetectedActors++;
                    /*int id = dao.readInt(readQuery, actor);
                    if(id != -1){
                        actormap.put(actor, id);
                    }*/
                }
            }
        }
        dao.setPreparedStatement(insertQuery);
        it = casts.iterator();
        int actorId;
        String movieId;
        while(it.hasNext()){
            tempCast = it.next();
            movieId = tempCast.getId();
            for(String actorname : tempCast.actors) {
                if (actormap.containsKey(actorname)) {
                    actorId = actormap.get(actorname);
                    dao.getStatement().setInt(1, actorId);
                    dao.getStatement().setString(2, movieId);
                    dao.addQueryToBatch();
                }
                //Add actors that are not in the actor file for later consideration
                else{
                    Actor actor = new Actor();
                    actor.setName(actorname);
                    if(actorsToAdd.containsKey(actor)){
                        actorsToAdd.get(actor).add(movieId);
                    }
                    else{
                        actorsToAdd.put(actor, new ArrayList<>());
                        actorsToAdd.get(actor).add(movieId);
                    }
                }
            }
        }
        dao.executeBatch();
        actorId = dao.getMaxKey("stars") + 1;
        dao.setPreparedStatement(insertStarQuery);
        Iterator<Map.Entry<Actor, List<String>>> iterator = actorsToAdd.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Actor, List<String>> entry = iterator.next();
            Actor actor = entry.getKey();
            dao.getStatement().setString(1, actor.getName());
            dao.addQueryToBatch();
            actor.setId(actorId);
            actorId++;
        }
        dao.executeBatch();
        dao.setPreparedStatement(insertQuery);
        iterator = actorsToAdd.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Actor, List<String>> entry = iterator.next();
            Actor actor = entry.getKey();
            List<String> movies = entry.getValue();
            dao.getStatement().setInt(1, actor.getId());
            for(String movieid : movies){
                dao.getStatement().setString(2, movieid);
                dao.addQueryToBatch();
            }
        }
        dao.executeBatch();
    }
    public void printInconsistencies(){
        System.out.println(undetectedActors + " actors detected in casts XML that are not in actors XML");
    }
}


class Director{
    private String year;
    private int id;
    private String name;
    public ArrayList<Movie> movies = new ArrayList<>();


    public void setName(String name){
        this.name = name;
    }

    public void setYear(String year){
        this.year = year;
    }

    public void setID(int id){
        this.id = id;
    }

    public String getYear(){
        return year;
    }

    public int getID(){
        return id;
    }
    public String getName(){
        return name;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Name: " + this.name);
        sb.append(", ");
        sb.append("Year : " + this.year);
        sb.append(", ");
        sb.append("Movies: " + this.movies.toString());
        return sb.toString();
    }
}

class Movie{
    private String title;
    private ArrayList<String> genres = new ArrayList<>();
    private String id;

    private String year;

    public void addGenre(String genre){
        this.genres.add(genre);
    }
    public ArrayList<String> getGenres(){
        return genres;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public String getTitle() {
        return this.title;
    }
    public void setYear(String year){
        this.year = year;
    }
    public String getYear(){
        return this.year;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Title: " + this.title);
        sb.append(", ");
        sb.append("Id: " + this.id);
        sb.append(", ");
        sb.append("Year : " + this.year);
        sb.append(", ");
        sb.append("Genres: " + this.genres.toString());
        return sb.toString();
    }
}

class Cast{
    private String id;
    protected ArrayList<String> actors = new ArrayList<>();

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return this.id;
    }

    public void addActor(String actor){
        this.actors.add(actor);
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("ID: " + this.id);
        sb.append(", ");
        sb.append("Actors: " + this.actors.toString());
        return sb.toString();
    }
}

class Actor{
    private int id;
    private String name;
    private String birthYear;

    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }
    public void setName(String name){
        this.name = name;
    }

    public void setBirthYear(String birthYear){
        this.birthYear = birthYear;
    }

    public String getName(){
        return this.name;
    }

    public String getBirthYear(){
        return this.birthYear;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Name: " + this.name);
        sb.append(", ");
        sb.append("BirthYear: " + this.birthYear);
        return sb.toString();
    }
}
