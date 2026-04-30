interface Memory {
    int DEFAULT_START_ADDRESS = 0x0000;
    int DEFAULT_END_ADDRESS = 0xFFFF;
    int MAX_MEMORY_SIZE = 65536;

    int readMemory(int address) throws SimulatorException;

    void writeMemory(int address, int data) throws SimulatorException;

    void resetMemory();

    int[] getMemoryDump();

    int getMemoryStart();

    int getMemoryEnd();

    void loadProgram(int startAddress, int[] data) throws SimulatorException;

    void displayMemoryRange(int fromAddress, int toAddress);
}

interface InstrucSet {

    // Data Transfer
    void mov(String dest, String src) throws SimulatorException; // MOV dest,src: copy src to dest

    void mvi(String dest, int data) throws SimulatorException; // MVI dest,d8: load immediate byte

    void lxi(String regPair, int data16) throws SimulatorException;// LXI rp,d16: load 16-bit immediate

    void lda(int address) throws SimulatorException; // LDA addr: A = mem[addr]

    void sta(int address) throws SimulatorException; // STA addr: mem[addr] = A

    void lhld(int address) throws SimulatorException; // LHLD addr: L=mem[a], H=mem[a+1]

    void shld(int address) throws SimulatorException; // SHLD addr: mem[a]=L, mem[a+1]=H

    void ldax(String regPair) throws SimulatorException; // LDAX rp: A = mem[rp]

    void stax(String regPair) throws SimulatorException; // STAX rp: mem[rp] = A

    void xchg(); // XCHG: swap DE and HL

    // Arithmetic
    void add(String src) throws SimulatorException; // ADD src: A = A + src, all flags

    void adc(String src) throws SimulatorException; // ADC src: A = A + src + CY

    void adi(int data) throws SimulatorException; // ADI d8: A = A + d8

    void aci(int data) throws SimulatorException; // ACI d8: A = A + d8 + CY

    void sub(String src) throws SimulatorException; // SUB src: A = A - src

    void sbb(String src) throws SimulatorException; // SBB src: A = A - src - CY

    void sui(int data) throws SimulatorException; // SUI d8: A = A - d8

    void sbi(int data) throws SimulatorException; // SBI d8: A = A - d8 - CY

    void inr(String dest) throws SimulatorException; // INR dest: dest++, CY unchanged

    void dcr(String dest) throws SimulatorException; // DCR dest: dest--, CY unchanged

    void inx(String regPair) throws SimulatorException;// INX rp: rp++, no flags

    void dcx(String regPair) throws SimulatorException;// DCX rp: rp--, no flags

    void dad(String regPair) throws SimulatorException;// DAD rp: HL = HL + rp, only CY

    void daa(); // DAA: decimal adjust A for BCD

    // Logical
    void ana(String src) throws SimulatorException; // ANA src: A = A & src, CY=0 AC=1

    void ani(int data) throws SimulatorException; // ANI d8: A = A & d8

    void ora(String src) throws SimulatorException; // ORA src: A = A | src, CY=0 AC=0

    void ori(int data) throws SimulatorException; // ORI d8: A = A | d8

    void xra(String src) throws SimulatorException; // XRA src: A = A ^ src, CY=0 AC=0

    void xri(int data) throws SimulatorException; // XRI d8: A = A ^ d8

    void cmp(String src) throws SimulatorException; // CMP src: flags from A-src, A unchanged

    void cpi(int data) throws SimulatorException; // CPI d8: flags from A-d8, A unchanged

    void rlc(); // RLC: rotate A left circular, CY = old bit7

    void rrc(); // RRC: rotate A right circular, CY = old bit0

    void ral(); // RAL: rotate A left through CY

    void rar(); // RAR: rotate A right through CY

    void cma(); // CMA: A = ~A (complement)

    void cmc(); // CMC: CY = ~CY

    void stc(); // STC: CY = 1

    // Branching
    void jmp(int address) throws SimulatorException; // JMP addr: unconditional

    void jc(int address) throws SimulatorException; // JC: if CY=1

    void jnc(int address) throws SimulatorException; // JNC: if CY=0

    void jz(int address) throws SimulatorException; // JZ: if Z=1

    void jnz(int address) throws SimulatorException; // JNZ: if Z=0

    void jp(int address) throws SimulatorException; // JP: if S=0 (positive)

    void jm(int address) throws SimulatorException; // JM: if S=1 (minus)

    void jpe(int address) throws SimulatorException; // JPE: if P=1 (even parity)

    void jpo(int address) throws SimulatorException; // JPO: if P=0 (odd parity)

    void call(int address) throws SimulatorException; // CALL: push PC+3, jump

    void cc(int address) throws SimulatorException;

    void cnc(int address) throws SimulatorException;

    void cz(int address) throws SimulatorException;

    void cnz(int address) throws SimulatorException;

    void cp(int address) throws SimulatorException;

