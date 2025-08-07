import java.io.*;
import java.nio.file.*;
import java.util.List;

public class MabelCompiler {
    private static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: java MabelCompiler [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String source = new String(bytes);
        
        if (path.endsWith(".mabel")) {
            // For now, just run .mabel files directly
            // Compilation to bytecode has a bug we're fixing
            System.out.println("Running Mabel program: " + path);
            run(source);
        } else if (path.endsWith(".mbc")) {
            // Run bytecode file
            runBytecodeFile(path);
        } else {
            // Run as source
            run(source);
        }

        if (hadError) System.exit(65);
    }

    private static void runBytecodeFile(String path) throws IOException {
        // In a real implementation, you'd serialize/deserialize the bytecode
        // For this demo, we'll just show the concept
        System.out.println("Running bytecode file: " + path);
        System.out.println("(Bytecode file execution would be implemented here)");
    }

    private static void compileToBytecode(String source, String outputFile) throws IOException {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        if (hadError) return;

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) return;

        Compiler compiler = new Compiler();
        Chunk chunk = compiler.compile(statements);

        // In a real implementation, you'd serialize the chunk to a file
        // For this demo, we'll create a simple text representation
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            writer.println("# Mabel Bytecode File");
            writer.println("# Generated from source");
            writer.println("# Instructions: " + chunk.size());
            for (int i = 0; i < chunk.size(); i++) {
                byte instruction = chunk.get(i);
                // Check if the instruction is a valid opcode
                if (instruction >= 0 && instruction < OpCode.values().length) {
                    writer.println(i + ": " + OpCode.values()[instruction]);
                } else {
                    writer.println(i + ": DATA(" + instruction + ")");
                }
            }
        }
    }

    private static void runPrompt() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Mabel Programming Language REPL");
        System.out.println("Type 'exit' to quit");

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null || line.equals("exit")) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();

            if (hadError) return;

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            if (hadError) return;

            Compiler compiler = new Compiler();
            Chunk chunk = compiler.compile(statements);

            VirtualMachine vm = new VirtualMachine(chunk);
            vm.run();
        } catch (Exception e) {
            System.err.println("Runtime error: " + e.getMessage());
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}