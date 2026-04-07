/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                  INSTRUCTION SET INTERFACE — 8085 SIMULATOR              ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  PURPOSE:                                                                ║
 * ║  Defines method signatures for EVERY instruction in the 8085 ISA.        ║
 * ║  The Architecture class implements this interface, providing the         ║
 * ║  actual logic for each instruction.                                      ║
 * ║                                                                          ║
 * ║  REAL HARDWARE CONTEXT:                                                  ║
 * ║  The 8085 has 74 unique mnemonics that expand to ~246 opcodes            ║
 * ║  (different register combinations = different opcodes).                  ║
 * ║  Instructions are grouped into 5 categories:                             ║
 * ║    1. Data Transfer  — Move data between registers/memory               ║
 * ║    2. Arithmetic      — ADD, SUB, INR, DCR, etc.                         ║
 * ║    3. Logical          — AND, OR, XOR, Compare, Rotate                    ║
 * ║    4. Branching        — JMP, CALL, RET (conditional & unconditional)    ║
 * ║    5. Machine Control  — HLT, NOP, PUSH, POP, I/O                       ║
 * ║                                                                          ║
 * ║  DESIGN DECISION:                                                        ║
 * ║  Each mnemonic is ONE method with parameters for registers/data.         ║
 * ║  e.g., mov("A", "B") covers opcode 0x78 (MOV A,B).                     ║
 * ║  This is cleaner than 246 separate methods.                              ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
public interface InstrucSet {

    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  CATEGORY 1: DATA TRANSFER INSTRUCTIONS                      ║
    // ║  Purpose: Move data between registers, memory, and immediates║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * MOV dest, src — Move data from source to destination.
     *
     * OPCODES: 0x40-0x7F (almost entire block, excluding HALT at 0x76)
     *
     * LOGIC:
     *   1. If both dest and src are registers: registers[dest] = registers[src]
     *   2. If src is "M": registers[dest] = memory[getHL()]
     *      (M refers to memory location pointed to by HL register pair)
     *   3. If dest is "M": memory[getHL()] = registers[src]
     *   4. No flags are affected by MOV.
     *
     * HARDWARE: Data travels on the 8-bit internal data bus.
     *
     * @param dest  Destination register ("A","B","C","D","E","H","L") or "M" for memory
     * @param src   Source register ("A","B","C","D","E","H","L") or "M" for memory
     * @throws SimulatorException if both dest and src are "M" (invalid) or invalid register name
     */
    void mov(String dest, String src) throws SimulatorException;

    /**
     * MVI dest, data — Move Immediate: Load an 8-bit constant into a register.
     *
     * OPCODES: 0x06, 0x0E, 0x16, 0x1E, 0x26, 0x2E, 0x36, 0x3E
     *
     * LOGIC:
     *   1. If dest is a register: registers[dest] = data
     *   2. If dest is "M": memory[getHL()] = data
     *   3. No flags affected.
     *   4. This is a 2-byte instruction (opcode + immediate data byte).
     *
     * @param dest  Destination register or "M"
     * @param data  8-bit immediate value (0x00 to 0xFF)
     * @throws SimulatorException if data > 0xFF or invalid register
     */
    void mvi(String dest, int data) throws SimulatorException;

    /**
     * LXI rp, data16 — Load Register Pair Immediate with 16-bit data.
     *
     * OPCODES: 0x01 (BC), 0x11 (DE), 0x21 (HL), 0x31 (SP)
     *
     * LOGIC:
     *   1. Split data16 into high byte and low byte:
     *      highByte = (data16 >> 8) & 0xFF
     *      lowByte  = data16 & 0xFF
     *   2. For pair "B": B = highByte, C = lowByte
     *      For pair "D": D = highByte, E = lowByte
     *      For pair "H": H = highByte, L = lowByte
     *      For "SP": stackPointer = data16
     *   3. No flags affected.
     *   4. This is a 3-byte instruction (opcode + low byte + high byte).
     *      NOTE: 8085 stores low byte first (little-endian)!
     *
     * @param regPair  Register pair identifier ("B"=BC, "D"=DE, "H"=HL, "SP")
     * @param data16   16-bit immediate value (0x0000 to 0xFFFF)
     * @throws SimulatorException if data > 0xFFFF or invalid register pair
     */
    void lxi(String regPair, int data16) throws SimulatorException;

