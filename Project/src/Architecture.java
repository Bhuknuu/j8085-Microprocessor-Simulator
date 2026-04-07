import java.util.HashMap;
import java.util.Map;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                    ARCHITECTURE — 8085 SIMULATOR CORE                    ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  PURPOSE:                                                                ║
 * ║  The HEART of the simulator. This class represents the entire 8085      ║
 * ║  CPU — registers, memory, ALU, program counter, stack pointer,          ║
 * ║  and the execution engine (fetch-decode-execute cycle).                  ║
 * ║                                                                          ║
 * ║  It implements TWO interfaces:                                           ║
 * ║    Memory    → gives it memory read/write/reset capabilities            ║
 * ║    InstrucSet → gives it all 74 instruction implementations             ║
 * ║                                                                          ║
 * ║  REAL HARDWARE CONTEXT:                                                  ║
 * ║  The 8085A microprocessor (Intel, 1976) is a complete CPU on a chip:     ║
 * ║    - 8-bit data bus, 16-bit address bus                                  ║
 * ║    - 6 general-purpose registers (B,C,D,E,H,L)                         ║
 * ║    - 1 accumulator (A) — primary register for arithmetic                ║
 * ║    - 1 flag register (F) — stores condition flags (S,Z,AC,P,CY)        ║
 * ║    - 2 temporary registers (W, Z) — used internally by CPU             ║
 * ║    - 16-bit program counter (PC) — points to next instruction           ║
 * ║    - 16-bit stack pointer (SP) — points to top of stack in memory       ║
 * ║                                                                          ║
 * ║  DESIGN DECISIONS:                                                       ║
 * ║  ┌─────────────────────────────────────────────────────────────┐         ║
 * ║  │ Data Structure    │ Why                                     │         ║
 * ║  ├─────────────────────────────────────────────────────────────┤         ║
 * ║  │ HashMap<String, Integer> for registers                     │         ║
 * ║  │   → Easy access by name ("A", "B", etc.)                  │         ║
 * ║  │   → O(1) lookup, clean code with registerGet("A")         │         ║
 * ║  │ int[] for memory                                           │         ║
 * ║  │   → Direct indexed access mimics real hardware addresses  │         ║
 * ║  │   → memory[0x2000] = value is natural and fast            │         ║
 * ║  │ ALU object for computations                                │         ║
 * ║  │   → Separates computation from state management           │         ║
 * ║  └─────────────────────────────────────────────────────────────┘         ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
public class Architecture implements Memory, InstrucSet {

    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  SECTION 1: REGISTER FILE                                    ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * General-purpose 8-bit registers: B, C, D, E, H, L
     *
     * PAIRS: In the 8085, these registers pair up to form 16-bit addresses:
     *   BC pair = B (high) + C (low) → used for indirect addressing
     *   DE pair = D (high) + E (low) → used for indirect addressing
     *   HL pair = H (high) + L (low) → used as memory pointer ("M")
     *
     * KEY: register name (String) → VALUE: 8-bit value (int, 0x00-0xFF)
     */
    private HashMap<String, Integer> generalRegisters;

    /**
     * Special 8-bit registers: A (Accumulator), W, Z (internal temp)
     *
     * A (Accumulator): The primary working register. ALL arithmetic and
     *   logical operations have A as one operand and store results in A.
     *   It is THE most important register in the 8085.
     *
     * W, Z (Temporary): Internal CPU registers not accessible to the
     *   programmer. The CPU uses them during multi-byte instruction execution.
     *   Example: During LDA 2050H, the CPU uses W and Z to hold the
     *   address bytes while fetching them from memory.
     */
    private HashMap<String, Integer> specialRegisters;

    /**
     * 16-bit registers stored as integers:
     *
     * Program Counter (PC): Points to the address of the NEXT instruction
     *   to fetch from memory. After each instruction, PC advances by the
     *   instruction's byte length (1, 2, or 3).
     *
     * Stack Pointer (SP): Points to the TOP of the stack in memory.
     *   The stack grows DOWNWARD (from high addresses to low addresses).
     *   PUSH decreases SP; POP increases SP.
     *   Typically initialized to a high memory address (e.g., 0xFFFF).
     */
    private int programCounter;  // PC: 0x0000 to 0xFFFF
    private int stackPointer;    // SP: 0x0000 to 0xFFFF

    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  SECTION 2: MEMORY                                           ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * Main memory array.
     *
     * STRUCTURE: Simple int array where index = address, value = data byte.
     *   memory[0x0000] = first byte
     *   memory[0xFFFF] = last byte (for 64KB)
     *
     * WHY ARRAY (not HashMap):
     *   Arrays mimic real hardware memory — direct indexed access, O(1) read/write,
     *   and contiguous storage just like physical RAM chips.
     *
     * RANGE: Allocated based on constructor parameter (default or custom).
     */
    private int[] memory;

    /** Start address of allocated memory (default: 0x0000) */
    private int memoryStartAddress;

    /** End address of allocated memory (default: 0xFFFF) */
    private int memoryEndAddress;

    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  SECTION 3: ALU AND STATE                                    ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * The Arithmetic Logic Unit — handles all computations and flags.
     */
    private ALU alu;

    /**
     * I/O Ports — simulates the 256 input/output ports of the 8085.
     * KEY: port number (0x00-0xFF) → VALUE: byte stored at that port
     */
    private HashMap<Integer, Integer> ioPorts;