    void cm(int address) throws SimulatorException;

    void cpe(int address) throws SimulatorException;

    void cpo(int address) throws SimulatorException;

    void ret() throws SimulatorException; // RET: pop PC from stack

    void rc() throws SimulatorException;

    void rnc() throws SimulatorException;

    void rz() throws SimulatorException;

    void rnz() throws SimulatorException;

    void rp() throws SimulatorException;

    void rm() throws SimulatorException;

    void rpe() throws SimulatorException;

    void rpo() throws SimulatorException;

    void rst(int n) throws SimulatorException; // RST n: call n*8

    void pchl(); // PCHL: PC = HL

    // Stack, I/O, Machine Control
    void push(String regPair) throws SimulatorException; // PUSH rp: push pair onto stack

    void pop(String regPair) throws SimulatorException; // POP rp: pop pair from stack

    void xthl() throws SimulatorException; // XTHL: exchange HL with top of stack

    void sphl(); // SPHL: SP = HL

    void in(int port) throws SimulatorException; // IN port: A = port[port]

    void out(int port) throws SimulatorException; // OUT port: port[port] = A

    void hlt(); // HLT: halt processor

    void nop(); // NOP: no operation

    void ei(); // EI: enable interrupts

    void di(); // DI: disable interrupts

    void rim(); // RIM: read interrupt mask

    void sim(); // SIM: set interrupt mask
}

class SimulatorException extends Exception {
    public enum ErrorType {
        InvalidMemoryAddress, // Address outside 0x0000-0xFFFF or allocated range
        InvalidRegister, // Unrecognized register name
        InvalidOpcode, // Unrecognized instruction opcode
        InvalidData, // Data value outside valid range (e.g., > 0xFF for 8-bit)
        StackOverflow, // Stack pointer went below 0x0000
        StackUnderflow, // Stack pointer went above initial SP value
        MemoryOutOfBound, // Program tried to access memory outside range
        HaltEncounter, // HLT instruction executed (not really an "error")
        SyntaxError, // Assembly syntax is invalid
        UndefinedAddress, // Jump/call to a label that doesn't exist
        StepLimitExceeded // [AG-FIX 1.13] Execution step limit hit (probable infinite loop)
    }

    private final ErrorType errorType;
    private final int address;

    public SimulatorException(String message) {
        super(message);
        this.errorType = ErrorType.InvalidData; // default
        this.address = -1;
    }

    public SimulatorException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
        this.address = -1;
    }

    public SimulatorException(String message, ErrorType errorType, int address) {
        super(message);
        this.errorType = errorType;
        this.address = address;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public int getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return String.format("[%s]%s: %s",
                errorType,
                (address != -1) ? String.format(" at 0x%04X", address) : "",
                getMessage());
    }
}

// ---------------------------------------------------------------------------
// CPUSnapshot — Immutable CPU state for thread-safe EDT reads
// [AG-FIX 1.1]
// ---------------------------------------------------------------------------
final class CPUSnapshot {
    public final int pc, sp, a, b, c, d, e, h, l;
    public final boolean fS, fZ, fAC, fP, fCY, halted;
    public final int[] mem;
    public final int memBase;
    // [AG-FIX 3.5] I/O Ports snapshot
    public final java.util.Map<Integer, Integer> ioPorts;

    public CPUSnapshot(int pc, int sp, int a, int b, int c, int d, int e, int h, int l,
            boolean fS, boolean fZ, boolean fAC, boolean fP, boolean fCY, boolean halted,
            int[] mem, int memBase, java.util.Map<Integer, Integer> ioPorts) {
        this.pc = pc;
        this.sp = sp;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.h = h;
        this.l = l;
        this.fS = fS;
        this.fZ = fZ;
        this.fAC = fAC;
        this.fP = fP;
        this.fCY = fCY;
        this.halted = halted;
        this.mem = mem;
        this.memBase = memBase;
        this.ioPorts = ioPorts;
    }
}

// ---------------------------------------------------------------------------
// ALU — Arithmetic Logic Unit with 5-flag register (S, Z, AC, P, CY)
// ---------------------------------------------------------------------------
class ALU {
    private boolean signFlag;
    private boolean zeroFlag;
    private boolean auxCarryFlag;
    private boolean parityFlag;
    private boolean carryFlag;

    public ALU() {
        resetFlags();
    }

    public void resetFlags() {
        signFlag = false;
        zeroFlag = false;
        auxCarryFlag = false;
        parityFlag = false;
        carryFlag = false;
    }

    // Updates all five flags based on a computation result
    public void updateAllFlags(int result, int op1Lower, int op2Lower, boolean isSubtraction) {
        if (isSubtraction) {
            carryFlag = (result < 0);
            auxCarryFlag = (op1Lower - op2Lower) < 0;
        } else {
            carryFlag = (result > 0xFF);
            auxCarryFlag = (op1Lower + op2Lower) > 0x0F;
        }
        int masked = result & 0xFF;
        zeroFlag = (masked == 0);
        signFlag = ((masked & 0x80) != 0);
        parityFlag = (Integer.bitCount(masked) % 2 == 0);
    }

