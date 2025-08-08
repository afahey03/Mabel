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
  SerializableExpression object;
  List<SerializableExpression> arguments;
  List<SerializableExpression> elements;

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

  static SerializableExpression get(SerializableExpression object, String name) {
    SerializableExpression expr = new SerializableExpression("get");
    expr.object = object;
    expr.name = name;
    return expr;
  }

  static SerializableExpression set(SerializableExpression object, String name, SerializableExpression value) {
    SerializableExpression expr = new SerializableExpression("set");
    expr.object = object;
    expr.name = name;
    expr.right = value;
    return expr;
  }

  static SerializableExpression thisExpr() {
    SerializableExpression expr = new SerializableExpression("this");
    return expr;
  }

  static SerializableExpression assign(String name, SerializableExpression value) {
    SerializableExpression expr = new SerializableExpression("assign");
    expr.name = name;
    expr.right = value;
    return expr;
  }

  static SerializableExpression array(List<SerializableExpression> elements) {
    SerializableExpression expr = new SerializableExpression("array");
    expr.elements = elements;
    return expr;
  }

  static SerializableExpression index(SerializableExpression object, SerializableExpression index) {
    SerializableExpression expr = new SerializableExpression("index");
    expr.object = object;
    expr.right = index;
    return expr;
  }
}