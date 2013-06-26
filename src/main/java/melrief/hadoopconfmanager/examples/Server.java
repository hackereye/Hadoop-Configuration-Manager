package melrief.hadoopconfmanager.examples;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import melrief.hadoopconfmanager.ConfigurationManager;
import melrief.hadoopconfmanager.Configurator;
import melrief.hadoopconfmanager.FieldType;
import melrief.hadoopconfmanager.converter.ConfigurationDescriptionConverter;
import melrief.hadoopconfmanager.converter.ConfigurationDescriptionToStringConverter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

public class Server extends Configured {

  /** Max number of connections accepted by the server */
  int maxConnections;
  
  /** Use or not a persistent connection */
  boolean keepAlive;
  
  /** The configuration manager */
  final ConfigurationManager<Server> confManager;
  
  public Server() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
    confManager = ConfigurationManager.createFor(this, System.err);
    confManager.addConfiguratorFor(
        FieldType.Integer
      , "maxConnections"
      , "Maximum number of connections accepted by the server"
      , 8
      , new Configurator<Integer, Server>() {
          protected void set(Server obj, Integer value) {
            obj.maxConnections = value;
          }
        });
    confManager.addConfiguratorFor(
        FieldType.Boolean
      , "keepAlive"
      , "Whether or not to allow persistent connections"
      , false
      , new Configurator<Boolean, Server>() {
          protected void set(Server obj, Boolean value) {
              obj.keepAlive = value;
          }
        });
  }
  
  /**
   * Set the configuration using {@link Configuration} API and then
   * configure this using its configuration manager
   */
  @Override
  public void setConf(Configuration configuration) {
    super.setConf(configuration);
    
    if (this.confManager != null) {
      this.confManager.configure(configuration);
    }
  }
  
  @Override
  public String toString() {
    return "Server(keepAlive: " + this.keepAlive 
                + ", maxConnections: " + this.maxConnections + ")";
  }
  
  public static void main(String[] args) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
    
    // create a new server
    Server server = new Server();
    
    // print the configuration descriptions
    ConfigurationDescriptionConverter converter = new ConfigurationDescriptionToStringConverter();
    converter.convert(server.confManager);
    System.out.println("Server configuration is:");
    converter.write(System.out);
    
    // print the current state of server
    System.out.println("Before configuration: " + server);
    
    // create a new configuration
    Configuration configuration = new Configuration();
    
    // set the empty configuration in server
    server.setConf(configuration);
    
    // print the new state of server: all fields have default values
    System.out.println("After empty configuration is set: " + server);
    
    // set the fields in the configuration
    configuration.setBoolean("keepAlive", true);
    configuration.setInt("maxConnections", 16);
    
    // set the new configuration in server
    server.setConf(configuration);
    
    // print the new state of server
    System.out.println("After configuration: " + server);
    
    System.exit(0);
  }
}
