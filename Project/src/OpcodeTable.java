import java.util.HashMap;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                   OPCODE TABLE — 8085 SIMULATOR                          ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  PURPOSE:                                                                ║
 * ║  A lookup table mapping every 8085 opcode (0x00 - 0xFF) to its          ║
 * ║  mnemonic name, byte size, and description.                              ║
 * ║                                                                          ║
 * ║  USED BY:                                                                ║
 * ║  - Assembler: to convert mnemonics → opcodes                            ║
 * ║  - Architecture: to disassemble opcodes → mnemonics for display         ║
 * ║  - UserInterface: to show the current instruction being executed         ║
 * ║                                                                          ║
 * ║  THE 8085 OPCODE MAP:                                                    ║
 * ║  The 8085 uses a single-byte opcode scheme (0x00-0xFF = 256 codes).     ║
 * ║  Some opcodes have operand bytes that follow:                            ║
 * ║    1-byte instructions: just the opcode (e.g., MOV A,B = 0x78)          ║
 * ║    2-byte instructions: opcode + 8-bit data (e.g., MVI A,42H = 0x3E,0x42)║
 * ║    3-byte instructions: opcode + 16-bit addr (e.g., JMP 2050H)          ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
public class OpcodeTable {

    // ═══════════════════════════════════════════════════════════════
    //  OPCODE ENTRY — Inner class to hold info about each opcode
    // ═══════════════════════════════════════════════════════════════

    /**
     * Represents one entry in the opcode table.
     */
    public static class OpcodeEntry {
        public final int opcode;       // e.g., 0x78
        public final String mnemonic;  // e.g., "MOV A,B"
        public final int byteSize;     // 1, 2, or 3
        public final String category;  // "DATA_TRANSFER", "ARITHMETIC", etc.

        public OpcodeEntry(int opcode, String mnemonic, int byteSize, String category) {
            this.opcode = opcode;
            this.mnemonic = mnemonic;
            this.byteSize = byteSize;
            this.category = category;
        }

