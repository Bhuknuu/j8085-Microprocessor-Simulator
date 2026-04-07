import java.util.HashMap;
import java.util.Map;

/*
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║               ALU (Arithmetic Logic Unit) — 8085 SIMULATOR              ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  PURPOSE:                                                                ║
 * ║  Handles ALL arithmetic & logical computations and FLAG management.      ║
 * ║  Separated from Architecture for clean responsibility boundaries:        ║
 * ║    Architecture → holds state (registers, memory)                       ║
 * ║    ALU          → performs computations and determines flag values       ║
 * ║                                                                          ║
 * ║  REAL HARDWARE CONTEXT:                                                  ║
 * ║  The ALU is a combinational circuit inside the 8085 chip.               ║
 * ║  It takes two 8-bit inputs (A and the operand) and produces:            ║
 * ║    - An 8-bit result (stored back in A)                                 ║
 * ║    - 5 flag bits: S, Z, AC, P, CY                                       ║
 * ║                                                                          ║
 * ║  THE FLAG REGISTER (F) layout:                                           ║
 * ║    Bit:  7    6    5    4    3    2    1    0                            ║
 * ║          S    Z    0    AC   0    P    1    CY                           ║
 * ║    Bits 5, 3 are always 0; bit 1 is always 1 (undefined/fixed).         ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

public class ALU {
    // ═══════════════════════════════════════════════════════════════
    //  FLAG BIT POSITIONS (within the Flag register byte)
    // ═══════════════════════════════════════════════════════════════

    /** Sign flag — bit 7 — Set if result is negative (bit 7 = 1) */
    public static final int FLAG_S  = 7;

    /** Zero flag — bit 6 — Set if result is zero */
    public static final int FLAG_Z  = 6;

    /** Auxiliary Carry — bit 4 — Set if carry from bit 3 to bit 4 */
    public static final int FLAG_AC = 4;

    /** Parity flag — bit 2 — Set if number of 1-bits is even */
    public static final int FLAG_P  = 2;

    /** Carry flag — bit 0 — Set if carry out of bit 7 (or borrow) */
    public static final int FLAG_CY = 0;

    // ═══════════════════════════════════════════════════════════════
    //  FLAGS STORAGE
    // ═══════════════════════════════════════════════════════════════

    /**
     * Individual flag values stored as booleans for clarity.
     * When we need the Flag register byte (for PUSH PSW), we compose it.
     */
    private boolean signFlag;       // S  — true if result has bit 7 = 1
    private boolean zeroFlag;       // Z  — true if result = 0
    private boolean auxCarryFlag;   // AC — true if carry from lower nibble
    private boolean parityFlag;     // P  — true if even number of 1-bits
    private boolean carryFlag;      // CY — true if carry/borrow occurred

    // ═══════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════

    /**
     * Initializes the ALU with all flags cleared (false).
     * This represents the power-on-reset state.
     */
    public ALU() {
        // TODO: Initialize all flags to false
        resetFlags();
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLAG MANAGEMENT METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Resets all flags to false (0).
     */
    public void resetFlags() {
        // TODO: Set all five flags to false
    }

    /**
     * Updates ALL five flags based on a computation result.
     *
     * LOGIC:
     *   result = the full result (may be > 0xFF if there was a carry)
     *   maskedResult = result & 0xFF  (the 8-bit truncated result)
     *
     *   S  = (maskedResult & 0x80) != 0     (check bit 7)
     *   Z  = maskedResult == 0               (is it zero?)
     *   CY = result > 0xFF || result < 0     (overflow or underflow)
     *   P  = countSetBits(maskedResult) % 2 == 0  (even parity)
     *   AC = computed from lower nibbles of the operands (passed separately)
     *
     * @param result         Full result of the computation (before masking to 8 bits)
     * @param operand1Lower  Lower nibble of first operand (A & 0x0F) — for AC computation
     * @param operand2Lower  Lower nibble of second operand (src & 0x0F) — for AC computation
     * @param isSubtraction  true if this was a SUB/SBB/CMP operation (changes CY/AC logic)
     */
    public void updateAllFlags(int result, int operand1Lower, int operand2Lower, boolean isSubtraction) {
        // TODO: Implement flag computation as described above
        //
        // STEP 1: Compute CY flag
        //   For addition:    CY = (result > 0xFF)
        //   For subtraction: CY = (result < 0) means borrow occurred
        //
        // STEP 2: Mask result to 8 bits for remaining flags
        //   int masked = result & 0xFF;
        //
        // STEP 3: Compute Z flag
        //   zeroFlag = (masked == 0);
        //
        // STEP 4: Compute S flag
        //   signFlag = ((masked & 0x80) != 0);
        //
        // STEP 5: Compute P flag
        //   parityFlag = (countSetBits(masked) % 2 == 0);
        //
        // STEP 6: Compute AC flag
        //   For addition:    AC = ((operand1Lower + operand2Lower) > 0x0F)
        //   For subtraction: AC = ((operand1Lower - operand2Lower) < 0)
    }

    /**
     * Updates ONLY S, Z, AC, P flags (NOT CY).
     * Used by INR and DCR instructions which preserve the Carry flag.
     *
     * @param result         Full result
     * @param operand1Lower  Lower nibble of first operand
     * @param operand2Lower  Lower nibble of second operand
     * @param isSubtraction  true if decrement
     */
    public void updateFlagsExceptCarry(int result, int operand1Lower, int operand2Lower,
                                       boolean isSubtraction) {
        // TODO: Same as updateAllFlags but DO NOT modify carryFlag
        //
        // Save current carry: boolean savedCY = carryFlag;
        // Call updateAllFlags(...)
        // Restore carry: carryFlag = savedCY;
    }

    // ═══════════════════════════════════════════════════════════════
    //  ARITHMETIC OPERATIONS
    //  These perform the computation AND set flags. Architecture
    //  calls these instead of doing raw math.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Performs 8-bit addition: result = a + b + carryIn.
     *
     * LOGIC:
     *   1. Compute fullResult = a + b + carryIn
     *   2. Update all flags based on fullResult
     *   3. Return fullResult & 0xFF (truncated to 8 bits)
     *
     * @param a        First operand (Accumulator value)
     * @param b        Second operand (register/memory/immediate value)
     * @param carryIn  0 for ADD/ADI, current CY value for ADC/ACI
     * @return         8-bit result
     */
    public int add(int a, int b, int carryIn) {
        // TODO: Implement addition with flag updates
        //
        // int result = a + b + carryIn;
        // updateAllFlags(result, a & 0x0F, (b + carryIn) & 0x0F, false);
        // return result & 0xFF;
        return 0; // placeholder
    }

    /**
     * Performs 8-bit subtraction: result = a - b - borrowIn.
     *
     * LOGIC:
     *   1. Compute fullResult = a - b - borrowIn
     *   2. Update all flags (CY = 1 if borrow occurred, i.e., result < 0)
     *   3. Return fullResult & 0xFF
     *
     * @param a         First operand (Accumulator value)
     * @param b         Second operand
     * @param borrowIn  0 for SUB/SUI, current CY for SBB/SBI
     * @return          8-bit result
     */
    public int subtract(int a, int b, int borrowIn) {
        // TODO: Implement subtraction with flag updates
        //
        // int result = a - b - borrowIn;
        // updateAllFlags(result, a & 0x0F, (b + borrowIn) & 0x0F, true);
        // return result & 0xFF;
        return 0; // placeholder
    }

    /**
     * Performs 8-bit increment: result = value + 1.
     * CY flag is NOT affected.
     *
     * @param value  The value to increment
     * @return       8-bit result
     */
    public int increment(int value) {
        // TODO: Implement increment with flags (except CY)
        //
        // int result = value + 1;
        // updateFlagsExceptCarry(result, value & 0x0F, 1, false);
        // return result & 0xFF;
        return 0; // placeholder
    }

    /**
     * Performs 8-bit decrement: result = value - 1.
     * CY flag is NOT affected.
     *
     * @param value  The value to decrement
     * @return       8-bit result
     */
    public int decrement(int value) {
        // TODO: Implement decrement with flags (except CY)
        return 0; // placeholder
    }

    // ═══════════════════════════════════════════════════════════════
    //  LOGICAL OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Performs bitwise AND: result = a & b.
     * CY is reset, AC is set. S, Z, P updated.
     *
     * @param a  First operand
     * @param b  Second operand
     * @return   8-bit result
     */
    public int and(int a, int b) {
        // TODO: Implement AND with flag updates
        //
        // int result = a & b;
        // carryFlag = false;    // CY always reset for AND
        // auxCarryFlag = true;  // AC always set for AND (8085 quirk!)
        // updateS_Z_P(result);
        // return result;
        return 0; // placeholder
    }

    /**
     * Performs bitwise OR: result = a | b.
     * CY and AC are both reset. S, Z, P updated.
     */
    public int or(int a, int b) {
        // TODO: Implement OR with flag updates
        return 0; // placeholder
    }

    /**
     * Performs bitwise XOR: result = a ^ b.
     * CY and AC are both reset. S, Z, P updated.
     */
    public int xor(int a, int b) {
        // TODO: Implement XOR with flag updates
        return 0; // placeholder
    }

    /**
     * Performs compare (subtraction without storing result).
     * Flags are set based on (a - b) but A is NOT modified.
     *
     * @param a  Value in accumulator
     * @param b  Value to compare against
     */
    public void compare(int a, int b) {
        // TODO: Implement compare
        //
        // Just call subtract(a, b, 0) but ignore the return value.
        // The flags will be set correctly by the subtraction.
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Counts the number of 1-bits in an 8-bit value.
     * Used for computing the Parity flag.
     *
     * LOGIC:
     *   Use the bit-counting trick:
     *   int count = 0;
     *   while (value != 0) { count += value & 1; value >>= 1; }
     *   OR use Integer.bitCount(value) in Java.
     *
     * @param value  8-bit value
     * @return       Number of 1-bits
     */
    private int countSetBits(int value) {
        // TODO: Implement bit counting
        // return Integer.bitCount(value & 0xFF);
        return 0; // placeholder
    }

    /**
     * Composes the Flag register byte from individual flag booleans.
     *
     * LAYOUT: [S Z 0 AC 0 P 1 CY]
     *   Bit 7 = S, Bit 6 = Z, Bit 5 = 0, Bit 4 = AC,
     *   Bit 3 = 0, Bit 2 = P, Bit 1 = 1 (always), Bit 0 = CY
     *
     * LOGIC:
     *   int flags = 0x02;  // bit 1 is always 1
     *   if (signFlag)     flags |= (1 << 7);
     *   if (zeroFlag)     flags |= (1 << 6);
     *   if (auxCarryFlag) flags |= (1 << 4);
     *   if (parityFlag)   flags |= (1 << 2);
     *   if (carryFlag)    flags |= (1 << 0);
     *
     * USE CASE: Called by PUSH PSW to push {A, Flags} onto the stack.
     *
     * @return  The composed flag register byte
     */
    public int getFlagsByte() {
        // TODO: Compose and return the flag byte
        return 0x02; // placeholder (just the fixed bit 1)
    }

    /**
     * Decomposes a flag register byte back into individual booleans.
     *
     * USE CASE: Called by POP PSW to restore flags from stack.
     *
     * @param flagsByte  The byte value popped from stack
     */
    public void setFlagsByte(int flagsByte) {
        // TODO: Extract each flag bit
        //
        // signFlag     = (flagsByte & 0x80) != 0;
        // zeroFlag     = (flagsByte & 0x40) != 0;
        // auxCarryFlag = (flagsByte & 0x10) != 0;
        // parityFlag   = (flagsByte & 0x04) != 0;
        // carryFlag    = (flagsByte & 0x01) != 0;
    }

    /**
     * Returns a formatted string showing all flag states.
     * Used by UserInterface to display processor status.
     *
     * @return  String like "S=0 Z=1 AC=0 P=1 CY=0"
     */
    public String flagsToString() {
        // TODO: Format and return flag display string
        return String.format("S=%d Z=%d AC=%d P=%d CY=%d",
                signFlag ? 1 : 0, zeroFlag ? 1 : 0, auxCarryFlag ? 1 : 0,
                parityFlag ? 1 : 0, carryFlag ? 1 : 0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  INDIVIDUAL FLAG GETTERS AND SETTERS
    // ═══════════════════════════════════════════════════════════════

    public boolean isSignFlag()     { return signFlag; }
    public boolean isZeroFlag()     { return zeroFlag; }
    public boolean isAuxCarryFlag() { return auxCarryFlag; }
    public boolean isParityFlag()   { return parityFlag; }
    public boolean isCarryFlag()    { return carryFlag; }

    public void setSignFlag(boolean val)     { this.signFlag = val; }
    public void setZeroFlag(boolean val)     { this.zeroFlag = val; }
    public void setAuxCarryFlag(boolean val) { this.auxCarryFlag = val; }
    public void setParityFlag(boolean val)   { this.parityFlag = val; }
    public void setCarryFlag(boolean val)    { this.carryFlag = val; }
}
