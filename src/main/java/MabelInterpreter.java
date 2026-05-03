import java.util.List;

/**
 * Clean, testable API for running Mabel source code.
 * Use {@link #run(String)} to execute source and capture printed output.
 */
class MabelInterpreter {

    /**
     * Executes the given Mabel source code and returns all lines written to
     * stdout by {@code print} statements, joined with {@code \n}.
     *
     * @param source Mabel source code
     * @return captured output (may be empty)
     * @throws MabelRuntimeException if a parse or runtime error occurs
     */
    static String run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        Compiler compiler = new Compiler();
        Chunk chunk = compiler.compile(statements);

        CapturingVM vm = new CapturingVM(chunk);
        vm.run();
        return vm.getOutput();
    }

    // -----------------------------------------------------------------------

    /** Wraps VirtualMachine to capture print output instead of writing stdout. */
    private static class CapturingVM extends VirtualMachine {
        private final StringBuilder output = new StringBuilder();

        CapturingVM(Chunk chunk) {
            super(chunk);
        }

        @Override
        public void printValue(String value) {
            output.append(value).append("\n");
        }

        String getOutput() {
            return output.toString();
        }
    }

    /** Thrown when the Mabel program encounters a runtime error. */
    static class MabelRuntimeException extends RuntimeException {
        MabelRuntimeException(String message) {
            super(message);
        }
    }
}
