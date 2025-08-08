import java.io.Serializable;
import java.util.*;

class SerializableClass implements MabelCallable, Serializable {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final SerializableClass superclass;
  private final Map<String, SerializableFunction> methods;
  private final Map<String, Object> defaultFieldValues;

  SerializableClass(String name, SerializableClass superclass,
      Map<String, SerializableFunction> methods,
      Map<String, Object> defaultFieldValues) {
    this.name = name;
    this.superclass = superclass;
    this.methods = methods;
    this.defaultFieldValues = defaultFieldValues;
  }

  SerializableFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }

    if (superclass != null) {
      return superclass.findMethod(name);
    }

    return null;
  }

  @Override
  public String toString() {
    return "<class " + name + ">";
  }

  @Override
  public Object call(VirtualMachine vm, List<Object> arguments) {
    // Create new instance
    SerializableInstance instance = new SerializableInstance(this);

    // Initialize fields with default values
    for (Map.Entry<String, Object> entry : defaultFieldValues.entrySet()) {
      instance.set(entry.getKey(), entry.getValue());
    }

    // Call constructor if it exists
    SerializableFunction initializer = findMethod("init");
    if (initializer != null) {
      // Bind 'this' to the instance for the constructor
      initializer.callAsMethod(instance, vm, arguments);
    } else if (arguments.size() != 0) {
      throw new RuntimeException("Expected 0 arguments but got " + arguments.size() + ".");
    }

    return instance;
  }

  @Override
  public int arity() {
    SerializableFunction initializer = findMethod("init");
    if (initializer == null)
      return 0;
    return initializer.arity();
  }

  public String getName() {
    return name;
  }

  public Map<String, Object> getDefaultFieldValues() {
    return new HashMap<>(defaultFieldValues);
  }
}