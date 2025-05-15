package com.example.banking;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BankingSwingApp extends JFrame {
    private final JComboBox<User> userCombo = new JComboBox<>();
    private final JLabel balanceLabel = new JLabel("Balance: $0.00");
    private final JTextField amountField = new JTextField(10);
    private final JButton depositButton = new JButton("Deposit");
    private final JButton expenseButton = new JButton("Expense");
    private final JButton addUserButton = new JButton("Add User");
    private final JButton deleteUserButton = new JButton("Delete User");
    private final JTextField newUserField = new JTextField(10);
    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{"All", "Deposit", "Expense"});
    private final JLabel totalDepositLabel = new JLabel("Total Deposits: $0.00");
    private final JLabel totalExpenseLabel = new JLabel("Total Expenses: $0.00");
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Date", "Type", "Amount", "Balance"}, 0);
    private final JTable historyTable = new JTable(tableModel);

    // Update these for your DB
    private final String jdbcURL = "jdbc:mysql://localhost:3306/banking_db";
    private final String jdbcUsername = "root";
    private final String jdbcPassword = "root";

    public BankingSwingApp() {
        super("Banking App (Multi-User, SQL)");
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 10);
            UIManager.put("Panel.arc", 15);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 12);
        } catch (Exception e) {
            // fallback to default
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 248, 250));

        // Custom header panel with gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(41, 128, 185),
                        getWidth(), 0, new Color(44, 62, 80));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));

        JLabel headerLabel = new JLabel("Banking Management System");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Top panel for user selection and balance
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("User Selection"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        topPanel.setBackground(new Color(245, 248, 250));

        JLabel userLabel = new JLabel("User:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topPanel.add(userLabel);

        userCombo.setPreferredSize(new Dimension(200, 30));
        userCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        topPanel.add(userCombo);

        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        balanceLabel.setForeground(new Color(0, 102, 51));
        balanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        topPanel.add(balanceLabel);

        deleteUserButton.setBackground(new Color(220, 53, 69));
        deleteUserButton.setForeground(Color.WHITE);
        deleteUserButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteUserButton.setFocusPainted(false);
        topPanel.add(deleteUserButton);

        // Panel for amount and actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Transaction"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        actionPanel.setBackground(new Color(245, 248, 250));

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionPanel.add(amountLabel);

        amountField.setPreferredSize(new Dimension(150, 30));
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        actionPanel.add(amountField);

        depositButton.setBackground(new Color(40, 167, 69));
        depositButton.setForeground(Color.WHITE);
        depositButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        depositButton.setFocusPainted(false);
        depositButton.setIcon(createCircleIcon(15, new Color(40, 167, 69), "+"));
        actionPanel.add(depositButton);

        expenseButton.setBackground(new Color(255, 193, 7));
        expenseButton.setForeground(Color.BLACK);
        expenseButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        expenseButton.setFocusPainted(false);
        expenseButton.setIcon(createCircleIcon(15, new Color(255, 193, 7), "-"));
        actionPanel.add(expenseButton);

        // Panel for adding new user
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        userPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Add New User"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        userPanel.setBackground(new Color(245, 248, 250));

        JLabel newUserLabel = new JLabel("New User:");
        newUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userPanel.add(newUserLabel);

        newUserField.setPreferredSize(new Dimension(200, 30));
        newUserField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userPanel.add(newUserField);

        addUserButton.setBackground(new Color(0, 123, 255));
        addUserButton.setForeground(Color.WHITE);
        addUserButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addUserButton.setFocusPainted(false);
        userPanel.add(addUserButton);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        filterPanel.setBackground(new Color(245, 248, 250));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filterPanel.add(filterLabel);

        filterCombo.setPreferredSize(new Dimension(150, 30));
        filterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filterPanel.add(filterCombo);

        totalDepositLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalDepositLabel.setForeground(new Color(40, 167, 69));
        totalDepositLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        filterPanel.add(totalDepositLabel);

        totalExpenseLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalExpenseLabel.setForeground(new Color(220, 53, 69));
        totalExpenseLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        filterPanel.add(totalExpenseLabel);

        // Table for transaction history with better styling
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        historyTable.setFillsViewportHeight(true);
        historyTable.setRowHeight(30);
        historyTable.setIntercellSpacing(new Dimension(10, 5));
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        historyTable.getTableHeader().setBackground(new Color(52, 152, 219));
        historyTable.getTableHeader().setForeground(Color.WHITE);
        historyTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        historyTable.setSelectionBackground(new Color(204, 229, 255));
        historyTable.setGridColor(new Color(220, 220, 220));
        // Alternate row color
        historyTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(255, 255, 255) : new Color(240, 245, 250));
                }
                return c;
            }
        });

        // Main content panel with slight padding
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        contentPanel.setBackground(new Color(245, 248, 250));

        JPanel northPanel = new JPanel(new BorderLayout(10, 10));
        northPanel.setBackground(new Color(245, 248, 250));
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(actionPanel, BorderLayout.CENTER);
        northPanel.add(userPanel, BorderLayout.SOUTH);

        contentPanel.add(northPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(filterPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        depositButton.addActionListener(e -> deposit());
        expenseButton.addActionListener(e -> expense());
        addUserButton.addActionListener(e -> addUser());
        deleteUserButton.addActionListener(e -> deleteUser());
        userCombo.addActionListener(e -> loadUserData());
        filterCombo.addActionListener(e -> loadUserData());

        setSize(950, 650);
        setLocationRelativeTo(null);
        setVisible(true);

        createTablesIfNotExist();
        loadUsers();
    }

    // Helper method to create circular icons for buttons
    private Icon createCircleIcon(int size, Color color, String text) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color.darker());
                g2d.fill(new RoundRectangle2D.Double(x, y, size, size, size, size));
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, size - 4));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (size - fm.stringWidth(text)) / 2;
                int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(text, textX, textY);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    private void createTablesIfNotExist() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) UNIQUE, balance DOUBLE DEFAULT 0)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, type VARCHAR(20), amount DOUBLE, date DATETIME, balance_after DOUBLE," +
                    "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        } catch (SQLException e) {
            showMessage("DB Error: " + e.getMessage());
        }
    }

    private void loadUsers() {
        userCombo.removeAllItems();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, balance FROM users")) {
            while (rs.next()) {
                userCombo.addItem(new User(rs.getInt("id"), rs.getString("name"), rs.getDouble("balance")));
            }
        } catch (SQLException e) {
            showMessage("DB Error: " + e.getMessage());
        }
        if (userCombo.getItemCount() > 0) {
            userCombo.setSelectedIndex(0);
            loadUserData();
        } else {
            balanceLabel.setText("Balance: $0.00");
            tableModel.setRowCount(0);
            totalDepositLabel.setText("Total Deposits: $0.00");
            totalExpenseLabel.setText("Total Expenses: $0.00");
        }
    }

    private void loadUserData() {
        User user = (User) userCombo.getSelectedItem();
        if (user == null) return;
        // Update balance
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT balance FROM users WHERE id=?")) {
            ps.setInt(1, user.id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.balance = rs.getDouble("balance");
                    balanceLabel.setText(String.format("Balance: $%.2f", user.balance));
                }
            }
        } catch (SQLException e) {
            showMessage("DB Error: " + e.getMessage());
        }
        // Load transactions with filter
        tableModel.setRowCount(0);
        String filter = (String) filterCombo.getSelectedItem();
        String sql = "SELECT * FROM transactions WHERE user_id=? ";
        if ("Deposit".equals(filter)) {
            sql += "AND type='Deposit' ";
        } else if ("Expense".equals(filter)) {
            sql += "AND type='Expense' ";
        }
        sql += "ORDER BY date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.id);
            try (ResultSet rs = ps.executeQuery()) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getTimestamp("date").toLocalDateTime().format(fmt),
                            rs.getString("type"),
                            String.format("%.2f", rs.getDouble("amount")),
                            String.format("%.2f", rs.getDouble("balance_after"))
                    });
                }
            }
        } catch (SQLException e) {
            showMessage("DB Error: " + e.getMessage());
        }
        // Show totals
        showTotals(user.id);
    }

    private void showTotals(int userId) {
        double totalDeposit = 0, totalExpense = 0;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT type, SUM(amount) as total FROM transactions WHERE user_id=? GROUP BY type")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    double total = rs.getDouble("total");
                    if ("Deposit".equals(type)) totalDeposit = total;
                    else if ("Expense".equals(type)) totalExpense = total;
                }
            }
        } catch (SQLException e) {
            // ignore for totals
        }
        totalDepositLabel.setText(String.format("Total Deposits: $%.2f", totalDeposit));
        totalExpenseLabel.setText(String.format("Total Expenses: $%.2f", totalExpense));
    }

    private void deposit() {
        User user = (User) userCombo.getSelectedItem();
        if (user == null) return;
        double amt = getAmount();
        if (amt <= 0) {
            showMessage("Enter a positive amount.");
            return;
        }
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            double newBalance = user.balance + amt;
            try (PreparedStatement ps1 = conn.prepareStatement("UPDATE users SET balance=? WHERE id=?");
                 PreparedStatement ps2 = conn.prepareStatement("INSERT INTO transactions (user_id, type, amount, date, balance_after) VALUES (?, ?, ?, ?, ?)")) {
                ps1.setDouble(1, newBalance);
                ps1.setInt(2, user.id);
                ps1.executeUpdate();

                ps2.setInt(1, user.id);
                ps2.setString(2, "Deposit");
                ps2.setDouble(3, amt);
                ps2.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps2.setDouble(5, newBalance);
                ps2.executeUpdate();

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            showMessage("DB Error: " + e.getMessage());
            return;
        }
        loadUsers();
        userCombo.setSelectedItem(user);
        loadUserData();
    }

    private void expense() {
        User user = (User) userCombo.getSelectedItem();
        if (user == null) return;
        double amt = getAmount();
        if (amt <= 0) {
            showMessage("Enter a positive amount.");
            return;
        }
        if (amt > user.balance) {
            showMessage("Insufficient balance.");
            return;
        }
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            double newBalance = user.balance - amt;
            try (PreparedStatement ps1 = conn.prepareStatement("UPDATE users SET balance=? WHERE id=?");
                 PreparedStatement ps2 = conn.prepareStatement("INSERT INTO transactions (user_id, type, amount, date, balance_after) VALUES (?, ?, ?, ?, ?)")) {
                ps1.setDouble(1, newBalance);
                ps1.setInt(2, user.id);
                ps1.executeUpdate();

                ps2.setInt(1, user.id);
                ps2.setString(2, "Expense");
                ps2.setDouble(3, amt);
                ps2.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps2.setDouble(5, newBalance);
                ps2.executeUpdate();

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            showMessage("DB Error: " + e.getMessage());
            return;
        }
        loadUsers();
        userCombo.setSelectedItem(user);
        loadUserData();
    }

    private void addUser() {
        String name = newUserField.getText().trim();
        if (name.isEmpty()) {
            showMessage("Enter a user name.");
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO users (name, balance) VALUES (?, 0)")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showMessage("User already exists.");
            } else {
                showMessage("DB Error: " + e.getMessage());
            }
            return;
        }
        newUserField.setText("");
        loadUsers();
    }

    private void deleteUser() {
        User user = (User) userCombo.getSelectedItem();
        if (user == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user '" + user.name + "' and all their transactions?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setInt(1, user.id);
            ps.executeUpdate();
        } catch (SQLException e) {
            showMessage("DB Error: " + e.getMessage());
            return;
        }
        loadUsers();
    }

    private double getAmount() {
        try {
            return Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    }

    static class User {
        int id;
        String name;
        double balance;
        User(int id, String name, double balance) {
            this.id = id; this.name = name; this.balance = balance;
        }
        public String toString() { return name + " (ID: " + id + ")"; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof User)) return false;
            return id == ((User) o).id;
        }
        @Override public int hashCode() { return id; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankingSwingApp::new);
    }
}
