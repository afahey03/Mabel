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
    }

    public void run() {
        while (ip < chunk.size()) {
            byte instruction = chunk.get(ip++);
            OpCode op = OpCode.values()[instruction];

            switch (op) {
                case CONSTANT:
                    Object constant = chunk.getConstant(Byte.toUnsignedInt(chunk.get(ip++)));
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
                    globals.put(name, peek());
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
                    } else {
                        throw new RuntimeException("Operands must be two numbers or two strings.");
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

                    // The function should be on top of the stack, arguments below it
                    Object callee = peek(0); // Function is at the top

                    // Debug only for problematic calls
                    if (!(callee instanceof MabelBuiltin)) {
                        System.out.println("DEBUG: CALL error - argCount=" + argCount + ", callee=" + callee + " ("
                                + callee.getClass().getSimpleName() + ")");
                        System.out.println("DEBUG: Stack top 3 items:");
                        for (int i = 0; i < Math.min(3, stack.size()); i++) {
                            System.out.println(
                                    "  [" + i + "] " + peek(i) + " (" + peek(i).getClass().getSimpleName() + ")");
                        }
                    }

                    if (callee instanceof MabelBuiltin) {
                        MabelBuiltin builtin = (MabelBuiltin) callee;
                        if (argCount != builtin.arity()) {
                            throw new RuntimeException(
                                    "Expected " + builtin.arity() + " arguments but got " + argCount + ".");
                        }

                        pop(); // Pop the function first

                        List<Object> args = new ArrayList<>();
                        for (int i = 0; i < argCount; i++) {
                            args.add(0, pop()); // Pop arguments in reverse order
                        }

                        Object result = builtin.call(args);
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
                        array.add(0, pop()); // Reverse order
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

    private String stringify(Object object) {
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
}