package melrief.hadoopconfmanager;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import melrief.hadoopconfmanager.converter.ConfigurationDescriptionConverter;

import org.apache.hadoop.conf.Configuration;

/**
 * This class simplify the configuration using
 * {@link org.apache.hadoop.conf.Configuration}. An instance
 * can be create using {@link ConfigurationManager#createFor(Object)}. Any
 * object can use an instance of this class parametrized on itself to:
 * <ul>
 * <li>declare how it can be configured using
 * {@link ConfigurationManager#addConfiguratorFor(Class, String, String, Object, Configurator)},
 * {@link ConfigurationManager#addConfiguratorForOrFalse(Class, String, String, Object, Configurator)}
 * or
 * {@link ConfigurationManager#addConfiguratorAndConfiguration(Configurator, ConfigurationDescription)}
 * </li>
 * <li>configure the target by passing a {@link Configuration} to
 * {@link ConfigurationManager#configure(Configuration)}</li>
 * <li>Use a {@link ConfigurationDescriptionConverter} to convert the
 * configuration descriptions to another format (plain text, XML)...</li>
 * </ul>
 * 
 * ConfigurationManager can be used in any class extending {@link Configurable}
 * by overriding the method {@link Configurable#setConf(Configuration)} and
 * adding a call to {@link ConfigurationManager#configure(Configuration)}:
 * 
 * <pre>
 * {@code
 * ConfigurationManager cm = ConfigurationManager.createFrom(this);
 * 
 * public void setConf(Configuration conf) {
 *  super.setConf(conf);
 *  if (conf != null) {
 *    this.cm.configure(this.getConf());
 *  }
 * }
 * }
 * </pre>
 * 
 * @param <O>
 *          the class to configure
 */
public class ConfigurationManager<O> {
  
  /** The object to configure */
  private O toConfigure;
  
  /** Where to print the warning if a configuration key is not set */
  private PrintStream stream;

  /** A list of configuration descriptions with their configurators */
  private HashSet<ConfiguratorConfiguration<?, O>> configuratorConfigurations = new HashSet<ConfiguratorConfiguration<?, O>>();

  /** Returns all the configuration descriptions */
  public HashSet<ConfigurationDescription<?>> getConfigurationDescriptions() {
    HashSet<ConfigurationDescription<?>> result = new HashSet<ConfigurationDescription<?>>();
    for (ConfiguratorConfiguration<?,O> cc : this.configuratorConfigurations) {
      result.add(cc.configuration);
    }
    return result;
  }

  private ConfigurationManager(O toConfigure, PrintStream stream) {
    this.toConfigure = toConfigure;
    this.stream = stream;
  }
  
  private ConfigurationManager(O toConfigure) {
    this(toConfigure, null);
  }

  /**
   * Creates a new configurator manager for the object to configure
   * 
   * @throws NullPointerException if toConfigure is null
   * @param toConfigure the object to configure
   * @param stream where to write warnings
   * @return the configuration manager instance
   */
  public static <O> ConfigurationManager<O> createFor(O toConfigure, PrintStream stream) {
    if (toConfigure == null) {
      throw new NullPointerException(
          "cannot create a configuration manager for null");
    }
    return new ConfigurationManager<O>(toConfigure, stream);
  }
  
  public static <O> ConfigurationManager<O> createFor(O toConfigure) {
    return ConfigurationManager.createFor(toConfigure, null);
  }

  /**
   * Apply each configuration separately to the object
   * 
   * @param the configuration to apply
   */
  public void configure(Configuration conf) {
    for (ConfiguratorConfiguration<?, O> configuratorConfiguration : this.configuratorConfigurations) {
      configuratorConfiguration.configure(this.toConfigure, conf);
    }
  }

  /**
   * Add a new configuration. This is a convenient version of
   * {@link ConfigurationManager#addConfiguratorAndConfiguration(Configurator, ConfigurationDescription)}
   * 
   * @param cls the class of the element to configure
   * @param key the key of the configuration
   * @param description the description of the configuration
   * @param defaultValue the default value of the configuration
   * @param configurator the configurator instance
   * 
   * @throws IllegalArgumentException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public <T> void addConfiguratorFor(FieldType<T> cls, String key,
      String description, T defaultValue, Configurator<T, O> configurator)
      throws IllegalArgumentException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    this.addConfiguratorAndConfiguration(configurator,
        ConfigurationDescription.from(cls, key, description, defaultValue, this.stream));
  }
  
  /**
   * Exception-free version of
   * {@link ConfigurationManager#addConfiguratorFor(FieldType, String, String, Object, Configurator)}
   * 
   * @return false if the configurator can't be added
   */
  public <T> boolean addConfiguratorForOrFalse(FieldType<T> cls, String key,
      String description, T defaultValue, Configurator<T, O> configurator) {
    try {
      this.addConfiguratorFor(cls, key, description, defaultValue, configurator);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    } catch (InstantiationException e) {
      return false;
    } catch (IllegalAccessException e) {
      return false;
    } catch (InvocationTargetException e) {
      return false;
    }
  }

  /**
   * Add a new configuration with a configuration description and
   * a configurator
   * 
   * @param configurator
   * @param configuration
   */
  public <T> void addConfiguratorAndConfiguration(
      Configurator<T, O> configurator, ConfigurationDescription<T> configuration) {
    this.configuratorConfigurations.add(new ConfiguratorConfiguration<T, O>(
        configuration, configurator));
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("Configuration keys for "
        + toConfigure.getClass() + ":");
    for (ConfiguratorConfiguration<?, ?> cc : this.configuratorConfigurations) {
      builder.append("\n").append(" ")
          .append(cc.configuration.toPrettyString());
    }
    return builder.toString();
  }
}

/** An utility class that encapsulate a configuration description and
 * a configurator and force them to be parametrized on the same type <T>
 */
class ConfiguratorConfiguration<T, O> {
  ConfigurationDescription<T> configuration;
  Configurator<T, O> configurator;

  public ConfiguratorConfiguration(ConfigurationDescription<T> configuration,
      Configurator<T, O> configurator) {
    this.configuration = configuration;
    this.configurator = configurator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((configuration == null) ? 0 : configuration.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    @SuppressWarnings("rawtypes")
    ConfiguratorConfiguration other = (ConfiguratorConfiguration) obj;
    if (configuration == null) {
      if (other.configuration != null)
        return false;
    } else if (!configuration.equals(other.configuration))
      return false;
    return true;
  }

  public void configure(O obj, Configuration conf) {
    this.configurator.configure(obj, this.configuration, conf);
  }
}
