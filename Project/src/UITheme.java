
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;


final class Theme {
    private Theme() {}

    public static volatile Color
        BASE      = new Color(0x121212),
        SURFACE_1 = new Color(0x171717),
        SURFACE_2 = new Color(0x1E1E1E),
        SURFACE_3 = new Color(0x252525);

    public static volatile Color
        TEXT_PRIMARY   = new Color(0xE2E2E2),
        TEXT_SECONDARY = new Color(0x828282),
        TEXT_DIM       = new Color(0x606060);

    public static volatile Color
        ACCENT     = new Color(0x9BF0E1),
        ACCENT_DIM = new Color(0x2A4A45),
        SUCCESS    = new Color(0x4ADB7A),
        WARNING    = new Color(0xDBA64A),
        ERROR      = new Color(0xDB4A4A),
        BORDER     = new Color(0x333333);

    public static final Font
        FONT_UI     = new Font(Font.SANS_SERIF,  Font.PLAIN,  12),
        FONT_UI_B   = new Font(Font.SANS_SERIF,  Font.BOLD,   12),
        FONT_LABEL  = new Font(Font.SANS_SERIF,  Font.BOLD,   11),
        FONT_MONO   = new Font(Font.MONOSPACED,  Font.PLAIN,  13),
        FONT_EDITOR = new Font(Font.MONOSPACED,  Font.PLAIN,  14);

    public static Border card()                        { return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(8, 8, 8, 8)); }
    public static Border padded(int t, int l, int b, int r) { return BorderFactory.createEmptyBorder(t, l, b, r); }
    public static Border topBorder()                   { return BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER); }
    public static Border bottomBorder()                { return BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER); }
    public static Border leftBorder()                  { return BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER); }
}


// ---------------------------------------------------------------------------
// ThemeManager — Built-in presets plus file import/export
// [FIX-3] Added 3 light themes: Light Gray, Solarized Light, GitHub Light
// ---------------------------------------------------------------------------
final class ThemeManager {
    private ThemeManager() {}

    public record ThemePreset(
            String name,
            String base, String surf1, String surf2, String surf3,
            String textPrimary, String textSecondary, String textDim,
            String accent, String accentDim,
            String success, String warning, String error, String border) {}

    public static final List<ThemePreset> BUILT_IN = new ArrayList<>(List.of(
        // ── Dark themes ──────────────────────────────────────────────────────
        new ThemePreset("Backstage Dark",
            "121212","171717","1E1E1E","252525",
            "E2E2E2","828282","606060",
            "9BF0E1","2A4A45","4ADB7A","DBA64A","DB4A4A","333333"),

        new ThemePreset("Midnight Blue",
            "0D1117","161B22","1C2128","21262D",
            "E6EDF3","8B949E","6E7681",
            "58A6FF","0D2137","3FB950","D29922","F85149","30363D"),

        new ThemePreset("Solarized Dark",
            "002B36","073642","003847","004052",
            "839496","657B83","586E75",
            "2AA198","002D38","859900","CB4B16","DC322F","073642"),

        new ThemePreset("Amber Terminal",
            "0D0D00","1A1A00","1F1F00","252500",
            "FFB000","CC8800","996600",
            "FFB000","2A2200","88BB00","FF6600","FF2200","333300"),

        new ThemePreset("Forest Green",
            "0A1A0A","0F230F","122312","152515",
            "C8E6C9","81C784","4CAF50",
            "4CAF50","0A2A0A","8BC34A","FFC107","EF5350","1B5E20"),

        new ThemePreset("High Contrast",
            "000000","0A0A0A","111111","1A1A1A",
            "FFFFFF","CCCCCC","888888",
            "FFFFFF","1A1A1A","00FF00","FFFF00","FF0000","444444"),

        // ── Light themes ─────────────────────────────────────────────────────
        // [FIX-3] Light Gray — clean neutral light theme
        new ThemePreset("Light Gray",
            "F5F5F5","EEEEEE","E8E8E8","DCDCDC",
            "1A1A1A","505050","909090",
            "007A6E","B2DDD8","2E7D32","E65100","C62828","BDBDBD"),

        // [FIX-3] Solarized Light — classic warm light theme
        new ThemePreset("Solarized Light",
            "FDF6E3","EEE8D5","E8DFD0","DDD6C1",
            "657B83","839496","93A1A1",
            "2AA198","D4EFED","859900","CB4B16","DC322F","C8BBA8"),

        // [FIX-3] GitHub Light — familiar and readable
        new ThemePreset("GitHub Light",
            "FFFFFF","F6F8FA","EAEEF2","D0D7DE",
            "1F2328","636C76","8B949E",
            "0969DA","CCE5FF","1A7F37","9A6700","CF222E","D0D7DE")
    ));

