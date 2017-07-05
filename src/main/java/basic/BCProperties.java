package basic;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class BCProperties  {
	
	// Set Logger
	Logger LOG = Logger.getLogger(BCProperties.class) ;

	
	static Properties properties = new Properties() ; ;
	static boolean isLoaded = false ;
	
	/**
	 * Initialize Properties (only once in program
	 */
	public static void loadProperties() {
		if (!isLoaded) {
			new BCProperties() ;
			isLoaded = true ;
		}
	}
	private BCProperties() {
		
		// Load Properties
		String fileName = System.getProperty("properties.file", System.getProperty("user.dir") + "/bcanalysis.properties") ;
		LOG.debug("Reading properties from " + fileName);

		InputStream is=null;
		try {
			is = new FileInputStream(fileName);
			properties.load(is);
			is.close();
		} catch (Exception e) {
			LOG.error("Error in loading Properties", e);
			System.exit(1);
		}
		if (is != null) {
			LOG.debug("Properties file found");
		}
		else {
			LOG.debug("No properties file found. Continuing with defaults");
		}

	}
	
	/**
	 * Get Property.
	 * @param key
	 * @return Property value
	 */
	public static String getProperty(String key) {
		return properties.getProperty(key) ;
	}
	
	/**
	 * Get Property with default.
	 * @param key
	 * @param defaultValue
	 * @return Property value or default if empty.
	 */
	public static String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue) ;
	}

}
