import java.util.*;

class MabelInstance {
  private MabelClass klass;
  private final Map<String, Object> fields = new HashMap<>();

  MabelInstance(MabelClass klass) {
    this.klass = klass;
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    MabelFunction method = klass.findMethod(name.lexeme);
    if (method != null)
      return method.bind(this);

    throw new RuntimeException("Undefined property '" + name.lexeme + "'.");
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return klass.name + " instance";
  }
}