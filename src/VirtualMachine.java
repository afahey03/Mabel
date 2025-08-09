import java.util.*;

class VirtualMachine {
    private final Chunk chunk;
    private int ip = 0;
    private final List<Object> stack = new ArrayList<>();
    private final Map<String, Object> globals = new HashMap<>();

    public VirtualMachine(Chunk chunk) {
        this.chunk = chunk;
        defineBuiltins();
    }

    private void defineBuiltins() {
        globals.put("len", new MabelBuiltin("len", 1) {
            @Override
            public Object call(List<Object> args) {
                Object arg = args.get(0);
                if (arg instanceof String) {
                    return (double) ((String) arg).length();
                } else if (arg instanceof List) {
                    return (double) ((List<?>) arg).size();
                }
                throw new RuntimeException("'len' can only be applied to strings and arrays");
            }
        });

        globals.put("str", new MabelBuiltin("str", 1) {
            @Override
            public Object call(List<Object> args) {
                return stringify(args.get(0));
            }
        });

        globals.put("num", new MabelBuiltin("num", 1) {
            @Override
            public Object call(List<Object> args) {
                Object arg = args.get(0);
                if (arg instanceof String) {
                    try {
                        return Double.parseDouble((String) arg);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Cannot convert '" + arg + "' to number");
                    }
                } else if (arg instanceof Double) {
                    return arg;
                }
                throw new RuntimeException("Cannot convert " + arg.getClass().getSimpleName() + " to number");
            }
        });

        // New array built-ins
        globals.put("push", new MabelBuiltin("push", 2) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);
                Object item = args.get(1);
                if (!(arr instanceof List)) {
                    throw new RuntimeException("'push' can only be applied to arrays");
                }
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;
                list.add(item);
                return item;
            }
        });

        globals.put("pop", new MabelBuiltin("pop", 1) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);
                if (!(arr instanceof List)) {
                    throw new RuntimeException("'pop' can only be applied to arrays");
                }
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;
                if (list.isEmpty()) {
                    throw new RuntimeException("Cannot pop from empty array");
                }
                return list.remove(list.size() - 1);
            }
        });

        globals.put("shift", new MabelBuiltin("shift", 1) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);
                if (!(arr instanceof List)) {
                    throw new RuntimeException("'shift' can only be applied to arrays");
                }
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;
                if (list.isEmpty()) {
                    throw new RuntimeException("Cannot shift from empty array");
                }
                return list.remove(0);
            }
        });

        globals.put("unshift", new MabelBuiltin("unshift", 2) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);
                Object item = args.get(1);
                if (!(arr instanceof List)) {
                    throw new RuntimeException("'unshift' can only be applied to arrays");
                }
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;
                list.add(0, item);
                return item;
            }
        });

        globals.put("slice", new MabelBuiltin("slice", 3) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);
                Object startObj = args.get(1);
                Object endObj = args.get(2);

                if (!(arr instanceof List)) {
                    throw new RuntimeException("'slice' can only be applied to arrays");
                }
                if (!(startObj instanceof Double)) {
                    throw new RuntimeException("'slice' start index must be a number");
                }
                if (!(endObj instanceof Double)) {
                    throw new RuntimeException("'slice' end index must be a number");
                }

                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;
                int start = ((Double) startObj).intValue();
                int end = ((Double) endObj).intValue();

                if (start < 0)
                    start = Math.max(0, list.size() + start);
                if (end < 0)
                    end = Math.max(0, list.size() + end);

                start = Math.max(0, Math.min(start, list.size()));
                end = Math.max(0, Math.min(end, list.size()));

                if (start > end) {
                    return new ArrayList<>();
                }

                return new ArrayList<>(list.subList(start, end));
            }
        });

        globals.put("indexOf", new MabelBuiltin("indexOf", 2) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);
                Object item = args.get(1);

                if (!(arr instanceof List)) {
                    throw new RuntimeException("'indexOf' can only be applied to arrays");
                }

                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;
                int index = list.indexOf(item);
                return (double) index;
            }
        });

        globals.put("contains", new MabelBuiltin("contains", 2) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);
                Object item = args.get(1);

                if (!(arr instanceof List)) {
                    throw new RuntimeException("'contains' can only be applied to arrays");
                }

                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;
                return list.contains(item);
            }
        });

        globals.put("reverse", new MabelBuiltin("reverse", 1) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);

                if (!(arr instanceof List)) {
                    throw new RuntimeException("'reverse' can only be applied to arrays");
                }

                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;
                Collections.reverse(list);
                return list;
            }
        });

        globals.put("sort", new MabelBuiltin("sort", 1) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);

                if (!(arr instanceof List)) {
                    throw new RuntimeException("'sort' can only be applied to arrays");
                }

                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;

                list.sort((a, b) -> {
                    if (a instanceof Double && b instanceof Double) {
                        return Double.compare((Double) a, (Double) b);
                    } else if (a instanceof String && b instanceof String) {
                        return ((String) a).compareTo((String) b);
                    } else {
                        if (a instanceof Double && b instanceof String)
                            return -1;
                        if (a instanceof String && b instanceof Double)
                            return 1;
                        return 0;
                    }
                });

                return list;
            }
        });

        globals.put("clear", new MabelBuiltin("clear", 1) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);

                if (!(arr instanceof List)) {
                    throw new RuntimeException("'clear' can only be applied to arrays");
                }

                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;
                list.clear();
                return null;
            }
        });

        globals.put("copy", new MabelBuiltin("copy", 1) {
            @Override
            public Object call(List<Object> args) {
                Object arr = args.get(0);

                if (!(arr instanceof List)) {
                    throw new RuntimeException("'copy' can only be applied to arrays");
                }

                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) arr;
                return new ArrayList<>(list);
            }
        });
    }

    public void run() {
        while (ip < chunk.size()) {
            byte instruction = chunk.get(ip++);
            OpCode op = OpCode.values()[instruction];

            switch (op) {
                case CONSTANT:
                    Object constant = chunk.getConstant(Byte.toUnsignedInt(chunk.get(ip++)));
                    // System.out.println("DEBUG: Loading constant: " + constant + " (type: " +
                    // (constant == null ? "null" : constant.getClass().getSimpleName()) + ")");
                    push(constant);
                    break;

                case NIL:
                    push(null);
                    break;

                case TRUE:
                    push(true);
                    break;

                case FALSE:
                    push(false);
                    break;

                case POP:
                    pop();
                    break;

                case GET_GLOBAL: {
                    String name = (String) chunk.getConstant(Byte.toUnsignedInt(chunk.get(ip++)));
                    Object value = globals.get(name);
                    if (value == null) {
                        throw new RuntimeException("Undefined variable '" + name + "'.");
                    }
                    push(value);
                    break;
                }

                case DEFINE_GLOBAL: {
                    String name = (String) chunk.getConstant(Byte.toUnsignedInt(chunk.get(ip++)));
                    Object value = peek();
                    // System.out.println("DEBUG: Defining global '" + name + "' = " + value +
                    // " (type: " + (value == null ? "null" : value.getClass().getSimpleName()) +
                    // ")");
                    globals.put(name, value);
                    pop();
                    break;
                }

                case SET_GLOBAL: {
                    String name = (String) chunk.getConstant(Byte.toUnsignedInt(chunk.get(ip++)));
                    if (!globals.containsKey(name)) {
                        throw new RuntimeException("Undefined variable '" + name + "'.");
                    }
                    globals.put(name, peek());
                    break;
                }

                case EQUAL: {
                    Object b = pop();
                    Object a = pop();
                    push(isEqual(a, b));
                    break;
                }

                case GREATER: {
                    Object b = pop();
                    Object a = pop();
                    if (!(a instanceof Double && b instanceof Double)) {
                        throw new RuntimeException("Operands must be numbers.");
                    }
                    push((Double) a > (Double) b);
                    break;
                }

                case LESS: {
                    Object b = pop();
                    Object a = pop();
                    if (!(a instanceof Double && b instanceof Double)) {
                        throw new RuntimeException("Operands must be numbers.");
                    }
                    push((Double) a < (Double) b);
                    break;
                }

                case ADD: {
                    Object b = pop();
                    Object a = pop();
                    if (a instanceof Double && b instanceof Double) {
                        push((Double) a + (Double) b);
                    } else if (a instanceof String && b instanceof String) {
                        push((String) a + (String) b);
                    } else if (a instanceof String || b instanceof String) {
                        push(stringify(a) + stringify(b));
                    } else if (a instanceof List && b instanceof List) {
                        List<Object> result = new ArrayList<>();
                        result.addAll((List<?>) a);
                        result.addAll((List<?>) b);
                        push(result);
                    } else if (a instanceof List) {
                        List<Object> result = new ArrayList<>((List<?>) a);
                        result.add(b);
                        push(result);
                    } else if (b instanceof List) {
                        List<Object> result = new ArrayList<>();
                        result.add(a);
                        result.addAll((List<?>) b);
                        push(result);
                    } else {
                        throw new RuntimeException("Operands must be two numbers, two strings, or arrays.");
                    }
                    break;
                }

                case SUBTRACT: {
                    Object b = pop();
                    Object a = pop();
                    if (!(a instanceof Double && b instanceof Double)) {
                        throw new RuntimeException("Operands must be numbers.");
                    }
                    push((Double) a - (Double) b);
                    break;
                }

                case MULTIPLY: {
                    Object b = pop();
                    Object a = pop();
                    if (!(a instanceof Double && b instanceof Double)) {
                        throw new RuntimeException("Operands must be numbers.");
                    }
                    push((Double) a * (Double) b);
                    break;
                }

                case DIVIDE: {
                    Object b = pop();
                    Object a = pop();
                    if (!(a instanceof Double && b instanceof Double)) {
                        throw new RuntimeException("Operands must be numbers.");
                    }
                    if ((Double) b == 0.0) {
                        throw new RuntimeException("Division by zero.");
                    }
                    push((Double) a / (Double) b);
                    break;
                }

                case MODULO: {
                    Object b = pop();
                    Object a = pop();
                    if (!(a instanceof Double && b instanceof Double)) {
                        throw new RuntimeException("Operands must be numbers.");
                    }
                    push((Double) a % (Double) b);
                    break;
                }

                case NOT:
                    push(!isTruthy(pop()));
                    break;

                case NEGATE: {
                    Object operand = pop();
                    if (!(operand instanceof Double)) {
                        throw new RuntimeException("Operand must be a number.");
                    }
                    push(-(Double) operand);
                    break;
                }

                case PRINT:
                    System.out.println(stringify(pop()));
                    break;

                case JUMP: {
                    int offset = (Byte.toUnsignedInt(chunk.get(ip)) << 8) | Byte.toUnsignedInt(chunk.get(ip + 1));
                    ip += offset + 2;
                    break;
                }

                case JUMP_IF_FALSE: {
                    int offset = (Byte.toUnsignedInt(chunk.get(ip)) << 8) | Byte.toUnsignedInt(chunk.get(ip + 1));
                    ip += 2;
                    if (!isTruthy(peek()))
                        ip += offset;
                    break;
                }

                case LOOP: {
                    int offset = (Byte.toUnsignedInt(chunk.get(ip)) << 8) | Byte.toUnsignedInt(chunk.get(ip + 1));
                    ip -= offset - 2;
                    break;
                }

                case CALL: {
                    int argCount = Byte.toUnsignedInt(chunk.get(ip++));
                    Object callee = peek(0);

                    if (callee instanceof MabelBuiltin) {
                        MabelBuiltin builtin = (MabelBuiltin) callee;
                        if (argCount != builtin.arity()) {
                            throw new RuntimeException(
                                    "Expected " + builtin.arity() + " arguments but got " + argCount + ".");
                        }
                        pop();
                        List<Object> args = new ArrayList<>();
                        for (int i = 0; i < argCount; i++) {
                            args.add(0, pop());
                        }
                        Object result = builtin.call(args);
                        push(result);
                    } else if (callee instanceof MabelCallable) {
                        MabelCallable callable = (MabelCallable) callee;
                        if (argCount != callable.arity()) {
                            throw new RuntimeException(
                                    "Expected " + callable.arity() + " arguments but got " + argCount + ".");
                        }
                        pop();
                        List<Object> args = new ArrayList<>();
                        for (int i = 0; i < argCount; i++) {
                            args.add(0, pop());
                        }
                        Object result = callable.call(this, args);
                        push(result);
                    } else if (callee instanceof SerializableClass) {
                        SerializableClass klass = (SerializableClass) callee;
                        if (argCount != klass.arity()) {
                            throw new RuntimeException(
                                    "Expected " + klass.arity() + " arguments but got " + argCount + ".");
                        }
                        pop();
                        List<Object> args = new ArrayList<>();
                        for (int i = 0; i < argCount; i++) {
                            args.add(0, pop());
                        }
                        Object result = klass.call(this, args);
                        push(result);
                    } else if (callee instanceof SerializableInstance.BoundMethod) {
                        SerializableInstance.BoundMethod method = (SerializableInstance.BoundMethod) callee;
                        if (argCount != method.arity()) {
                            throw new RuntimeException(
                                    "Expected " + method.arity() + " arguments but got " + argCount + ".");
                        }
                        pop();
                        List<Object> args = new ArrayList<>();
                        for (int i = 0; i < argCount; i++) {
                            args.add(0, pop());
                        }
                        Object result = method.call(this, args);
                        push(result);
                    } else {
                        throw new RuntimeException("Can only call functions and classes. Got: " +
                                (callee == null ? "null" : callee.getClass().getSimpleName()));
                    }
                    break;
                }

                case ARRAY: {
                    int elementCount = Byte.toUnsignedInt(chunk.get(ip++));
                    List<Object> array = new ArrayList<>();
                    for (int i = 0; i < elementCount; i++) {
                        array.add(0, pop());
                    }
                    push(array);
                    break;
                }

                case INDEX_GET: {
                    Object index = pop();
                    Object object = pop();

                    if (object instanceof List && index instanceof Double) {
                        List<?> list = (List<?>) object;
                        int i = ((Double) index).intValue();
                        if (i < 0 || i >= list.size()) {
                            throw new RuntimeException("Array index out of bounds.");
                        }
                        push(list.get(i));
                    } else if (object instanceof String && index instanceof Double) {
                        String str = (String) object;
                        int i = ((Double) index).intValue();
                        if (i < 0 || i >= str.length()) {
                            throw new RuntimeException("String index out of bounds.");
                        }
                        push(String.valueOf(str.charAt(i)));
                    } else {
                        throw new RuntimeException("Invalid index operation.");
                    }
                    break;
                }

                case RETURN:
                    return;

                case GET_PROPERTY: {
                    Object object = pop();
                    String name = (String) chunk.getConstant(Byte.toUnsignedInt(chunk.get(ip++)));

                    if (object instanceof MabelInstance) {
                        MabelInstance instance = (MabelInstance) object;
                        try {
                            Object value = instance.get(new Token(TokenType.IDENTIFIER, name, null, 0));
                            push(value);
                        } catch (RuntimeException e) {
                            throw new RuntimeException("Undefined property '" + name + "'.");
                        }
                    } else if (object instanceof SerializableInstance) {
                        SerializableInstance instance = (SerializableInstance) object;
                        try {
                            Object value = instance.get(name);
                            push(value);
                        } catch (RuntimeException e) {
                            throw new RuntimeException("Undefined property '" + name + "'.");
                        }
                    } else {
                        throw new RuntimeException("Only instances have properties.");
                    }
                    break;
                }

                case SET_PROPERTY: {
                    Object object = pop();
                    String name = (String) chunk.getConstant(Byte.toUnsignedInt(chunk.get(ip++)));
                    Object value = pop();

                    if (object instanceof MabelInstance) {
                        MabelInstance instance = (MabelInstance) object;
                        instance.set(new Token(TokenType.IDENTIFIER, name, null, 0), value);
                        push(value);
                    } else if (object instanceof SerializableInstance) {
                        SerializableInstance instance = (SerializableInstance) object;
                        instance.set(name, value);
                        push(value);
                    } else {
                        throw new RuntimeException("Only instances have fields.");
                    }
                    break;
                }

                case CLASS: {
                    String name = (String) chunk.getConstant(Byte.toUnsignedInt(chunk.get(ip++)));
                    MabelClass klass = new MabelClass(name, null, new HashMap<>());
                    push(klass);
                    break;
                }

                case INDEX_SET: {
                    Object value = pop();
                    Object index = pop();
                    Object object = pop();

                    if (object instanceof List && index instanceof Double) {
                        @SuppressWarnings("unchecked")
                        List<Object> list = (List<Object>) object;
                        int i = ((Double) index).intValue();
                        if (i < 0 || i >= list.size()) {
                            throw new RuntimeException("Array index out of bounds.");
                        }
                        list.set(i, value);
                        push(value);
                    } else {
                        throw new RuntimeException("Invalid index set operation.");
                    }
                    break;
                }

                default:
                    throw new RuntimeException("Unknown opcode: " + op);
            }
        }

    }

    private void push(Object value) {
        stack.add(value);
    }

    private Object pop() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack underflow.");
        }
        return stack.remove(stack.size() - 1);
    }

    private Object peek() {
        return peek(0);
    }

    private Object peek(int distance) {
        return stack.get(stack.size() - 1 - distance);
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (Boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;
        return a.equals(b);
    }

    public String stringify(Object object) {
        if (object == null)
            return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        if (object instanceof List) {
            List<?> list = (List<?>) object;
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(stringify(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return object.toString();
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        for (Stmt statement : statements) {
            if (statement != null) {
                executeStatement(statement, environment);
            }
        }
    }

    public Map<String, Object> getGlobals() {
        return globals;
    }

    private void executeStatement(Stmt statement, Environment environment) {
        try {
            // System.out.println("DEBUG: Executing statement: " +
            // statement.getClass().getSimpleName());
            if (statement instanceof Stmt.Print) {
                Stmt.Print printStmt = (Stmt.Print) statement;
                Object value = evaluateExpression(printStmt.expression, environment);
                System.out.println(stringify(value));
            } else if (statement instanceof Stmt.Expression) {
                Stmt.Expression exprStmt = (Stmt.Expression) statement;
                evaluateExpression(exprStmt.expression, environment);
            } else if (statement instanceof Stmt.Var) {
                Stmt.Var varStmt = (Stmt.Var) statement;
                Object value = null;
                if (varStmt.initializer != null) {
                    value = evaluateExpression(varStmt.initializer, environment);
                }
                environment.define(varStmt.name.lexeme, value);
            } else if (statement instanceof Stmt.Return) {
                Stmt.Return returnStmt = (Stmt.Return) statement;
                Object value = null;
                if (returnStmt.value != null) {
                    value = evaluateExpression(returnStmt.value, environment);
                }
                throw new ReturnValue(value);
            }
        } catch (RuntimeException e) {
            System.err.println("Error executing statement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Object evaluateExpression(Expr expression, Environment environment) {
        if (expression instanceof Expr.Literal) {
            return ((Expr.Literal) expression).value;
        } else if (expression instanceof Expr.Variable) {
            Expr.Variable varExpr = (Expr.Variable) expression;
            return environment.get(varExpr.name);
        } else if (expression instanceof Expr.Binary) {
            Expr.Binary binExpr = (Expr.Binary) expression;
            Object left = evaluateExpression(binExpr.left, environment);
            Object right = evaluateExpression(binExpr.right, environment);

            switch (binExpr.operator.type) {
                case PLUS:
                    if (left instanceof Double && right instanceof Double) {
                        return (Double) left + (Double) right;
                    } else if (left instanceof String || right instanceof String) {
                        return stringify(left) + stringify(right);
                    }
                    break;
                case MINUS:
                    if (left instanceof Double && right instanceof Double) {
                        return (Double) left - (Double) right;
                    }
                    break;
                default:
                    break;
            }
        } else if (expression instanceof Expr.Call) {
            Expr.Call callExpr = (Expr.Call) expression;
            Object callee = evaluateExpression(callExpr.callee, environment);

            List<Object> arguments = new ArrayList<>();
            for (Expr arg : callExpr.arguments) {
                arguments.add(evaluateExpression(arg, environment));
            }

            if (callee instanceof MabelCallable) {
                return ((MabelCallable) callee).call(this, arguments);
            } else if (callee instanceof MabelBuiltin) {
                return ((MabelBuiltin) callee).call(arguments);
            }
        }

        return null;
    }
}