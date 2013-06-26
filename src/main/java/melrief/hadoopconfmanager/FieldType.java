package melrief.hadoopconfmanager;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import melrief.hadoopconfmanager.ConfigurationDescription.BooleanConfiguration;
import melrief.hadoopconfmanager.ConfigurationDescription.ClassConfiguration;
import melrief.hadoopconfmanager.ConfigurationDescription.DoubleConfiguration;
import melrief.hadoopconfmanager.ConfigurationDescription.FloatConfiguration;
import melrief.hadoopconfmanager.ConfigurationDescription.IntConfiguration;
import melrief.hadoopconfmanager.ConfigurationDescription.IntegerRangesConfiguration;
import melrief.hadoopconfmanager.ConfigurationDescription.LongConfiguration;
import melrief.hadoopconfmanager.ConfigurationDescription.StringConfiguration;

import org.apache.hadoop.conf.Configuration.IntegerRanges;

/**
 * The types of the field that can be configured using
 * a {@link ConfigurationManager} should be registered to this class using
 * {@link FieldType#registerNewConfiguration(Class, Class)}. Note that the
 * second class must have a constructor with the signature
 * {@link ConfigurationDescription#ConfigurationDescription(String, String, Object, PrintStream)}
 * or the static method will throw a {@link ConstructorNotFoundException}
 */
public class FieldType<T> {

  static HashMap<FieldType<?>, Constructor<? extends ConfigurationDescription<?>>> registeredClasses = new HashMap<FieldType<?>, Constructor<? extends ConfigurationDescription<?>>>();
  
  public final static FieldType<Boolean> Boolean = registerNewConfiguration(
      Boolean.class, BooleanConfiguration.class);
  public final static FieldType<Class> Class = registerNewConfiguration(
      Class.class, ClassConfiguration.class);
  public final static FieldType<Double> Double = registerNewConfiguration(
      Double.class, DoubleConfiguration.class);
  public final static FieldType<Integer> Integer = registerNewConfiguration(
      Integer.class, IntConfiguration.class);
  public final static FieldType<IntegerRanges> IntegerRanges = registerNewConfiguration(
      IntegerRanges.class, IntegerRangesConfiguration.class);
  public final static FieldType<Long> Long = registerNewConfiguration(
      Long.class, LongConfiguration.class);
  public final static FieldType<Float> Float = registerNewConfiguration(
      Float.class, FloatConfiguration.class);
  public final static FieldType<String> String = registerNewConfiguration(
      String.class, StringConfiguration.class);
  
  /**
   * Register a type and the type of its configuration description
   * 
   * @param cls the field type
   * @param confDescription the description type based on the field type
   * @return an instance of FieldType for the type passed as parameter
   * @throws ConstructorNotFoundException if the constructor is not found 
   */
  public static <T1> FieldType<T1> registerNewConfiguration(Class<T1> cls,
      Class<? extends ConfigurationDescription<T1>> confDescription) {
    if (cls == null || confDescription == null) {
      throw new NullPointerException();
    }
    
    Constructor<?>[] constructors = confDescription.getConstructors();
    Constructor<?> constructor = null;
    for (Constructor<?> current : constructors) {
      Class<?>[] parameterTypes = current.getParameterTypes();
      if (parameterTypes.length != 4 
          || parameterTypes[0] != String.class
          || parameterTypes[1] != String.class
          || parameterTypes[3] != PrintStream.class) {
        continue;
      } else {
        constructor = current;
        break;
      }
    }
    
    if (constructor == null) {
      throw new ConstructorNotFoundException(confDescription);
    }
    
    FieldType<T1> cc = new FieldType<T1>(cls);
    registeredClasses.put(cc, (Constructor<? extends ConfigurationDescription<?>>) constructor);
    return cc;
  }
  
  private Class<T> cls;

  private FieldType(Class<T> cls) {
    this.cls = cls;
  }
}