    /**
     * CPU state flags (not to be confused with the ALU's condition flags).
     */
    private boolean halted;             // true when HLT instruction executes
    private boolean interruptsEnabled;  // true when EI has been called

    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  SECTION 4: CONSTRUCTORS                                     ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * CONSTRUCTOR 1: Default Memory Range (Full 64KB: 0000H to FFFFH)
     *
     * LOGIC:
     *   1. Allocate memory array of size MAX_MEMORY_SIZE (65536)
     *   2. Initialize all registers to 0x00
     *   3. Set PC = 0x0000 (start executing from address 0)
     *   4. Set SP = 0xFFFF (stack starts at top of memory)
     *   5. Create ALU instance
     *   6. Initialize I/O ports map
     *   7. Set halted = false, interruptsEnabled = false
     *
     * USE CASE: Standard simulator mode — full 8085 memory space.
     */
    public Architecture() {
        // TODO: Call the parameterized constructor with default values
        //   this(DEFAULT_START_ADDRESS, DEFAULT_END_ADDRESS);

        // OR initialize everything directly:

        // TODO: Step 1 — Initialize memory
        // this.memoryStartAddress = DEFAULT_START_ADDRESS;
        // this.memoryEndAddress = DEFAULT_END_ADDRESS;
        // this.memory = new int[MAX_MEMORY_SIZE];

        // TODO: Step 2 — Initialize registers
        // initRegisters();

        // TODO: Step 3 — Initialize 16-bit registers
        // this.programCounter = 0x0000;
        // this.stackPointer = 0xFFFF;

        // TODO: Step 4 — Initialize ALU
        // this.alu = new ALU();

        // TODO: Step 5 — Initialize I/O ports
        // this.ioPorts = new HashMap<>();

        // TODO: Step 6 — Initialize state
        // this.halted = false;
        // this.interruptsEnabled = false;
    }

    /**
     * CONSTRUCTOR 2: Custom Memory Range.
     *
     * LOGIC:
     *   Same as default but allocates only (endAddr - startAddr + 1) bytes.
     *   Validates that startAddr < endAddr and both are within 0x0000-0xFFFF.
     *
     * USE CASE: For educational demos with limited memory (e.g., 0x2000-0x20FF
     *   gives just 256 bytes — easier to visualize in the UI).
     *
     * @param startAddr  Base address of memory
     * @param endAddr    Top address of memory
     * @throws SimulatorException if addresses are invalid
     */
    public Architecture(int startAddr, int endAddr) throws SimulatorException {
        // TODO: Validate parameters
        //   if (startAddr < 0 || endAddr > 0xFFFF || startAddr >= endAddr)
        //       throw new SimulatorException("Invalid memory range", ErrorType.INVALID_MEMORY_ADDRESS);

        // TODO: Same initialization as default constructor but with custom range
        //   this.memoryStartAddress = startAddr;
        //   this.memoryEndAddress = endAddr;
        //   this.memory = new int[endAddr - startAddr + 1];
        //   ... rest of initialization ...
    }

    /**
     * Initializes all register HashMaps with default values (0x00).
     *
     * LOGIC:
     *   General registers: {"B":0, "C":0, "D":0, "E":0, "H":0, "L":0}
     *   Special registers: {"A":0, "W":0, "Z":0}
     */
    private void initRegisters() {
        // TODO: Initialize general purpose register map
        //   generalRegisters = new HashMap<>();
        //   String[] gpRegs = {"B", "C", "D", "E", "H", "L"};
        //   for (String reg : gpRegs) { generalRegisters.put(reg, 0x00); }

        // TODO: Initialize special register map
        //   specialRegisters = new HashMap<>();
        //   specialRegisters.put("A", 0x00);
        //   specialRegisters.put("W", 0x00);
        //   specialRegisters.put("Z", 0x00);
    }

    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  SECTION 5: REGISTER ACCESS HELPERS                          ║
    // ║  These simplify reading/writing registers throughout the     ║
    // ║  instruction implementations.                                ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * Gets the value of any 8-bit register by name.
     *
     * LOGIC:
     *   1. Check if name is in generalRegisters → return its value
     *   2. Else check specialRegisters → return its value
     *   3. Else throw SimulatorException for invalid register name
     *
     * @param name  Register name ("A","B","C","D","E","H","L","W","Z")
     * @return      The 8-bit value in that register
     * @throws SimulatorException if register name is invalid
     */
    public int getRegister(String name) throws SimulatorException {
        // TODO: Implement register lookup across both maps
        //
        // name = name.toUpperCase();
        // if (generalRegisters.containsKey(name)) return generalRegisters.get(name);
        // if (specialRegisters.containsKey(name)) return specialRegisters.get(name);
        // throw new SimulatorException("Invalid register: " + name, ErrorType.INVALID_REGISTER);
        return 0; // placeholder
    }

    /**
     * Sets the value of any 8-bit register by name.
     *
     * @param name   Register name
     * @param value  8-bit value (0x00-0xFF)
     * @throws SimulatorException if invalid register or value out of range
     */
    public void setRegister(String name, int value) throws SimulatorException {
        // TODO: Validate value is 0x00-0xFF, then store in appropriate map
        //
        // if (value < 0x00 || value > 0xFF)
        //     throw new SimulatorException("Value out of 8-bit range: " + value, ErrorType.INVALID_DATA);
        // name = name.toUpperCase();
        // if (generalRegisters.containsKey(name)) { generalRegisters.put(name, value); return; }
        // if (specialRegisters.containsKey(name)) { specialRegisters.put(name, value); return; }
        // throw new SimulatorException("Invalid register: " + name, ErrorType.INVALID_REGISTER);
    }