    public static void apply(ThemePreset p) {
        Theme.BASE           = h(p.base());
        Theme.SURFACE_1      = h(p.surf1());
        Theme.SURFACE_2      = h(p.surf2());
        Theme.SURFACE_3      = h(p.surf3());
        Theme.TEXT_PRIMARY   = h(p.textPrimary());
        Theme.TEXT_SECONDARY = h(p.textSecondary());
        Theme.TEXT_DIM       = h(p.textDim());
        Theme.ACCENT         = h(p.accent());
        Theme.ACCENT_DIM     = h(p.accentDim());
        Theme.SUCCESS        = h(p.success());
        Theme.WARNING        = h(p.warning());
        Theme.ERROR          = h(p.error());
        Theme.BORDER         = h(p.border());
    }

    public static void exportToFile(ThemePreset p, File f) throws IOException {
        Properties pr = new Properties();
        pr.setProperty("name",          p.name());
        pr.setProperty("base",          p.base());
        pr.setProperty("surf1",         p.surf1());
        pr.setProperty("surf2",         p.surf2());
        pr.setProperty("surf3",         p.surf3());
        pr.setProperty("textPrimary",   p.textPrimary());
        pr.setProperty("textSecondary", p.textSecondary());
        pr.setProperty("textDim",       p.textDim());
        pr.setProperty("accent",        p.accent());
        pr.setProperty("accentDim",     p.accentDim());
        pr.setProperty("success",       p.success());
        pr.setProperty("warning",       p.warning());
        pr.setProperty("error",         p.error());
        pr.setProperty("border",        p.border());
        try (FileWriter fw = new FileWriter(f)) { pr.store(fw, "j8085 Theme"); }
    }

    public static ThemePreset importFromFile(File f) throws IOException {
        Properties p = new Properties();
        try (FileReader fr = new FileReader(f)) { p.load(fr); }
        return new ThemePreset(
                p.getProperty("name",          "Custom"),
                p.getProperty("base",          "121212"),
                p.getProperty("surf1",         "171717"),
                p.getProperty("surf2",         "1E1E1E"),
                p.getProperty("surf3",         "252525"),
                p.getProperty("textPrimary",   "E2E2E2"),
                p.getProperty("textSecondary", "828282"),
                p.getProperty("textDim",       "606060"),
                p.getProperty("accent",        "9BF0E1"),
                p.getProperty("accentDim",     "2A4A45"),
                p.getProperty("success",       "4ADB7A"),
                p.getProperty("warning",       "DBA64A"),
                p.getProperty("error",         "DB4A4A"),
                p.getProperty("border",        "333333"));
    }

    private static Color h(String hex) {
        try { return new Color(Integer.parseInt(hex.replace("#", ""), 16)); }
        catch (NumberFormatException e) { return Color.GRAY; }
    }
}


// ---------------------------------------------------------------------------
// ThinScrollBarUI — Slim, theme-aware custom scrollbar skin
// ---------------------------------------------------------------------------
class ThinScrollBarUI extends BasicScrollBarUI {
    @Override protected void configureScrollBarColors() {
        thumbColor           = Theme.BORDER;
        trackColor           = Theme.SURFACE_1;
        thumbHighlightColor  = Theme.TEXT_DIM;
        thumbDarkShadowColor = Theme.BASE;
    }
    @Override protected JButton createDecreaseButton(int o) { return zb(); }
    @Override protected JButton createIncreaseButton(int o) { return zb(); }
    private JButton zb() { JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }

    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        g.setColor(Theme.SURFACE_1); g.fillRect(r.x,r.y,r.width,r.height);
    }

    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if(r.isEmpty()) return;
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isDragging?Theme.TEXT_SECONDARY:Theme.BORDER);
        g2.fillRoundRect(r.x+2,r.y+2,r.width-4,r.height-4,4,4);
        g2.dispose();
    }

    @Override public Dimension getPreferredSize(JComponent c) {
        return scrollbar.getOrientation()==JScrollBar.VERTICAL
                ? new Dimension(7,0) : new Dimension(0,7);
    }
}
