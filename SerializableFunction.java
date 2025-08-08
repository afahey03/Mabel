import java.io.Serializable;
import java.util.*;

class SerializableFunction implements MabelCallable, Serializable {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final List<String> paramNames;
  private final List<SerializableStatement> body;
  private static int callDepth = 0;

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
    if (callDepth >= 100) { // Change to whatever
      throw new RuntimeException("Stack overflow: recursion depth exceeded 100");
    }

    callDepth++;
    // System.out.println("DEBUG: [depth=" + callDepth + "] Calling " + name + "
    // with args: " + arguments);

    try {
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
        // System.out.println("DEBUG: [depth=" + callDepth + "] Binding " +
        // paramNames.get(i) + " = " + arguments.get(i));
        environment.define(paramNames.get(i), arguments.get(i));
      }

      Object result = null;
      for (int i = 0; i < body.size(); i++) {
        SerializableStatement stmt = body.get(i);
        if (stmt != null) {
          // System.out.println("DEBUG: [depth=" + callDepth + "] Executing statement " +
          // i + ": " + stmt.type);
          result = executeSerializableStatement(stmt, environment, vm);
        } else {
          // System.out.println("WARNING: [depth=" + callDepth + "] Skipping null
          // statement " + i);
        }
      }

      /*
       * System.out
       * .println("DEBUG: [depth=" + callDepth + "] Function " + name +
       * " completed normally, returning: " + result);
       */
      callDepth--;
      return result;

    } catch (ReturnValue returnValue) {
      // System.out.println("DEBUG: [depth=" + callDepth + "] Function " + name + "
      // returned: " + returnValue.value);
      callDepth--;
      return returnValue.value;
    } catch (Exception e) {
      callDepth--;
      throw e;
    }
  }

  private Object executeSerializableStatement(SerializableStatement stmt, Environment env, VirtualMachine vm) {
    if (stmt == null) {
      // System.out.println("DEBUG: executeSerializableStatement received null
      // statement");
      return null;
    }

    // System.out.println("DEBUG: Executing statement type: " + stmt.type);

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

      case "if":
        Object condition = evaluateSerializableExpression(stmt.condition, env, vm);
        // System.out.println("DEBUG: If condition result: " + condition + " (truthy: "
        // + isTruthy(condition) + ")");
        if (isTruthy(condition)) {
          if (stmt.thenBranch != null) {
            // System.out.println("DEBUG: Executing then branch: " + stmt.thenBranch.type);
            return executeSerializableStatement(stmt.thenBranch, env, vm);
          } else {
            System.out.println("ERROR: Then branch is null!");
          }
        } else if (stmt.elseBranch != null) {
          // System.out.println("DEBUG: Executing else branch: " + stmt.elseBranch.type);
          return executeSerializableStatement(stmt.elseBranch, env, vm);
        } else {
          // System.out.println("DEBUG: No else branch, condition was false");
        }
        return null;

      case "block":
        Object result = null;
        if (stmt.statements != null) {
          for (SerializableStatement blockStmt : stmt.statements) {
            if (blockStmt != null) { // Add null check
              result = executeSerializableStatement(blockStmt, env, vm);
            } else {
              System.out.println("WARNING: Skipping null statement in block");
            }
          }
        }
        return result;

      default:
        System.err.println("Unknown statement type: " + stmt.type);
        return null;
    }
  }

  private boolean isTruthy(Object object) {
    if (object == null)
      return false;
    if (object instanceof Boolean)
      return (Boolean) object;
    return true;
  }

  private Object evaluateSerializableExpression(SerializableExpression expr, Environment env, VirtualMachine vm) {
    switch (expr.type) {
      case "literal":
        return expr.value;

      case "variable":
        try {
          return env.get(expr.name);
        } catch (RuntimeException e) {
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
          case "<=":
            if (left instanceof Double && right instanceof Double) {
              return (Double) left <= (Double) right;
            }
            break;
          case ">=":
            if (left instanceof Double && right instanceof Double) {
              return (Double) left >= (Double) right;
            }
            break;
          case "<":
            if (left instanceof Double && right instanceof Double) {
              return (Double) left < (Double) right;
            }
            break;
          case ">":
            if (left instanceof Double && right instanceof Double) {
              return (Double) left > (Double) right;
            }
            break;
          case "==":
            return isEqual(left, right);
          case "!=":
            return !isEqual(left, right);
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

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null)
      return true;
    if (a == null)
      return false;
    return a.equals(b);
  }

  @Override
  public String toString() {
    return "<fn " + name + ">";
  }
}