    /**
     * Gets the 16-bit value of a register pair.
     *
     * LOGIC: Combine high and low registers:
     *   value = (highReg << 8) | lowReg
     *   "B" pair → B*256 + C
     *   "D" pair → D*256 + E
     *   "H" pair → H*256 + L
     *   "SP"     → stackPointer
     *
     * @param pair  Register pair identifier ("B","D","H","SP")
     * @return      16-bit combined value
     * @throws SimulatorException if invalid pair name
     */
    public int getRegisterPair(String pair) throws SimulatorException {
        // TODO: Implement register pair value computation
        //
        // switch (pair.toUpperCase()) {
        //     case "B": return (getRegister("B") << 8) | getRegister("C");
        //     case "D": return (getRegister("D") << 8) | getRegister("E");
        //     case "H": return (getRegister("H") << 8) | getRegister("L");
        //     case "SP": return stackPointer;
        //     default: throw new SimulatorException("Invalid register pair: " + pair);
        // }
        return 0; // placeholder
    }

    /**
     * Sets a 16-bit register pair value.
     *
     * LOGIC: Split 16-bit value into high and low bytes:
     *   highByte = (value >> 8) & 0xFF
     *   lowByte  = value & 0xFF
     *
     * @param pair   Register pair identifier
     * @param value  16-bit value (0x0000-0xFFFF)
     * @throws SimulatorException if invalid pair or value
     */
    public void setRegisterPair(String pair, int value) throws SimulatorException {
        // TODO: Split value and store in the pair's registers
        //
        // int high = (value >> 8) & 0xFF;
        // int low  = value & 0xFF;
        // switch (pair.toUpperCase()) {
        //     case "B": setRegister("B", high); setRegister("C", low); break;
        //     case "D": setRegister("D", high); setRegister("E", low); break;
        //     case "H": setRegister("H", high); setRegister("L", low); break;
        //     case "SP": stackPointer = value & 0xFFFF; break;
        //     default: throw new SimulatorException("Invalid register pair: " + pair);
        // }
    }

    /**
     * Gets the 16-bit value of the HL pair.
     * This is used SO frequently (HL = memory pointer) that it
     * deserves its own convenience method.
     *
     * @return H*256 + L
     * @throws SimulatorException if register access fails
     */
    public int getHL() throws SimulatorException {
        return getRegisterPair("H");
    }

    /**
     * Gets the value of an operand which may be a register name OR "M".
     * "M" means "memory location pointed to by HL".
     *
     * LOGIC:
     *   if (operand == "M") → return memory[HL]
     *   else                → return register[operand]
     *
     * This pattern is used by EVERY arithmetic/logical instruction.
     *
     * @param operand  Register name or "M"
     * @return         The 8-bit value
     * @throws SimulatorException if invalid operand
     */
    public int getOperandValue(String operand) throws SimulatorException {
        // TODO: Implement the M-or-register pattern
        //
        // if (operand.equalsIgnoreCase("M")) {
        //     return readMemory(getHL());
        // } else {
        //     return getRegister(operand);
        // }
        return 0; // placeholder
    }

    /**
     * Sets the value of an operand which may be a register name OR "M".
     *
     * @param operand  Register name or "M"
     * @param value    8-bit value to store
     * @throws SimulatorException if invalid operand
     */
    public void setOperandValue(String operand, int value) throws SimulatorException {
        // TODO: Implement set for M-or-register
        //
        // if (operand.equalsIgnoreCase("M")) {
        //     writeMemory(getHL(), value);
        // } else {
        //     setRegister(operand, value);
        // }
    }


    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  SECTION 6: MEMORY INTERFACE IMPLEMENTATION                  ║
    // ╚═══════════════════════════════════════════════════════════════╝

    @Override
    public int readMemory(int address) throws SimulatorException {
        // TODO: Validate address, then return memory[address - memoryStartAddress]
        //
        // if (address < memoryStartAddress || address > memoryEndAddress) {
        //     throw new SimulatorException(
        //         "Memory read out of bounds: 0x" + Integer.toHexString(address),
        //         SimulatorException.ErrorType.INVALID_MEMORY_ADDRESS, address);
        // }
        // return memory[address - memoryStartAddress];
        return 0; // placeholder
    }

    @Override
    public void writeMemory(int address, int data) throws SimulatorException {
        // TODO: Validate address and data, then store
        //
        // if (address < memoryStartAddress || address > memoryEndAddress) {
        //     throw new SimulatorException(...);
        // }
        // if (data < 0x00 || data > 0xFF) {
        //     throw new SimulatorException("Data out of byte range: " + data, ErrorType.INVALID_DATA);
        // }
        // memory[address - memoryStartAddress] = data;
    }

    @Override
    public void resetMemory() {
        // TODO: Fill the entire memory array with 0
        //   java.util.Arrays.fill(memory, 0x00);
    }

    @Override
    public int[] getMemoryDump() {
        // TODO: Return a defensive copy
        //   return java.util.Arrays.copyOf(memory, memory.length);
        return new int[0]; // placeholder
    }

    @Override
    public int getMemoryStart() {
        return memoryStartAddress;
    }

    @Override
    public int getMemoryEnd() {
        return memoryEndAddress;
    }

