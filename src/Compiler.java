import java.util.*;

class Compiler implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Chunk chunk = new Chunk();

    public Compiler() {
    }

    public Compiler(VirtualMachine vm) {
    }

    public Chunk compile(List<Stmt> statements) {
        for (Stmt statement : statements) {
            compile(statement);
        }
        return chunk;
    }

    private void compile(Stmt stmt) {
        stmt.accept(this);
    }

    private void compile(Expr expr) {
        expr.accept(this);
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        Object value = expr.value;
        if (value == null) {
            emitByte(OpCode.NIL);
        } else if (value instanceof Boolean) {
            emitByte((Boolean) value ? OpCode.TRUE : OpCode.FALSE);
        } else {
            emitConstant(value);
        }
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        compile(expr.left);
        compile(expr.right);

        switch (expr.operator.type) {
            case PLUS:
                emitByte(OpCode.ADD);
                break;
            case MINUS:
                emitByte(OpCode.SUBTRACT);
                break;
            case MULTIPLY:
                emitByte(OpCode.MULTIPLY);
                break;
            case DIVIDE:
                emitByte(OpCode.DIVIDE);
                break;
            case MODULO:
                emitByte(OpCode.MODULO);
                break;
            case EQUALS:
                emitByte(OpCode.EQUAL);
                break;
            case NOT_EQUALS:
                emitBytes(OpCode.EQUAL, OpCode.NOT);
                break;
            case GREATER:
                emitByte(OpCode.GREATER);
                break;
            case GREATER_EQUAL:
                emitBytes(OpCode.LESS, OpCode.NOT);
                break;
            case LESS:
                emitByte(OpCode.LESS);
                break;
            case LESS_EQUAL:
                emitBytes(OpCode.GREATER, OpCode.NOT);
                break;
            default:
                throw new RuntimeException("Unknown binary operator: " + expr.operator.type);
        }
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        compile(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                emitByte(OpCode.NEGATE);
                break;
            case NOT:
                emitByte(OpCode.NOT);
                break;
            default:
                throw new RuntimeException("Unknown unary operator: " + expr.operator.type);
        }
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        int constant = makeConstant(expr.name.lexeme);
        emitBytes(OpCode.GET_GLOBAL, (byte) constant);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        compile(expr.value);
        int constant = makeConstant(expr.name.lexeme);
        emitBytes(OpCode.SET_GLOBAL, (byte) constant);
        return null;
    }

    @Override
    public Void visitIndexSetExpr(Expr.IndexSet expr) {
        compile(expr.object);
        compile(expr.index);
        compile(expr.value);
        emitByte(OpCode.INDEX_SET);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        compile(expr.left);

        if (expr.operator.type == TokenType.OR) {
            int elseJump = emitJump(OpCode.JUMP_IF_FALSE);
            int endJump = emitJump(OpCode.JUMP);

            patchJump(elseJump);
            emitByte(OpCode.POP);

            compile(expr.right);
            patchJump(endJump);
        } else {
            int endJump = emitJump(OpCode.JUMP_IF_FALSE);

            emitByte(OpCode.POP);
            compile(expr.right);

            patchJump(endJump);
        }
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        for (Expr argument : expr.arguments) {
            compile(argument);
        }
        compile(expr.callee);
        emitBytes(OpCode.CALL, (byte) expr.arguments.size());
        return null;
    }

    @Override
    public Void visitArrayExpr(Expr.Array expr) {
        for (Expr element : expr.elements) {
            compile(element);
        }
        emitBytes(OpCode.ARRAY, (byte) expr.elements.size());
        return null;
    }

    @Override
    public Void visitIndexExpr(Expr.Index expr) {
        compile(expr.object);
        compile(expr.index);
        emitByte(OpCode.INDEX_GET);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        compile(expr.expression);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        compile(expr.object);
        int constant = makeConstant(expr.name.lexeme);
        emitBytes(OpCode.GET_PROPERTY, (byte) constant);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        compile(expr.object);
        int constant = makeConstant(expr.name.lexeme);
        compile(expr.value);
        emitBytes(OpCode.SET_PROPERTY, (byte) constant);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        int constant = makeConstant("this");
        emitBytes(OpCode.GET_GLOBAL, (byte) constant);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        int constant = makeConstant(expr.method.lexeme);
        emitBytes(OpCode.GET_SUPER, (byte) constant);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        compile(stmt.expression);
        emitByte(OpCode.POP);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        compile(stmt.expression);
        emitByte(OpCode.PRINT);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer != null) {
            compile(stmt.initializer);
        } else {
            emitByte(OpCode.NIL);
        }

        int constant = makeConstant(stmt.name.lexeme);
        emitBytes(OpCode.DEFINE_GLOBAL, (byte) constant);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        for (Stmt statement : stmt.statements) {
            if (statement != null) {
                compile(statement);
            }
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        compile(stmt.condition);

        int thenJump = emitJump(OpCode.JUMP_IF_FALSE);
        emitByte(OpCode.POP);
        compile(stmt.thenBranch);

        int elseJump = emitJump(OpCode.JUMP);

        patchJump(thenJump);
        emitByte(OpCode.POP);

        if (stmt.elseBranch != null)
            compile(stmt.elseBranch);
        patchJump(elseJump);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        int loopStart = chunk.size();
        compile(stmt.condition);

        int exitJump = emitJump(OpCode.JUMP_IF_FALSE);
        emitByte(OpCode.POP);
        compile(stmt.body);
        emitLoop(loopStart);

        patchJump(exitJump);
        emitByte(OpCode.POP);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        // System.out.println("DEBUG: Compiling class: " + stmt.name.lexeme);

        Map<String, Object> defaultFieldValues = new HashMap<>();
        Map<String, SerializableFunction> methods = new HashMap<>();

        for (Stmt.Function method : stmt.methods) {
            List<String> paramNames = new ArrayList<>();
            for (Token param : method.params) {
                paramNames.add(param.lexeme);
            }

            List<SerializableStatement> serializableBody = new ArrayList<>();
            for (Stmt bodyStmt : method.body) {
                SerializableStatement serializable = convertStatement(bodyStmt);
                if (serializable != null) {
                    serializableBody.add(serializable);
                }
            }

            SerializableFunction function = new SerializableFunction(
                    method.name.lexeme,
                    paramNames,
                    serializableBody);

            methods.put(method.name.lexeme, function);
        }

        int constant = makeConstant(stmt.name.lexeme);

        SerializableClass klass;
        if (stmt.superclass != null) {
            klass = new SerializableClass(
                    stmt.name.lexeme,
                    stmt.superclass.name.lexeme,
                    methods,
                    defaultFieldValues);
        } else {
            klass = SerializableClass.createWithoutSuperclass(
                    stmt.name.lexeme,
                    methods,
                    defaultFieldValues);
        }

        emitConstant(klass);
        emitBytes(OpCode.DEFINE_GLOBAL, (byte) constant);

        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        // System.out.println("DEBUG: Compiling function: " + stmt.name.lexeme);

        List<String> paramNames = new ArrayList<>();
        for (Token param : stmt.params) {
            paramNames.add(param.lexeme);
        }

        List<SerializableStatement> serializableBody = new ArrayList<>();
        for (Stmt bodyStmt : stmt.body) {
            SerializableStatement serializable = convertStatement(bodyStmt);
            if (serializable != null) {
                serializableBody.add(serializable);
            }
        }

        SerializableFunction function = new SerializableFunction(
                stmt.name.lexeme,
                paramNames,
                serializableBody);

        int constant = makeConstant(stmt.name.lexeme);
        emitConstant(function);
        emitBytes(OpCode.DEFINE_GLOBAL, (byte) constant);

        return null;
    }

    private SerializableStatement convertStatement(Stmt stmt) {
        if (stmt instanceof Stmt.Print) {
            Stmt.Print printStmt = (Stmt.Print) stmt;
            return SerializableStatement.print(convertExpression(printStmt.expression));
        } else if (stmt instanceof Stmt.Var) {
            Stmt.Var varStmt = (Stmt.Var) stmt;
            SerializableExpression init = varStmt.initializer != null ? convertExpression(varStmt.initializer) : null;
            return SerializableStatement.var(varStmt.name.lexeme, init);
        } else if (stmt instanceof Stmt.Return) {
            Stmt.Return returnStmt = (Stmt.Return) stmt;
            SerializableExpression value = returnStmt.value != null ? convertExpression(returnStmt.value) : null;
            return SerializableStatement.returnStmt(value);
        } else if (stmt instanceof Stmt.Expression) {
            Stmt.Expression exprStmt = (Stmt.Expression) stmt;
            return SerializableStatement.expression(convertExpression(exprStmt.expression));
        } else if (stmt instanceof Stmt.If) {
            Stmt.If ifStmt = (Stmt.If) stmt;
            SerializableStatement thenBranch = convertStatement(ifStmt.thenBranch);
            SerializableStatement elseBranch = ifStmt.elseBranch != null ? convertStatement(ifStmt.elseBranch) : null;
            return SerializableStatement.ifStmt(
                    convertExpression(ifStmt.condition),
                    thenBranch,
                    elseBranch);
        } else if (stmt instanceof Stmt.Block) {
            Stmt.Block blockStmt = (Stmt.Block) stmt;
            List<SerializableStatement> statements = new ArrayList<>();
            for (Stmt s : blockStmt.statements) {
                SerializableStatement converted = convertStatement(s);
                if (converted != null) {
                    statements.add(converted);
                }
            }
            return SerializableStatement.block(statements);
        } else if (stmt instanceof Stmt.While) {
            Stmt.While whileStmt = (Stmt.While) stmt;
            return SerializableStatement.whileStmt(
                    convertExpression(whileStmt.condition),
                    convertStatement(whileStmt.body));
        } else if (stmt instanceof Stmt.For) {
            Stmt.For forStmt = (Stmt.For) stmt;
            SerializableStatement initializer = forStmt.initializer != null ? convertStatement(forStmt.initializer)
                    : null;
            SerializableExpression condition = forStmt.condition != null ? convertExpression(forStmt.condition) : null;
            SerializableExpression increment = forStmt.increment != null ? convertExpression(forStmt.increment) : null;
            SerializableStatement body = convertStatement(forStmt.body);
            return SerializableStatement.forStmt(initializer, condition, increment, body);
        }

        return null;
    }

    private SerializableExpression convertExpression(Expr expr) {
        if (expr instanceof Expr.Literal) {
            return SerializableExpression.literal(((Expr.Literal) expr).value);
        } else if (expr instanceof Expr.Variable) {
            return SerializableExpression.variable(((Expr.Variable) expr).name.lexeme);
        } else if (expr instanceof Expr.Binary) {
            Expr.Binary binExpr = (Expr.Binary) expr;
            return SerializableExpression.binary(
                    convertExpression(binExpr.left),
                    binExpr.operator.lexeme,
                    convertExpression(binExpr.right));
        } else if (expr instanceof Expr.Call) {
            Expr.Call callExpr = (Expr.Call) expr;

            if (callExpr.callee instanceof Expr.Super) {
                Expr.Super superExpr = (Expr.Super) callExpr.callee;
                List<SerializableExpression> args = new ArrayList<>();
                for (Expr arg : callExpr.arguments) {
                    args.add(convertExpression(arg));
                }
                return SerializableExpression.superMethodCall(superExpr.method.lexeme, args);
            }

            List<SerializableExpression> args = new ArrayList<>();
            for (Expr arg : callExpr.arguments) {
                args.add(convertExpression(arg));
            }
            return SerializableExpression.call(convertExpression(callExpr.callee), args);
        } else if (expr instanceof Expr.Get) {
            Expr.Get getExpr = (Expr.Get) expr;
            return SerializableExpression.get(
                    convertExpression(getExpr.object),
                    getExpr.name.lexeme);
        } else if (expr instanceof Expr.Set) {
            Expr.Set setExpr = (Expr.Set) expr;
            return SerializableExpression.set(
                    convertExpression(setExpr.object),
                    setExpr.name.lexeme,
                    convertExpression(setExpr.value));
        } else if (expr instanceof Expr.This) {
            return SerializableExpression.thisExpr();
        } else if (expr instanceof Expr.Super) {
            Expr.Super superExpr = (Expr.Super) expr;
            return SerializableExpression.superExpr(superExpr.method.lexeme);
        } else if (expr instanceof Expr.Assign) {
            Expr.Assign assignExpr = (Expr.Assign) expr;
            return SerializableExpression.assign(
                    assignExpr.name.lexeme,
                    convertExpression(assignExpr.value));
        } else if (expr instanceof Expr.Array) {
            Expr.Array arrayExpr = (Expr.Array) expr;
            List<SerializableExpression> elements = new ArrayList<>();
            for (Expr element : arrayExpr.elements) {
                elements.add(convertExpression(element));
            }
            return SerializableExpression.array(elements);
        } else if (expr instanceof Expr.Index) {
            Expr.Index indexExpr = (Expr.Index) expr;
            return SerializableExpression.index(
                    convertExpression(indexExpr.object),
                    convertExpression(indexExpr.index));
        } else if (expr instanceof Expr.IndexSet) {
            Expr.IndexSet indexSetExpr = (Expr.IndexSet) expr;
            return SerializableExpression.indexSet(
                    convertExpression(indexSetExpr.object),
                    convertExpression(indexSetExpr.index),
                    convertExpression(indexSetExpr.value));
        } else if (expr instanceof Expr.Grouping) {
            Expr.Grouping groupingExpr = (Expr.Grouping) expr;
            return convertExpression(groupingExpr.expression);
        } else if (expr instanceof Expr.Unary) {
            Expr.Unary unaryExpr = (Expr.Unary) expr;
            SerializableExpression unary = new SerializableExpression("unary");
            unary.operator = unaryExpr.operator.lexeme;
            unary.right = convertExpression(unaryExpr.right);
            return unary;
        } else if (expr instanceof Expr.Logical) {
            Expr.Logical logicalExpr = (Expr.Logical) expr;
            return SerializableExpression.binary(
                    convertExpression(logicalExpr.left),
                    logicalExpr.operator.lexeme,
                    convertExpression(logicalExpr.right));
        }

        return SerializableExpression.literal(null);
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value != null) {
            compile(stmt.value);
        } else {
            emitByte(OpCode.NIL);
        }
        emitByte(OpCode.RETURN);
        return null;
    }

    private void emitByte(OpCode op) {
        chunk.write(op, 1);
    }

    private void emitByte(byte b) {
        chunk.write(b, 1);
    }

    private void emitBytes(OpCode op1, OpCode op2) {
        emitByte(op1);
        emitByte(op2);
    }

    private void emitBytes(OpCode op, byte b) {
        emitByte(op);
        emitByte(b);
    }

    private void emitConstant(Object value) {
        emitBytes(OpCode.CONSTANT, (byte) makeConstant(value));
    }

    private int makeConstant(Object value) {
        int constant = chunk.addConstant(value);
        if (constant > 255) {
            throw new RuntimeException("Too many constants in one chunk.");
        }
        return constant;
    }

    private int emitJump(OpCode instruction) {
        emitByte(instruction);
        emitByte((byte) 0xff);
        emitByte((byte) 0xff);
        return chunk.size() - 2;
    }

    private void patchJump(int offset) {
        int jump = chunk.size() - offset - 2;

        if (jump > 0xffff) {
            throw new RuntimeException("Too much code to jump over.");
        }

        chunk.set(offset, (byte) ((jump >> 8) & 0xff));
        chunk.set(offset + 1, (byte) (jump & 0xff));
    }

    private void emitLoop(int loopStart) {
        emitByte(OpCode.LOOP);

        int offset = chunk.size() - loopStart + 2;
        if (offset > 0xffff)
            throw new RuntimeException("Loop body too large.");

        emitByte((byte) ((offset >> 8) & 0xff));
        emitByte((byte) (offset & 0xff));
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        if (stmt.initializer != null) {
            compile(stmt.initializer);
        }

        int loopStart = chunk.size();

        Expr condition = stmt.condition;
        if (condition == null) {
            emitByte(OpCode.TRUE);
        } else {
            compile(condition);
        }

        int exitJump = emitJump(OpCode.JUMP_IF_FALSE);
        emitByte(OpCode.POP);

        compile(stmt.body);

        if (stmt.increment != null) {
            compile(stmt.increment);
            emitByte(OpCode.POP);
        }

        emitLoop(loopStart);

        patchJump(exitJump);
        emitByte(OpCode.POP);

        return null;
    }
}