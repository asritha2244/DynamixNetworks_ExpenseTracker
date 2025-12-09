import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ExpenseTracker.java
 * A single-file Java Swing application that provides:
 * - Add income & expenses
 * - Categorize transactions (Food, Rent, Entertainment, etc.)
 * - View transaction history (JTable)
 * - Generate reports (daily, monthly, yearly)
 * - Date filtering (From - To)
 * - Save / Load to CSV
 * - Dark mode toggle and an attractive color theme
 *
 * Compile:
 *   javac src/ExpenseTracker.java
 * Run:
 *   java -cp src ExpenseTracker
 *
 * Works with Java 8+ (uses java.time.*)
 */
public class ExpenseTracker extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField amountField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> categoryCombo;
    private JTextField noteField;
    private JSpinner dateSpinnerFrom, dateSpinnerTo, dateSpinnerEntry;
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private List<Transaction> transactions = new ArrayList<>();
    private boolean darkMode = false;

    private static final String[] CATEGORIES = new String[] {
        "Food", "Rent", "Entertainment", "Salary", "Transport", "Utilities", "Shopping", "Health", "Other"
    };

    public ExpenseTracker() {
        super("Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 600);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        getContentPane().add(main);

        // Top panel - entry
        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createTitledBorder("Add Transaction"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6); c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; top.add(new JLabel("Type:"), c);
        typeCombo = new JComboBox<>(new String[]{"Expense","Income"});
        c.gridx=1; top.add(typeCombo, c);

        c.gridx=2; top.add(new JLabel("Category:"), c);
        categoryCombo = new JComboBox<>(CATEGORIES);
        c.gridx=3; top.add(categoryCombo, c);

        c.gridx=0; c.gridy=1; top.add(new JLabel("Amount:"), c);
        amountField = new JTextField(); c.gridx=1; top.add(amountField, c);

        c.gridx=2; top.add(new JLabel("Date (yyyy-mm-dd):"), c);
        dateSpinnerEntry = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editorEntry = new JSpinner.DateEditor(dateSpinnerEntry, "yyyy-MM-dd");
        dateSpinnerEntry.setEditor(editorEntry);
        c.gridx=3; top.add(dateSpinnerEntry, c);

        c.gridx=0; c.gridy=2; top.add(new JLabel("Note:"), c);
        noteField = new JTextField(); c.gridx=1; c.gridwidth=3; top.add(noteField, c);
        c.gridwidth=1;

        JButton addBtn = new JButton("Add Transaction");
        addBtn.addActionListener(e -> addTransaction());
        c.gridx=0; c.gridy=3; c.gridwidth=4; top.add(addBtn, c);
        c.gridwidth=1;

        main.add(top, BorderLayout.NORTH);

        // Center - table
        String[] cols = new String[]{"ID","Date","Type","Category","Amount","Note"};
        tableModel = new DefaultTableModel(cols,0) {
            public boolean isCellEditable(int r, int c){ return false; }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Transaction History"));
        main.add(scroll, BorderLayout.CENTER);

        // Right panel - controls, filters, reports
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createTitledBorder("Controls"));

        // Date filter
        JPanel df = new JPanel(new GridLayout(4,1,6,6));
        df.add(new JLabel("Filter: From"));
        dateSpinnerFrom = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        dateSpinnerFrom.setEditor(new JSpinner.DateEditor(dateSpinnerFrom, "yyyy-MM-dd"));
        df.add(dateSpinnerFrom);
        df.add(new JLabel("Filter: To"));
        dateSpinnerTo = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        dateSpinnerTo.setEditor(new JSpinner.DateEditor(dateSpinnerTo, "yyyy-MM-dd"));
        df.add(dateSpinnerTo);
        right.add(df);

        JButton filterBtn = new JButton("Apply Date Filter");
        filterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        filterBtn.addActionListener(e -> applyDateFilter());
        right.add(Box.createRigidArea(new Dimension(0,8)));
        right.add(filterBtn);

        right.add(Box.createRigidArea(new Dimension(0,12)));

        // Reports
        right.add(new JLabel("Reports"));
        JPanel rp = new JPanel(new GridLayout(3,1,6,6));
        JButton daily = new JButton("Daily Report");
        JButton monthly = new JButton("Monthly Report");
        JButton yearly = new JButton("Yearly Report");
        daily.addActionListener(e -> showReport("daily"));
        monthly.addActionListener(e -> showReport("monthly"));
        yearly.addActionListener(e -> showReport("yearly"));
        rp.add(daily); rp.add(monthly); rp.add(yearly);
        right.add(rp);

        right.add(Box.createRigidArea(new Dimension(0,12)));

        JButton saveBtn = new JButton("Save to CSV");
        saveBtn.addActionListener(e -> saveToFile());
        JButton loadBtn = new JButton("Load from CSV");
        loadBtn.addActionListener(e -> loadFromFile());
        right.add(saveBtn);
        right.add(Box.createRigidArea(new Dimension(0,6)));
        right.add(loadBtn);

        right.add(Box.createRigidArea(new Dimension(0,12)));
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelected());
        right.add(deleteBtn);

        right.add(Box.createRigidArea(new Dimension(0,12)));
        JToggleButton dark = new JToggleButton("Toggle Dark Mode");
        dark.addActionListener(e -> { darkMode = dark.isSelected(); applyTheme(); });
        right.add(dark);

        right.add(Box.createVerticalGlue());
        main.add(right, BorderLayout.EAST);

        // initial theme
        applyTheme();
    }

    private void addTransaction() {
        String type = (String) typeCombo.getSelectedItem();
        String category = (String) categoryCombo.getSelectedItem();
        String note = noteField.getText().trim();
        String amtText = amountField.getText().trim();
        if (amtText.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter amount"); return; }
        double amount;
        try { amount = Double.parseDouble(amtText); }
        catch(Exception ex){ JOptionPane.showMessageDialog(this, "Invalid amount"); return; }
        Date d = (Date) dateSpinnerEntry.getValue();
        LocalDate date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Transaction t = new Transaction(UUID.randomUUID().toString(), date, type, category, amount, note);
        transactions.add(t);
        addRow(t);
        clearEntry();
    }

    private void addRow(Transaction t) {
        tableModel.addRow(new Object[]{t.id, t.date.format(fmt), t.type, t.category, String.format("%.2f", t.amount), t.note});
    }

    private void clearEntry() {
        amountField.setText("");
        noteField.setText("");
    }

    private void applyDateFilter() {
        Date fromDate = (Date) dateSpinnerFrom.getValue();
        Date toDate = (Date) dateSpinnerTo.getValue();
        LocalDate from = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate to = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // Ensure from <= to
        if (from.isAfter(to)) { JOptionPane.showMessageDialog(this, "'From' must be <= 'To'"); return; }
        // clear table
        tableModel.setRowCount(0);
        transactions.stream()
            .filter(t -> !t.date.isBefore(from) && !t.date.isAfter(to))
            .sorted(Comparator.comparing(t -> t.date))
            .forEach(this::addRow);
    }

    private void showReport(String type) {
        // aggregate by day/month/year
        Map<String, Double> incomeMap = new HashMap<>();
        Map<String, Double> expenseMap = new HashMap<>();

        for (Transaction t : transactions) {
            String key;
            if (type.equals("daily")) key = t.date.format(fmt);
            else if (type.equals("monthly")) key = t.date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            else key = String.valueOf(t.date.getYear());

            Map<String, Double> target = t.type.equalsIgnoreCase("Income") ? incomeMap : expenseMap;
            target.put(key, target.getOrDefault(key, 0.0) + t.amount);
        }

        // Build simple report text
        Set<String> keys = new TreeSet<>();
        keys.addAll(incomeMap.keySet()); keys.addAll(expenseMap.keySet());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-16s %-12s %-12s %-12s%n", "Period", "Income", "Expense", "Net"));
        for (String k : keys) {
            double inc = incomeMap.getOrDefault(k, 0.0);
            double exp = expenseMap.getOrDefault(k, 0.0);
            sb.append(String.format("%-16s %-12.2f %-12.2f %-12.2f%n", k, inc, exp, inc - exp));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(600,400));
        JOptionPane.showMessageDialog(this, sp, Character.toUpperCase(type.charAt(0)) + type.substring(1) + " Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save CSV");
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(f)) {
            pw.println("id,date,type,category,amount,note");
            for (Transaction t : transactions) {
                pw.printf("%s,%s,%s,%s,%.2f,%s%n",
                    escapeCsv(t.id),
                    t.date.format(fmt),
                    t.type,
                    escapeCsv(t.category),
                    t.amount,
                    escapeCsv(t.note));
            }
            JOptionPane.showMessageDialog(this, "Saved " + transactions.size() + " transactions to " + f.getAbsolutePath());
        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
        }
    }

    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load CSV");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        List<Transaction> loaded = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String header = br.readLine(); // skip
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = splitCsv(line);
                if (parts.length < 6) continue;
                String id = parts[0];
                LocalDate date = LocalDate.parse(parts[1], fmt);
                String type = parts[2];
                String category = parts[3];
                double amount = Double.parseDouble(parts[4]);
                String note = parts[5];
                loaded.add(new Transaction(id, date, type, category, amount, note));
            }
            transactions = loaded;
            refreshTable();
            JOptionPane.showMessageDialog(this, "Loaded " + transactions.size() + " transactions from " + f.getAbsolutePath());
        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        transactions.stream().sorted(Comparator.comparing(t->t.date)).forEach(this::addRow);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row to delete"); return; }
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        transactions.removeIf(t -> t.id.equals(id));
        tableModel.removeRow(modelRow);
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private String[] splitCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i=0;i<line.length();i++) {
            char ch = line.charAt(i);
            if (ch=='"') { inQuotes = !inQuotes; continue; }
            if (ch==',' && !inQuotes) { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(ch);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private void applyTheme() {
        Color bg, fg, panel;
        if (darkMode) {
            bg = new Color(34,34,34);
            fg = Color.WHITE;
            panel = new Color(45,45,45);
        } else {
            bg = new Color(245,250,255);
            fg = Color.DARK_GRAY;
            panel = new Color(230,245,255);
        }
        getContentPane().setBackground(bg);
        for (Component comp : getContentPane().getComponents()) styleComponent(comp, bg, fg, panel);
        table.setBackground(darkMode ? new Color(50,50,50) : Color.WHITE);
        table.setForeground(fg);
        table.setGridColor(darkMode ? Color.GRAY : new Color(200,220,240));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(darkMode ? new Color(64,64,64) : new Color(200,230,255));
        table.getTableHeader().setForeground(fg);
        repaint();
    }

    private void styleComponent(Component comp, Color bg, Color fg, Color panel) {
        if (comp instanceof JPanel) {
            comp.setBackground(panel);
            for (Component c : ((JPanel) comp).getComponents()) styleComponent(c, bg, fg, panel);
        } else {
            comp.setBackground(panel);
            comp.setForeground(fg);
            if (comp instanceof JButton) {
                ((JButton) comp).setFocusPainted(false);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExpenseTracker ex = new ExpenseTracker();
            ex.setVisible(true);
        });
    }

    static class Transaction {
        String id;
        LocalDate date;
        String type;
        String category;
        double amount;
        String note;
        Transaction(String id, LocalDate date, String type, String category, double amount, String note) {
            this.id = id; this.date = date; this.type = type; this.category = category; this.amount = amount; this.note = note;
        }
    }
}
