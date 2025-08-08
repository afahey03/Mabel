import java.util.*;

class MabelClass implements MabelCallable {
  final String name;
  final MabelClass superclass;
  private final Map<String, MabelFunction> methods;

  MabelClass(String name, MabelClass superclass, Map<String, MabelFunction> methods) {
    this.name = name;
    this.superclass = superclass;
    this.methods = methods;
  }

  MabelFunction findMethod(String name) {
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
    return name;
  }

  @Override
  public Object call(VirtualMachine vm, List<Object> arguments) {
    MabelInstance instance = new MabelInstance(this);
    MabelFunction initializer = findMethod("init");
    if (initializer != null) {
      initializer.bind(instance).call(vm, arguments);
    }
    return instance;
  }

  @Override
  public int arity() {
    MabelFunction initializer = findMethod("init");
    if (initializer == null)
      return 0;
    return initializer.arity();
  }
}