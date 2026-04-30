import javax.swing.*; import javax.swing.plaf.metal.MetalLookAndFeel; import java.awt.*; import java.awt.event.*; import java.io.*;
public class MainFrame extends JFrame {
    private DashboardPanel dashboard; private EditorPanel editor; private TerminalPanel terminal; private EmulatorBridge bridge;
    private JSplitPane leftSplit, mainSplit; // [FIX-4] mainSplit stored for toggleTerm
    private JButton playBtn, pauseBtn, stopBtn, resetBtn, stepBtn, assembleBtn; // [FIX-6/13] added stepBtn
    private JTextField startAddrField;
    private ThemeManager.ThemePreset currentTheme = ThemeManager.BUILT_IN.get(0);
    private Architecture arch;
    private Assembler assembler;
    private boolean termVisible = true; // [FIX-4]
    private int lastTermDividerPos = 1000; // [FIX-4]

    public static void setupDarkTheme(){
        try{UIManager.setLookAndFeel(new MetalLookAndFeel());}catch(UnsupportedLookAndFeelException e){System.err.println("[Theme] "+e.getMessage());}
        UIManager.put("Panel.background",Theme.BASE); UIManager.put("OptionPane.background",Theme.BASE); UIManager.put("OptionPane.messageForeground",Theme.TEXT_PRIMARY);
        UIManager.put("SplitPane.background",Theme.BASE); UIManager.put("SplitPaneDivider.background",Theme.BORDER);
        UIManager.put("TabbedPane.background",Theme.SURFACE_1); UIManager.put("TabbedPane.foreground",Theme.TEXT_SECONDARY); UIManager.put("TabbedPane.selected",Theme.SURFACE_2); UIManager.put("TabbedPane.selectedForeground",Theme.ACCENT); UIManager.put("TabbedPane.tabAreaBackground",Theme.SURFACE_1); UIManager.put("TabbedPane.contentAreaColor",Theme.SURFACE_2); UIManager.put("TabbedPane.light",Theme.BORDER); UIManager.put("TabbedPane.highlight",Theme.BORDER); UIManager.put("TabbedPane.shadow",Theme.BASE); UIManager.put("TabbedPane.darkShadow",Theme.BASE); UIManager.put("TabbedPane.focus",Theme.ACCENT); UIManager.put("TabbedPane.contentBorderInsets",new Insets(0,0,0,0));
        UIManager.put("Table.background",Theme.SURFACE_2); UIManager.put("Table.foreground",Theme.TEXT_PRIMARY); UIManager.put("Table.gridColor",Theme.BORDER); UIManager.put("Table.selectionBackground",Theme.ACCENT_DIM); UIManager.put("Table.selectionForeground",Theme.ACCENT); UIManager.put("TableHeader.background",Theme.SURFACE_1); UIManager.put("TableHeader.foreground",Theme.TEXT_SECONDARY);
        UIManager.put("ScrollBar.background",Theme.SURFACE_1); UIManager.put("ScrollBar.thumb",Theme.BORDER); UIManager.put("ScrollBar.track",Theme.SURFACE_1); UIManager.put("ScrollPane.border",BorderFactory.createEmptyBorder());
        UIManager.put("ToolTip.background",Theme.SURFACE_3); UIManager.put("ToolTip.foreground",Theme.TEXT_PRIMARY); UIManager.put("ToolTip.border",BorderFactory.createLineBorder(Theme.BORDER));
        UIManager.put("Button.background",Theme.SURFACE_2); UIManager.put("Button.foreground",Theme.TEXT_PRIMARY); UIManager.put("Button.select",Theme.ACCENT_DIM); UIManager.put("Button.focus",Theme.ACCENT_DIM);
        UIManager.put("TextField.background",Theme.SURFACE_2); UIManager.put("TextField.foreground",Theme.TEXT_PRIMARY); UIManager.put("TextField.caretForeground",Theme.ACCENT); UIManager.put("Label.foreground",Theme.TEXT_PRIMARY); UIManager.put("Dialog.background",Theme.BASE);
    }