    @Override
    public void loadProgram(int startAddress, int[] data) throws SimulatorException {
        // TODO: Validate and copy program bytes into memory
        //
        // for (int i = 0; i < data.length; i++) {
        //     writeMemory(startAddress + i, data[i]);
        // }
    }

    @Override
    public void displayMemoryRange(int fromAddress, int toAddress) {
        // TODO: Print formatted hex dump
        //
        // System.out.printf("%-6s", "ADDR");
        // for (int col = 0; col < 16; col++) System.out.printf(" %02X", col);
        // System.out.println();
        // System.out.println("─".repeat(54));
        //
        // for (int addr = fromAddress; addr <= toAddress; addr += 16) {
        //     System.out.printf("%04X: ", addr);
        //     for (int col = 0; col < 16 && (addr + col) <= toAddress; col++) {
        //         try {
        //             System.out.printf(" %02X", readMemory(addr + col));
        //         } catch (SimulatorException e) {
        //             System.out.printf(" ??");
        //         }
        //     }
        //     System.out.println();
        // }
    }


    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  SECTION 7: EXECUTION ENGINE                                 ║
    // ║  The fetch-decode-execute cycle — the HEARTBEAT of the CPU.  ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * Executes one instruction at the current Program Counter.
     *
     * THIS IS THE CORE EXECUTION METHOD.
     *
     * THE FETCH - DECODE - EXECUTE CYCLE:
     * ┌─────────────────────────────────────────────────────────┐
     * │ FETCH:   Read opcode byte from memory[PC]              │
     * │ DECODE:  Determine which instruction this opcode is     │
     * │ EXECUTE: Perform the operation, update registers/memory │
     * │ ADVANCE: Move PC to the next instruction               │
     * └─────────────────────────────────────────────────────────┘
     *
     * LOGIC:
     *   1. Fetch: int opcode = readMemory(programCounter);
     *   2. Decode: Use a large switch statement on the opcode value
     *   3. For single-byte instructions (e.g., MOV, ADD): just execute
     *   4. For 2-byte instructions (e.g., MVI): fetch operand at PC+1
     *   5. For 3-byte instructions (e.g., JMP, LDA): fetch 2 bytes at PC+1, PC+2
     *   6. Advance PC by the instruction's byte-length (1, 2, or 3)
     *      EXCEPT for branch instructions that modify PC directly
     *
     * @throws SimulatorException if invalid opcode or execution error
     */
    public void executeInstruction() throws SimulatorException {
        // TODO: Implement the fetch-decode-execute cycle
        //
        // if (halted) return;  // Don't execute if CPU is halted
        //
        // // === FETCH ===
        // int opcode = readMemory(programCounter);
        //
        // // === DECODE & EXECUTE ===
        // switch (opcode) {
        //
        //     // --- NOP ---
        //     case 0x00: nop(); programCounter += 1; break;
        //
        //     // --- LXI B, d16 ---
        //     case 0x01: {
        //         int low  = readMemory(programCounter + 1);
        //         int high = readMemory(programCounter + 2);
        //         lxi("B", (high << 8) | low);
        //         programCounter += 3;
        //         break;
        //     }
        //
        //     // --- STAX B ---
        //     case 0x02: stax("B"); programCounter += 1; break;
        //
        //     // --- INX B ---
        //     case 0x03: inx("B"); programCounter += 1; break;
        //
        //     // ... CONTINUE FOR ALL 256 OPCODES ...
        //     // ... This is the largest single method in the project ...
        //     // ... Consider organizing by opcode range or using helper methods ...
        //
        //     // --- MOV B,B (0x40) through MOV A,A (0x7F) ---
        //     //     Except 0x76 which is HLT
        //     //     Pattern: dest = (opcode >> 3) & 0x07, src = opcode & 0x07
        //     //     Register encoding: 0=B, 1=C, 2=D, 3=E, 4=H, 5=L, 6=M, 7=A
        //
        //     // --- HLT ---
        //     case 0x76: hlt(); programCounter += 1; break;
        //
        //     // --- Unknown opcode ---
        //     default:
        //         throw new SimulatorException(
        //             "Unknown opcode: 0x" + String.format("%02X", opcode),
        //             SimulatorException.ErrorType.INVALID_OPCODE, programCounter);
        // }
    }

    /**
     * Runs the processor from the current PC until HLT is encountered.
     *
     * LOGIC:
     *   while (!halted) {
     *       executeInstruction();
     *   }
     *
     * This is the "Run" button in the UI — continuous execution.
     *
     * @throws SimulatorException if any instruction fails
     */
    public void run() throws SimulatorException {
        // TODO: Implement run loop
        //
        // halted = false;
        // int maxInstructions = 100000;  // Safety limit to prevent infinite loops
        // int count = 0;
        // while (!halted && count < maxInstructions) {
        //     executeInstruction();
        //     count++;
        // }
        // if (count >= maxInstructions) {
        //     System.out.println("WARNING: Execution limit reached. Possible infinite loop.");
        // }
    }

    /**
     * Runs the processor starting from a specific address.
     *
     * @param startAddress  Address to begin execution from
     * @throws SimulatorException if execution fails
     */
    public void runFrom(int startAddress) throws SimulatorException {
        // TODO: Set PC to startAddress, then call run()
        //   programCounter = startAddress;
        //   run();
    }

