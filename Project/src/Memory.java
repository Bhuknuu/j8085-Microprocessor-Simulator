// Memory Interface: Defines contracts for 8085 memory operations (16-bit address bus, 8-bit data)
public interface Memory {
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
