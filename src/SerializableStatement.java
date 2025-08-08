import java.io.Serializable;
import java.util.List;

class SerializableStatement implements Serializable {
  private static final long serialVersionUID = 1L;

  String type;
  String name;
  SerializableExpression expression;
  SerializableExpression initializer;
  SerializableExpression condition;
  SerializableStatement thenBranch;
  SerializableStatement elseBranch;
  SerializableStatement body; // For while loops
  List<SerializableStatement> statements;

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

  static SerializableStatement ifStmt(SerializableExpression condition, SerializableStatement thenBranch,
      SerializableStatement elseBranch) {
    SerializableStatement stmt = new SerializableStatement("if");
    stmt.condition = condition;
    stmt.thenBranch = thenBranch;
    stmt.elseBranch = elseBranch;
    return stmt;
  }

  static SerializableStatement whileStmt(SerializableExpression condition, SerializableStatement body) {
    SerializableStatement stmt = new SerializableStatement("while");
    stmt.condition = condition;
    stmt.body = body;
    return stmt;
  }

  static SerializableStatement block(List<SerializableStatement> statements) {
    SerializableStatement stmt = new SerializableStatement("block");
    stmt.statements = statements;
    return stmt;
  }
}