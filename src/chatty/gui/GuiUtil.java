
package chatty.gui;

import chatty.Helper;
import chatty.gui.components.textpane.ChannelTextPane;
import chatty.lang.Language;
import chatty.util.Debugging;
import chatty.util.MiscUtil;
import chatty.util.ProcessManager;
import chatty.util.StringUtil;
import chatty.util.commands.CustomCommand;
import chatty.util.commands.Parameters;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

/**
 * Some Utility functions or constants for GUI related stuff
 * 
 * @author tduva
 */
public class GuiUtil {
    
    private static final Logger LOGGER = Logger.getLogger(GuiUtil.class.getName());
    
    public final static Insets NORMAL_BUTTON_INSETS = new Insets(2, 14, 2, 14);
    public final static Insets SMALL_BUTTON_INSETS = new Insets(-1, 10, -1, 10);
    public final static Insets SMALLER_BUTTON_INSETS = new Insets(0, 4, 0, 4);
    public final static Insets SPECIAL_BUTTON_INSETS = new Insets(2, 12, 2, 6);
    public final static Insets SPECIAL_SMALL_BUTTON_INSETS = new Insets(-1, 12, -1, 6);
    
    private static final String CLOSE_DIALOG_ACTION_MAP_KEY = "CLOSE_DIALOG_ACTION_MAP_KEY";
    private static final KeyStroke ESCAPE_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    
    