    /**
     * LDA address — Load Accumulator Direct from memory address.
     *
     * OPCODE: 0x3A
     *
     * LOGIC:
     *   A = memory[address]
     *   Direct addressing: the 16-bit address is embedded in the instruction.
     *   3-byte instruction (opcode + low addr + high addr).
     *
     * @param address  16-bit memory address
     * @throws SimulatorException if address out of range
     */
    void lda(int address) throws SimulatorException;

    /**
     * STA address — Store Accumulator Direct to memory address.
     *
     * OPCODE: 0x32
     *
     * LOGIC:
     *   memory[address] = A
     *   3-byte instruction.
     *
     * @param address  16-bit memory address
     * @throws SimulatorException if address out of range
     */
    void sta(int address) throws SimulatorException;

    /**
     * LHLD address — Load H-L pair Direct from memory.
     *
     * OPCODE: 0x2A
     *
     * LOGIC:
     *   L = memory[address]       (low byte first!)
     *   H = memory[address + 1]   (high byte second)
     *   3-byte instruction.
     *
     * @param address  16-bit memory address
     * @throws SimulatorException if address out of range
     */
    void lhld(int address) throws SimulatorException;

    /**
     * SHLD address — Store H-L pair Direct to memory.
     *
     * OPCODE: 0x22
     *
     * LOGIC:
     *   memory[address]     = L   (low byte first!)
     *   memory[address + 1] = H   (high byte second)
     *
     * @param address  16-bit memory address
     * @throws SimulatorException if address out of range
     */
    void shld(int address) throws SimulatorException;

    /**
     * LDAX rp — Load Accumulator Indirect via register pair.
     *
     * OPCODES: 0x0A (BC), 0x1A (DE)
     *
     * LOGIC:
     *   If rp = "B": A = memory[BC]   (where BC = B*256 + C)
     *   If rp = "D": A = memory[DE]   (where DE = D*256 + E)
     *   Only BC and DE pairs are valid for LDAX.
     *
     * @param regPair  "B" (for BC) or "D" (for DE)
     * @throws SimulatorException if invalid register pair
     */
    void ldax(String regPair) throws SimulatorException;

    /**
     * STAX rp — Store Accumulator Indirect via register pair.
     *
     * OPCODES: 0x02 (BC), 0x12 (DE)
     *
     * LOGIC:
     *   If rp = "B": memory[BC] = A
     *   If rp = "D": memory[DE] = A
     *
     * @param regPair  "B" (for BC) or "D" (for DE)
     * @throws SimulatorException if invalid register pair
     */
    void stax(String regPair) throws SimulatorException;

    /**
     * XCHG — Exchange DE and HL register pairs.
     *
     * OPCODE: 0xEB
     *
     * LOGIC:
     *   temp = D;  D = H;  H = temp;
     *   temp = E;  E = L;  L = temp;
     *   No flags affected.
     */
    void xchg();


    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  CATEGORY 2: ARITHMETIC INSTRUCTIONS                         ║
    // ║  Purpose: ADD, SUB, INR, DCR — affect flags                  ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * ADD src — Add register/memory to Accumulator.
     *
     * OPCODES: 0x80-0x87
     *
     * LOGIC:
     *   result = A + operand   (operand = register value or memory[HL])
     *   A = result & 0xFF      (keep only lower 8 bits)
     *   Update ALL flags: S, Z, AC, P, CY
     *
     * FLAG COMPUTATION:
     *   CY (Carry)     = 1 if result > 0xFF (carry out of bit 7)
     *   Z  (Zero)      = 1 if (result & 0xFF) == 0
     *   S  (Sign)      = 1 if bit 7 of result is 1 (negative in signed)
     *   P  (Parity)    = 1 if number of 1-bits in result is EVEN
     *   AC (Aux Carry)  = 1 if carry out of bit 3 (lower nibble overflow)
     *     AC check: ((A & 0x0F) + (operand & 0x0F)) > 0x0F
     *
     * @param src  Register name or "M" for memory
     * @throws SimulatorException if invalid register
     */
    void add(String src) throws SimulatorException;

    /**
     * ADC src — Add with Carry: A = A + src + CY.
     *
     * OPCODES: 0x88-0x8F
     *
     * LOGIC: Same as ADD but includes the current Carry flag in the sum.
     *   result = A + operand + CY
     *   Flags updated same as ADD.
     *
     * USE CASE: Multi-byte addition. For example, adding two 16-bit numbers:
     *   First ADD the low bytes, then ADC the high bytes (carries propagate).
     *
     * @param src  Register name or "M"
     * @throws SimulatorException if invalid register
     */
    void adc(String src) throws SimulatorException;

