/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                        MAIN — 8085 SIMULATOR                             ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  PURPOSE:                                                                ║
 * ║  The entry point of the j8085 Microprocessor Simulator application.      ║
 * ║  Creates the UserInterface and hands control to it.                      ║
 * ║                                                                          ║
 * ║  APPLICATION STARTUP SEQUENCE:                                           ║
 * ║    1. JVM calls Main.main()                                              ║
 * ║    2. Main creates a UserInterface object                                ║
 * ║    3. Main calls ui.start()                                              ║
 * ║    4. UserInterface takes over:                                          ║
 * ║       a. Displays welcome banner                                         ║
 * ║       b. Asks for memory configuration                                   ║
 * ║       c. Creates Architecture (CPU) with chosen config                   ║
 * ║       d. Enters the main menu loop                                       ║
 * ║       e. Menu loop continues until user selects "Exit"                   ║
 * ║    5. Control returns to main(), program terminates                      ║
 * ║                                                                          ║
 * ║  ARCHITECTURE OVERVIEW:                                                  ║
 * ║                                                                          ║
 * ║    Main                                                                  ║
 * ║     └─► UserInterface  (CLI layer — handles all I/O)                    ║
 * ║          ├─► Architecture  (CPU core — registers, memory, execution)    ║
 * ║          │    ├─► implements Memory     (memory operations)             ║
 * ║          │    ├─► implements InstrucSet  (instruction execution)        ║
 * ║          │    └─► uses ALU  (arithmetic, logic, flag management)        ║
 * ║          └─► Assembler  (assembly text → machine code bytes)           ║
 * ║               └─► uses OpcodeTable (opcode ↔ mnemonic lookup)          ║
 * ║                                                                          ║
 * ║  FILE MANIFEST:                                                          ║
 * ║  ┌────────────────────────────────────────────────────────────────┐      ║
 * ║  │ File                  │ Role                                   │      ║
 * ║  ├────────────────────────────────────────────────────────────────┤      ║
 * ║  │ Main.java             │ Entry point (this file)                │      ║
 * ║  │ UserInterface.java    │ CLI menus, input/output, display       │      ║
 * ║  │ Architecture.java     │ CPU core: registers, memory, execute   │      ║
 * ║  │ Memory.java           │ Interface: memory operation contracts  │      ║
 * ║  │ InstrucSet.java       │ Interface: all 8085 instruction sigs   │      ║
 * ║  │ ALU.java              │ Arithmetic/logic ops, flag register    │      ║
 * ║  │ Assembler.java        │ Assembly language → machine code       │      ║
 * ║  │ OpcodeTable.java      │ Opcode ↔ mnemonic mapping table       │      ║
 * ║  │ SimulatorException.java│ Custom error handling                 │      ║
 * ║  └────────────────────────────────────────────────────────────────┘      ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */
public class Main {

    /**
     * Application entry point.
     *
     * @param args  Command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        // TODO: Create and start the UserInterface
        //
        // LOGIC:
        //   1. Instantiate the UserInterface
        //   2. Call start() to begin the interactive session
        //   3. When start() returns (user chose "Exit"), program ends naturally

        UserInterface ui = new UserInterface();
        ui.start();
    }
}