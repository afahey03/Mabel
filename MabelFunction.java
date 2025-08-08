import java.io.Serializable;
import java.util.*;

class MabelFunction implements MabelCallable, Serializable {
  private static final long serialVersionUID = 1L;

  private final Stmt.Function declaration;
  private final Environment closure;
  private final boolean isInitializer;

  MabelFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
    this.declaration = declaration;
    this.closure = closure;
    this.isInitializer = isInitializer;
  }

  MabelFunction bind(MabelInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new MabelFunction(declaration, environment, isInitializer);
  }

  @Override
  public String toString() {
    if (declaration == null)
      return "<native fn>";
    return "<fn " + declaration.name.lexeme + ">";
  }

  @Override
  public int arity() {
    if (declaration == null)
      return 0;
    return declaration.params.size();
  }

  @Override
  public Object call(VirtualMachine vm, List<Object> arguments) {
    if (declaration == null)
      return null;

    System.out.println("DEBUG: Function " + declaration.name.lexeme + " called with args: " + arguments);

    // Create new environment for function execution with access to globals
    Environment globalEnv = new Environment();

    // Add built-ins to the environment
    Map<String, Object> vmGlobals = vm.getGlobals();
    globalEnv.define("print", vmGlobals.get("print"));
    globalEnv.define("len", vmGlobals.get("len"));
    globalEnv.define("str", vmGlobals.get("str"));
    globalEnv.define("num", vmGlobals.get("num"));

    Environment environment = new Environment(globalEnv);

    // Bind parameters to arguments
    if (declaration.params != null && arguments != null) {
      for (int i = 0; i < declaration.params.size(); i++) {
        if (i < arguments.size()) {
          String paramName = declaration.params.get(i).lexeme;
          Object argValue = arguments.get(i);
          System.out.println("DEBUG: Binding parameter '" + paramName + "' to value: " + argValue);
          environment.define(paramName, argValue);
        }
      }
    }

    try {
      // Execute function body
      System.out.println("DEBUG: Executing function body...");
      vm.executeBlock(declaration.body, environment);
    } catch (ReturnValue returnValue) {
      System.out.println("DEBUG: Function returned: " + returnValue.value);
      if (isInitializer)
        return closure != null ? closure.getAt(0, "this") : null;
      return returnValue.value;
    }

    if (isInitializer)
      return closure != null ? closure.getAt(0, "this") : null;
    System.out.println("DEBUG: Function completed normally");
    return null;
  }
}