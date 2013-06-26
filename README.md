# Hadoop Configuration Manager

Configuration Manager is a library that simplify and
automatize the usage of
[Hadoop Configuration](http://hadoop.apache.org/docs/stable/api/org/apache/hadoop/conf/Configuration.html).
The goal of this project are:

- use a *declarative style* to define the configuration of an object
- *easy to integrate* in existing projects, no need to change the class
  hierarchy and can be used with
[Configurable](http://hadoop.apache.org/docs/stable/api/org/apache/hadoop/conf/Configured.html)
- *default values* and *notifications* for missing keys
- *conversions* of the object expected configuration to any format, e.g. XML
  properties for hadoop configuration (_mapred-site.xml_, ...) or plain text

The file [Server.java](src/main/java/melrief/hadoopconfmanager/examples/Server.java)
contains an example of how to use it. The important part are:

```Java
/** Max number of connections accepted by the server */
int maxConnections;

/** Use or not a persistent connection */
boolean keepAlive;

/** Declare the configuration manager */
final ConfigurationManager<Server> confManager;
  
public Server() throws IllegalArgumentException
                     , InstantiationException
                     , IllegalAccessException
                     , InvocationTargetException {

    // instantiate the configuration manager and set the configurations
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
```
