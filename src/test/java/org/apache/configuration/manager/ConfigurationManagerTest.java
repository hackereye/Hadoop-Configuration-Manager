package org.apache.configuration.manager;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import melrief.hadoopconfmanager.ConfigurationDescription;
import melrief.hadoopconfmanager.ConfigurationManager;
import melrief.hadoopconfmanager.Configurator;
import melrief.hadoopconfmanager.ConstructorNotFoundException;
import melrief.hadoopconfmanager.FieldType;

import org.apache.hadoop.conf.Configuration;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ConfigurationManagerTest extends TestCase {
  public ConfigurationManagerTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(ConfigurationManagerTest.class);
  }

  static class MyIntegerContainer {
    MyInteger internalInteger;
  }

  static public class MyInteger {
    int i;

    public MyInteger(int i) {
      this.i = i;
    }

    public MyInteger() {
      this(0);
    }
  }

  static public class MyIntegerConfDescription extends
      ConfigurationDescription<MyInteger> {

    public MyIntegerConfDescription(String key, String description,
        MyInteger defaultValue, PrintStream stream) {
      super(key, description, defaultValue, stream);
    }

    @Override
    protected MyInteger get(Configuration conf) {
      return new MyInteger(conf.getInt(this.getKey(), this.getDefaultValue().i));
    }

  }

  // test register new field type
  public void testAddFieldType() throws IllegalArgumentException,
      InstantiationException, IllegalAccessException,
      InvocationTargetException, ConstructorNotFoundException {

    FieldType<MyInteger> fieldType = FieldType.registerNewConfiguration(
        MyInteger.class, MyIntegerConfDescription.class);

    MyIntegerContainer obj = new MyIntegerContainer();
    ConfigurationManager<MyIntegerContainer> manager = ConfigurationManager
        .createFor(obj);
    manager.addConfiguratorFor(fieldType, "i", "an field of type MyInteger",
        new MyInteger(0), new Configurator<MyInteger, MyIntegerContainer>() {
          protected void set(MyIntegerContainer obj, MyInteger value) {
            obj.internalInteger = value;
          }
        });
    Configuration conf = new Configuration();
    conf.setInt("i", 1);
    manager.configure(conf);
    assertTrue(obj.internalInteger.i == 1);
  }

  static public class MyIntegerConfDescriptionWrong extends
      ConfigurationDescription<MyInteger> {

    public MyIntegerConfDescriptionWrong() {
      super(null, null, null, null);
    }

    @Override
    protected MyInteger get(Configuration conf) {
      return new MyInteger(conf.getInt(this.getKey(), this.getDefaultValue().i));
    }

  }

  public void testAddWrongFieldType() {
    try {
      FieldType<MyInteger> fieldType = FieldType.registerNewConfiguration(
          MyInteger.class, MyIntegerConfDescriptionWrong.class);
      Assert.fail("Invalid constructor accepted for "
          + MyIntegerConfDescriptionWrong.class.getCanonicalName());
    } catch (ConstructorNotFoundException ce) {
    }
  }

  static class TestClass {
    final static boolean DEFAULT_B = false;
    boolean b;

    final static double DEFAULT_D = 0d;
    double d;

    final static float DEFAULT_F = 0f;
    float f;

    final static int DEFAULT_I = 0;
    int i;

    final static long DEFAULT_L = 0l;
    long l;

    ConfigurationManager<TestClass> configurator;

    public TestClass() throws IllegalArgumentException, InstantiationException,
        IllegalAccessException, InvocationTargetException {
      this.configurator = ConfigurationManager.createFor(this);
      this.configurator.addConfiguratorFor(FieldType.Boolean, "b",
          "a boolean configuration", DEFAULT_B,
          new Configurator<Boolean, TestClass>() {
            protected void set(TestClass obj, Boolean value) {
              obj.b = value;
            }
          });
      this.configurator.addConfiguratorFor(FieldType.Double, "d",
          "a double configuration", DEFAULT_D,
          new Configurator<Double, TestClass>() {
            protected void set(TestClass obj, Double value) {
              obj.d = value;
            }
          });
      this.configurator.addConfiguratorFor(FieldType.Float, "f",
          "a float configuration", DEFAULT_F,
          new Configurator<Float, TestClass>() {
            protected void set(TestClass obj, Float value) {
              obj.f = value;
            }
          });
      this.configurator.addConfiguratorFor(FieldType.Integer, "i",
          "a int configuration", DEFAULT_I,
          new Configurator<Integer, TestClass>() {
            protected void set(TestClass obj, Integer value) {
              obj.i = value;
            }
          });
      this.configurator.addConfiguratorFor(FieldType.Long, "l",
          "a long configuration", DEFAULT_L,
          new Configurator<Long, TestClass>() {
            protected void set(TestClass obj, Long value) {
              obj.l = value;
            }
          });
    }
  }

  // test default configuration and filled configuration
  public void testClassConfiguration() throws IllegalArgumentException,
      InstantiationException, IllegalAccessException, InvocationTargetException {
    Configuration conf = new Configuration();
    TestClass obj = new TestClass();
    obj.configurator.configure(conf);

    assertTrue(obj.b == TestClass.DEFAULT_B);
    assertTrue(obj.d == TestClass.DEFAULT_D);
    assertTrue(obj.f == TestClass.DEFAULT_F);
    assertTrue(obj.i == TestClass.DEFAULT_I);
    assertTrue(obj.l == TestClass.DEFAULT_L);

    conf.setBoolean("b", true);
    conf.set("d", String.valueOf(1d));
    conf.setFloat("f", 1f);
    conf.setInt("i", 1);
    conf.setLong("l", 1l);

    obj.configurator.configure(conf);

    assertTrue(obj.b);
    assertTrue(obj.d == 1);
    assertTrue(obj.f == 1f);
    assertTrue(obj.i == 1);
    assertTrue(obj.l == 1l);
  }

}
