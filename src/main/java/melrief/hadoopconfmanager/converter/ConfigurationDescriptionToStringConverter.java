package melrief.hadoopconfmanager.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;

import melrief.hadoopconfmanager.ConfigurationDescription;


public class ConfigurationDescriptionToStringConverter extends
    ConfigurationDescriptionConverter {

  HashSet<String> keys;
  StringBuilder builder;
  
  public ConfigurationDescriptionToStringConverter() {
    this.keys = new HashSet<String>();
    this.builder = new StringBuilder();
  }

  @Override
  public boolean addConfigurationDescription(ConfigurationDescription<?> conf) {
    String key = conf.getKey();
    
    if (keys.contains(key)) {
      return false;
    }
    
    keys.add(key);
    
    builder.append(key).append(" type: ").append(conf.getType())
           .append(" default: ").append(conf.getDefaultValue())
           .append(" description: ").append(conf.getDescription())
           .append(System.lineSeparator());
    
    return true;
  }

  @Override
  public void write(OutputStream stream) throws IOException {
    stream.write(builder.toString().getBytes());
  }
}
