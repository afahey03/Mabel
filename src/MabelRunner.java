import java.io.*;

class MabelRunner {
    public static void main(String[] args) {
        try {
            InputStream resourceStream = MabelRunner.class.getResourceAsStream("/program.mbc");
            if (resourceStream == null) {
                System.err.println("Error: No compiled Mabel program found in this JAR");
                System.exit(1);
            }
            
            ObjectInputStream ois = new ObjectInputStream(resourceStream);
            SerializableChunk serializedChunk = (SerializableChunk) ois.readObject();
            Chunk chunk = serializedChunk.toChunk();
            
            VirtualMachine vm = new VirtualMachine(chunk);
            vm.run();
            
        } catch (Exception e) {
            System.err.println("Runtime error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}