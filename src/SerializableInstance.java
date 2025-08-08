import java.io.Serializable;
import java.util.*;

class SerializableInstance implements Serializable {
  private static final long serialVersionUID = 1L;

  private final SerializableClass klass;
  private final Map<String, Object> fields;

  SerializableInstance(SerializableClass klass) {
    this.klass = klass;
    this.fields = new HashMap<>();
  }

  Object get(String name) {
    // Check fields first
    if (fields.containsKey(name)) {
      return fields.get(name);
    }

    // Then check for methods
    SerializableFunction method = klass.findMethod(name);
    if (method != null) {
      // Return a bound method (method with 'this' already set)
      return new BoundMethod(this, method);
    }

    throw new RuntimeException("Undefined property '" + name + "'.");
  }

  void set(String name, Object value) {
    fields.put(name, value);
  }

  @Override
  public String toString() {
    return klass.getName() + " instance";
  }

  // Helper class for bound methods
  static class BoundMethod implements MabelCallable, Serializable {
    private static final long serialVersionUID = 1L;

    private final SerializableInstance instance;
    private final SerializableFunction method;

    BoundMethod(SerializableInstance instance, SerializableFunction method) {
      this.instance = instance;
      this.method = method;
    }

    @Override
    public int arity() {
      return method.arity();
    }

    @Override
    public Object call(VirtualMachine vm, List<Object> arguments) {
      return method.callAsMethod(instance, vm, arguments);
    }

    @Override
    public String toString() {
      return "<bound " + method + ">";
    }
  }
}