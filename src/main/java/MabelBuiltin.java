import java.util.*;

abstract class MabelBuiltin {
    private final String name;
    private final int arity;

    public MabelBuiltin(String name, int arity) {
        this.name = name;
        this.arity = arity;
    }

    public abstract Object call(List<Object> args);

    public int arity() {
        return arity;
    }

    @Override
    public String toString() {
        return "<builtin " + name + ">";
    }
}