import java.util.*;

enum OpCode {
    CONSTANT,
    NIL,
    TRUE,
    FALSE,
    POP,
    GET_GLOBAL,
    DEFINE_GLOBAL,
    SET_GLOBAL,
    EQUAL,
    GREATER,
    LESS,
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    MODULO,
    NOT,
    NEGATE,
    PRINT,
    JUMP,
    JUMP_IF_FALSE,
    LOOP,
    CALL,
    RETURN,
    ARRAY,
    INDEX_GET,
    INDEX_SET
}

class Chunk {
    private final List<Byte> code = new ArrayList<>();
    private final List<Integer> lines = new ArrayList<>();
    private final List<Object> constants = new ArrayList<>();

    public void write(byte data, int line) {
        code.add(data);
        lines.add(line);
    }

    public void write(OpCode op, int line) {
        write((byte) op.ordinal(), line);
    }

    public int addConstant(Object value) {
        constants.add(value);
        return constants.size() - 1;
    }

    public byte get(int offset) {
        return code.get(offset);
    }

    public int getUnsigned(int offset) {
        return Byte.toUnsignedInt(code.get(offset));
    }

    public Object getConstant(int index) {
        return constants.get(index);
    }

    public int size() {
        return code.size();
    }

    public int getLine(int offset) {
        return lines.get(offset);
    }

    public void set(int offset, byte value) {
        code.set(offset, value);
    }
}