    /**
     * ADI data — Add Immediate to Accumulator.
     *
     * OPCODE: 0xC6
     *
     * LOGIC: A = A + data; update all flags. 2-byte instruction.
     *
     * @param data  8-bit immediate value
     * @throws SimulatorException if data > 0xFF
     */
    void adi(int data) throws SimulatorException;

    /**
     * ACI data — Add Immediate with Carry: A = A + data + CY.
     *
     * OPCODE: 0xCE
     *
     * @param data  8-bit immediate value
     * @throws SimulatorException if data > 0xFF
     */
    void aci(int data) throws SimulatorException;

    /**
     * SUB src — Subtract register/memory from Accumulator.
     *
     * OPCODES: 0x90-0x97
     *
     * LOGIC:
     *   result = A - operand
     *   Internally, subtraction uses 2's complement:
     *     A + (~operand + 1)   where ~ is bitwise NOT
     *   A = result & 0xFF
     *   Update ALL flags.
     *   CY = 1 if borrow occurred (i.e., A < operand)
     *
     * @param src  Register name or "M"
     * @throws SimulatorException if invalid register
     */
    void sub(String src) throws SimulatorException;

    /**
     * SBB src — Subtract with Borrow: A = A - src - CY.
     *
     * OPCODES: 0x98-0x9F
     *
     * LOGIC: Same as SUB but subtracts the Carry flag too.
     *   result = A - operand - CY
     *   USE CASE: Multi-byte subtraction (borrow propagation).
     *
     * @param src  Register name or "M"
     * @throws SimulatorException if invalid register
     */
    void sbb(String src) throws SimulatorException;

    /**
     * SUI data — Subtract Immediate from Accumulator.
     *
     * OPCODE: 0xD6
     *
     * @param data  8-bit immediate value
     * @throws SimulatorException if data > 0xFF
     */
    void sui(int data) throws SimulatorException;

    /**
     * SBI data — Subtract Immediate with Borrow: A = A - data - CY.
     *
     * OPCODE: 0xDE
     *
     * @param data  8-bit immediate value
     * @throws SimulatorException if data > 0xFF
     */
    void sbi(int data) throws SimulatorException;

    /**
     * INR dest — Increment register or memory by 1.
     *
     * OPCODES: 0x04, 0x0C, 0x14, 0x1C, 0x24, 0x2C, 0x34, 0x3C
     *
     * LOGIC:
     *   dest = dest + 1
     *   Flags affected: S, Z, AC, P  (NOTE: CY is NOT affected!)
     *   This is important — INR does NOT change the Carry flag.
     *
     * @param dest  Register name or "M"
     * @throws SimulatorException if invalid register
     */
    void inr(String dest) throws SimulatorException;

    /**
     * DCR dest — Decrement register or memory by 1.
     *
     * OPCODES: 0x05, 0x0D, 0x15, 0x1D, 0x25, 0x2D, 0x35, 0x3D
     *
     * LOGIC:
     *   dest = dest - 1
     *   Flags affected: S, Z, AC, P  (CY NOT affected, same as INR)
     *
     * @param dest  Register name or "M"
     * @throws SimulatorException if invalid register
     */
    void dcr(String dest) throws SimulatorException;

    /**
     * INX rp — Increment Register Pair by 1.
     *
     * OPCODES: 0x03 (BC), 0x13 (DE), 0x23 (HL), 0x33 (SP)
     *
     * LOGIC:
     *   Treat the register pair as a 16-bit value, add 1.
     *   e.g., for HL: value = H*256+L; value++; H = value>>8; L = value&0xFF
     *   NO flags are affected (this is a 16-bit operation).
     *
     * @param regPair  "B" (BC), "D" (DE), "H" (HL), or "SP"
     * @throws SimulatorException if invalid register pair
     */
    void inx(String regPair) throws SimulatorException;

    /**
     * DCX rp — Decrement Register Pair by 1.
     *
     * OPCODES: 0x0B (BC), 0x1B (DE), 0x2B (HL), 0x3B (SP)
     *
     * LOGIC: Same as INX but subtracts 1. No flags affected.
     *
     * @param regPair  "B" (BC), "D" (DE), "H" (HL), or "SP"
     * @throws SimulatorException if invalid register pair
     */
    void dcx(String regPair) throws SimulatorException;

