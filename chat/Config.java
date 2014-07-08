package chat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;



public class Config {
    private static final String PROPERTIES_FILE = "./server.properties";

    public static int PORT;
    public static int HISTORY_LENGTH;
    public static String HELLO_MESSAGE;

    static {
        Properties properties = new Properties();
        FileInputStream propertiesFile = null;

        try {
            propertiesFile = new FileInputStream(PROPERTIES_FILE);
            properties.load(propertiesFile);

            PORT             = Integer.parseInt(properties.getProperty("PORT"));
            HISTORY_LENGTH   = Integer.parseInt(properties.getProperty("HISTORY_LENGTH"));
            HELLO_MESSAGE    = properties.getProperty("HELLO_MESSAGE");

        } catch (FileNotFoundException ex) {
            System.err.println("Properties config file not found");
        } catch (IOException ex) {
            System.err.println("Error while reading file");
        } finally {
            try {
                propertiesFile.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
