import java.util.Scanner;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                  USER INTERFACE — 8085 SIMULATOR                         ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  PURPOSE:                                                                ║
 * ║  The CLI (Command-Line Interface) layer that the user interacts with.    ║
 * ║  Handles all input/output, menus, display formatting, and delegates      ║
 * ║  actual simulation work to Architecture + Assembler.                     ║
 * ║                                                                          ║
 * ║  INTERACTION FLOW:                                                       ║
 * ║    1. Display welcome banner                                             ║
 * ║    2. Ask user: default memory or custom range?                          ║
 * ║    3. Initialize Architecture with chosen memory config                  ║
 * ║    4. Enter main menu loop:                                              ║
 * ║       ┌─────────────────────────────────────────────┐                    ║
 * ║       │ 1. Enter Program (assembly code input)      │                    ║
 * ║       │ 2. Run Program (execute from address)       │                    ║
 * ║       │ 3. Step Through (single-step debugging)     │                    ║
 * ║       │ 4. View Registers                            │                    ║
 * ║       │ 5. View Memory                               │                    ║
 * ║       │ 6. Load Machine Code (raw bytes)             │                    ║
 * ║       │ 7. Reset Simulator                           │                    ║
 * ║       │ 8. Help                                      │                    ║
 * ║       │ 9. Exit                                      │                    ║
 * ║       └─────────────────────────────────────────────┘                    ║
 * ║    5. User selects option, perform action, return to menu               ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
public class UserInterface {

    // ═══════════════════════════════════════════════════════════════
    //  FIELDS
    // ═══════════════════════════════════════════════════════════════

    /** Scanner for reading user input from System.in */
    private Scanner scanner;

    /** The CPU/memory system being simulated */
    private Architecture architecture;

    /** The assembler for converting assembly → machine code */
    private Assembler assembler;

    /** Whether the simulator is currently running (main loop control) */
    private boolean running;

    // ═══════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════

    /**
     * Creates the UserInterface and initializes the scanner.
     * Architecture is NOT created here — it's created in start()
     * after the user chooses their memory configuration.
     */
    public UserInterface() {
        // TODO: Initialize scanner
        //   this.scanner = new Scanner(System.in);
        //   this.running = true;
    }

    // ═══════════════════════════════════════════════════════════════
    //  MAIN ENTRY POINT
    // ═══════════════════════════════════════════════════════════════

