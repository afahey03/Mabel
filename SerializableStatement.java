import java.io.Serializable;

class SerializableStatement implements Serializable {
  private static final long serialVersionUID = 1L;

  String type;
  String name;
  SerializableExpression expression;
  SerializableExpression initializer;

  SerializableStatement(String type) {
    this.type = type;
  }

  static SerializableStatement print(SerializableExpression expr) {
    SerializableStatement stmt = new SerializableStatement("print");
    stmt.expression = expr;
    return stmt;
  }

  static SerializableStatement var(String name, SerializableExpression initializer) {
    SerializableStatement stmt = new SerializableStatement("var");
    stmt.name = name;
    stmt.initializer = initializer;
    return stmt;
  }

  static SerializableStatement returnStmt(SerializableExpression expr) {
    SerializableStatement stmt = new SerializableStatement("return");
    stmt.expression = expr;
    return stmt;
  }

  static SerializableStatement expression(SerializableExpression expr) {
    SerializableStatement stmt = new SerializableStatement("expression");
    stmt.expression = expr;
    return stmt;
  }
}