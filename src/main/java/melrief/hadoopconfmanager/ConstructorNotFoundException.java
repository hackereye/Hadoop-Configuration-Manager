package melrief.hadoopconfmanager;

public class ConstructorNotFoundException 
  extends RuntimeException {

  public ConstructorNotFoundException(Class<?> confDescription) {
    super("Cannot find suitable constructor for " + confDescription.getCanonicalName());
  }

}