    /**
     * DAD rp — Double Add: Add register pair to HL.
     *
     * OPCODES: 0x09 (BC), 0x19 (DE), 0x29 (HL), 0x39 (SP)
     *
     * LOGIC:
     *   HL = HL + rp  (16-bit addition)
     *   Only CY flag is affected (set if carry out of bit 15).
     *   S, Z, AC, P are NOT affected.
     *
     * @param regPair  "B" (BC), "D" (DE), "H" (HL), or "SP"
     * @throws SimulatorException if invalid register pair
     */
    void dad(String regPair) throws SimulatorException;

    /**
     * DAA — Decimal Adjust Accumulator.
     *
     * OPCODE: 0x27
     *
     * LOGIC (for BCD arithmetic):
     *   1. If lower nibble of A > 9 OR AC flag is set:
     *      A = A + 0x06; set AC
     *   2. If upper nibble of A > 9 OR CY flag is set:
     *      A = A + 0x60; set CY
     *   All flags updated after adjustment.
     *
     * USE CASE: After adding two BCD numbers with ADD, call DAA to
     *   correct the result back to valid BCD.
     *   Example: 0x15 + 0x27 = 0x3C → DAA → 0x42 (correct BCD)
     */
    void daa();


    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  CATEGORY 3: LOGICAL INSTRUCTIONS                            ║
    // ║  Purpose: AND, OR, XOR, Compare, Rotate, Complement          ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * ANA src — Logical AND with Accumulator.
     *
     * OPCODES: 0xA0-0xA7
     *
     * LOGIC:
     *   A = A & operand   (bitwise AND)
     *   CY is reset (cleared to 0). AC is set.
     *   S, Z, P are updated based on result.
     *
     * @param src  Register name or "M"
     * @throws SimulatorException if invalid register
     */
    void ana(String src) throws SimulatorException;

    /**
     * ANI data — AND Immediate with Accumulator.
     *
     * OPCODE: 0xE6
     *
     * LOGIC: A = A & data; CY reset, AC set, update S/Z/P.
     *
     * @param data  8-bit immediate value
     * @throws SimulatorException if data > 0xFF
     */
    void ani(int data) throws SimulatorException;

    /**
     * ORA src — Logical OR with Accumulator.
     *
     * OPCODES: 0xB0-0xB7
     *
     * LOGIC:
     *   A = A | operand   (bitwise OR)
     *   CY and AC are both reset.
     *   S, Z, P updated.
     *
     * @param src  Register name or "M"
     * @throws SimulatorException if invalid register
     */
    void ora(String src) throws SimulatorException;

    /**
     * ORI data — OR Immediate with Accumulator.
     *
     * OPCODE: 0xF6
     *
     * @param data  8-bit immediate value
     * @throws SimulatorException if data > 0xFF
     */
    void ori(int data) throws SimulatorException;

    /**
     * XRA src — Logical XOR with Accumulator.
     *
     * OPCODES: 0xA8-0xAF
     *
     * LOGIC:
     *   A = A ^ operand   (bitwise XOR)
     *   CY and AC are both reset.
     *   S, Z, P updated.
     *
     * TRICK: XRA A   (XOR A with itself) is the quickest way to zero A.
     *        Result is always 0, sets Zero flag.
     *
     * @param src  Register name or "M"
     * @throws SimulatorException if invalid register
     */
    void xra(String src) throws SimulatorException;

    /**
     * XRI data — XOR Immediate with Accumulator.
     *
     * OPCODE: 0xEE
     *
     * @param data  8-bit immediate value
     * @throws SimulatorException if data > 0xFF
     */
    void xri(int data) throws SimulatorException;

    /**
     * CMP src — Compare register/memory with Accumulator.
     *
     * OPCODES: 0xB8-0xBF
     *
     * LOGIC:
     *   Internally performs A - operand BUT DOES NOT STORE THE RESULT.
     *   Only the FLAGS are updated:
     *     If A == operand: Z = 1, CY = 0
     *     If A <  operand: CY = 1, Z = 0
     *     If A >  operand: CY = 0, Z = 0
     *   S, AC, P are also updated based on the subtraction result.
     *
     * USE CASE: Use before conditional jumps (JZ, JC, JNZ, JNC).
     *
     * @param src  Register name or "M"
     * @throws SimulatorException if invalid register
     */
    void cmp(String src) throws SimulatorException;

