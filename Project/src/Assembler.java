import java.util.HashMap;
import java.util.Map;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                   ASSEMBLER — 8085 SIMULATOR                             ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  PURPOSE:                                                                ║
 * ║  Translates human-readable 8085 assembly language into machine code      ║
 * ║  (bytes) that the Architecture's execution engine can process.           ║
 * ║                                                                          ║
 * ║  PIPELINE (how assembly becomes running code):                           ║
 * ║                                                                          ║
 * ║    User types:   "MVI A, 42H"                                           ║
 * ║         ↓                                                                ║
 * ║    Assembler:    Parses mnemonic "MVI" with operands "A" and "42H"       ║
 * ║         ↓                                                                ║
 * ║    Machine Code: [0x3E, 0x42]  (opcode byte + data byte)                ║
 * ║         ↓                                                                ║
 * ║    Memory:       These bytes are loaded at the specified address         ║
 * ║         ↓                                                                ║
 * ║    CPU:          Fetches 0x3E, decodes as MVI A, executes               ║
 * ║                                                                          ║
 * ║  DESIGN DECISIONS:                                                       ║
 * ║  - Two-pass assembly for label resolution                               ║
 * ║  - First pass: collect all labels and their addresses                    ║
 * ║  - Second pass: generate machine code, resolving label references       ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
public class Assembler {

    // ═══════════════════════════════════════════════════════════════
    //  FIELDS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Symbol table: maps labels to their memory addresses.
     *
     * EXAMPLE:
     *   If the program has "LOOP: INR B" at address 0x2005,
     *   then symbolTable.put("LOOP", 0x2005).
     *   Later, "JMP LOOP" resolves to JMP 0x2005.
     */
    private HashMap<String, Integer> symbolTable;

    /**
     * The starting address where the assembled code will be loaded.
     * Set by the user or defaults to 0x2000 (common convention).
     */
    private int originAddress;

    /**
     * Reference to the Architecture for loading assembled code.
     */
    private Architecture arch;

    // ═══════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════

    /**
     * Creates an Assembler linked to a specific Architecture instance.
     *
     * @param architecture  The CPU/memory system to load code into
     */
    public Assembler(Architecture architecture) {
        // TODO: Initialize fields
        //   this.arch = architecture;
        //   this.symbolTable = new HashMap<>();
        //   this.originAddress = 0x2000;  // Default start address
    }

    // ═══════════════════════════════════════════════════════════════
    //  MAIN ASSEMBLY METHOD
    // ═══════════════════════════════════════════════════════════════

    /**
     * Assembles a multi-line assembly program and loads it into memory.
     *
     * LOGIC (Two-Pass Assembly):
     *
     * PASS 1 — Label Collection:
     *   For each line:
     *     1. Strip comments (anything after ';')
     *     2. Trim whitespace
     *     3. Skip empty lines
     *     4. If line contains a label (ends with ':'), record it
     *        in symbolTable with current address
     *     5. Determine instruction size (1, 2, or 3 bytes) and advance address
     *
     * PASS 2 — Code Generation:
     *   For each line:
     *     1. Parse the mnemonic and operands
     *     2. Look up the opcode using OpcodeTable
     *     3. Resolve any label references using symbolTable
     *     4. Generate the machine code bytes
     *     5. Load bytes into memory via arch.writeMemory()
     *
     * @param program      Multi-line assembly source code
     * @param startAddress Address to begin loading code
     * @return             Array of machine code bytes (for verification)
     * @throws SimulatorException if syntax error or undefined label
     */
    public int[] assemble(String program, int startAddress) throws SimulatorException {
        // TODO: Implement two-pass assembly
        //
        // this.originAddress = startAddress;
        // String[] lines = program.split("\\n");
        //
        // // === PASS 1: Collect Labels ===
        // int currentAddr = startAddress;
        // for (String line : lines) {
        //     line = stripComment(line).trim();
        //     if (line.isEmpty()) continue;
        //
        //     // Check for label
        //     if (line.contains(":")) {
        //         String label = line.substring(0, line.indexOf(':')).trim();
        //         symbolTable.put(label.toUpperCase(), currentAddr);
        //         line = line.substring(line.indexOf(':') + 1).trim();
        //         if (line.isEmpty()) continue;
        //     }
        //
        //     // Determine instruction size and advance address
        //     currentAddr += getInstructionSize(line);
        // }
        //
        // // === PASS 2: Generate Machine Code ===
        // currentAddr = startAddress;
        // List<Integer> machineCode = new ArrayList<>();
        // for (String line : lines) {
        //     line = stripComment(line).trim();
        //     if (line.isEmpty()) continue;
        //     if (line.contains(":")) {
        //         line = line.substring(line.indexOf(':') + 1).trim();
        //         if (line.isEmpty()) continue;
        //     }
        //     int[] bytes = assembleInstruction(line, currentAddr);
        //     for (int b : bytes) {
        //         arch.writeMemory(currentAddr, b);
        //         machineCode.add(b);
        //         currentAddr++;
        //     }
        // }
        //
        // return machineCode.stream().mapToInt(Integer::intValue).toArray();
        return new int[0]; // placeholder
    }