    /**
     * Executes a single instruction (step mode) for debugging.
     *
     * USE CASE: The user presses "Step" in the UI to execute one
     * instruction at a time and observe register/memory changes.
     *
     * @throws SimulatorException if the instruction fails
     */
    public void step() throws SimulatorException {
        // TODO: Just call executeInstruction() once
        //   executeInstruction();
    }

    /**
     * Decodes an opcode value to its mnemonic name for display.
     *
     * LOGIC:
     *   Use the OpcodeTable class to look up the mnemonic.
     *   e.g., 0x78 → "MOV A,B"
     *
     * USE CASE: Displaying the current instruction in the UI.
     *
     * @param opcode  The opcode byte
     * @return        Mnemonic string (e.g., "MOV A,B")
     */
    public String disassemble(int opcode) {
        // TODO: Look up opcode in OpcodeTable and return mnemonic
        //   return OpcodeTable.getMnemonic(opcode);
        return "???"; // placeholder
    }

    /**
     * Helper: Maps register encoding (0-7 used in opcodes) to register name.
     *
     * THE 8085 REGISTER ENCODING (used in the opcode bit-fields):
     *   0 = B,  1 = C,  2 = D,  3 = E
     *   4 = H,  5 = L,  6 = M (memory via HL), 7 = A
     *
     * @param code  3-bit register code (0-7)
     * @return      Register name string
     */
    private String regCodeToName(int code) {
        // TODO: Implement the mapping
        //
        // String[] REG_NAMES = {"B", "C", "D", "E", "H", "L", "M", "A"};
        // if (code >= 0 && code <= 7) return REG_NAMES[code];
        // return "?";
        return "?"; // placeholder
    }


    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  SECTION 8: INSTRUCTION SET IMPLEMENTATION                   ║
    // ║  Implements ALL methods from InstrucSet interface.           ║
    // ║  Each method is the "execute" part of fetch-decode-execute.  ║
    // ╚═══════════════════════════════════════════════════════════════╝

    // ─────────────────────────────────────────────────────────────
    //  DATA TRANSFER INSTRUCTIONS
    // ─────────────────────────────────────────────────────────────

    @Override
    public void mov(String dest, String src) throws SimulatorException {
        // TODO: Implement MOV
        //
        // 1. Get source value:  int value = getOperandValue(src);
        // 2. Set destination:   setOperandValue(dest, value);
        // 3. No flags affected.
        //
        // EDGE CASE: MOV M,M (opcode 0x76) is actually HLT, not a valid MOV.
        //   if (dest.equals("M") && src.equals("M"))
        //       throw new SimulatorException("MOV M,M is not valid (this is HLT)");
    }

    @Override
    public void mvi(String dest, int data) throws SimulatorException {
        // TODO: Implement MVI
        //   setOperandValue(dest, data);
    }

    @Override
    public void lxi(String regPair, int data16) throws SimulatorException {
        // TODO: Implement LXI
        //   setRegisterPair(regPair, data16);
    }

    @Override
    public void lda(int address) throws SimulatorException {
        // TODO: A = memory[address]
        //   setRegister("A", readMemory(address));
    }

    @Override
    public void sta(int address) throws SimulatorException {
        // TODO: memory[address] = A
        //   writeMemory(address, getRegister("A"));
    }

    @Override
    public void lhld(int address) throws SimulatorException {
        // TODO: L = memory[address], H = memory[address+1]
        //   setRegister("L", readMemory(address));
        //   setRegister("H", readMemory(address + 1));
    }

    @Override
    public void shld(int address) throws SimulatorException {
        // TODO: memory[address] = L, memory[address+1] = H
        //   writeMemory(address, getRegister("L"));
        //   writeMemory(address + 1, getRegister("H"));
    }

    @Override
    public void ldax(String regPair) throws SimulatorException {
        // TODO: A = memory[register_pair]
        //   int addr = getRegisterPair(regPair);
        //   setRegister("A", readMemory(addr));
    }

    @Override
    public void stax(String regPair) throws SimulatorException {
        // TODO: memory[register_pair] = A
        //   int addr = getRegisterPair(regPair);
        //   writeMemory(addr, getRegister("A"));
    }

    @Override
    public void xchg() {
        // TODO: Swap D↔H and E↔L
        //
        // try {
        //     int tempD = getRegister("D");
        //     int tempE = getRegister("E");
        //     setRegister("D", getRegister("H"));
        //     setRegister("E", getRegister("L"));
        //     setRegister("H", tempD);
        //     setRegister("L", tempE);
        // } catch (SimulatorException e) {
        //     // Should never happen with valid register names
        //     e.printStackTrace();
        // }
    }

    // ─────────────────────────────────────────────────────────────
    //  ARITHMETIC INSTRUCTIONS
    // ─────────────────────────────────────────────────────────────

    @Override
    public void add(String src) throws SimulatorException {
        // TODO: A = A + src; update all flags
        //
        // int a = getRegister("A");
        // int operand = getOperandValue(src);
        // int result = alu.add(a, operand, 0);
        // setRegister("A", result);
    }

    @Override
    public void adc(String src) throws SimulatorException {
        // TODO: A = A + src + CY
        //
        // int a = getRegister("A");
        // int operand = getOperandValue(src);
        // int carry = alu.isCarryFlag() ? 1 : 0;
        // int result = alu.add(a, operand, carry);
        // setRegister("A", result);
    }

    @Override
    public void adi(int data) throws SimulatorException {
        // TODO: A = A + data
        //   Same as add() but operand is immediate data
    }

