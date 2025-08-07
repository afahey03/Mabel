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
            String bytecodeFile = path.replace(".mabel", ".mbc");
            System.out.println("Compiling Mabel program: " + path);
            compileToBytecode(source, bytecodeFile);
        } else if (path.endsWith(".mbc")) {
            runBytecodeFile(path);
        } else if (path.endsWith(".jar")) {
            System.out.println("Use: java -jar " + path);
            return;
        } else {
            // Run as source
            run(source);
        }

        if (hadError) System.exit(65);
    }

    private static void runBytecodeFile(String path) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            SerializableChunk serializedChunk = (SerializableChunk) ois.readObject();
            Chunk chunk = serializedChunk.toChunk();
            
            System.out.println("Running compiled Mabel bytecode: " + path);
            VirtualMachine vm = new VirtualMachine(chunk);
            vm.run();
            
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Invalid bytecode file format");
        } catch (Exception e) {
            System.err.println("Error running bytecode: " + e.getMessage());
        }
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

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            oos.writeObject(new SerializableChunk(chunk));
        }
        
        String jarFile = outputFile.replace(".mbc", ".jar");
        createExecutableJar(chunk, jarFile);
    }

    private static void createExecutableJar(Chunk chunk, String jarFile) throws IOException {
        Path tempDir = Files.createTempDirectory("mabel-build");
        
        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(tempDir.resolve("program.mbc")))) {
            oos.writeObject(new SerializableChunk(chunk));
        }
        
        try (var jar = new java.util.jar.JarOutputStream(Files.newOutputStream(Paths.get(jarFile)))) {
            java.util.jar.Manifest manifest = new java.util.jar.Manifest();
            manifest.getMainAttributes().put(java.util.jar.Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(java.util.jar.Attributes.Name.MAIN_CLASS, "MabelRunner");
            
            jar.putNextEntry(new java.util.jar.JarEntry("META-INF/"));
            jar.closeEntry();
            
            jar.putNextEntry(new java.util.jar.JarEntry("META-INF/MANIFEST.MF"));
            manifest.write(jar);
            jar.closeEntry();
            
            jar.putNextEntry(new java.util.jar.JarEntry("program.mbc"));
            Files.copy(tempDir.resolve("program.mbc"), jar);
            jar.closeEntry();
            
            Path currentDir = Paths.get(".");
            try (var stream = Files.walk(currentDir, 1)) {
                stream.filter(path -> path.toString().endsWith(".class"))
                      .forEach(path -> {
                          try {
                              String className = path.getFileName().toString();
                              jar.putNextEntry(new java.util.jar.JarEntry(className));
                              Files.copy(path, jar);
                              jar.closeEntry();
                          } catch (IOException e) {
                              throw new RuntimeException(e);
                          }
                      });
            }
        }
        
        deleteDirectory(tempDir);
        
        System.out.println("Created executable JAR: " + jarFile);
        System.out.println("Run with: java -jar " + jarFile);
        
    }

    private static void createJarFromDirectory(Path sourceDir, Path jarFile) throws IOException {
        try (var jar = new java.util.jar.JarOutputStream(Files.newOutputStream(jarFile))) {
            Files.walk(sourceDir)
                 .filter(path -> !Files.isDirectory(path))
                 .forEach(path -> {
                     try {
                         Path relativePath = sourceDir.relativize(path);
                         String entryName = relativePath.toString().replace('\\', '/');
                         
                         jar.putNextEntry(new java.util.jar.JarEntry(entryName));
                         Files.copy(path, jar);
                         jar.closeEntry();
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 });
        }
    }

    private static void deleteDirectory(Path dir) throws IOException {
        Files.walk(dir)
             .sorted((a, b) -> b.compareTo(a))
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     // Nothing
                 }
             });
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