/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                 SIMULATOR EXCEPTION — 8085 SIMULATOR                     ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  PURPOSE:                                                                ║
 * ║  Custom exception class for all simulator-specific errors.               ║
 * ║  Centralizes error handling so that invalid operations produce            ║
 * ║  clear, descriptive error messages instead of generic Java exceptions.   ║
 * ║                                                                          ║
 * ║  EXAMPLES OF ERRORS THIS HANDLES:                                        ║
 * ║  - Invalid memory address (outside allocated range)                      ║
 * ║  - Invalid register name (typo like "X" instead of "A")                 ║
 * ║  - Invalid opcode (unrecognized instruction)                             ║
 * ║  - Stack overflow/underflow                                              ║
 * ║  - Data out of 8-bit/16-bit range                                        ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
public class SimulatorException extends Exception {

    // ═══════════════════════════════════════════════════════════════
    //  ERROR TYPE ENUMERATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Categorizes the kind of error that occurred.
     * This helps the UserInterface display context-appropriate error messages.
     */
    public enum ErrorType {
        INVALID_MEMORY_ADDRESS,   // Address outside 0x0000-0xFFFF or allocated range
        INVALID_REGISTER,         // Unrecognized register name
        INVALID_OPCODE,           // Unrecognized instruction opcode
        INVALID_DATA,             // Data value outside valid range (e.g., > 0xFF for 8-bit)
        STACK_OVERFLOW,           // Stack pointer went below 0x0000
        STACK_UNDERFLOW,          // Stack pointer went above initial SP value
        MEMORY_OUT_OF_BOUNDS,     // Program tried to access memory outside range
        HALT_ENCOUNTERED,         // HLT instruction executed (not really an "error")
        SYNTAX_ERROR,             // Assembly syntax is invalid
        UNDEFINED_LABEL           // Jump/call to a label that doesn't exist
    }

    // ═══════════════════════════════════════════════════════════════
    //  FIELDS
    // ═══════════════════════════════════════════════════════════════

    /** The category of error */
    private final ErrorType errorType;

    /** The memory address or PC value where the error occurred (if applicable) */
    private final int address;

    // ═══════════════════════════════════════════════════════════════
    //  CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Constructor with message only.
     *
     * @param message  Human-readable description of the error
     */
    public SimulatorException(String message) {
        super(message);
        this.errorType = ErrorType.INVALID_DATA; // default
        this.address = -1;
    }

    /**
     * Constructor with message and error type.
     *
     * @param message    Human-readable description of the error
     * @param errorType  Category of the error
     */
    public SimulatorException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
        this.address = -1;
    }

    /**
     * Full constructor with message, error type, and address context.
     *
     * @param message    Human-readable description of the error
     * @param errorType  Category of the error
     * @param address    Memory address where the error occurred
     */
    public SimulatorException(String message, ErrorType errorType, int address) {
        super(message);
        this.errorType = errorType;
        this.address = address;
    }

    // ═══════════════════════════════════════════════════════════════
    //  GETTERS
    // ═══════════════════════════════════════════════════════════════

    public ErrorType getErrorType() {
        return errorType;
    }

    public int getAddress() {
        return address;
    }

    /**
     * Produces a formatted error string for display.
     *
     * LOGIC:
     *   Combine the error type, address (as hex), and message into
     *   a single formatted string like:
     *   "[INVALID_MEMORY_ADDRESS] at 0xABCD: Address out of range"
     *
     * @return Formatted error string
     */
    @Override
    public String toString() {
        // TODO: Implement formatted error output
        //  Format: "[ERROR_TYPE] at 0xADDR: message"
        //  If address == -1, omit the "at 0x..." part
        return String.format("[%s]%s: %s",
                errorType,
                (address != -1) ? String.format(" at 0x%04X", address) : "",
                getMessage());
    }
}