    // ═══════════════════════════════════════════════════════════════
    //  INSTRUCTION PARSING
    // ═══════════════════════════════════════════════════════════════

    /**
     * Assembles a single instruction line into machine code bytes.
     *
     * LOGIC:
     *   1. Split line into mnemonic and operands
     *      e.g., "MOV A, B" → mnemonic="MOV", operands=["A", "B"]
     *   2. Based on the mnemonic, determine:
     *      - The opcode (using OpcodeTable or a switch)
     *      - Whether there are immediate data bytes
     *   3. Build the byte array: [opcode] or [opcode, data] or [opcode, low, high]
     *
     * EXAMPLES:
     *   "NOP"        → [0x00]                    (1 byte)
     *   "MVI A, 42H" → [0x3E, 0x42]              (2 bytes)
     *   "JMP 2050H"  → [0xC3, 0x50, 0x20]        (3 bytes, note little-endian!)
     *   "MOV A, B"   → [0x78]                     (1 byte)
     *
     * @param line           Assembly instruction text
     * @param currentAddress Current memory address (for relative calculations)
     * @return               Array of machine code bytes
     * @throws SimulatorException if syntax error
     */
    private int[] assembleInstruction(String line, int currentAddress) throws SimulatorException {
        // TODO: Implement instruction parsing and opcode generation
        //
        // String[] parts = line.trim().split("\\s+", 2);  // Split into mnemonic + rest
        // String mnemonic = parts[0].toUpperCase();
        // String operandStr = (parts.length > 1) ? parts[1].trim() : "";
        // String[] operands = operandStr.isEmpty() ? new String[0]
        //                     : operandStr.split("\\s*,\\s*");
        //
        // switch (mnemonic) {
        //     case "NOP": return new int[]{0x00};
        //     case "HLT": return new int[]{0x76};
        //
        //     case "MOV": {
        //         int destCode = getRegisterCode(operands[0]);
        //         int srcCode  = getRegisterCode(operands[1]);
        //         return new int[]{0x40 | (destCode << 3) | srcCode};
        //     }
        //
        //     case "MVI": {
        //         int destCode = getRegisterCode(operands[0]);
        //         int data = parseImmediate8(operands[1]);
        //         return new int[]{0x06 | (destCode << 3), data};
        //     }
        //
        //     case "LXI": {
        //         int rpCode = getRegPairCode(operands[0]);
        //         int data16 = parseImmediate16(operands[1]);
        //         return new int[]{0x01 | (rpCode << 4), data16 & 0xFF, (data16 >> 8) & 0xFF};
        //     }
        //
        //     case "JMP": {
        //         int addr = resolveAddress(operands[0]);
        //         return new int[]{0xC3, addr & 0xFF, (addr >> 8) & 0xFF};
        //     }
        //
        //     // TODO: Add cases for ALL mnemonics...
        //
        //     default:
        //         throw new SimulatorException("Unknown mnemonic: " + mnemonic,
        //                 SimulatorException.ErrorType.SYNTAX_ERROR);
        // }
        return new int[0]; // placeholder
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Returns the 3-bit register code used in opcodes.
     *
     * ENCODING: B=0, C=1, D=2, E=3, H=4, L=5, M=6, A=7
     *
     * @param regName  Register name
     * @return         3-bit code (0-7)
     * @throws SimulatorException if invalid register name
     */
    private int getRegisterCode(String regName) throws SimulatorException {
        // TODO: Implement the mapping
        //
        // switch (regName.toUpperCase().trim()) {
        //     case "B": return 0;  case "C": return 1;
        //     case "D": return 2;  case "E": return 3;
        //     case "H": return 4;  case "L": return 5;
        //     case "M": return 6;  case "A": return 7;
        //     default: throw new SimulatorException("Invalid register: " + regName);
        // }
        return 0; // placeholder
    }

    /**
     * Returns the 2-bit register pair code used in opcodes.
     *
     * ENCODING: B(=BC)=0, D(=DE)=1, H(=HL)=2, SP(or PSW)=3
     *
     * @param pairName  Pair name ("B","D","H","SP","PSW")
     * @return          2-bit code (0-3)
     * @throws SimulatorException if invalid pair name
     */
    private int getRegPairCode(String pairName) throws SimulatorException {
        // TODO: Implement
        return 0; // placeholder
    }

    /**
     * Parses an 8-bit immediate value from assembly text.
     *
     * FORMATS SUPPORTED:
     *   "42H" or "42h"  → hexadecimal = 0x42
     *   "0x2A"          → hexadecimal = 0x2A
     *   "66"            → decimal = 0x42
     *   "01000010B"     → binary = 0x42
     *
     * @param text  The immediate value text
     * @return      Parsed integer (0x00-0xFF)
     * @throws SimulatorException if format invalid or value out of range
     */
    private int parseImmediate8(String text) throws SimulatorException {
        // TODO: Parse different number formats
        //
        // text = text.trim().toUpperCase();
        // int value;
        // if (text.endsWith("H")) {
        //     value = Integer.parseInt(text.substring(0, text.length()-1), 16);
        // } else if (text.startsWith("0X")) {
        //     value = Integer.parseInt(text.substring(2), 16);
        // } else if (text.endsWith("B")) {
        //     value = Integer.parseInt(text.substring(0, text.length()-1), 2);
        // } else {
        //     value = Integer.parseInt(text);
        // }
        // if (value < 0 || value > 0xFF)
        //     throw new SimulatorException("8-bit value out of range: " + text);
        // return value;
        return 0; // placeholder
    }

    /**
     * Parses a 16-bit immediate value from assembly text.
     * Same formats as 8-bit but allows values up to 0xFFFF.
     *
     * @param text  The immediate value text
     * @return      Parsed integer (0x0000-0xFFFF)
     * @throws SimulatorException if format invalid or value out of range
     */
    private int parseImmediate16(String text) throws SimulatorException {
        // TODO: Same as parseImmediate8 but allow up to 0xFFFF
        return 0; // placeholder
    }

    /**
     * Resolves a label or address reference.
     *
     * LOGIC:
     *   1. Try parsing as a numeric address first (e.g., "2050H")
     *   2. If that fails, look up in symbolTable (e.g., "LOOP")
     *   3. If not found anywhere, throw UNDEFINED_LABEL error
     *
     * @param ref  Address string or label name
     * @return     16-bit address
     * @throws SimulatorException if label undefined
     */
    private int resolveAddress(String ref) throws SimulatorException {
        // TODO: Implement address/label resolution
        //
        // try {
        //     return parseImmediate16(ref);
        // } catch (Exception e) {
        //     // Not a numeric address, try as label
        //     String label = ref.trim().toUpperCase();
        //     if (symbolTable.containsKey(label)) {
        //         return symbolTable.get(label);
        //     }
        //     throw new SimulatorException("Undefined label: " + ref,
        //             SimulatorException.ErrorType.UNDEFINED_LABEL);
        // }
        return 0; // placeholder
    }

    /**
     * Determines the size (in bytes) of an instruction from its mnemonic.
     *
     * SIZE RULES:
     *   1 byte:  MOV, ADD, SUB, INR, DCR, XCHG, HLT, NOP, RLC, CMA, etc.
     *   2 bytes: MVI, ADI, SUI, ANI, ORI, XRI, CPI, IN, OUT, etc.
     *   3 bytes: LXI, LDA, STA, LHLD, SHLD, JMP, Jcc, CALL, Ccc, etc.
     *
     * @param line  Assembly instruction text
     * @return      Instruction size in bytes (1, 2, or 3)
     */
    private int getInstructionSize(String line) {
        // TODO: Parse mnemonic and return byte count
        //
        // String mnemonic = line.trim().split("\\s+")[0].toUpperCase();
        // switch (mnemonic) {
        //     // 3-byte instructions (opcode + 16-bit data/address)
        //     case "LXI": case "LDA": case "STA": case "LHLD": case "SHLD":
        //     case "JMP": case "JC": case "JNC": case "JZ": case "JNZ":
        //     case "JP": case "JM": case "JPE": case "JPO":
        //     case "CALL": case "CC": case "CNC": case "CZ": case "CNZ":
        //     case "CP": case "CM": case "CPE": case "CPO":
        //         return 3;
        //
        //     // 2-byte instructions (opcode + 8-bit data)
        //     case "MVI": case "ADI": case "ACI": case "SUI": case "SBI":
        //     case "ANI": case "ORI": case "XRI": case "CPI":
        //     case "IN": case "OUT":
        //         return 2;
        //
        //     // 1-byte instructions (opcode only)
        //     default:
        //         return 1;
        // }
        return 1; // placeholder
    }

    /**
     * Strips comments from an assembly line.
     * Comments start with ';' and continue to end of line.
     *
     * EXAMPLE: "MOV A, B ; copy B to A"  →  "MOV A, B "
     *
     * @param line  Raw assembly line
     * @return      Line without comment
     */
    private String stripComment(String line) {
        // TODO: Remove everything after ';'
        //
        // int commentIdx = line.indexOf(';');
        // return (commentIdx >= 0) ? line.substring(0, commentIdx) : line;
        return line; // placeholder
    }

    /**
     * Returns the symbol table for debugging/display.
     *
     * @return Map of label names to addresses
     */
    public HashMap<String, Integer> getSymbolTable() {
        return symbolTable;
    }
}
