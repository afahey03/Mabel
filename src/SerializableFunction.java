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
    return callAsMethod(null, vm, arguments);
  }

  public Object callAsMethod(SerializableInstance instance, VirtualMachine vm, List<Object> arguments) {
    if (callDepth >= 100) {
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
        if (value instanceof MabelBuiltin || value instanceof SerializableClass) {
          globalEnv.define(key, value);
        }
      }

      Environment environment = new Environment(globalEnv);

      if (instance != null) {
        environment.define("this", instance);
      }

      for (int i = 0; i < paramNames.size() && i < arguments.size(); i++) {
        // System.out.println("DEBUG: [depth=" + callDepth + "] Binding " +
        // paramNames.get(i) + " = " + arguments.get(i));
        environment.define(paramNames.get(i), arguments.get(i));
      }

      try {
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
      }
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

      case "while":
        while (isTruthy(evaluateSerializableExpression(stmt.condition, env, vm))) {
          if (stmt.body != null) {
            executeSerializableStatement(stmt.body, env, vm);
          }
        }
        return null;

      case "block":
        Object result = null;
        if (stmt.statements != null) {
          for (SerializableStatement blockStmt : stmt.statements) {
            if (blockStmt != null) {
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

      case "unary":
        Object operand = evaluateSerializableExpression(expr.right, env, vm);
        switch (expr.operator) {
          case "-":
            if (operand instanceof Double) {
              return -(Double) operand;
            }
            throw new RuntimeException("Operand must be a number.");
          case "!":
          case "not":
            return !isTruthy(operand);
          default:
            throw new RuntimeException("Unknown unary operator: " + expr.operator);
        }

      case "binary":
        Object left = evaluateSerializableExpression(expr.left, env, vm);
        Object right = evaluateSerializableExpression(expr.right, env, vm);

        switch (expr.operator) {
          case "+":
            if (left instanceof Double && right instanceof Double) {
              return (Double) left + (Double) right;
            } else if (left instanceof String || right instanceof String) {
              return vm.stringify(left) + vm.stringify(right);
            } else if (left instanceof List && right instanceof List) {
              List<Object> result = new ArrayList<>();
              result.addAll((List<?>) left);
              result.addAll((List<?>) right);
              return result;
            } else if (left instanceof List) {
              List<Object> result = new ArrayList<>((List<?>) left);
              result.add(right);
              return result;
            } else if (right instanceof List) {
              List<Object> result = new ArrayList<>();
              result.add(left);
              result.addAll((List<?>) right);
              return result;
            }
            break;
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
          case "%":
            if (left instanceof Double && right instanceof Double) {
              return (Double) left % (Double) right;
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
          case "and":
            return isTruthy(left) && isTruthy(right);
          case "or":
            return isTruthy(left) || isTruthy(right);
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
        } else if (callee instanceof SerializableInstance.BoundMethod) {
          return ((SerializableInstance.BoundMethod) callee).call(vm, args);
        }
        throw new RuntimeException("Can only call functions and methods.");

      case "get":
        Object object = evaluateSerializableExpression(expr.object, env, vm);
        if (object instanceof SerializableInstance) {
          return ((SerializableInstance) object).get(expr.name);
        }
        throw new RuntimeException("Only instances have properties.");

      case "set":
        Object obj = evaluateSerializableExpression(expr.object, env, vm);
        Object setValue = evaluateSerializableExpression(expr.right, env, vm);
        if (obj instanceof SerializableInstance) {
          ((SerializableInstance) obj).set(expr.name, setValue);
          return setValue;
        }
        throw new RuntimeException("Only instances have fields.");

      case "this":
        try {
          return env.get("this");
        } catch (RuntimeException e) {
          throw new RuntimeException("Cannot use 'this' outside a class.");
        }

      case "assign":
        Object assignValue = evaluateSerializableExpression(expr.right, env, vm);
        try {
          env.assign(new Token(TokenType.IDENTIFIER, expr.name, null, 0), assignValue);
        } catch (RuntimeException e) {
          vm.getGlobals().put(expr.name, assignValue);
        }
        return assignValue;

      case "array":
        List<Object> elements = new ArrayList<>();
        if (expr.elements != null) {
          for (SerializableExpression element : expr.elements) {
            elements.add(evaluateSerializableExpression(element, env, vm));
          }
        }
        return elements;

      case "index":
        Object array = evaluateSerializableExpression(expr.object, env, vm);
        Object index = evaluateSerializableExpression(expr.right, env, vm);
        if (array instanceof List && index instanceof Double) {
          List<?> list = (List<?>) array;
          int i = ((Double) index).intValue();
          if (i < 0 || i >= list.size()) {
            throw new RuntimeException("Array index out of bounds.");
          }
          return list.get(i);
        } else if (array instanceof String && index instanceof Double) {
          String str = (String) array;
          int i = ((Double) index).intValue();
          if (i < 0 || i >= str.length()) {
            throw new RuntimeException("String index out of bounds.");
          }
          return String.valueOf(str.charAt(i));
        }
        throw new RuntimeException("Invalid index operation.");

      case "indexSet":
        Object arr = evaluateSerializableExpression(expr.object, env, vm);
        Object idx = evaluateSerializableExpression(expr.right, env, vm);
        Object indexSetValue = evaluateSerializableExpression(expr.indexSetValue, env, vm);

        if (arr instanceof List && idx instanceof Double) {
          @SuppressWarnings("unchecked")
          List<Object> list = (List<Object>) arr;
          int i = ((Double) idx).intValue();
          if (i < 0 || i >= list.size()) {
            throw new RuntimeException("Array index out of bounds.");
          }
          list.set(i, indexSetValue);
          return indexSetValue;
        }
        throw new RuntimeException("Invalid index set operation.");
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