    /**
     * CPI data — Compare Immediate with Accumulator.
     *
     * OPCODE: 0xFE
     *
     * LOGIC: Same as CMP but with an immediate value.
     *
     * @param data  8-bit immediate value
     * @throws SimulatorException if data > 0xFF
     */
    void cpi(int data) throws SimulatorException;

    /**
     * RLC — Rotate Accumulator Left (circular).
     *
     * OPCODE: 0x07
     *
     * LOGIC:
     *   CY = bit 7 of A   (the bit that "falls off" the left)
     *   A = (A << 1) | CY  (bit 7 wraps around to bit 0)
     *   Only CY is affected. S, Z, AC, P are NOT affected.
     *
     * VISUAL:  [b7 b6 b5 b4 b3 b2 b1 b0]
     *       →  [b6 b5 b4 b3 b2 b1 b0 b7]  CY = old b7
     */
    void rlc();

    /**
     * RRC — Rotate Accumulator Right (circular).
     *
     * OPCODE: 0x0F
     *
     * LOGIC:
     *   CY = bit 0 of A
     *   A = (CY << 7) | (A >> 1)
     *   Only CY affected.
     */
    void rrc();

    /**
     * RAL — Rotate Accumulator Left through Carry.
     *
     * OPCODE: 0x17
     *
     * LOGIC:
     *   temp = bit 7 of A
     *   A = (A << 1) | old_CY   (old carry goes to bit 0)
     *   CY = temp               (bit 7 goes to carry)
     *   Only CY affected.
     *
     * DIFFERENCE FROM RLC:
     *   RLC: bit7 → bit0 AND bit7 → CY   (9-bit circular including CY)
     *   RAL: bit7 → CY, CY → bit0        (9-bit shift through CY)
     */
    void ral();

    /**
     * RAR — Rotate Accumulator Right through Carry.
     *
     * OPCODE: 0x1F
     *
     * LOGIC:
     *   temp = bit 0 of A
     *   A = (old_CY << 7) | (A >> 1)
     *   CY = temp
     */
    void rar();

    /**
     * CMA — Complement Accumulator (bitwise NOT).
     *
     * OPCODE: 0x2F
     *
     * LOGIC:
     *   A = ~A & 0xFF   (flip all 8 bits, i.e., 1's complement)
     *   No flags affected.
     */
    void cma();

    /**
     * CMC — Complement Carry flag.
     *
     * OPCODE: 0x3F
     *
     * LOGIC:
     *   CY = CY ^ 1   (toggle the carry flag: 0→1, 1→0)
     *   No other flags affected.
     */
    void cmc();

    /**
     * STC — Set Carry flag.
     *
     * OPCODE: 0x37
     *
     * LOGIC: CY = 1. No other flags affected.
     */
    void stc();


    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  CATEGORY 4: BRANCHING INSTRUCTIONS                          ║
    // ║  Purpose: JMP, CALL, RET — control flow                     ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * JMP address — Unconditional Jump.
     *
     * OPCODE: 0xC3
     *
     * LOGIC:
     *   PC = address   (program counter set to new address)
     *   Execution continues from the new address.
     *   3-byte instruction (opcode + low addr + high addr).
     *
     * @param address  16-bit target address
     * @throws SimulatorException if address out of memory range
     */
    void jmp(int address) throws SimulatorException;

    /**
     * JC address — Jump if Carry flag is set (CY == 1).
     * OPCODE: 0xDA
     *
     * LOGIC: if (CY == 1) PC = address; else PC = PC + 3 (skip instruction)
     */
    void jc(int address) throws SimulatorException;

    /** JNC address — Jump if No Carry (CY == 0). OPCODE: 0xD2 */
    void jnc(int address) throws SimulatorException;

    /** JZ address — Jump if Zero flag set (Z == 1). OPCODE: 0xCA */
    void jz(int address) throws SimulatorException;

    /** JNZ address — Jump if Not Zero (Z == 0). OPCODE: 0xC2 */
    void jnz(int address) throws SimulatorException;

    /** JP address — Jump if Plus/Positive (S == 0). OPCODE: 0xF2 */
    void jp(int address) throws SimulatorException;

    /** JM address — Jump if Minus/Negative (S == 1). OPCODE: 0xFA */
    void jm(int address) throws SimulatorException;

    /** JPE address — Jump if Parity Even (P == 1). OPCODE: 0xEA */
    void jpe(int address) throws SimulatorException;