    public static void installEscapeCloseOperation(final JDialog dialog) {
        Action closingAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispatchEvent(
                        new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING
                        ));
            }
        };

        JRootPane root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                ESCAPE_STROKE,
                CLOSE_DIALOG_ACTION_MAP_KEY);
        root.getActionMap().put(CLOSE_DIALOG_ACTION_MAP_KEY, closingAction);
    }
    
    /**
     * Shows a JOptionPane that doesn't steal focus when opened, but is
     * focusable afterwards.
     * 
     * @param parent The parent Component
     * @param title The title
     * @param message The message
     * @param messageType The type of message as in JOptionPane
     * @param optionType The option type as in JOptionPane
     * @param options The options as in JOptionPane
     * @return The selected option or -1 if none was selected
     */
    public static int showNonAutoFocusOptionPane(Component parent, String title, String message,
            int messageType, int optionType, Object[] options) {
        JOptionPane p = new JOptionPane(message, messageType, optionType);
        p.setOptions(options);
        final JDialog d = p.createDialog(parent, title);
        d.setAutoRequestFocus(false);
        d.setFocusableWindowState(false);
        // Make focusable after showing the dialog, so that it can be focused
        // by the user, but doesn't steal focus from the user when it opens.
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                d.setFocusableWindowState(true);
            }
        });
        d.setVisible(true);
        // Find index of result
        Object value = p.getValue();
        for (int i = 0; i < options.length; i++) {
            if (options[i] == value) {
                return i;
            }
        }
        return -1;
    }
    
    public static void showNonModalMessage(Component parent, String title, String message, int type) {
        showNonModalMessage(parent, title, message, type, false);
    }
    
    public static void showNonModalMessage(Component parent, String title, String message, int type, boolean allowHtml) {
        if (!allowHtml) {
            message = Helper.htmlspecialchars_encode(message);
        }
        message = "<html><body style='font-family: Monospaced;width:400px;'>"+message;
        JOptionPane pane = new JOptionPane(message, type);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.setModal(false);
        dialog.setVisible(true);
    }
    
    /**
     * Checks if the given {@code Point} is on a screen. The point can be moved
     * horizontally before checking by specifying a {@code xOffset}. The
     * original point is not modified.
     * 
     * @param p The {@code Point} to check
     * @param xOffset The horizontal offset in pixels
     * @return {@code true} if the point is on screen, {@code false} otherwise
     */
    public static boolean isPointOnScreen(Point p, int xOffset, int yOffset) {
        Point moved = new Point(p.x + xOffset, p.y + yOffset);
        return isPointOnScreen(moved);
    }
    
    /**
     * Checks if the given {@code Point} is on a screen.
     * 
     * @param p The {@code Point} to check
     * @return {@code true} if the point is on screen, {@code false} otherwise
     */
    public static boolean isPointOnScreen(Point p) {
        GraphicsDevice[] screens = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getScreenDevices();
        for (GraphicsDevice screen : screens) {
            if (screen.getDefaultConfiguration().getBounds().contains(p)) {
                return true;
            }
        }
        return false;
    }
    
    private static final int MOUSE_LOCATION_HGAP = 60;
    
    public static void setLocationToMouse(Component c) {
        // Check might still be useful, even if this config is not used
        if (c.getGraphicsConfiguration() == null) {
            return;
        }
        // Use screen the mouse is on
        Rectangle screen = getEffectiveScreenBounds(MouseInfo.getPointerInfo().getDevice().getDefaultConfiguration());
        Point mouseLocation = new Point(MouseInfo.getPointerInfo().getLocation());
        int width = c.getWidth();
        int height = c.getHeight();
        
        // Move to left side by default
        mouseLocation.translate(- width - MOUSE_LOCATION_HGAP, - height/2);
        
        // Top boundary
        if (mouseLocation.y < screen.y) {
            mouseLocation.y = screen.y;
        }
        // Bottom boundary
        if (mouseLocation.y + height > screen.y + screen.height) {
            mouseLocation.y = screen.y + screen.height - height;
        }
        // Left boundary
        if (mouseLocation.x < screen.x) {
            mouseLocation.x += width + MOUSE_LOCATION_HGAP*2;
        }
        // Right boundary
        if (mouseLocation.x + width > screen.x + screen.width) {
            mouseLocation.x -= width + MOUSE_LOCATION_HGAP*2;
        }
        
        c.setLocation(mouseLocation);
    }
    
    /**
     * Changes the given x,y position (if necessary) so that an object with the
     * given Dimension would stay within the given bounds Rectangle (if the
     * coordinates refer to the upper left corner).
     * 
     * @param bounds
     * @param size
     * @param x
     * @param y
     * @return A new Point object containing the changed coordinates
     */
    public static Point getLocationWithinBounds(Rectangle bounds, Dimension size, int x, int y) {
        // Bottom
        if (y + size.height > bounds.y + bounds.height) {
            y = bounds.y + bounds.height - size.height;
        }
        
        // Top (after Bottom, to ensure access to titlebar)
        if (y < bounds.y) {
            y = bounds.y;
        }
        
        // Right
        if (x + size.width > bounds.x + bounds.width) {
            x = bounds.x + bounds.width - size.width;
        }
        
        // Left
        if (x < bounds.x) {
            x = bounds.x;
        }
        
        return new Point(x, y);
    }
    
    /**
     * Set the location of the given Window to be centered on the given
     * Component. The difference to window.setLocationRelativeTo() is that it
     * doesn't move the window horizontally when moving it vertically.
     *
     * @param w
     * @param source 
     */
    public static void setLocationRelativeTo(Window w, Component source) {
        if (source == null || !source.isShowing()) {
            w.setLocationRelativeTo(source);
        }
        Dimension wSize = w.getSize();
        Dimension sourceSize = source.getSize();
        Point location = source.getLocationOnScreen();
        int x = location.x + (sourceSize.width / 2) - (wSize.width / 2);
        int y = location.y + (sourceSize.height / 2) - (wSize.height / 2);
        
        Rectangle bounds = getEffectiveScreenBounds(source);
        w.setLocation(getLocationWithinBounds(bounds, w.getSize(), x, y));
    }
    
    /**
     * Get the bounds for the given GraphicsConfiguration, with insets (e.g.
     * taskbar) removed.
     * 
     * @param config
     * @return 
     */
    public static Rectangle getEffectiveScreenBounds(GraphicsConfiguration config) {
        Rectangle bounds = new Rectangle(config.getBounds());
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
        Debugging.println("screenbounds", "%s %s", bounds, insets);
        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= insets.right + insets.left;
        bounds.height -= insets.bottom + insets.top;
        return bounds;
    }
    
    /**
     * Get the bounds of the screen of the given Component, with screen insets
     * (e.g. taskbar) removed.
     * 
     * @param c
     * @return 
     */
    public static Rectangle getEffectiveScreenBounds(Component c) {
        GraphicsConfiguration config = c.getGraphicsConfiguration();
        if (config == null) {
            return null;
        }
        return getEffectiveScreenBounds(config);
    }
    
    /**
     * Moves the window very quickly to create a shaking effect, ending on the
     * original location. Uses Thread.sleep() so length should not be too high.
     *
     * @param window The window to shake
     * @param intensity The distance the window will move
     * @param length The number of iterations of the shaking effect
     */
    public static void shake(Window window, int intensity, int length) {
        Point original = window.getLocation();
        for (int i=0;i<length;i++) {
            try {
                // Using Thread.sleep() is not ideal because it freezes the GUI,
                // but it's really short
                Thread.sleep(50);
                window.setLocation(original.x+intensity, original.y);
                Thread.sleep(10);
                window.setLocation(original.x, original.y-intensity);
                Thread.sleep(10);
                window.setLocation(original.x-intensity, original.y+intensity);
                Thread.sleep(10);
                window.setLocation(original);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                // No action required
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame dialog = new JFrame();
            dialog.setSize(100, 100);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            JButton button = new JButton("Shake");
            button.addActionListener(e -> shake(dialog, 2, 2));
            dialog.add(button);
            dialog.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        });
    }
    
    public static GridBagConstraints makeGbc(int x, int y, int w, int h) {
        return makeGbc(x, y, w, h, GridBagConstraints.EAST);
    }
    
    public static GridBagConstraints makeGbc(int x, int y, int w, int h, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        gbc.anchor = anchor;
        gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }
    
    public static JPanel northWrap(JPanel panel) {
        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.NORTH);
        return container;
    }

    /**
     * Detect retina display.
     * 
     * https://stackoverflow.com/questions/20767708/how-do-you-detect-a-retina-display-in-java
     * 
     * @return 
     */
    public static boolean hasRetinaDisplay() {
        Object obj = Toolkit.getDefaultToolkit().getDesktopProperty(
                "apple.awt.contentScaleFactor");
        if (obj instanceof Float) {
            int scale = ((Float)obj).intValue();
            return (scale == 2); // 1 indicates a regular mac display.
        }
        return false;
    }
    
    /**
     * Recursively set the font size of the given component and all
     * subcomponents.
     * 
     * @param fontSize
     * @param component 
     */
    public static void setFontSize(float fontSize, Component component) {
        if (fontSize <= 0) {
            return;
        }
        if (component instanceof Container) {
            synchronized(component.getTreeLock()) {
                for (Component c : ((Container) component).getComponents()) {
                    GuiUtil.setFontSize(fontSize, c);
                }
            }
        }
        component.setFont(component.getFont().deriveFont(fontSize));
    }
    
    /**
     * Returns the current sort keys of the given table encoded in a String.
     * 
     * <p>This is intended to be used together with
     * {@link setSortingForTable(JTable, String)}.</p>
     *
     * @param table
     * @return 
     */
    public static String getSortingFromTable(JTable table) {
        List<? extends RowSorter.SortKey> keys = table.getRowSorter().getSortKeys();
        String result = "";
        for (RowSorter.SortKey key : keys) {
            int order = 0;
            if (key.getSortOrder() == SortOrder.ASCENDING) {
                order = 1;
            } else if (key.getSortOrder() == SortOrder.DESCENDING) {
                order = 2;
            }
            result += String.format("%s:%s;", key.getColumn(), order);
        }
        return result;
    }
    
    /**
     * Sets the sort keys for the RowSorter of the given JTable. Doesn't change
     * the sorting if the sorting parameter doesn't contain any valid sort key.
     * 
     * <p>This is intended to be used together with
     * {@link getSortingFromTable(JTable)}.</p>
     *
     * @param table
     * @param sorting 
     */
    public static void setSortingForTable(JTable table, String sorting) {
        List<RowSorter.SortKey> keys = new ArrayList<>();
        StringTokenizer t = new StringTokenizer(sorting, ";");
        while (t.hasMoreTokens()) {
            String[] split = t.nextToken().split(":");
            if (split.length == 2) {
                try {
                    int rowId = Integer.parseInt(split[0]);
                    int orderId = Integer.parseInt(split[1]);
                    SortOrder order;
                    switch (orderId) {
                        case 1: order = SortOrder.ASCENDING; break;
                        case 2: order = SortOrder.DESCENDING; break;
                        default: order = SortOrder.UNSORTED;
                    }
                    keys.add(new RowSorter.SortKey(rowId, order));
                } catch (NumberFormatException ex) {
                    // Just don't add anything
                }
            }
        }
        try {
            if (!keys.isEmpty()) {
                table.getRowSorter().setSortKeys(keys);
            }
        } catch (IllegalArgumentException ex) {
            // Don't change sorting
        }
    }
    
    /**
     * Adds the Copy/Paste/Cut shortcuts for Mac (Command instead of Ctrl).
     * 
     * <p>Normally the Look&Feel should do that automatically, but for some
     * reason it doesn't seem to do it.</p>
     */
    public static void addMacKeyboardActions() {
        if (MiscUtil.OS_MAC) {
            addMacKeyboardActionsTo("TextField.focusInputMap");
            addMacKeyboardActionsTo("TextArea.focusInputMap");
            addMacKeyboardActionsTo("TextPane.focusInputMap");
        }
    }
    
    /**
     * Based on: https://stackoverflow.com/a/7253059/2375667
     */
    private static void addMacKeyboardActionsTo(String key) {
        InputMap im = (InputMap) UIManager.get(key);

        // Copy/paste actions
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);

        // Navigation actions
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.META_DOWN_MASK), DefaultEditorKit.beginLineAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.META_DOWN_MASK), DefaultEditorKit.endLineAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK), DefaultEditorKit.previousWordAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK), DefaultEditorKit.nextWordAction);

        // Navigation selection actions
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), DefaultEditorKit.selectionBeginLineAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), DefaultEditorKit.selectionEndLineAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), DefaultEditorKit.selectionPreviousWordAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), DefaultEditorKit.selectionNextWordAction);

        // Other actions
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK), DefaultEditorKit.selectAllAction);
    }
    
    private static class TextContextMenu extends JPopupMenu {
        
        public TextContextMenu() {
            add(new DefaultEditorKit.CopyAction(), Language.getString("textCm.copy"), KeyEvent.VK_C);
            add(new DefaultEditorKit.CutAction(), Language.getString("textCm.cut"), KeyEvent.VK_X);
            add(new DefaultEditorKit.PasteAction(), Language.getString("textCm.paste"), KeyEvent.VK_P);
        }
        
        private void add(Action action, String name, int key) {
            action.putValue(Action.NAME, name);
            action.putValue(Action.MNEMONIC_KEY, key);
            add(action);
        }
        
    }
    
    private static TextContextMenu textContextMenu;
    
    /**
     * Sets copy/cut/paste context menu to the text component. Menu is created
     * when this is first called and shared between all following calls.
     * 
     * @param c The text component
     */
    public static void installTextContextMenu(JTextComponent c) {
        if (textContextMenu == null) {
            textContextMenu = new TextContextMenu();
        }
        c.setComponentPopupMenu(textContextMenu);
    }
    
    /**
     * Executes a process with the given commandText, with some parameters
     * replaced. The resulting command will be split into arguments by spaces,
     * although sections can be quoted to group them together. Escaped quotes
     * (\") are ignored.
     * 
     * If the commandText isn't a valid CustomCommand, nothing will be executed.
     * 
     * @param commandText A command in CustomCommand format
     * @param title The title of the notification
     * @param message The message of the notification
     * @param channel The associated channel (may be null)
     * @return A result message intended for output to the user while testing
     */
    public static String showCommandNotification(String commandText, String title,
            String message, String channel) {
        CustomCommand command = CustomCommand.parse(commandText);

        Parameters param = Parameters.create("");
        param.put("title", title.replace("\"", "\\\""));
        param.put("message", message.replace("\"", "\\\""));
        param.put("channel", channel);
        param.put("chan", channel);
        
        if (command.hasError()) {
            LOGGER.warning("Notification command error: "+command.getSingleLineError());
            return "Error: "+command.getSingleLineError();
        } else {
            String resultCommand = command.replace(param);
            ProcessManager.execute(resultCommand, "Notification");
            return "Running: "+resultCommand;
        }
    }
    
    /**
     * Java 8u161/162 introduced a bug that causes high CPU usage when a
     * JTextField/JTextArea is focused as first component after the window is
     * focused.
     * 
     * This workaround aims to prevent this by rejecting that focus change if it
     * occurs and focusing another component first, then focusing the original
     * text component.
     * 
     * This may or may not actually work, but it seemed fine in testing.
     */
    public static void installTextComponentFocusWorkaround() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addVetoableChangeListener(new VetoableChangeListener() {

            private boolean rejectNext = false;
            private JComponent target;

            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                if (evt.getNewValue() != null) {
                    if (rejectNext && evt.getPropertyName().equals("focusOwner")) {
                        if (evt.getNewValue() instanceof JTextComponent) {
                            JComponent component = (JComponent) evt.getNewValue();
                            // Move focus up, this usually moves it to the
                            // window itself
                            KeyboardFocusManager.getCurrentKeyboardFocusManager().upFocusCycle(component);
                            target = component;
                            LOGGER.info("[Focus] Rejected JTextComponent focus");
                            // Reject focus change as well, otherwise this
                            // didn't seem to work
                            throw new PropertyVetoException("Rejected JTextComponent focus", evt);
                        } else {
                            // If anything else was focused, no need to reject
                            // anymore, change focus back if necessary
                            rejectNext = false;
                            if (target != null) {
                                LOGGER.info("[Focus] Temp: " + evt.getNewValue());
                                target.requestFocus();
                                target = null;
                            }
                        }
                    } else if (evt.getPropertyName().equals("focusedWindow")) {
                        // Next focus on a text component should be rejected
                        LOGGER.info("[Focus] Window focused");
                        rejectNext = true;
                    }
                }

                // Debug
                String oldV = evt.getOldValue() != null ? evt.getOldValue().getClass().toString() : null;
                String newV = evt.getNewValue() != null ? evt.getNewValue().getClass().toString() : null;
                //System.out.println(evt.getPropertyName()+": "+oldV+" -> "+newV);
            }
        });
    }
    
    public static void focusTest() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addVetoableChangeListener(new VetoableChangeListener() {

            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                if (evt.getOldValue() != null) {
                    System.out.println("from: "+evt.getOldValue().getClass().getName()+" ("+evt.getPropertyName()+")");
                }
                if (evt.getNewValue() != null) {
                    System.out.println("to: "+evt.getNewValue().getClass().getName()+" ("+evt.getPropertyName()+")");
                }
                if (evt.getNewValue() instanceof ChannelTextPane) {
                    //System.out.println("prevent");
                    //throw new PropertyVetoException("abc", evt);
                }
            }
        });
    }
    
    /**
     * Set a DocumentFilter that limits the text length and allows or filters
     * linebreak characters.
     * 
     * Note that this replaces an already set DocumentFilter.
     * 
     * @param comp The JTextComponent, using AbstractDocument
     * @param limit The character limit
     * @param allowNewlines false to filter linebreak characters
     */
    public static void installLengthLimitDocumentFilter(JTextComponent comp, int limit, boolean allowNewlines) {
        if (limit < 0) {
            throw new IllegalArgumentException("Invalid limit < 0");
        }
        DocumentFilter filter = new DocumentFilter() {
            
            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset,
                    int delLength, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null || text.isEmpty()) {
                    super.replace(fb, offset, delLength, text, attrs);
                } else {
                    int currentLength = fb.getDocument().getLength();
                    int overLimit = (currentLength + text.length()) - limit - delLength;
                    if (overLimit > 0) {
                        /**
                         * Might be negative otherwise if limit already exceeded
                         * (e.g. if the filter wasn't always active).
                         */
                        int newLength = Math.max(text.length() - overLimit, 0);
                        text = text.substring(0, newLength);
                    }
                    if (!allowNewlines) {
                        text = StringUtil.removeLinebreakCharacters(text);
                    }
                    super.replace(fb, offset, delLength, text, attrs);
                }
            }
            
        };
        Document doc = comp.getDocument();
        if (doc instanceof AbstractDocument) {
            AbstractDocument ad = (AbstractDocument)doc;
//            if (ad.getDocumentFilter() != null) {
//                System.out.println("Filter already installed "+comp);
//            }
            ad.setDocumentFilter(filter);
        } else {
            throw new IllegalArgumentException("Textcomponent not using AbstractDocument");
        }
    }
    
    /**
     * Set the height of the target component to the height of the source
     * component, by using preferred size.
     * 
     * @param target The component to change the size on
     * @param source The component to retrieve the height from
     */
    public static void matchHeight(JComponent target, JComponent source) {
        target.setPreferredSize(
                new Dimension(
                        target.getPreferredSize().width,
                        source.getPreferredSize().height
                ));
    }
    
    public static ImageIcon getScaledIcon(Icon icon, int w, int h) {
        BufferedImage img = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
    
}
