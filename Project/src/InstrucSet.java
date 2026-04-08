// InstrucSet: All 74 mnemonics of the Intel 8085 instruction set
public interface InstrucSet {

    // Data Transfer
    void mov(String dest, String src) throws SimulatorException;   // MOV dest,src: copy src to dest
    void mvi(String dest, int data) throws SimulatorException;     // MVI dest,d8: load immediate byte
    void lxi(String regPair, int data16) throws SimulatorException;// LXI rp,d16: load 16-bit immediate
    void lda(int address) throws SimulatorException;               // LDA addr: A = mem[addr]
    void sta(int address) throws SimulatorException;               // STA addr: mem[addr] = A
    void lhld(int address) throws SimulatorException;              // LHLD addr: L=mem[a], H=mem[a+1]
    void shld(int address) throws SimulatorException;              // SHLD addr: mem[a]=L, mem[a+1]=H
    void ldax(String regPair) throws SimulatorException;           // LDAX rp: A = mem[rp]
    void stax(String regPair) throws SimulatorException;           // STAX rp: mem[rp] = A
    void xchg();                                                   // XCHG: swap DE and HL

    // Arithmetic
    void add(String src) throws SimulatorException;   // ADD src: A = A + src, all flags
    void adc(String src) throws SimulatorException;   // ADC src: A = A + src + CY
    void adi(int data) throws SimulatorException;     // ADI d8: A = A + d8
    void aci(int data) throws SimulatorException;     // ACI d8: A = A + d8 + CY
    void sub(String src) throws SimulatorException;   // SUB src: A = A - src
    void sbb(String src) throws SimulatorException;   // SBB src: A = A - src - CY
    void sui(int data) throws SimulatorException;     // SUI d8: A = A - d8
    void sbi(int data) throws SimulatorException;     // SBI d8: A = A - d8 - CY
    void inr(String dest) throws SimulatorException;  // INR dest: dest++, CY unchanged
    void dcr(String dest) throws SimulatorException;  // DCR dest: dest--, CY unchanged
    void inx(String regPair) throws SimulatorException;// INX rp: rp++, no flags
    void dcx(String regPair) throws SimulatorException;// DCX rp: rp--, no flags
    void dad(String regPair) throws SimulatorException;// DAD rp: HL = HL + rp, only CY
    void daa();                                        // DAA: decimal adjust A for BCD

    // Logical
    void ana(String src) throws SimulatorException;   // ANA src: A = A & src, CY=0 AC=1
    void ani(int data) throws SimulatorException;     // ANI d8: A = A & d8
    void ora(String src) throws SimulatorException;   // ORA src: A = A | src, CY=0 AC=0
    void ori(int data) throws SimulatorException;     // ORI d8: A = A | d8
    void xra(String src) throws SimulatorException;   // XRA src: A = A ^ src, CY=0 AC=0
    void xri(int data) throws SimulatorException;     // XRI d8: A = A ^ d8
    void cmp(String src) throws SimulatorException;   // CMP src: flags from A-src, A unchanged
    void cpi(int data) throws SimulatorException;     // CPI d8: flags from A-d8, A unchanged
    void rlc();   // RLC: rotate A left circular, CY = old bit7
    void rrc();   // RRC: rotate A right circular, CY = old bit0
    void ral();   // RAL: rotate A left through CY
    void rar();   // RAR: rotate A right through CY
    void cma();   // CMA: A = ~A (complement)
    void cmc();   // CMC: CY = ~CY
    void stc();   // STC: CY = 1

    // Branching
    void jmp(int address) throws SimulatorException;  // JMP addr: unconditional
    void jc(int address) throws SimulatorException;   // JC: if CY=1
    void jnc(int address) throws SimulatorException;  // JNC: if CY=0
    void jz(int address) throws SimulatorException;   // JZ: if Z=1
    void jnz(int address) throws SimulatorException;  // JNZ: if Z=0
    void jp(int address) throws SimulatorException;   // JP: if S=0 (positive)
    void jm(int address) throws SimulatorException;   // JM: if S=1 (minus)
    void jpe(int address) throws SimulatorException;  // JPE: if P=1 (even parity)
    void jpo(int address) throws SimulatorException;  // JPO: if P=0 (odd parity)
    void call(int address) throws SimulatorException; // CALL: push PC+3, jump
    void cc(int address) throws SimulatorException;
    void cnc(int address) throws SimulatorException;
    void cz(int address) throws SimulatorException;
    void cnz(int address) throws SimulatorException;
    void cp(int address) throws SimulatorException;
    void cm(int address) throws SimulatorException;
    void cpe(int address) throws SimulatorException;
    void cpo(int address) throws SimulatorException;
    void ret() throws SimulatorException;             // RET: pop PC from stack
    void rc() throws SimulatorException;
    void rnc() throws SimulatorException;
    void rz() throws SimulatorException;
    void rnz() throws SimulatorException;
    void rp() throws SimulatorException;
    void rm() throws SimulatorException;
    void rpe() throws SimulatorException;
    void rpo() throws SimulatorException;
    void rst(int n) throws SimulatorException;        // RST n: call n*8
    void pchl();                                       // PCHL: PC = HL

    // Stack, I/O, Machine Control
    void push(String regPair) throws SimulatorException; // PUSH rp: push pair onto stack
    void pop(String regPair) throws SimulatorException;  // POP rp: pop pair from stack
    void xthl() throws SimulatorException;               // XTHL: exchange HL with top of stack
    void sphl();                                         // SPHL: SP = HL
    void in(int port) throws SimulatorException;         // IN port: A = port[port]
    void out(int port) throws SimulatorException;        // OUT port: port[port] = A
    void hlt();    // HLT: halt processor
    void nop();    // NOP: no operation
    void ei();     // EI: enable interrupts
    void di();     // DI: disable interrupts
    void rim();    // RIM: read interrupt mask
    void sim();    // SIM: set interrupt mask
}