    @Override
    public void aci(int data) throws SimulatorException {
        // TODO: A = A + data + CY
    }

    @Override
    public void sub(String src) throws SimulatorException {
        // TODO: A = A - src
        //
        // int a = getRegister("A");
        // int operand = getOperandValue(src);
        // int result = alu.subtract(a, operand, 0);
        // setRegister("A", result);
    }

    @Override
    public void sbb(String src) throws SimulatorException {
        // TODO: A = A - src - CY
    }

    @Override
    public void sui(int data) throws SimulatorException {
        // TODO: A = A - data
    }

    @Override
    public void sbi(int data) throws SimulatorException {
        // TODO: A = A - data - CY
    }

    @Override
    public void inr(String dest) throws SimulatorException {
        // TODO: dest = dest + 1, flags except CY
        //
        // int value = getOperandValue(dest);
        // int result = alu.increment(value);
        // setOperandValue(dest, result);
    }

    @Override
    public void dcr(String dest) throws SimulatorException {
        // TODO: dest = dest - 1, flags except CY
        //
        // int value = getOperandValue(dest);
        // int result = alu.decrement(value);
        // setOperandValue(dest, result);
    }

    @Override
    public void inx(String regPair) throws SimulatorException {
        // TODO: regPair = regPair + 1 (16-bit), no flags
        //
        // int value = getRegisterPair(regPair);
        // value = (value + 1) & 0xFFFF;  // Wrap around at 0xFFFF → 0x0000
        // setRegisterPair(regPair, value);
    }

    @Override
    public void dcx(String regPair) throws SimulatorException {
        // TODO: regPair = regPair - 1 (16-bit), no flags
        //
        // int value = getRegisterPair(regPair);
        // value = (value - 1) & 0xFFFF;  // Wrap around at 0x0000 → 0xFFFF
        // setRegisterPair(regPair, value);
    }

    @Override
    public void dad(String regPair) throws SimulatorException {
        // TODO: HL = HL + regPair (16-bit add), only CY affected
        //
        // int hl = getRegisterPair("H");
        // int rp = getRegisterPair(regPair);
        // int result = hl + rp;
        // alu.setCarryFlag(result > 0xFFFF);  // CY if overflow
        // setRegisterPair("H", result & 0xFFFF);
    }

    @Override
    public void daa() {
        // TODO: Implement Decimal Adjust Accumulator for BCD arithmetic
        //
        // try {
        //     int a = getRegister("A");
        //     int correction = 0;
        //     boolean newCarry = alu.isCarryFlag();
        //
        //     // Step 1: Adjust lower nibble
        //     if ((a & 0x0F) > 9 || alu.isAuxCarryFlag()) {
        //         correction += 0x06;
        //     }
        //
        //     // Step 2: Adjust upper nibble
        //     if (((a + correction) >> 4) > 9 || alu.isCarryFlag()) {
        //         correction += 0x60;
        //         newCarry = true;
        //     }
        //
        //     int result = alu.add(a, correction, 0);
        //     alu.setCarryFlag(newCarry);
        //     setRegister("A", result);
        // } catch (SimulatorException e) {
        //     e.printStackTrace();
        // }
    }

    // ─────────────────────────────────────────────────────────────
    //  LOGICAL INSTRUCTIONS
    // ─────────────────────────────────────────────────────────────

    @Override
    public void ana(String src) throws SimulatorException {
        // TODO: A = A AND src
        //
        // int a = getRegister("A");
        // int operand = getOperandValue(src);
        // int result = alu.and(a, operand);
        // setRegister("A", result);
    }

    @Override
    public void ani(int data) throws SimulatorException {
        // TODO: A = A AND data
    }

    @Override
    public void ora(String src) throws SimulatorException {
        // TODO: A = A OR src
    }

    @Override
    public void ori(int data) throws SimulatorException {
        // TODO: A = A OR data
    }

    @Override
    public void xra(String src) throws SimulatorException {
        // TODO: A = A XOR src
    }

    @Override
    public void xri(int data) throws SimulatorException {
        // TODO: A = A XOR data
    }

    @Override
    public void cmp(String src) throws SimulatorException {
        // TODO: Compare A with src (subtract but don't store result)
        //
        // int a = getRegister("A");
        // int operand = getOperandValue(src);
        // alu.compare(a, operand);
        // NOTE: A is NOT modified!
    }

    @Override
    public void cpi(int data) throws SimulatorException {
        // TODO: Compare A with immediate data
    }

    @Override
    public void rlc() {
        // TODO: Rotate A left circular
        //
        // try {
        //     int a = getRegister("A");
        //     int bit7 = (a >> 7) & 1;
        //     int result = ((a << 1) | bit7) & 0xFF;
        //     alu.setCarryFlag(bit7 == 1);
        //     setRegister("A", result);
        // } catch (SimulatorException e) { e.printStackTrace(); }
    }

    @Override
    public void rrc() {
        // TODO: Rotate A right circular
        //
        // try {
        //     int a = getRegister("A");
        //     int bit0 = a & 1;
        //     int result = ((bit0 << 7) | (a >> 1)) & 0xFF;
        //     alu.setCarryFlag(bit0 == 1);
        //     setRegister("A", result);
        // } catch (SimulatorException e) { e.printStackTrace(); }
    }