    /**
     * Starts the simulator UI.
     * This is called from Main.main() and runs the entire user session.
     *
     * LOGIC:
     *   1. Display welcome banner
     *   2. Configure memory (ask user for default/custom)
     *   3. Create Architecture with chosen config
     *   4. Create Assembler linked to Architecture
     *   5. Enter the main menu loop
     */
    public void start() {
        // TODO: Implement the startup sequence
        //
        // printWelcomeBanner();
        // configureMemory();
        // assembler = new Assembler(architecture);
        //
        // while (running) {
        //     printMainMenu();
        //     int choice = readIntInput("Enter your choice: ");
        //     handleMenuChoice(choice);
        // }
        //
        // printGoodbye();
        // scanner.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  DISPLAY METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Prints the welcome banner with project info.
     *
     * DESIGN: Use box-drawing characters for a polished look.
     */
    private void printWelcomeBanner() {
        // TODO: Design and print a welcome screen
        //
        // System.out.println("╔══════════════════════════════════════════════════╗");
        // System.out.println("║                                                  ║");
        // System.out.println("║        ╦  ╔═╗ ╔═╗ ╔═╗ ╔═╗                      ║");
        // System.out.println("║        ║  8 0 8 5   S I M                       ║");
        // System.out.println("║        ╩  ╚═╝ ╚═╝ ╚═╝ ╚═╝                      ║");
        // System.out.println("║                                                  ║");
        // System.out.println("║    Intel 8085 Microprocessor Simulator            ║");
        // System.out.println("║    Version 1.0 — PBL Project                     ║");
        // System.out.println("║                                                  ║");
        // System.out.println("╚══════════════════════════════════════════════════╝");
        // System.out.println();
    }

    /**
     * Prints the main menu options.
     */
    private void printMainMenu() {
        // TODO: Print the numbered menu
        //
        // System.out.println("\n┌─────────── MAIN MENU ───────────┐");
        // System.out.println("│                                  │");
        // System.out.println("│  1. Enter Assembly Program        │");
        // System.out.println("│  2. Run Program                   │");
        // System.out.println("│  3. Step Through (Debug)          │");
        // System.out.println("│  4. View Registers & Flags        │");
        // System.out.println("│  5. View Memory                   │");
        // System.out.println("│  6. Load Machine Code (Hex)       │");
        // System.out.println("│  7. Set Register/Memory Value     │");
        // System.out.println("│  8. Reset Simulator               │");
        // System.out.println("│  9. Help                          │");
        // System.out.println("│  0. Exit                          │");
        // System.out.println("│                                  │");
        // System.out.println("└──────────────────────────────────┘");
    }

    /**
     * Prints the goodbye message.
     */
    private void printGoodbye() {
        // TODO: Print exit message
        //   System.out.println("\nThank you for using j8085 Simulator. Goodbye!");
    }

    // ═══════════════════════════════════════════════════════════════
    //  MEMORY CONFIGURATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Asks the user whether to use default or custom memory range
     * and creates the Architecture accordingly.
     *
     * LOGIC:
     *   1. Display options:
     *      a) Default: 0000H - FFFFH (64KB)
     *      b) Custom: user enters start and end addresses
     *   2. Based on choice, call the appropriate Architecture constructor
     *   3. Handle invalid input gracefully (re-prompt)
     */
    private void configureMemory() {
        // TODO: Implement memory selection dialog
        //
        // System.out.println("═══ Memory Configuration ═══");
        // System.out.println("1. Default memory range (0000H - FFFFH, 64KB)");
        // System.out.println("2. Custom memory range");
        // int choice = readIntInput("Select option: ");
        //
        // if (choice == 1) {
        //     architecture = new Architecture();
        //     System.out.println("✓ Initialized with default memory (64KB).");
        // } else if (choice == 2) {
        //     int start = readHexInput("Enter start address (hex): ");
        //     int end   = readHexInput("Enter end address (hex): ");
        //     try {
        //         architecture = new Architecture(start, end);
        //         System.out.printf("✓ Initialized with custom memory (%04XH - %04XH).\n", start, end);
        //     } catch (SimulatorException e) {
        //         System.out.println("✗ Error: " + e.getMessage());
        //         System.out.println("  Falling back to default memory.");
        //         architecture = new Architecture();
        //     }
        // } else {
        //     System.out.println("Invalid choice. Using default memory.");
        //     architecture = new Architecture();
        // }
    }

    // ═══════════════════════════════════════════════════════════════
    //  MENU ACTION HANDLERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Routes the user's menu choice to the appropriate handler.
     *
     * @param choice  Menu option number (0-9)
     */
    private void handleMenuChoice(int choice) {
        // TODO: Implement switch for all menu options
        //
        // switch (choice) {
        //     case 1: handleEnterProgram();     break;
        //     case 2: handleRunProgram();       break;
        //     case 3: handleStepThrough();      break;
        //     case 4: handleViewRegisters();    break;
        //     case 5: handleViewMemory();       break;
        //     case 6: handleLoadMachineCode();  break;
        //     case 7: handleSetValue();         break;
        //     case 8: handleReset();            break;
        //     case 9: handleHelp();             break;
        //     case 0: running = false;          break;
        //     default:
        //         System.out.println("Invalid option. Please try again.");
        // }
    }

    /**
     * OPTION 1: Enter Assembly Program.
     *
     * LOGIC:
     *   1. Ask for starting address (default 2000H)
     *   2. Read multi-line assembly input until user enters "END" or empty line
     *   3. Pass to Assembler.assemble()
     *   4. Display assembled machine code bytes
     *   5. Set PC to start address
     */
    private void handleEnterProgram() {
        // TODO: Implement program entry
        //
        // System.out.println("\n═══ Enter Assembly Program ═══");
        // int startAddr = readHexInput("Start address (default 2000H): ");
        // if (startAddr == -1) startAddr = 0x2000;
        //
        // System.out.println("Enter your program (type 'END' on a new line to finish):");
        // StringBuilder program = new StringBuilder();
        // while (true) {
        //     String line = scanner.nextLine();
        //     if (line.trim().equalsIgnoreCase("END")) break;
        //     program.append(line).append("\n");
        // }
        //
        // try {
        //     int[] machineCode = assembler.assemble(program.toString(), startAddr);
        //     System.out.printf("✓ Assembled %d bytes at %04XH.\n", machineCode.length, startAddr);
        //     architecture.setProgramCounter(startAddr);
        //
        //     // Display assembled bytes
        //     System.out.print("Machine code: ");
        //     for (int b : machineCode) System.out.printf("%02X ", b);
        //     System.out.println();
        // } catch (SimulatorException e) {
        //     System.out.println("✗ Assembly error: " + e.getMessage());
        // }
    }

    /**
     * OPTION 2: Run Program.
     *
     * LOGIC:
     *   1. Ask for starting address (or use current PC)
     *   2. Call architecture.runFrom(address)
     *   3. After HLT, display final register state
     */
    private void handleRunProgram() {
        // TODO: Implement program execution
        //
        // System.out.println("\n═══ Run Program ═══");
        // System.out.printf("Current PC = %04XH\n", architecture.getProgramCounter());
        // int addr = readHexInput("Run from address (Enter for current PC): ");
        // if (addr == -1) addr = architecture.getProgramCounter();
        //
        // try {
        //     architecture.runFrom(addr);
        //     System.out.println("✓ Program halted.");
        //     System.out.println(architecture.getCPUState());
        // } catch (SimulatorException e) {
        //     System.out.println("✗ Runtime error: " + e);
        // }
    }

    /**
     * OPTION 3: Step Through (single-step debugging).
     *
     * LOGIC:
     *   1. Execute one instruction at current PC
     *   2. Display the instruction just executed
     *   3. Display current register state
     *   4. Ask: Continue stepping? (Y/N)
     */
    private void handleStepThrough() {
        // TODO: Implement step-by-step execution
        //
        // System.out.println("\n═══ Step Through ═══");
        // System.out.println("Press Enter to step, 'q' to quit stepping.");
        //
        // while (!architecture.isHalted()) {
        //     int pc = architecture.getProgramCounter();
        //     try {
        //         int opcode = architecture.readMemory(pc);
        //         System.out.printf("[%04XH] %s\n", pc, architecture.disassemble(opcode));
        //         architecture.step();
        //         System.out.println(architecture.getCPUState());
        //     } catch (SimulatorException e) {
        //         System.out.println("✗ Error: " + e);
        //         break;
        //     }
        //
        //     String input = scanner.nextLine().trim();
        //     if (input.equalsIgnoreCase("q")) break;
        // }
        // if (architecture.isHalted()) {
        //     System.out.println("✓ Program halted (HLT encountered).");
        // }
    }

    /**
     * OPTION 4: View Registers and Flags.
     *
     * LOGIC:
     *   Simply call architecture.getCPUState() and print it.
     */
    private void handleViewRegisters() {
        // TODO: Display CPU state
        //
        //   System.out.println(architecture.getCPUState());
    }

    /**
     * OPTION 5: View Memory.
     *
     * LOGIC:
     *   1. Ask for start address and end address
     *   2. Call architecture.displayMemoryRange(start, end)
     */
    private void handleViewMemory() {
        // TODO: Implement memory viewing
        //
        // System.out.println("\n═══ View Memory ═══");
        // int from = readHexInput("From address: ");
        // int to   = readHexInput("To address: ");
        // architecture.displayMemoryRange(from, to);
    }

    /**
     * OPTION 6: Load Machine Code directly (raw hex bytes).
     *
     * LOGIC:
     *   1. Ask for starting address
     *   2. Read hex bytes from user (space-separated)
     *   3. Parse each hex string to int
     *   4. Write each byte to memory sequentially
     *
     * USE CASE: User already has the opcode bytes (from a textbook or
     *   manual assembly) and wants to enter them directly without
     *   going through the Assembler.
     *
     * EXAMPLE INPUT: "3E 42 06 05 80 76"
     *   This is: MVI A,42H | MVI B,05H | ADD B | HLT
     */
    private void handleLoadMachineCode() {
        // TODO: Implement direct machine code loading
        //
        // System.out.println("\n═══ Load Machine Code ═══");
        // int addr = readHexInput("Start address: ");
        // System.out.println("Enter hex bytes (space-separated), e.g.: 3E 42 06 05 80 76");
        // String input = scanner.nextLine().trim();
        //
        // String[] hexBytes = input.split("\\s+");
        // try {
        //     for (int i = 0; i < hexBytes.length; i++) {
        //         int value = Integer.parseInt(hexBytes[i], 16);
        //         architecture.writeMemory(addr + i, value);
        //     }
        //     System.out.printf("✓ Loaded %d bytes at %04XH.\n", hexBytes.length, addr);
        //     architecture.setProgramCounter(addr);
        // } catch (Exception e) {
        //     System.out.println("✗ Error loading code: " + e.getMessage());
        // }
    }

    /**
     * OPTION 7: Manually set a register or memory value.
     *
     * LOGIC:
     *   1. Ask: set Register or Memory?
     *   2. For register: ask name and value
     *   3. For memory: ask address and value
     */
    private void handleSetValue() {
        // TODO: Implement manual value setting
        //
        // System.out.println("\n═══ Set Value ═══");
        // System.out.println("1. Set Register");
        // System.out.println("2. Set Memory Location");
        // int choice = readIntInput("Choice: ");
        //
        // if (choice == 1) {
        //     System.out.print("Register name (A/B/C/D/E/H/L): ");
        //     String reg = scanner.nextLine().trim().toUpperCase();
        //     int val = readHexInput("Value (hex): ");
        //     try {
        //         architecture.setRegister(reg, val);
        //         System.out.printf("✓ %s = %02XH\n", reg, val);
        //     } catch (SimulatorException e) {
        //         System.out.println("✗ " + e.getMessage());
        //     }
        // } else if (choice == 2) {
        //     int addr = readHexInput("Address (hex): ");
        //     int val  = readHexInput("Value (hex): ");
        //     try {
        //         architecture.writeMemory(addr, val);
        //         System.out.printf("✓ [%04XH] = %02XH\n", addr, val);
        //     } catch (SimulatorException e) {
        //         System.out.println("✗ " + e.getMessage());
        //     }
        // }
    }

    /**
     * OPTION 8: Reset the simulator.
     *
     * LOGIC:
     *   Call architecture.reset() to clear all state.
     */
    private void handleReset() {
        // TODO: Reset and confirm
        //
        // architecture.reset();
        // System.out.println("✓ Simulator reset. All registers and memory cleared.");
    }

    /**
     * OPTION 9: Display help text.
     *
     * LOGIC:
     *   Print a guide explaining:
     *   - How to write 8085 assembly
     *   - Common instructions with examples
     *   - How to use the simulator
     */
    private void handleHelp() {
        // TODO: Implement help display
        //
        // System.out.println("\n═══════════════════════════════════════════════");
        // System.out.println("  j8085 Simulator — Quick Reference Guide");
        // System.out.println("═══════════════════════════════════════════════");
        // System.out.println();
        // System.out.println("  WRITING PROGRAMS:");
        // System.out.println("    - Use standard 8085 assembly mnemonics");
        // System.out.println("    - Labels end with ':'  (e.g., LOOP:)");
        // System.out.println("    - Comments start with ';' (e.g., ; add B to A)");
        // System.out.println("    - Numbers: 42H (hex), 66 (decimal), 01000010B (binary)");
        // System.out.println("    - Every program MUST end with HLT");
        // System.out.println();
        // System.out.println("  COMMON INSTRUCTIONS:");
        // System.out.println("    MVI A, 42H    ; Load 42H into A");
        // System.out.println("    MOV B, A      ; Copy A into B");
        // System.out.println("    ADD B         ; A = A + B");
        // System.out.println("    SUB C         ; A = A - C");
        // System.out.println("    JMP 2000H     ; Jump to address 2000H");
        // System.out.println("    CMP B         ; Compare A with B (sets flags)");
        // System.out.println("    JZ EQUAL      ; Jump to EQUAL label if A == B");
        // System.out.println("    HLT           ; Stop execution");
        // System.out.println();
        // System.out.println("  EXAMPLE PROGRAM (Add two numbers):");
        // System.out.println("    MVI A, 05H    ; A = 05");
        // System.out.println("    MVI B, 03H    ; B = 03");
        // System.out.println("    ADD B         ; A = A + B = 08");
        // System.out.println("    STA 3000H     ; Store result at 3000H");
        // System.out.println("    HLT           ; Stop");
        // System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════
    //  INPUT HELPER METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Reads an integer from the user with a prompt.
     * Handles invalid input gracefully (returns -1).
     *
     * @param prompt  The prompt text to display
     * @return        The integer entered, or -1 on invalid input
     */
    private int readIntInput(String prompt) {
        // TODO: Implement with error handling
        //
        // System.out.print(prompt);
        // try {
        //     String input = scanner.nextLine().trim();
        //     if (input.isEmpty()) return -1;
        //     return Integer.parseInt(input);
        // } catch (NumberFormatException e) {
        //     return -1;
        // }
        return -1; // placeholder
    }

    /**
     * Reads a hexadecimal value from the user with a prompt.
     *
     * LOGIC:
     *   Strip trailing 'H' or 'h' if present.
     *   Strip leading '0x' or '0X' if present.
     *   Parse as base-16 integer.
     *
     * @param prompt  The prompt text to display
     * @return        The parsed hex value, or -1 on invalid input
     */
    private int readHexInput(String prompt) {
        // TODO: Implement hex input parsing
        //
        // System.out.print(prompt);
        // try {
        //     String input = scanner.nextLine().trim().toUpperCase();
        //     if (input.isEmpty()) return -1;
        //     if (input.endsWith("H")) input = input.substring(0, input.length() - 1);
        //     if (input.startsWith("0X")) input = input.substring(2);
        //     return Integer.parseInt(input, 16);
        // } catch (NumberFormatException e) {
        //     System.out.println("Invalid hex value. Please try again.");
        //     return -1;
        // }
        return -1; // placeholder
    }
}