        @Override
        public String toString() {
            return String.format("0x%02X: %-12s (%d byte%s) [%s]",
                    opcode, mnemonic, byteSize, byteSize > 1 ? "s" : "", category);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  THE TABLE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Map of opcode value → OpcodeEntry.
     * Populated in the static initializer block.
     */
    private static final HashMap<Integer, OpcodeEntry> table = new HashMap<>();

    /**
     * Reverse map: mnemonic → opcode value.
     * Used by the Assembler for mnemonic-to-opcode lookups.
     */
    private static final HashMap<String, Integer> mnemonicMap = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════
    //  STATIC INITIALIZER — Populates the table with ALL opcodes
    // ═══════════════════════════════════════════════════════════════

    static {
        // TODO: Populate the entire opcode table.
        //
        // This is a large but straightforward data-entry task.
        // Below are the key entries organized by category.
        // The team should fill in ALL 256 entries.
        //
        // FORMAT: addEntry(opcode, mnemonic, byteSize, category);

        // ─── DATA TRANSFER ─────────────────────────────────────
        addEntry(0x00, "NOP", 1, "MACHINE_CONTROL");

        addEntry(0x01, "LXI B", 3, "DATA_TRANSFER");
        addEntry(0x02, "STAX B", 1, "DATA_TRANSFER");
        addEntry(0x06, "MVI B", 2, "DATA_TRANSFER");
        addEntry(0x0A, "LDAX B", 1, "DATA_TRANSFER");
        addEntry(0x0E, "MVI C", 2, "DATA_TRANSFER");

        addEntry(0x11, "LXI D", 3, "DATA_TRANSFER");
        addEntry(0x12, "STAX D", 1, "DATA_TRANSFER");
        addEntry(0x16, "MVI D", 2, "DATA_TRANSFER");
        addEntry(0x1A, "LDAX D", 1, "DATA_TRANSFER");
        addEntry(0x1E, "MVI E", 2, "DATA_TRANSFER");

        addEntry(0x21, "LXI H", 3, "DATA_TRANSFER");
        addEntry(0x22, "SHLD", 3, "DATA_TRANSFER");
        addEntry(0x26, "MVI H", 2, "DATA_TRANSFER");
        addEntry(0x2A, "LHLD", 3, "DATA_TRANSFER");
        addEntry(0x2E, "MVI L", 2, "DATA_TRANSFER");

        addEntry(0x31, "LXI SP", 3, "DATA_TRANSFER");
        addEntry(0x32, "STA", 3, "DATA_TRANSFER");
        addEntry(0x36, "MVI M", 2, "DATA_TRANSFER");
        addEntry(0x3A, "LDA", 3, "DATA_TRANSFER");
        addEntry(0x3E, "MVI A", 2, "DATA_TRANSFER");

        // MOV instructions: 0x40 - 0x7F (except 0x76 = HLT)
        // TODO: Add all 63 MOV opcodes
        //   Pattern: opcode = 0x40 + (destCode << 3) + srcCode
        //   Register codes: B=0, C=1, D=2, E=3, H=4, L=5, M=6, A=7
        //
        //   Example entries:
        addEntry(0x40, "MOV B,B", 1, "DATA_TRANSFER");
        addEntry(0x41, "MOV B,C", 1, "DATA_TRANSFER");
        // ... (fill in 0x42 through 0x75, skip 0x76) ...
        addEntry(0x77, "MOV M,A", 1, "DATA_TRANSFER");
        addEntry(0x78, "MOV A,B", 1, "DATA_TRANSFER");
        addEntry(0x79, "MOV A,C", 1, "DATA_TRANSFER");
        addEntry(0x7A, "MOV A,D", 1, "DATA_TRANSFER");
        addEntry(0x7B, "MOV A,E", 1, "DATA_TRANSFER");
        addEntry(0x7C, "MOV A,H", 1, "DATA_TRANSFER");
        addEntry(0x7D, "MOV A,L", 1, "DATA_TRANSFER");
        addEntry(0x7E, "MOV A,M", 1, "DATA_TRANSFER");
        addEntry(0x7F, "MOV A,A", 1, "DATA_TRANSFER");

        addEntry(0xEB, "XCHG", 1, "DATA_TRANSFER");

        // ─── ARITHMETIC ────────────────────────────────────────
        addEntry(0x03, "INX B", 1, "ARITHMETIC");
        addEntry(0x04, "INR B", 1, "ARITHMETIC");
        addEntry(0x05, "DCR B", 1, "ARITHMETIC");
        addEntry(0x09, "DAD B", 1, "ARITHMETIC");
        addEntry(0x0B, "DCX B", 1, "ARITHMETIC");
        addEntry(0x0C, "INR C", 1, "ARITHMETIC");
        addEntry(0x0D, "DCR C", 1, "ARITHMETIC");

        // TODO: Add remaining INR, DCR, INX, DCX, DAD entries for D, H, SP pairs

        // ADD instructions: 0x80-0x87
        addEntry(0x80, "ADD B", 1, "ARITHMETIC");
        addEntry(0x81, "ADD C", 1, "ARITHMETIC");
        addEntry(0x82, "ADD D", 1, "ARITHMETIC");
        addEntry(0x83, "ADD E", 1, "ARITHMETIC");
        addEntry(0x84, "ADD H", 1, "ARITHMETIC");
        addEntry(0x85, "ADD L", 1, "ARITHMETIC");
        addEntry(0x86, "ADD M", 1, "ARITHMETIC");
        addEntry(0x87, "ADD A", 1, "ARITHMETIC");

        // ADC: 0x88-0x8F
        // TODO: Add all ADC entries (same pattern as ADD)

        // SUB: 0x90-0x97
        addEntry(0x90, "SUB B", 1, "ARITHMETIC");
        // TODO: Add 0x91-0x97

        // SBB: 0x98-0x9F
        // TODO: Add all SBB entries

        addEntry(0x27, "DAA", 1, "ARITHMETIC");
        addEntry(0xC6, "ADI", 2, "ARITHMETIC");
        addEntry(0xCE, "ACI", 2, "ARITHMETIC");
        addEntry(0xD6, "SUI", 2, "ARITHMETIC");
        addEntry(0xDE, "SBI", 2, "ARITHMETIC");

        // ─── LOGICAL ───────────────────────────────────────────
        // ANA: 0xA0-0xA7
        addEntry(0xA0, "ANA B", 1, "LOGICAL");
        // TODO: Add 0xA1-0xA7

        // XRA: 0xA8-0xAF
        addEntry(0xA8, "XRA B", 1, "LOGICAL");
        addEntry(0xAF, "XRA A", 1, "LOGICAL");
        // TODO: Add rest

        // ORA: 0xB0-0xB7
        addEntry(0xB0, "ORA B", 1, "LOGICAL");
        // TODO: Add rest

        // CMP: 0xB8-0xBF
        addEntry(0xB8, "CMP B", 1, "LOGICAL");
        // TODO: Add rest

        addEntry(0xE6, "ANI", 2, "LOGICAL");
        addEntry(0xEE, "XRI", 2, "LOGICAL");
        addEntry(0xF6, "ORI", 2, "LOGICAL");
        addEntry(0xFE, "CPI", 2, "LOGICAL");

        addEntry(0x07, "RLC", 1, "LOGICAL");
        addEntry(0x0F, "RRC", 1, "LOGICAL");
        addEntry(0x17, "RAL", 1, "LOGICAL");
        addEntry(0x1F, "RAR", 1, "LOGICAL");
        addEntry(0x2F, "CMA", 1, "LOGICAL");
        addEntry(0x37, "STC", 1, "LOGICAL");
        addEntry(0x3F, "CMC", 1, "LOGICAL");

        // ─── BRANCHING ─────────────────────────────────────────
        addEntry(0xC3, "JMP", 3, "BRANCHING");
        addEntry(0xC2, "JNZ", 3, "BRANCHING");
        addEntry(0xCA, "JZ", 3, "BRANCHING");
        addEntry(0xD2, "JNC", 3, "BRANCHING");
        addEntry(0xDA, "JC", 3, "BRANCHING");
        addEntry(0xE2, "JPO", 3, "BRANCHING");
        addEntry(0xEA, "JPE", 3, "BRANCHING");
        addEntry(0xF2, "JP", 3, "BRANCHING");
        addEntry(0xFA, "JM", 3, "BRANCHING");

        addEntry(0xCD, "CALL", 3, "BRANCHING");
        addEntry(0xC4, "CNZ", 3, "BRANCHING");
        addEntry(0xCC, "CZ", 3, "BRANCHING");
        addEntry(0xD4, "CNC", 3, "BRANCHING");
        addEntry(0xDC, "CC", 3, "BRANCHING");
        addEntry(0xE4, "CPO", 3, "BRANCHING");
        addEntry(0xEC, "CPE", 3, "BRANCHING");
        addEntry(0xF4, "CP", 3, "BRANCHING");
        addEntry(0xFC, "CM", 3, "BRANCHING");

        addEntry(0xC9, "RET", 1, "BRANCHING");
        addEntry(0xC0, "RNZ", 1, "BRANCHING");
        addEntry(0xC8, "RZ", 1, "BRANCHING");
        addEntry(0xD0, "RNC", 1, "BRANCHING");
        addEntry(0xD8, "RC", 1, "BRANCHING");
        addEntry(0xE0, "RPO", 1, "BRANCHING");
        addEntry(0xE8, "RPE", 1, "BRANCHING");
        addEntry(0xF0, "RP", 1, "BRANCHING");
        addEntry(0xF8, "RM", 1, "BRANCHING");

        addEntry(0xE9, "PCHL", 1, "BRANCHING");

        // RST instructions
        addEntry(0xC7, "RST 0", 1, "BRANCHING");
        addEntry(0xCF, "RST 1", 1, "BRANCHING");
        addEntry(0xD7, "RST 2", 1, "BRANCHING");
        addEntry(0xDF, "RST 3", 1, "BRANCHING");
        addEntry(0xE7, "RST 4", 1, "BRANCHING");
        addEntry(0xEF, "RST 5", 1, "BRANCHING");
        addEntry(0xF7, "RST 6", 1, "BRANCHING");
        addEntry(0xFF, "RST 7", 1, "BRANCHING");

        // ─── STACK / IO / MACHINE CONTROL ──────────────────────
        addEntry(0xC5, "PUSH B", 1, "STACK_IO_MACHINE");
        addEntry(0xD5, "PUSH D", 1, "STACK_IO_MACHINE");
        addEntry(0xE5, "PUSH H", 1, "STACK_IO_MACHINE");
        addEntry(0xF5, "PUSH PSW", 1, "STACK_IO_MACHINE");

        addEntry(0xC1, "POP B", 1, "STACK_IO_MACHINE");
        addEntry(0xD1, "POP D", 1, "STACK_IO_MACHINE");
        addEntry(0xE1, "POP H", 1, "STACK_IO_MACHINE");
        addEntry(0xF1, "POP PSW", 1, "STACK_IO_MACHINE");

        addEntry(0xE3, "XTHL", 1, "STACK_IO_MACHINE");
        addEntry(0xF9, "SPHL", 1, "STACK_IO_MACHINE");

        addEntry(0xDB, "IN", 2, "STACK_IO_MACHINE");
        addEntry(0xD3, "OUT", 2, "STACK_IO_MACHINE");

        addEntry(0x76, "HLT", 1, "MACHINE_CONTROL");
        addEntry(0xFB, "EI", 1, "MACHINE_CONTROL");
        addEntry(0xF3, "DI", 1, "MACHINE_CONTROL");
        addEntry(0x20, "RIM", 1, "MACHINE_CONTROL");
        addEntry(0x30, "SIM", 1, "MACHINE_CONTROL");
    }

    // ═══════════════════════════════════════════════════════════════
    //  TABLE MANAGEMENT METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Adds an entry to both the opcode table and the reverse mnemonic map.
     */
    private static void addEntry(int opcode, String mnemonic, int byteSize, String category) {
        table.put(opcode, new OpcodeEntry(opcode, mnemonic, byteSize, category));
        mnemonicMap.put(mnemonic.toUpperCase(), opcode);
    }

    /**
     * Looks up the mnemonic for a given opcode.
     *
     * @param opcode  The opcode byte (0x00-0xFF)
     * @return        Mnemonic string, or "???" if unknown
     */
    public static String getMnemonic(int opcode) {
        OpcodeEntry entry = table.get(opcode);
        return (entry != null) ? entry.mnemonic : "???";
    }

    /**
     * Looks up the opcode for a given mnemonic.
     *
     * @param mnemonic  The mnemonic string (e.g., "MOV A,B")
     * @return          Opcode value, or -1 if unknown
     */
    public static int getOpcode(String mnemonic) {
        Integer opcode = mnemonicMap.get(mnemonic.toUpperCase());
        return (opcode != null) ? opcode : -1;
    }

    /**
     * Returns the byte size of an instruction given its opcode.
     *
     * @param opcode  The opcode byte
     * @return        1, 2, or 3 (or 1 as default for unknown)
     */
    public static int getByteSize(int opcode) {
        OpcodeEntry entry = table.get(opcode);
        return (entry != null) ? entry.byteSize : 1;
    }

    /**
     * Returns the full OpcodeEntry for detailed information.
     */
    public static OpcodeEntry getEntry(int opcode) {
        return table.get(opcode);
    }

    /**
     * Prints the entire opcode table (for debugging/reference).
     */
    public static void printTable() {
        // TODO: Print all entries sorted by opcode value
        //
        // table.entrySet().stream()
        //     .sorted(Map.Entry.comparingByKey())
        //     .forEach(e -> System.out.println(e.getValue()));
    }
}