    /** JPO address — Jump if Parity Odd (P == 0). OPCODE: 0xE2 */
    void jpo(int address) throws SimulatorException;

    /**
     * CALL address — Unconditional Subroutine Call.
     *
     * OPCODE: 0xCD
     *
     * LOGIC:
     *   1. Push return address (PC + 3) onto the stack:
     *      SP = SP - 1;  memory[SP] = (PC+3) high byte
     *      SP = SP - 1;  memory[SP] = (PC+3) low byte
     *   2. PC = address  (jump to subroutine)
     *
     * HARDWARE: The stack grows DOWNWARD in memory (SP decreases).
     *
     * @param address  16-bit subroutine address
     * @throws SimulatorException if address out of range or stack overflow
     */
    void call(int address) throws SimulatorException;

    /** CC — Call if Carry. OPCODE: 0xDC */
    void cc(int address) throws SimulatorException;

    /** CNC — Call if No Carry. OPCODE: 0xD4 */
    void cnc(int address) throws SimulatorException;

    /** CZ — Call if Zero. OPCODE: 0xCC */
    void cz(int address) throws SimulatorException;

    /** CNZ — Call if Not Zero. OPCODE: 0xC4 */
    void cnz(int address) throws SimulatorException;

    /** CP — Call if Plus. OPCODE: 0xF4 */
    void cp(int address) throws SimulatorException;

    /** CM — Call if Minus. OPCODE: 0xFC */
    void cm(int address) throws SimulatorException;

    /** CPE — Call if Parity Even. OPCODE: 0xEC */
    void cpe(int address) throws SimulatorException;

    /** CPO — Call if Parity Odd. OPCODE: 0xE4 */
    void cpo(int address) throws SimulatorException;

    /**
     * RET — Return from Subroutine.
     *
     * OPCODE: 0xC9
     *
     * LOGIC:
     *   1. Pop return address from stack:
     *      lowByte  = memory[SP];  SP = SP + 1;
     *      highByte = memory[SP];  SP = SP + 1;
     *   2. PC = (highByte << 8) | lowByte
     *
     * MUST BE PRECEDED BY A CALL INSTRUCTION (otherwise stack is corrupted).
     *
     * @throws SimulatorException if stack underflow
     */
    void ret() throws SimulatorException;

    /** RC — Return if Carry. OPCODE: 0xD8 */
    void rc() throws SimulatorException;

    /** RNC — Return if No Carry. OPCODE: 0xD0 */
    void rnc() throws SimulatorException;

    /** RZ — Return if Zero. OPCODE: 0xC8 */
    void rz() throws SimulatorException;

    /** RNZ — Return if Not Zero. OPCODE: 0xC0 */
    void rnz() throws SimulatorException;

    /** RP — Return if Plus. OPCODE: 0xF0 */
    void rp() throws SimulatorException;

    /** RM — Return if Minus. OPCODE: 0xF8 */
    void rm() throws SimulatorException;

    /** RPE — Return if Parity Even. OPCODE: 0xE8 */
    void rpe() throws SimulatorException;

    /** RPO — Return if Parity Odd. OPCODE: 0xE0 */
    void rpo() throws SimulatorException;

    /**
     * RST n — Restart (software interrupt vector call).
     *
     * OPCODES: 0xC7, 0xCF, 0xD7, 0xDF, 0xE7, 0xEF, 0xF7, 0xFF
     *
     * LOGIC:
     *   1. Push current PC onto stack (same as CALL)
     *   2. PC = n * 8   (where n = 0 to 7)
     *      RST 0 → jumps to 0x0000
     *      RST 1 → jumps to 0x0008
     *      ...
     *      RST 7 → jumps to 0x0038
     *
     * @param n  Restart number (0-7)
     * @throws SimulatorException if n > 7
     */
    void rst(int n) throws SimulatorException;

    /**
     * PCHL — Load Program Counter with HL.
     *
     * OPCODE: 0xE9
     *
     * LOGIC: PC = HL  (unconditional jump to address in HL pair)
     */
    void pchl();


    // ╔═══════════════════════════════════════════════════════════════╗
    // ║  CATEGORY 5: STACK, I/O, AND MACHINE CONTROL                 ║
    // ║  Purpose: Stack ops, port I/O, HLT, NOP, interrupts          ║
    // ╚═══════════════════════════════════════════════════════════════╝

