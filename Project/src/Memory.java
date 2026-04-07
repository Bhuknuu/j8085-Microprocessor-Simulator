public interface Memory {
    int DEFAULT_START_ADDRESS = 0x0000;
    int DEFAULT_END_ADDRESS = 0xFFFF;
    int MAX_MEMORY_SIZE = 65536;

    /* Reads a single byte from memory at the given address.
     * LOGIC:
     *   1. Validate that 'address' is within the allocated memory range
     *   2. If valid, return memory[address] (the byte stored there)
     *   3. If invalid, throw SimulatorException with the bad address
     * @param address  The 16-bit memory address (0x0000 to 0xFFFF)
     * @return         The byte value stored at that address (0x00 to 0xFF)
     * @throws SimulatorException if address is out of allocated range
     */
    int readMemory(int address) throws SimulatorException;

    /*
     * Writes a single byte to memory at the given address
     * LOGIC:
     *   1. Validate that 'address' is within the allocated memory range
     *   2. Validate that 'data' is a valid byte (0x00 to 0xFF)
     *   3. Set memory[address] = data
     * @param address  The 16-bit memory address (0x0000 to 0xFFFF)
     * @param data     The byte value to store (0x00 to 0xFF)
     * @throws SimulatorException if address is out of range or data is invalid
     */
    void writeMemory(int address, int data) throws SimulatorException;

    /*
     * Resets ALL memory locations to 0x00 (zero).
     *
     * LOGIC:
     *   Iterate through the entire allocated memory array and set each
     *   element to 0. This simulates a power-on-reset condition.
     *
     * HARDWARE PARALLEL:
     *   On real hardware, memory contents after power-on are undefined
     *   (random garbage). We initialize to 0 for cleanliness in our simulator.
     */
    void resetMemory();

    /*
     * Returns the entire memory array for display/debugging purposes.
     *
     * LOGIC:
     *   Return a COPY of the internal memory array (defensive copy)
     *   so that external code cannot corrupt internal state.
     *
     * USE CASE:
     *   The UserInterface calls this to show the user the current
     *   state of memory in the simulator's display panel.
     *
     * @return A copy of the internal memory array
     */
    int[] getMemoryDump();

    /*
     * Returns the starting address of the allocated memory range.
     *
     * @return The base address (e.g., 0x0000 for default)
     */
    int getMemoryStart();

    /**
     * Returns the ending address of the allocated memory range.
     *
     * @return The top address (e.g., 0xFFFF for default full range)
     */
    int getMemoryEnd();

    /**
     * Loads a block of data into memory starting at the given address.
     *
     * LOGIC:
     *   1. For each byte in the data array:
     *      memory[startAddress + i] = data[i]
     *   2. Validate that startAddress + data.length doesn't exceed memory bounds
     *
     * USE CASE:
     *   When the user writes an assembly program, the Assembler converts it
     *   to machine code bytes. This method loads those bytes into memory
     *   starting at the user-specified address (typically 2000H or 8000H).
     *
     * @param startAddress  Where to begin loading in memory
     * @param data          Array of byte values (machine code) to load
     * @throws SimulatorException if the data would overflow memory bounds
     */
    void loadProgram(int startAddress, int[] data) throws SimulatorException;

    /**
     * Displays a formatted hex dump of memory between two addresses.
     *
     * LOGIC:
     *   1. Validate both addresses are within range
     *   2. Print rows of 16 bytes each, formatted as:
     *      ADDRESS: XX XX XX XX  XX XX XX XX  XX XX XX XX  XX XX XX XX
     *
     * USE CASE:
     *   Allows the user to inspect a specific region of memory after
     *   loading a program or after execution to see results.
     *
     * @param fromAddress  Start address for the dump
     * @param toAddress    End address for the dump
     */
    void displayMemoryRange(int fromAddress, int toAddress);
}