    // Updates S, Z, AC, P but preserves CY — used by INR/DCR
    public void updateFlagsExceptCarry(int result, int op1Lower, int op2Lower, boolean isSubtraction) {
        boolean savedCy = carryFlag;
        updateAllFlags(result, op1Lower, op2Lower, isSubtraction);
        carryFlag = savedCy;
    }

    // Updates only S, Z, P from an 8-bit result — used by logical ops
    private void updateSzp(int result) {
        int masked = result & 0xFF;
        signFlag = ((masked & 0x80) != 0);
        zeroFlag = (masked == 0);
        parityFlag = (Integer.bitCount(masked) % 2 == 0);
    }

    // Arithmetic: result = a + b + carryIn, updates all flags, returns 8-bit
    // [AG-FIX] AC: mask b BEFORE adding carry to prevent nibble wrap
    public int add(int a, int b, int carryIn) {
        int result = a + b + carryIn;
        updateAllFlags(result, a & 0x0F, (b & 0x0F) + carryIn, false);
        return result & 0xFF;
    }

    // Arithmetic: result = a - b - borrowIn, updates all flags, returns 8-bit
    // [AG-FIX] AC: mask b BEFORE adding borrow to prevent nibble wrap
    public int subtract(int a, int b, int borrowIn) {
        int result = a - b - borrowIn;
        updateAllFlags(result, a & 0x0F, (b & 0x0F) + borrowIn, true);
        return result & 0xFF;
    }

    // Increment: result = value + 1, CY unchanged
    public int increment(int value) {
        int result = value + 1;
        updateFlagsExceptCarry(result, value & 0x0F, 1, false);
        return result & 0xFF;
    }

    // Decrement: result = value - 1, CY unchanged
    public int decrement(int value) {
        int result = value - 1;
        updateFlagsExceptCarry(result, value & 0x0F, 1, true);
        return result & 0xFF;
    }

    // AND: CY=0, AC=1, update S/Z/P
    public int and(int a, int b) {
        int result = (a & b) & 0xFF;
        carryFlag = false;
        auxCarryFlag = true;
        updateSzp(result);
        return result;
    }

    // OR: CY=0, AC=0, update S/Z/P
    public int or(int a, int b) {
        int result = (a | b) & 0xFF;
        carryFlag = false;
        auxCarryFlag = false;
        updateSzp(result);
        return result;
    }

    // XOR: CY=0, AC=0, update S/Z/P
    public int xor(int a, int b) {
        int result = (a ^ b) & 0xFF;
        carryFlag = false;
        auxCarryFlag = false;
        updateSzp(result);
        return result;
    }

    // Compare: flags from (a - b) but result discarded
    public void compare(int a, int b) {
        subtract(a, b, 0);
    }

    // Compose flag register byte: [S Z 0 AC 0 P 1 CY]
    public int getFlagsByte() {
        int flags = 0x02; // bit 1 always 1
        if (signFlag)
            flags |= 0x80;
        if (zeroFlag)
            flags |= 0x40;
        if (auxCarryFlag)
            flags |= 0x10;
        if (parityFlag)
            flags |= 0x04;
        if (carryFlag)
            flags |= 0x01;
        return flags;
    }

    // Decompose flag register byte back into booleans
    public void setFlagsByte(int flagsByte) {
        signFlag = (flagsByte & 0x80) != 0;
        zeroFlag = (flagsByte & 0x40) != 0;
        auxCarryFlag = (flagsByte & 0x10) != 0;
        parityFlag = (flagsByte & 0x04) != 0;
        carryFlag = (flagsByte & 0x01) != 0;
    }

    public String flagsToString() {
        return String.format("S=%d Z=%d AC=%d P=%d CY=%d",
                signFlag ? 1 : 0, zeroFlag ? 1 : 0, auxCarryFlag ? 1 : 0,
                parityFlag ? 1 : 0, carryFlag ? 1 : 0);
    }

    // Getters
    public boolean isSignFlag() {
        return signFlag;
    }

    public boolean isZeroFlag() {
        return zeroFlag;
    }

    public boolean isAuxCarryFlag() {
        return auxCarryFlag;
    }

    public boolean isParityFlag() {
        return parityFlag;
    }

    public boolean isCarryFlag() {
        return carryFlag;
    }

    // Setters
    public void setSignFlag(boolean val) {
        signFlag = val;
    }

    public void setZeroFlag(boolean val) {
        zeroFlag = val;
    }

    public void setAuxCarryFlag(boolean val) {
        auxCarryFlag = val;
    }

    public void setParityFlag(boolean val) {
        parityFlag = val;
    }

    public void setCarryFlag(boolean val) {
        carryFlag = val;
    }
}
