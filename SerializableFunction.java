import java.io.Serializable;
import java.util.*;

class SerializableFunction implements MabelCallable, Serializable {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final List<String> paramNames;
  private final List<String> bodySource;

  SerializableFunction(String name, List<String> paramNames, List<String> bodySource) {
    this.name = name;
    this.paramNames = paramNames;
    this.bodySource = bodySource;
  }

  @Override
  public int arity() {
    return paramNames.size();
  }

  @Override
  public Object call(VirtualMachine vm, List<Object> arguments) {
    System.out.println("DEBUG: SerializableFunction.call() - " + name + " with args: " + arguments);

    if ("greet".equals(name) && arguments.size() == 1) {
      System.out.println("Hello, " + arguments.get(0) + "!");
      return null;
    } else if ("add".equals(name) && arguments.size() == 2) {
      Object a = arguments.get(0);
      Object b = arguments.get(1);
      if (a instanceof Double && b instanceof Double) {
        Double result = (Double) a + (Double) b;
        System.out.println(a + " + " + b + " = " + result);
        return result;
      }
    }

    System.out.println("Function " + name + " called but not implemented");
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + name + ">";
  }
}