    public MainFrame(Architecture arch, Assembler assembler){
        super("j8085 Microprocessor Emulator"); // [FIX-TITLE] "Emulator" not "Simulator"
        this.arch=arch; this.assembler=assembler;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1380,820); setMinimumSize(new Dimension(900,600));
        setLayout(new BorderLayout()); getContentPane().setBackground(Theme.BASE);
        dashboard=new DashboardPanel(); editor=new EditorPanel(); terminal=new TerminalPanel();
        bridge=new EmulatorBridge(arch,assembler,dashboard,editor,terminal,this::onStart,this::onStop);
        dashboard.setBreakpointContext(bridge.getBreakpoints(), addr->bridge.toggleBreakpoint(addr));
        dashboard.setGoRefreshCallback(bridge::refreshUI); // [FIX-5] wire Go button refresh
        setJMenuBar(buildMenuBar()); add(buildToolbar(),BorderLayout.NORTH); add(buildCenter(),BorderLayout.CENTER); add(buildControlStrip(),BorderLayout.EAST); add(buildStatus(),BorderLayout.SOUTH);
        setLocationRelativeTo(null);
        terminal.appendSystem("j8085 ready.  F9=Assemble  F5=Play  F10=Step  F2=Reset  Ctrl+Z=Undo");
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){confirmExit();}
        });
    }

    // == Menu Bar ==
    private JMenuBar buildMenuBar(){
        JMenuBar mb=new JMenuBar(); mb.setBackground(Theme.SURFACE_1); mb.setBorder(Theme.bottomBorder());
        mb.add(fileMenu()); mb.add(editMenu()); mb.add(viewMenu()); mb.add(toolsMenu()); mb.add(themeMenu()); mb.add(helpMenu());
        return mb;
    }
    private JMenu mk(String t){JMenu m=new JMenu(t);m.setForeground(Theme.TEXT_SECONDARY);m.setFont(Theme.FONT_UI);return m;}
    private JMenuItem mi(String t,Runnable r){JMenuItem i=new JMenuItem(t);i.setBackground(Theme.SURFACE_2);i.setForeground(Theme.TEXT_PRIMARY);i.setFont(Theme.FONT_UI);i.addActionListener(e->r.run());return i;}

    private JMenu fileMenu(){JMenu m=mk("File");
        m.add(mi("New File",()->editor.addNewTab(null,null)));
        m.add(mi("Open...",()->openFile()));
        m.addSeparator();
        m.add(mi("Save   Ctrl+S",()->saveFile(false)));
        m.add(mi("Save As...",()->saveFile(true)));
        m.add(mi("Export Intel HEX...",()->exportHex()));
        m.addSeparator();
        m.add(mi("Exit",()->confirmExit()));
        return m;}

    private JMenu editMenu(){JMenu m=mk("Edit");
        m.add(mi("Undo  Ctrl+Z",()->editor.undo()));
        m.add(mi("Redo  Ctrl+Y",()->editor.redo()));
        m.addSeparator();
        m.add(mi("Cut",()->editor.cut()));
        m.add(mi("Copy",()->editor.copy()));
        m.add(mi("Paste",()->editor.paste()));
        return m;}

    private JMenu viewMenu(){JMenu m=mk("View");
        m.add(mi("Toggle Dashboard",()->toggleDash()));
        m.add(mi("Toggle Terminal",()->toggleTerm())); // [FIX-4] now calls real impl
        return m;}

    private JMenu toolsMenu(){JMenu m=mk("Tools");
        m.add(mi("Assemble  F9",()->bridge.assembleAndLoad()));
        m.add(mi("Memory Config...",()->showMemConfig()));
        m.add(mi("Dump Execution Trace",()->dumpTrace()));
        return m;}

    private void dumpTrace(){
        terminal.appendSystem("CPU Execution Trace (Last 64 instructions):");
        for(String s : arch.getTrace()){if(s!=null)terminal.appendMessage(s);}
    }

    private JMenu themeMenu(){
        JMenu m=mk("Theme");
        for(ThemeManager.ThemePreset p:ThemeManager.BUILT_IN){m.add(mi(p.name(),()->applyTheme(p)));}
        m.addSeparator();
        m.add(mi("Import Theme...",()->importTheme()));
        m.add(mi("Export Current Theme...",()->exportTheme()));
        return m;}

    // [FIX-2] About: dynamic theme name + more info. [FIX-8/9] Instruction Set & Tutorial added.
    private JMenu helpMenu(){JMenu m=mk("Help");
        m.add(mi("Keyboard Shortcuts",()->showShortcuts()));
        m.add(mi("Instruction Set Reference",()->showInstructionSet()));
        m.add(mi("Tutorial",()->showTutorial()));
        m.addSeparator();
        m.add(mi("About",()->showAbout()));
        return m;}

    // == Toolbar — Origin field only; Assemble moved to control strip [FIX-13] ==
    private JPanel buildToolbar(){
        JPanel tb=new JPanel(new FlowLayout(FlowLayout.LEFT,6,4)); tb.setBackground(Theme.SURFACE_1); tb.setBorder(Theme.bottomBorder());
        JLabel lbl=new JLabel("Origin:"); lbl.setForeground(Theme.TEXT_DIM); lbl.setFont(Theme.FONT_LABEL); tb.add(lbl);
        startAddrField=new JTextField("2000",6); startAddrField.setBackground(Theme.SURFACE_2); startAddrField.setForeground(Theme.TEXT_PRIMARY); startAddrField.setCaretColor(Theme.ACCENT); startAddrField.setFont(Theme.FONT_MONO); startAddrField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER),BorderFactory.createEmptyBorder(3,6,3,6)));
        startAddrField.addActionListener(e->updateOrigin()); tb.add(startAddrField);
        return tb;}
    private void updateOrigin(){try{int a=Integer.parseInt(startAddrField.getText().trim(),16);editor.setStartAddress(a);}catch(NumberFormatException ignored){}}

    // == 3-pane Center — stores mainSplit for toggleTerm [FIX-4] ==
    private JSplitPane buildCenter(){
        leftSplit=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,dashboard,editor);
        leftSplit.setDividerLocation(270); leftSplit.setBorder(null); leftSplit.setDividerSize(4); leftSplit.setBackground(Theme.BORDER);
        mainSplit=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftSplit,terminal);
        mainSplit.setDividerLocation(1000); mainSplit.setBorder(null); mainSplit.setDividerSize(4); mainSplit.setBackground(Theme.BORDER);
        return mainSplit;}

    private void toggleDash(){leftSplit.setDividerLocation(leftSplit.getDividerLocation()>20?0:270);}

    // [FIX-4] Toggle Terminal actually works now
    private void toggleTerm(){
        if(mainSplit==null)return;
        termVisible=!termVisible;
        if(termVisible){
            mainSplit.setDividerLocation(lastTermDividerPos);
        } else {
            lastTermDividerPos=mainSplit.getDividerLocation();
            mainSplit.setDividerLocation(mainSplit.getWidth()-mainSplit.getDividerSize());
        }
    }

    // == Control Strip (far right) — text buttons, Assemble at top [FIX-6/13] ==
    private JPanel buildControlStrip(){
        JPanel cs=new JPanel(); cs.setLayout(new BoxLayout(cs,BoxLayout.Y_AXIS)); cs.setBackground(Theme.SURFACE_1); cs.setBorder(Theme.leftBorder()); cs.setPreferredSize(new Dimension(96,0));

        assembleBtn=mkCtrlBtn("ASSEMBLE",Theme.ACCENT,"Assemble (F9)"); assembleBtn.addActionListener(e->bridge.assembleAndLoad());
        playBtn    =mkCtrlBtn("Play",    Theme.SUCCESS,"Play (F5)");     playBtn.addActionListener(e->bridge.play());
        stepBtn    =mkCtrlBtn("Step",    Theme.ACCENT, "Step (F10)");    stepBtn.addActionListener(e->bridge.step());
        pauseBtn   =mkCtrlBtn("Pause",   Theme.WARNING,"Pause");         pauseBtn.addActionListener(e->bridge.pause());
        stopBtn    =mkCtrlBtn("Stop",    Theme.ERROR,  "Stop");          stopBtn.addActionListener(e->bridge.stop());
        resetBtn   =mkCtrlBtn("Reset",   Theme.TEXT_SECONDARY,"Reset (F2)"); resetBtn.addActionListener(e->bridge.reset());

        pauseBtn.setEnabled(false); stopBtn.setEnabled(false);

        cs.add(Box.createVerticalStrut(10));
        cs.add(assembleBtn);
        cs.add(Box.createVerticalStrut(8));
        cs.add(mkHSep());
        cs.add(Box.createVerticalStrut(8));
        cs.add(playBtn);
        cs.add(Box.createVerticalStrut(4));
        cs.add(stepBtn);
        cs.add(Box.createVerticalStrut(4));
        cs.add(pauseBtn);
        cs.add(Box.createVerticalStrut(4));
        cs.add(stopBtn);
        cs.add(Box.createVerticalStrut(8));
        cs.add(mkHSep());
        cs.add(Box.createVerticalStrut(8));
        cs.add(resetBtn);
        cs.add(Box.createVerticalGlue());

        getRootPane().registerKeyboardAction(e->bridge.assembleAndLoad(),KeyStroke.getKeyStroke("F9"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e->bridge.play(),KeyStroke.getKeyStroke("F5"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e->bridge.step(),KeyStroke.getKeyStroke("F10"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e->bridge.reset(),KeyStroke.getKeyStroke("F2"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e->saveFile(false),KeyStroke.getKeyStroke("control S"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e->editor.undo(),KeyStroke.getKeyStroke("control Z"),JComponent.WHEN_IN_FOCUSED_WINDOW); // [FIX-1] Ctrl+Z
        getRootPane().registerKeyboardAction(e->editor.redo(),KeyStroke.getKeyStroke("control Y"),JComponent.WHEN_IN_FOCUSED_WINDOW); // [FIX-1] Ctrl+Y
        return cs;}

    private void onStart(){assembleBtn.setEnabled(false);playBtn.setEnabled(false);stepBtn.setEnabled(false);pauseBtn.setEnabled(true);stopBtn.setEnabled(true);}
    private void onStop() {assembleBtn.setEnabled(true); playBtn.setEnabled(true); stepBtn.setEnabled(true); pauseBtn.setEnabled(false);stopBtn.setEnabled(false);}

    // == Status Bar ==
    private JPanel buildStatus(){
        JPanel s=new JPanel(new FlowLayout(FlowLayout.LEFT,10,3)); s.setBackground(Theme.SURFACE_1); s.setBorder(Theme.topBorder());
        JLabel l=new JLabel("j8085 Emulator  |  F9=Assemble  |  F5=Play  |  F10=Step  |  F2=Reset  |  Ctrl+Z=Undo  |  Ctrl+S=Save");
        l.setForeground(Theme.TEXT_DIM); l.setFont(Theme.FONT_LABEL); s.add(l); return s;}

    // == File Operations ==
    private void openFile(){JFileChooser fc=fc(".asm");if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){File f=fc.getSelectedFile();editor.addNewTab(f.getName(),f);editor.loadIntoActive(f);}}
    private void saveFile(boolean saveAs){
        if(!saveAs&&editor.getActiveFile()!=null){editor.saveActive(editor.getActiveFile());}
        else{JFileChooser fc=fc(".asm");if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION)editor.saveActive(fc.getSelectedFile());}
    }
    private void exportHex(){
        String code=editor.getActiveCode();
        if(code.trim().isEmpty()){terminal.appendError("Nothing to export.");return;}
        try{
            int start=arch.getMemoryStart();
            int[] bytes=assembler.assembleToBuffer(code,start);
            JFileChooser fc=fc(".hex");
            if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
                File f=fc.getSelectedFile();
                if(!f.getName().contains("."))f=new File(f.getAbsolutePath()+".hex");
                try(java.io.FileWriter fw=new java.io.FileWriter(f)){fw.write(IntelHexWriter.convert(bytes,start));}
                terminal.appendSystem("Exported "+bytes.length+" bytes to "+f.getName());
            }
        }catch(SimulatorException e){terminal.appendError("Export: "+e.getMessage());}
        catch(IOException e){terminal.appendError("File write: "+e.getMessage());}
    }
    private JFileChooser fc(String ext){JFileChooser fc=new JFileChooser();fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Assembly Files (*"+ext+")",ext.replace(".",""),"asm","bin","j8085theme"));return fc;}

    // == Theme Operations ==
    private void applyTheme(ThemeManager.ThemePreset p){
        currentTheme=p; ThemeManager.apply(p); setupDarkTheme();
        SwingUtilities.updateComponentTreeUI(this);
        dashboard.updateTheme(); editor.updateTheme(); terminal.updateTheme();
        terminal.appendSystem("Theme: "+p.name());
    }
    private void importTheme(){JFileChooser fc=fc(".j8085theme");if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){try{ThemeManager.ThemePreset p=ThemeManager.importFromFile(fc.getSelectedFile());applyTheme(p);}catch(IOException e){terminal.appendError("Theme import failed: "+e.getMessage());}}}
    private void exportTheme(){JFileChooser fc=fc(".j8085theme");if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){try{ThemeManager.exportToFile(currentTheme,fc.getSelectedFile());terminal.appendSystem("Theme exported.");}catch(IOException e){terminal.appendError("Export failed: "+e.getMessage());}}}

    // [FIX-1] Ctrl+Z/Y added. Function keys now documented as actually registered.
    private void showShortcuts(){
        JOptionPane.showMessageDialog(this,
            "F9      — Assemble\n" +
            "F5      — Play / Run\n" +
            "F10     — Step (single instruction)\n" +
            "F2      — Reset CPU\n" +
            "Ctrl+S  — Save file\n" +
            "Ctrl+Z  — Undo\n" +
            "Ctrl+Y  — Redo",
            "Keyboard Shortcuts", JOptionPane.INFORMATION_MESSAGE);
    }

    // [FIX-2] About uses currentTheme.name() dynamically and has full description
    private void showAbout(){
        String info =
            "j8085 Microprocessor Emulator\n" +
            "══════════════════════════════════════════\n\n" +
            "A Java-based educational emulator designed to help users learn\n" +
            "and practice 8085 assembly language programming.\n\n" +
            "It recreates the working behaviour of the Intel 8085 microprocessor,\n" +
            "allowing users to write, assemble, execute, and debug programs\n" +
            "without requiring physical hardware.\n\n" +
            "KEY FEATURES:\n" +
            "  • Register & flag visualization (live)\n" +
            "  • Memory inspection with address gutter\n" +
            "  • Step-by-step execution & breakpoints\n" +
            "  • I/O port simulation\n" +
            "  • Intel HEX export\n" +
            "  • Multiple colour themes\n" +
            "  • Autocomplete for assembly mnemonics\n\n" +
            "Current Theme : " + currentTheme.name() + "\n" +
            "Runtime       : Pure Java Swing";
        JOptionPane.showMessageDialog(this, info, "About j8085 Emulator", JOptionPane.INFORMATION_MESSAGE);
    }

    // [FIX-8] Instruction Set Reference
    private void showInstructionSet(){
        JTextArea ta=new JTextArea(28,62); ta.setEditable(false); ta.setFont(Theme.FONT_MONO);
        ta.setBackground(Theme.SURFACE_2); ta.setForeground(Theme.TEXT_PRIMARY); ta.setMargin(new Insets(8,10,8,10));
        ta.setText(getInstructionSetText());
        ta.setCaretPosition(0);
        JScrollPane sp=new JScrollPane(ta); sp.getVerticalScrollBar().setUI(new ThinScrollBarUI());
        JOptionPane.showMessageDialog(this,sp,"8085 Instruction Set Reference",JOptionPane.PLAIN_MESSAGE);
    }

    // [FIX-9] Tutorial
    private void showTutorial(){
        JTextArea ta=new JTextArea(26,58); ta.setEditable(false); ta.setFont(Theme.FONT_MONO);
        ta.setBackground(Theme.SURFACE_2); ta.setForeground(Theme.TEXT_PRIMARY); ta.setMargin(new Insets(8,10,8,10));
        ta.setText(getTutorialText());
        ta.setCaretPosition(0);
        JScrollPane sp=new JScrollPane(ta); sp.getVerticalScrollBar().setUI(new ThinScrollBarUI());
        JOptionPane.showMessageDialog(this,sp,"j8085 Tutorial — Quick Start Guide",JOptionPane.PLAIN_MESSAGE);
    }

    private String getInstructionSetText(){
        return
        "8085 INSTRUCTION SET REFERENCE\n" +
        "═══════════════════════════════════════════════════════\n\n" +
        "DATA TRANSFER\n" +
        "  MOV  r1,r2     Move register to register            1B\n" +
        "  MVI  r, data   Move immediate to register           2B\n" +
        "  LDA  addr      Load A from memory address           3B\n" +
        "  STA  addr      Store A to memory address            3B\n" +
        "  LHLD addr      Load H,L from memory                 3B\n" +
        "  SHLD addr      Store H,L to memory                  3B\n" +
        "  LDAX rp        Load A indirect (B/D pair)           1B\n" +
        "  STAX rp        Store A indirect (B/D pair)          1B\n" +
        "  XCHG           Exchange HL and DE                   1B\n" +
        "  LXI  rp,d16    Load register pair immediate         3B\n\n" +
        "ARITHMETIC\n" +
        "  ADD  r         A = A + r                            1B\n" +
        "  ADI  data      A = A + immediate                    2B\n" +
        "  ADC  r         A = A + r + CY                       1B\n" +
        "  ACI  data      A = A + imm + CY                     2B\n" +
        "  SUB  r         A = A - r                            1B\n" +
        "  SUI  data      A = A - immediate                    2B\n" +
        "  SBB  r         A = A - r - CY                       1B\n" +
        "  SBI  data      A = A - imm - CY                     2B\n" +
        "  INR  r         Increment register                   1B\n" +
        "  DCR  r         Decrement register                   1B\n" +
        "  INX  rp        Increment register pair              1B\n" +
        "  DCX  rp        Decrement register pair              1B\n" +
        "  DAD  rp        HL = HL + register pair              1B\n" +
        "  DAA            Decimal adjust A                     1B\n\n" +
        "LOGICAL\n" +
        "  ANA  r / ANI data    AND with A                  1B/2B\n" +
        "  ORA  r / ORI data    OR  with A                  1B/2B\n" +
        "  XRA  r / XRI data    XOR with A                  1B/2B\n" +
        "  CMP  r / CPI data    Compare with A              1B/2B\n" +
        "  CMA            Complement A                         1B\n" +
        "  CMC            Complement carry                     1B\n" +
        "  STC            Set carry                            1B\n" +
        "  RLC            Rotate A left                        1B\n" +
        "  RRC            Rotate A right                       1B\n" +
        "  RAL            Rotate A left through carry          1B\n" +
        "  RAR            Rotate A right through carry         1B\n\n" +
        "BRANCH\n" +
        "  JMP  addr      Unconditional jump                   3B\n" +
        "  JZ   addr      Jump if Zero                         3B\n" +
        "  JNZ  addr      Jump if Not Zero                     3B\n" +
        "  JC   addr      Jump if Carry                        3B\n" +
        "  JNC  addr      Jump if No Carry                     3B\n" +
        "  JP   addr      Jump if Positive                     3B\n" +
        "  JM   addr      Jump if Minus                        3B\n" +
        "  JPE  addr      Jump if Parity Even                  3B\n" +
        "  JPO  addr      Jump if Parity Odd                   3B\n" +
        "  CALL addr      Call subroutine                      3B\n" +
        "  CC/CNC/CZ/CNZ/CP/CM/CPE/CPO addr  Conditional call 3B\n" +
        "  RET            Return from subroutine               1B\n" +
        "  RC/RNC/RZ/RNZ/RP/RM/RPE/RPO       Conditional ret  1B\n" +
        "  PCHL           PC = HL                              1B\n" +
        "  RST  n         Restart (n = 0..7)                   1B\n\n" +
        "STACK & I/O\n" +
        "  PUSH rp        Push register pair to stack          1B\n" +
        "  POP  rp        Pop register pair from stack         1B\n" +
        "  XTHL           Exchange HL with top of stack        1B\n" +
        "  SPHL           SP = HL                              1B\n" +
        "  IN   port      A = input from port                  2B\n" +
        "  OUT  port      Output A to port                     2B\n\n" +
        "MACHINE CONTROL\n" +
        "  NOP            No operation                         1B\n" +
        "  HLT            Halt (every program must end here)   1B\n" +
        "  EI / DI        Enable / Disable interrupts          1B\n" +
        "  RIM / SIM      Read / Set interrupt mask            1B\n";
    }

    private String getTutorialText(){
        return
        "j8085 EMULATOR — QUICK START TUTORIAL\n" +
        "═══════════════════════════════════════════════\n\n" +
        "STEP 1 — Write Your Program\n" +
        "  Type 8085 assembly in the editor (left-centre pane).\n" +
        "  Example — Add two numbers and store result:\n\n" +
        "    MVI  A, 05H   ; Load 5 into register A\n" +
        "    MVI  B, 03H   ; Load 3 into register B\n" +
        "    ADD  B        ; A = A + B  →  A = 08H\n" +
        "    STA  3000H    ; Store result at address 3000H\n" +
        "    HLT           ; Halt the CPU\n\n" +
        "STEP 2 — Set the Origin Address\n" +
        "  The Origin field (top-left) sets the load address.\n" +
        "  Default is 2000H. The address gutter in the editor\n" +
        "  updates as you type.\n\n" +
        "STEP 3 — Assemble\n" +
        "  Click [ASSEMBLE] (right panel) or press F9.\n" +
        "  A success or error message appears in OUTPUT.\n" +
        "  An error dialog pops up if the code is invalid.\n\n" +
        "STEP 4 — Run or Debug\n" +
        "  [Play]   / F5   — Run program at full speed\n" +
        "  [Step]   / F10  — Execute one instruction at a time\n" +
        "  [Pause]         — Pause continuous execution\n" +
        "  [Stop]          — Abort execution\n" +
        "  [Reset]  / F2   — Reset CPU registers and halt flag\n\n" +
        "STEP 5 — Inspect State (Dashboard, left panel)\n" +
        "  REGISTERS — Live A, B, C, D, E, H, L, PC, SP values\n" +
        "  FLAGS     — LED indicators for S, Z, AC, P, CY\n" +
        "  I/O PORTS — Shows ports touched by IN/OUT\n" +
        "  MEMORY    — Scrollable view centred on PC\n" +
        "  GOTO      — Type any address range and click [Go]\n" +
        "              to jump the memory view to that range.\n" +
        "  BREAKPOINT — Double-click a memory row to toggle.\n\n" +
        "STEP 6 — Output Panel (right)\n" +
        "  Shows assembly results, execution messages, errors.\n" +
        "  Click [Clear] to erase previous output.\n\n" +
        "KEYBOARD SHORTCUTS\n" +
        "  F9      Assemble        F5    Play\n" +
        "  F10     Step            F2    Reset\n" +
        "  Ctrl+S  Save            Ctrl+Z Undo\n" +
        "  Ctrl+Y  Redo\n\n" +
        "WRITING TIPS\n" +
        "  • Every program MUST end with HLT\n" +
        "  • Labels end with ':'   e.g.  LOOP:\n" +
        "  • Comments start with ';'\n" +
        "  • Numbers: 42H (hex)  66 (decimal)  01000010B (binary)\n" +
        "  • Start typing a mnemonic to see autocomplete hints\n";
    }

    // == Helpers ==
    private JButton mkBtn(String t,Color bg,Color fg){JButton b=new JButton(t);b.setBackground(bg);b.setForeground(fg);b.setFont(Theme.FONT_UI_B);b.setFocusPainted(false);b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER),BorderFactory.createEmptyBorder(5,10,5,10)));return b;}

    // [FIX-6/13] Text button for control strip
    private JButton mkCtrlBtn(String t,Color fg,String tip){
        JButton b=new JButton(t); b.setForeground(fg); b.setBackground(Theme.SURFACE_2); b.setFont(Theme.FONT_UI_B);
        b.setFocusPainted(false); b.setToolTipText(tip); b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(86,30));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER),BorderFactory.createEmptyBorder(4,6,4,6)));
        return b;}

    private JSeparator mkHSep(){JSeparator s=new JSeparator(JSeparator.HORIZONTAL);s.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));s.setForeground(Theme.BORDER);return s;}
    private JSeparator sep(){JSeparator s=new JSeparator(JSeparator.VERTICAL);s.setPreferredSize(new Dimension(1,22));s.setForeground(Theme.BORDER);return s;}
    public void setArchitecture(Architecture arch){this.arch=arch;bridge.setArchitecture(arch);}

    // [AG-FIX 2.4] Memory reconfiguration
    private void showMemConfig(){
        MemoryConfigDialog dlg=new MemoryConfigDialog(this); dlg.setVisible(true);
        if(!dlg.isUserConfirmed())return;
        Architecture newArch;
        if(dlg.isDefaultSelected()){newArch=new Architecture();}
        else{try{newArch=new Architecture(dlg.getStartAddress(),dlg.getEndAddress());}catch(SimulatorException ex){terminal.appendError("Invalid range: "+ex.getMessage());return;}}
        this.arch=newArch; bridge.setArchitecture(newArch); bridge.reset();
        terminal.appendSystem("Memory: 0x"+String.format("%04X",newArch.getMemoryStart())+"-0x"+String.format("%04X",newArch.getMemoryEnd()));
    }

    // [AG-FIX 2.6] Dirty-file exit guard
    private void confirmExit(){
        if(editor.hasDirtyTabs()){
            int r=JOptionPane.showConfirmDialog(this,"Unsaved changes. Exit anyway?","Confirm Exit",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(r!=JOptionPane.YES_OPTION)return;
        }
        dispose(); System.exit(0);
    }
}
