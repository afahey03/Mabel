import java.io.Serializable;
import java.util.*;

class SerializableExpression implements Serializable {
  private static final long serialVersionUID = 1L;

  String type;
  Object value;
  String name;
  String operator;
  SerializableExpression left;
  SerializableExpression right;
  SerializableExpression callee;
  List<SerializableExpression> arguments;

  SerializableExpression(String type) {
    this.type = type;
  }

  static SerializableExpression literal(Object value) {
    SerializableExpression expr = new SerializableExpression("literal");
    expr.value = value;
    return expr;
  }

  static SerializableExpression variable(String name) {
    SerializableExpression expr = new SerializableExpression("variable");
    expr.name = name;
    return expr;
  }

  static SerializableExpression binary(SerializableExpression left, String operator, SerializableExpression right) {
    SerializableExpression expr = new SerializableExpression("binary");
    expr.left = left;
    expr.operator = operator;
    expr.right = right;
    return expr;
  }

  static SerializableExpression call(SerializableExpression callee, List<SerializableExpression> arguments) {
    SerializableExpression expr = new SerializableExpression("call");
    expr.callee = callee;
    expr.arguments = arguments;
    return expr;
  }
}