    @Override
    public void ral() {
        // TODO: Rotate A left through carry
        //
        // try {
        //     int a = getRegister("A");
        //     int oldCY = alu.isCarryFlag() ? 1 : 0;
        //     alu.setCarryFlag(((a >> 7) & 1) == 1);
        //     int result = ((a << 1) | oldCY) & 0xFF;
        //     setRegister("A", result);
        // } catch (SimulatorException e) { e.printStackTrace(); }
    }

    @Override
    public void rar() {
        // TODO: Rotate A right through carry
    }

    @Override
    public void cma() {
        // TODO: A = ~A (complement)
        //
        // try {
        //     int a = getRegister("A");
        //     setRegister("A", (~a) & 0xFF);
        // } catch (SimulatorException e) { e.printStackTrace(); }
    }

    @Override
    public void cmc() {
        // TODO: CY = ~CY (complement carry)
        //   alu.setCarryFlag(!alu.isCarryFlag());
    }

    @Override
    public void stc() {
        // TODO: CY = 1
        //   alu.setCarryFlag(true);
    }

    // ─────────────────────────────────────────────────────────────
    //  BRANCHING INSTRUCTIONS
    // ─────────────────────────────────────────────────────────────

    @Override
    public void jmp(int address) throws SimulatorException {
        // TODO: PC = address (unconditional jump)
        //   programCounter = address;
    }

    @Override
    public void jc(int address) throws SimulatorException {
        // TODO: if CY == 1, PC = address
        //   if (alu.isCarryFlag()) programCounter = address;
    }

    @Override
    public void jnc(int address) throws SimulatorException {
        // TODO: if CY == 0, PC = address
    }

    @Override
    public void jz(int address) throws SimulatorException {
        // TODO: if Z == 1, PC = address
    }

    @Override
    public void jnz(int address) throws SimulatorException {
        // TODO: if Z == 0, PC = address
    }

    @Override
    public void jp(int address) throws SimulatorException {
        // TODO: if S == 0 (positive), PC = address
    }

    @Override
    public void jm(int address) throws SimulatorException {
        // TODO: if S == 1 (minus/negative), PC = address
    }

    @Override
    public void jpe(int address) throws SimulatorException {
        // TODO: if P == 1 (even parity), PC = address
    }

    @Override
    public void jpo(int address) throws SimulatorException {
        // TODO: if P == 0 (odd parity), PC = address
    }

    @Override
    public void call(int address) throws SimulatorException {
        // TODO: PUSH return address onto stack, then JMP
        //
        // LOGIC:
        //   1. The return address is the address of the instruction AFTER this CALL
        //      (which is PC + 3, since CALL is a 3-byte instruction).
        //      NOTE: By the time this method is called from executeInstruction(),
        //      PC may already be advanced — adjust accordingly.
        //   2. Push high byte of return address:
        //      stackPointer--;
        //      writeMemory(stackPointer, (returnAddr >> 8) & 0xFF);
        //   3. Push low byte:
        //      stackPointer--;
        //      writeMemory(stackPointer, returnAddr & 0xFF);
        //   4. PC = address
    }

    @Override public void cc(int address) throws SimulatorException {
        // TODO: if CY == 1, call(address)
    }
    @Override public void cnc(int address) throws SimulatorException {
        // TODO: if CY == 0, call(address)
    }
    @Override public void cz(int address) throws SimulatorException {
        // TODO: if Z == 1, call(address)
    }
    @Override public void cnz(int address) throws SimulatorException {
        // TODO: if Z == 0, call(address)
    }
    @Override public void cp(int address) throws SimulatorException {
        // TODO: if S == 0, call(address)
    }
    @Override public void cm(int address) throws SimulatorException {
        // TODO: if S == 1, call(address)
    }
    @Override public void cpe(int address) throws SimulatorException {
        // TODO: if P == 1, call(address)
    }
    @Override public void cpo(int address) throws SimulatorException {
        // TODO: if P == 0, call(address)
    }

    @Override
    public void ret() throws SimulatorException {
        // TODO: POP return address from stack, set PC
        //
        // int low  = readMemory(stackPointer); stackPointer++;
        // int high = readMemory(stackPointer); stackPointer++;
        // programCounter = (high << 8) | low;
    }

    @Override public void rc() throws SimulatorException {
        // TODO: if CY == 1, ret()
    }
    @Override public void rnc() throws SimulatorException {
        // TODO: if CY == 0, ret()
    }
    @Override public void rz() throws SimulatorException {
        // TODO: if Z == 1, ret()
    }
    @Override public void rnz() throws SimulatorException {
        // TODO: if Z == 0, ret()
    }
    @Override public void rp() throws SimulatorException {
        // TODO: if S == 0, ret()
    }
    @Override public void rm() throws SimulatorException {
        // TODO: if S == 1, ret()
    }
    @Override public void rpe() throws SimulatorException {
        // TODO: if P == 1, ret()
    }
    @Override public void rpo() throws SimulatorException {
        // TODO: if P == 0, ret()
    }

    @Override
    public void rst(int n) throws SimulatorException {
        // TODO: Push PC, then PC = n * 8
        //
        // if (n < 0 || n > 7)
        //     throw new SimulatorException("Invalid RST number: " + n);
        // call(n * 8);  // RST is essentially CALL to fixed addresses
    }

