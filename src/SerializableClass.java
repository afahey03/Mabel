import java.io.Serializable;
import java.util.*;

class SerializableClass implements MabelCallable, Serializable {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final String superclassName;
  private transient SerializableClass superclass;
  private final Map<String, SerializableFunction> methods;
  private final Map<String, Object> defaultFieldValues;

  SerializableClass(String name, String superclassName,
      Map<String, SerializableFunction> methods,
      Map<String, Object> defaultFieldValues) {
    this.name = name;
    this.superclassName = superclassName;
    this.superclass = null;
    this.methods = methods;
    this.defaultFieldValues = defaultFieldValues;
  }

  static SerializableClass createWithoutSuperclass(String name,
      Map<String, SerializableFunction> methods,
      Map<String, Object> defaultFieldValues) {
    return new SerializableClass(name, null, methods, defaultFieldValues);
  }

  public void resolveSuperclass(Map<String, Object> globals) {
    if (superclassName != null && superclass == null) {
      Object superObj = globals.get(superclassName);
      if (superObj instanceof SerializableClass) {
        this.superclass = (SerializableClass) superObj;
        this.superclass.resolveSuperclass(globals);
      } else if (superObj != null) {
        throw new RuntimeException("Superclass must be a class, not " +
            superObj.getClass().getSimpleName());
      }
    }
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

  public Map<String, Object> getAllFieldDefaults() {
    Map<String, Object> allDefaults = new HashMap<>();

    if (superclass != null) {
      allDefaults.putAll(superclass.getAllFieldDefaults());
    }

    allDefaults.putAll(defaultFieldValues);

    return allDefaults;
  }

  @Override
  public String toString() {
    String result = "<class " + name;
    if (superclassName != null) {
      result += " extends " + superclassName;
    }
    result += ">";
    return result;
  }

  @Override
  public Object call(VirtualMachine vm, List<Object> arguments) {
    if (superclassName != null && superclass == null) {
      resolveSuperclass(vm.getGlobals());
    }

    SerializableInstance instance = new SerializableInstance(this);

    for (Map.Entry<String, Object> entry : getAllFieldDefaults().entrySet()) {
      instance.set(entry.getKey(), entry.getValue());
    }

    SerializableFunction initializer = findMethod("init");
    if (initializer != null) {
      initializer.callAsMethod(instance, vm, arguments);
    } else if (arguments.size() != 0) {
      if (superclass != null) {
        SerializableFunction superInit = superclass.findMethod("init");
        if (superInit != null) {
          superInit.callAsMethod(instance, vm, arguments);
        } else {
          throw new RuntimeException("Expected 0 arguments but got " + arguments.size() + ".");
        }
      } else {
        throw new RuntimeException("Expected 0 arguments but got " + arguments.size() + ".");
      }
    }

    return instance;
  }

  @Override
  public int arity() {
    SerializableFunction initializer = findMethod("init");
    if (initializer == null && superclass != null) {
      return superclass.arity();
    }
    if (initializer == null)
      return 0;
    return initializer.arity();
  }

  public String getName() {
    return name;
  }

  public SerializableClass getSuperclass() {
    return superclass;
  }

  public Map<String, Object> getDefaultFieldValues() {
    return new HashMap<>(defaultFieldValues);
  }
}