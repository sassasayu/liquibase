package liquibase.test;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.JDBCException;
import liquibase.migrator.JUnitJDBCDriverClassLoader;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class TestContext {
    private static TestContext instance = new TestContext();

    private Set<Database> availableDatabases;
    private Set<Database> allDatabases;
    private Set<Connection> availableConnections;

    private final String[] DEFAULT_TEST_URLS = new String[]{
            "jdbc:Cache://127.0.0.1:1972/liquibase",
            "jdbc:db2://localhost:50000/liquibas",
            "jdbc:derby:liquibase;create=true",
            "jdbc:firebirdsql:localhost/3050:c:\\firebird\\liquibase.fdb",
            "jdbc:h2:mem:liquibase",
            "jdbc:hsqldb:mem:liquibase",
            "jdbc:jtds:sqlserver://localhost;databaseName=liquibase",
//            "jdbc:jtds:sqlserver://windev1.sundog.net;instance=latest;DatabaseName=liquibase",
            "jdbc:sqlserver://localhost;databaseName=liquibase",
            "jdbc:mysql://localhost/liquibase",
            "jdbc:oracle:thin:@localhost/XE",
            "jdbc:postgresql://localhost/liquibase",
//            "jdbc:jtds:sybase://localhost/nathan:5000",
//            "jdbc:sybase:Tds:"+ InetAddress.getLocalHost().getHostName()+":5000/liquibase",
    };

    private Connection openConnection(final String url) throws Exception {
        String username = getUsername(url);
        String password = getPassword(url);

        JUnitJDBCDriverClassLoader jdbcDriverLoader = JUnitJDBCDriverClassLoader.getInstance();
        Driver driver = (Driver) Class.forName(DatabaseFactory.getInstance().findDefaultDriver(url), true, jdbcDriverLoader).newInstance();

        Properties info = new Properties();
        info.put("user", username);
        if (password != null) {
            info.put("password", password);
        }

        final Connection connection;
        try {
            connection = driver.connect(url, info);
        } catch (SQLException e) {
            System.out.println("Could not connect to " + url + ": Will not test against");
            return null; //could not connect
        }
        if (connection == null) {
            throw new JDBCException("Connection could not be created to " + url + " with driver " + driver.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                try {
                    connection.close();
//                    System.out.println(url+" closed successfully");
                } catch (SQLException e) {
                    System.out.println("Could not close "+url);
                    e.printStackTrace();
                }
            }
        }));

        return connection;
    }

    private String getUsername(String url) {
        if (url.startsWith("jdbc:hsqldb")) {
            return "sa";
        } else if (url.indexOf("windev1") > 0) {
            return "sundog";
        }
        return "liquibase";
    }

    private String getPassword(String url) {
        if (url.startsWith("jdbc:hsqldb")) {
            return "";
        } else if (url.indexOf("windev1") > 0) {
            return "sundog";
        }
        return "liquibase";
    }

    public static TestContext getInstance() {
        return instance;
    }


    public String[] getTestUrls() {
        return DEFAULT_TEST_URLS;
    }

    public Set<Database> getAllDatabases() {
        if (allDatabases == null) {
            allDatabases = new HashSet<Database>();

            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                allDatabases.add(database);
            }

        }
        return allDatabases;
    }

    public Set<Database> getAvailableDatabases() throws Exception {
        if (availableDatabases == null) {
            availableDatabases = new HashSet<Database>();
            for (Connection conn : getAvailableConnections()) {
                availableDatabases.add(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn));
            }
        }
        return availableDatabases;
    }


    public Set<Connection> getAvailableConnections() throws Exception {
        if (availableConnections == null) {
            availableConnections = new HashSet<Connection>();
            for (String url : getTestUrls()) {
                Connection connection = openConnection(url);
                if (connection != null) {
                    availableConnections.add(connection);
                }
            }
        }

        return availableConnections;
    }
}
