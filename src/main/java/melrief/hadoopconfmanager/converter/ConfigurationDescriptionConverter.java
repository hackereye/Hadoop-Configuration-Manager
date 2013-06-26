package melrief.hadoopconfmanager.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import melrief.hadoopconfmanager.ConfigurationDescription;
import melrief.hadoopconfmanager.ConfigurationManager;

/** */
public abstract class ConfigurationDescriptionConverter {

  public ConfigurationDescriptionConverter() {
    super();
  }

  public abstract boolean addConfigurationDescription(ConfigurationDescription<?> conf);

  /**
   * Try to add many configuration descriptions
   * 
   * @return a list of all the configuration not added
   */
  public List<ConfigurationDescription<?>> addConfigurationDescriptions(ConfigurationDescription<?>[] confs) {
    List<ConfigurationDescription<?>> notAdded = new ArrayList<ConfigurationDescription<?>>();
    for (ConfigurationDescription<?> conf : confs) {
      if (!this.addConfigurationDescription(conf)) {
        notAdded.add(conf);
      }
    }
    return notAdded;
  }

  public abstract void write(OutputStream stream) throws IOException;

  public void convert(ConfigurationManager<?> manager) {
    for(ConfigurationDescription<?> description :
                          manager.getConfigurationDescriptions()) {
      this.addConfigurationDescription(description);
    }
  }
}