    @Override
    public void pchl() {
        // TODO: PC = HL
        //
        // try {
        //     programCounter = getHL();
        // } catch (SimulatorException e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────
    //  STACK, I/O, AND MACHINE CONTROL INSTRUCTIONS
    // ─────────────────────────────────────────────────────────────

    @Override
    public void push(String regPair) throws SimulatorException {
        // TODO: Push register pair onto stack
        //
        // int high, low;
        // if (regPair.equalsIgnoreCase("PSW")) {
        //     high = getRegister("A");
        //     low  = alu.getFlagsByte();
        // } else {
        //     int value = getRegisterPair(regPair);
        //     high = (value >> 8) & 0xFF;
        //     low  = value & 0xFF;
        // }
        // stackPointer--;
        // writeMemory(stackPointer, high);
        // stackPointer--;
        // writeMemory(stackPointer, low);
    }

    @Override
    public void pop(String regPair) throws SimulatorException {
        // TODO: Pop register pair from stack
        //
        // int low  = readMemory(stackPointer); stackPointer++;
        // int high = readMemory(stackPointer); stackPointer++;
        //
        // if (regPair.equalsIgnoreCase("PSW")) {
        //     setRegister("A", high);
        //     alu.setFlagsByte(low);
        // } else {
        //     setRegisterPair(regPair, (high << 8) | low);
        // }
    }

    @Override
    public void xthl() throws SimulatorException {
        // TODO: Exchange HL with top of stack
        //
        // int memLow  = readMemory(stackPointer);
        // int memHigh = readMemory(stackPointer + 1);
        //
        // writeMemory(stackPointer, getRegister("L"));
        // writeMemory(stackPointer + 1, getRegister("H"));
        //
        // setRegister("L", memLow);
        // setRegister("H", memHigh);
    }

    @Override
    public void sphl() {
        // TODO: SP = HL
        //
        // try {
        //     stackPointer = getHL();
        // } catch (SimulatorException e) { e.printStackTrace(); }
    }

    @Override
    public void in(int port) throws SimulatorException {
        // TODO: A = input from port
        //
        // int value = ioPorts.getOrDefault(port, 0x00);
        // setRegister("A", value);
    }

    @Override
    public void out(int port) throws SimulatorException {
        // TODO: Output A to port
        //
        // ioPorts.put(port, getRegister("A"));
    }

    @Override
    public void hlt() {
        // TODO: Set halted flag
        //   halted = true;
    }

    @Override
    public void nop() {
        // Do nothing — this is correct!
        // PC advancement happens in executeInstruction()
    }

    @Override
    public void ei() {
        // TODO: Enable interrupts
        //   interruptsEnabled = true;
    }

    @Override
    public void di() {
        // TODO: Disable interrupts
        //   interruptsEnabled = false;
    }

    @Override
    public void rim() {
        // TODO: Read Interrupt Mask into A (simplified)
        //   Basic implementation: setRegister("A", 0x00);
    }

    @Override
    public void sim() {
        // TODO: Set Interrupt Mask from A (simplified)
        //   Basic implementation: no-op for now
    }


    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  SECTION 9: STATE INSPECTION (for UserInterface)             ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * Returns a complete snapshot of the CPU state for display.
     *
     * @return Formatted multi-line string showing all registers, PC, SP, flags
     */
    public String getCPUState() {
        // TODO: Build and return formatted CPU state
        //
        // StringBuilder sb = new StringBuilder();
        // sb.append("╔══════════════════════════════════════╗\n");
        // sb.append("║         8085 CPU STATE               ║\n");
        // sb.append("╠══════════════════════════════════════╣\n");
        // sb.append(String.format("║  A  = %02XH                          ║\n", getRegister("A")));
        // sb.append(String.format("║  B  = %02XH   C  = %02XH              ║\n", ...));
        // sb.append(String.format("║  D  = %02XH   E  = %02XH              ║\n", ...));
        // sb.append(String.format("║  H  = %02XH   L  = %02XH              ║\n", ...));
        // sb.append(String.format("║  SP = %04XH  PC = %04XH            ║\n", stackPointer, programCounter));
        // sb.append("║  Flags: " + alu.flagsToString() + "  ║\n");
        // sb.append("╚══════════════════════════════════════╝\n");
        // return sb.toString();
        return "CPU state display not implemented"; // placeholder
    }

    /**
     * Resets the entire CPU to power-on state.
     *
     * LOGIC:
     *   1. Reset all registers to 0
     *   2. Reset PC = 0x0000, SP = 0xFFFF
     *   3. Reset all flags
     *   4. Reset memory
     *   5. Clear halted flag
     */
    public void reset() {
        // TODO: Full CPU reset
        //
        // initRegisters();
        // programCounter = 0x0000;
        // stackPointer = 0xFFFF;
        // alu.resetFlags();
        // resetMemory();
        // halted = false;
        // interruptsEnabled = false;
        // ioPorts.clear();
    }

    // ═══════════════════════════════════════════════════════════════
    //  GETTERS for UserInterface access
    // ═══════════════════════════════════════════════════════════════

    public int getProgramCounter() { return programCounter; }
    public void setProgramCounter(int pc) { this.programCounter = pc & 0xFFFF; }

    public int getStackPointer() { return stackPointer; }
    public void setStackPointer(int sp) { this.stackPointer = sp & 0xFFFF; }

    public boolean isHalted() { return halted; }
    public ALU getALU() { return alu; }

    public HashMap<String, Integer> getGeneralRegisters() { return generalRegisters; }
    public HashMap<String, Integer> getSpecialRegisters() { return specialRegisters; }
}
