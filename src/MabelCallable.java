import java.util.*;

interface MabelCallable {
  int arity();

  Object call(VirtualMachine vm, List<Object> arguments);
}