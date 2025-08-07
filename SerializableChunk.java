import java.io.*;
import java.util.*;

class SerializableChunk implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final List<Byte> code;
    private final List<Integer> lines;
    private final List<Object> constants;
    
    public SerializableChunk(Chunk chunk) {
        this.code = new ArrayList<>();
        this.lines = new ArrayList<>();
        this.constants = new ArrayList<>();
        
        for (int i = 0; i < chunk.size(); i++) {
            this.code.add(chunk.get(i));
            this.lines.add(chunk.getLine(i));
        }
        
        this.constants.addAll(chunk.getConstants());
    }
    
    public Chunk toChunk() {
        Chunk chunk = new Chunk();
        
        for (int i = 0; i < code.size(); i++) {
            chunk.write(code.get(i), lines.get(i));
        }
        
        for (Object constant : constants) {
            chunk.addConstant(constant);
        }
        
        return chunk;
    }
}