    /**
     * PUSH rp — Push Register Pair onto Stack.
     *
     * OPCODES: 0xC5 (BC), 0xD5 (DE), 0xE5 (HL), 0xF5 (PSW = A + Flags)
     *
     * LOGIC:
     *   SP = SP - 1;  memory[SP] = high_register;
     *   SP = SP - 1;  memory[SP] = low_register;
     *   For PSW: high = A, low = Flag register (S Z 0 AC 0 P 1 CY format)
     *
     * NOTE: Stack grows downward! SP decreases by 2.
     *
     * @param regPair  "B" (BC), "D" (DE), "H" (HL), or "PSW" (A + Flags)
     * @throws SimulatorException if stack overflow
     */
    void push(String regPair) throws SimulatorException;

    /**
     * POP rp — Pop Register Pair from Stack.
     *
     * OPCODES: 0xC1 (BC), 0xD1 (DE), 0xE1 (HL), 0xF1 (PSW)
     *
     * LOGIC:
     *   low_register  = memory[SP];  SP = SP + 1;
     *   high_register = memory[SP];  SP = SP + 1;
     *   For PSW: A = high, Flags = low
     *
     * @param regPair  "B" (BC), "D" (DE), "H" (HL), or "PSW"
     * @throws SimulatorException if stack underflow
     */
    void pop(String regPair) throws SimulatorException;

    /**
     * XTHL — Exchange Top of Stack with HL.
     *
     * OPCODE: 0xE3
     *
     * LOGIC:
     *   L ↔ memory[SP]
     *   H ↔ memory[SP + 1]
     */
    void xthl() throws SimulatorException;

    /**
     * SPHL — Move HL to Stack Pointer.
     *
     * OPCODE: 0xF9
     *
     * LOGIC: SP = HL (H*256 + L)
     */
    void sphl();

    /**
     * IN port — Input from I/O Port to Accumulator.
     *
     * OPCODE: 0xDB
     *
     * LOGIC:
     *   A = read_from_port(port)
     *   In our simulator, we'll use a HashMap<Integer, Integer> for ports.
     *   2-byte instruction (opcode + port address).
     *
     * @param port  8-bit port address (0x00 to 0xFF)
     * @throws SimulatorException if port is invalid
     */
    void in(int port) throws SimulatorException;

    /**
     * OUT port — Output from Accumulator to I/O Port.
     *
     * OPCODE: 0xD3
     *
     * LOGIC:
     *   write_to_port(port, A)
     *   In our simulator, store in the port HashMap.
     *
     * @param port  8-bit port address (0x00 to 0xFF)
     * @throws SimulatorException if port is invalid
     */
    void out(int port) throws SimulatorException;

    /**
     * HLT — Halt the processor.
     *
     * OPCODE: 0x76
     *
     * LOGIC:
     *   Set a boolean flag 'halted = true'.
     *   The execution loop checks this flag and stops.
     *   Every 8085 program MUST end with HLT.
     *
     * HARDWARE: The processor enters a halted state and waits
     *   for an interrupt or reset to resume.
     */
    void hlt();

    /**
     * NOP — No Operation.
     *
     * OPCODE: 0x00
     *
     * LOGIC:
     *   Do nothing. Simply advance PC by 1.
     *   Used for timing delays or code alignment.
     */
    void nop();

    /**
     * EI — Enable Interrupts.
     *
     * OPCODE: 0xFB
     *
     * LOGIC: Set interruptsEnabled = true.
     */
    void ei();

    /**
     * DI — Disable Interrupts.
     *
     * OPCODE: 0xF3
     *
     * LOGIC: Set interruptsEnabled = false.
     */
    void di();

    /**
     * RIM — Read Interrupt Mask (8085 specific, not in 8080).
     *
     * OPCODE: 0x20
     *
     * LOGIC:
     *   Loads the interrupt mask status and serial input bit
     *   into the Accumulator.
     *   A = [SID | I7.5 | I6.5 | I5.5 | IE | M7.5 | M6.5 | M5.5]
     *   (For simplicity in simulator, can return a fixed value)
     */
    void rim();

    /**
     * SIM — Set Interrupt Mask (8085 specific).
     *
     * OPCODE: 0x30
     *
     * LOGIC:
     *   Uses the Accumulator value to set interrupt masks
     *   and serial output data.
     *   (For simplicity in simulator, can be a no-op initially)
     */
    void sim();
}
