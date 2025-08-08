import java.io.Serializable;
import java.util.*;

class SerializableFunction implements MabelCallable, Serializable {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final List<String> paramNames;
  private final List<SerializableStatement> body;

  SerializableFunction(String name, List<String> paramNames, List<SerializableStatement> body) {
    this.name = name;
    this.paramNames = paramNames;
    this.body = body;
  }

  @Override
  public int arity() {
    return paramNames.size();
  }

  @Override
  public Object call(VirtualMachine vm, List<Object> arguments) {
    Environment globalEnv = new Environment();

    Map<String, Object> vmGlobals = vm.getGlobals();
    for (String key : vmGlobals.keySet()) {
      Object value = vmGlobals.get(key);
      if (value instanceof MabelBuiltin) {
        globalEnv.define(key, value);
      }
    }

    Environment environment = new Environment(globalEnv);

    for (int i = 0; i < paramNames.size() && i < arguments.size(); i++) {
      environment.define(paramNames.get(i), arguments.get(i));
    }

    try {
      Object result = null;
      for (SerializableStatement stmt : body) {
        result = executeSerializableStatement(stmt, environment, vm);
      }
      return result;
    } catch (ReturnValue returnValue) {
      return returnValue.value;
    }
  }

  private Object executeSerializableStatement(SerializableStatement stmt, Environment env, VirtualMachine vm) {
    switch (stmt.type) {
      case "print":
        Object value = evaluateSerializableExpression(stmt.expression, env, vm);
        System.out.println(vm.stringify(value));
        return null;

      case "var":
        Object initValue = null;
        if (stmt.initializer != null) {
          initValue = evaluateSerializableExpression(stmt.initializer, env, vm);
        }
        env.define(stmt.name, initValue);
        return null;

      case "return":
        Object returnValue = null;
        if (stmt.expression != null) {
          returnValue = evaluateSerializableExpression(stmt.expression, env, vm);
        }
        throw new ReturnValue(returnValue);

      case "expression":
        return evaluateSerializableExpression(stmt.expression, env, vm);

      default:
        System.err.println("Unknown statement type: " + stmt.type);
        return null;
    }
  }

  private Object evaluateSerializableExpression(SerializableExpression expr, Environment env, VirtualMachine vm) {
    switch (expr.type) {
      case "literal":
        return expr.value;

      case "variable":
        try {
          return env.get(expr.name);
        } catch (RuntimeException e) {
          // Try globals
          Map<String, Object> globals = vm.getGlobals();
          if (globals.containsKey(expr.name)) {
            return globals.get(expr.name);
          }
          throw e;
        }

      case "binary":
        Object left = evaluateSerializableExpression(expr.left, env, vm);
        Object right = evaluateSerializableExpression(expr.right, env, vm);

        switch (expr.operator) {
          case "+":
            if (left instanceof Double && right instanceof Double) {
              return (Double) left + (Double) right;
            } else {
              return vm.stringify(left) + vm.stringify(right);
            }
          case "-":
            if (left instanceof Double && right instanceof Double) {
              return (Double) left - (Double) right;
            }
            break;
          case "*":
            if (left instanceof Double && right instanceof Double) {
              return (Double) left * (Double) right;
            }
            break;
          case "/":
            if (left instanceof Double && right instanceof Double) {
              return (Double) left / (Double) right;
            }
            break;
        }
        break;

      case "call":
        Object callee = evaluateSerializableExpression(expr.callee, env, vm);
        List<Object> args = new ArrayList<>();
        if (expr.arguments != null) {
          for (SerializableExpression arg : expr.arguments) {
            args.add(evaluateSerializableExpression(arg, env, vm));
          }
        }

        if (callee instanceof MabelCallable) {
          return ((MabelCallable) callee).call(vm, args);
        } else if (callee instanceof MabelBuiltin) {
          return ((MabelBuiltin) callee).call(args);
        }
        break;
    }

    return null;
  }

  @Override
  public String toString() {
    return "<fn " + name + ">";
  }
}
