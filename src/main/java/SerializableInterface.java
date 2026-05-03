import java.io.Serializable;
import java.util.*;

class SerializableInterface implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final List<String> requiredMethods;

  SerializableInterface(String name, List<String> requiredMethods) {
    this.name = name;
    this.requiredMethods = requiredMethods;
  }

  public String getName() {
    return name;
  }

  public List<String> getRequiredMethods() {
    return new ArrayList<>(requiredMethods);
  }

  public boolean isImplementedBy(SerializableClass klass) {
    for (String method : requiredMethods) {
      if (klass.findMethod(method) == null) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return "<interface " + name + ">";
  }
}