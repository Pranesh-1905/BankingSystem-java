package com.example.banking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDashboard {
    private String jdbcURL = "jdbc:mysql://localhost:3306/shopping_cart_db"; 
    private String jdbcUsername = "root"; 
    private String jdbcPassword = "root"; 

    public void addProductToCart(int userId, int productId, int quantity, double totalPrice) {
        // SQL to insert a new product into the user's cart
        String sql = "INSERT INTO Cart (userId, productId, quantity, totalPrice) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, productId);
            preparedStatement.setInt(3, quantity);
            preparedStatement.setDouble(4, totalPrice);
            preparedStatement.executeUpdate();
            System.out.println("Product added to cart successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateProductQuantity(int userId, int productId, int newQuantity) {
        // SQL to update the quantity and total price of a product in the user's cart
        String sql = "UPDATE Cart SET quantity = ?, totalPrice = ? WHERE userId = ? AND productId = ?";
        try (Connection connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            double totalPrice = newQuantity * getProductPrice(productId);
            preparedStatement.setInt(1, newQuantity);
            preparedStatement.setDouble(2, totalPrice);
            preparedStatement.setInt(3, userId);
            preparedStatement.setInt(4, productId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product quantity updated successfully!");
            } else {
                System.out.println("Product not found in cart.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getProductsInCart(int userId) {
        // SQL to select all products in the user's cart
        List<String> products = new ArrayList<>();
        String sql = "SELECT * FROM Cart WHERE userId = ?";
        try (Connection connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int productId = resultSet.getInt("productId");
                int quantity = resultSet.getInt("quantity");
                double totalPrice = resultSet.getDouble("totalPrice");
                products.add("Product ID: " + productId + ", Quantity: " + quantity + ", Total Price: " + totalPrice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public void removeProductFromCart(int userId, int productId) {
        // SQL to remove a product from the user's cart
        String sql = "DELETE FROM Cart WHERE userId = ? AND productId = ?";
        try (Connection connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, productId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product removed from cart successfully!");
            } else {
                System.out.println("Product not found in cart.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    double getProductPrice(int productId) {
        switch (productId) {
            case 1: return 999.99; 
            case 2: return 499.99; 
            case 3: return 199.99; 
            default: return 0.0; 
        }
    }

    // --- Swing UI for Cart Management ---
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new CartSwingUI(new AccountDashboard());
        });
    }
}

// --- Swing UI class ---
class CartSwingUI extends javax.swing.JFrame {
    private final AccountDashboard dashboard;
    private final javax.swing.JTextField userIdField = new javax.swing.JTextField("1", 5);
    private final javax.swing.JTable cartTable;
    private final javax.swing.table.DefaultTableModel cartTableModel;
    private final javax.swing.JComboBox<ProductItem> productCombo;
    private final javax.swing.JTextField quantityField = new javax.swing.JTextField("1", 5);
    private final javax.swing.JLabel statusLabel = new javax.swing.JLabel(" ");
    private final javax.swing.JLabel totalLabel = new javax.swing.JLabel("Total: $0.00");

    private static final ProductItem[] PRODUCTS = {
        new ProductItem(1, "Laptop", 999.99),
        new ProductItem(2, "Smartphone", 499.99),
        new ProductItem(3, "Headphones", 199.99)
    };

    public CartSwingUI(AccountDashboard dashboard) {
        super("Shopping Cart Dashboard");
        this.dashboard = dashboard;
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLayout(new java.awt.BorderLayout());

        // Top panel for user selection
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        topPanel.add(new javax.swing.JLabel("User ID:"));
        topPanel.add(userIdField);
        javax.swing.JButton refreshBtn = new javax.swing.JButton("Refresh Cart");
        topPanel.add(refreshBtn);
        javax.swing.JButton clearBtn = new javax.swing.JButton("Clear Cart");
        topPanel.add(clearBtn);

        // Cart table
        String[] columns = {"Product ID", "Name", "Quantity", "Total Price"};
        cartTableModel = new javax.swing.table.DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        cartTable = new javax.swing.JTable(cartTableModel);
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(cartTable);

        // Bottom panel for actions
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        productCombo = new javax.swing.JComboBox<>(PRODUCTS);
        bottomPanel.add(new javax.swing.JLabel("Product:"));
        bottomPanel.add(productCombo);
        bottomPanel.add(new javax.swing.JLabel("Quantity:"));
        bottomPanel.add(quantityField);

        javax.swing.JButton addBtn = new javax.swing.JButton("Add");
        javax.swing.JButton updateBtn = new javax.swing.JButton("Update");
        javax.swing.JButton removeBtn = new javax.swing.JButton("Remove");
        bottomPanel.add(addBtn);
        bottomPanel.add(updateBtn);
        bottomPanel.add(removeBtn);

        // Status and total
        javax.swing.JPanel statusPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        statusPanel.add(statusLabel, java.awt.BorderLayout.WEST);
        statusPanel.add(totalLabel, java.awt.BorderLayout.EAST);

        add(topPanel, java.awt.BorderLayout.NORTH);
        add(scrollPane, java.awt.BorderLayout.CENTER);
        add(bottomPanel, java.awt.BorderLayout.SOUTH);
        add(statusPanel, java.awt.BorderLayout.PAGE_END);

        // Action listeners
        refreshBtn.addActionListener(e -> refreshCart());
        addBtn.addActionListener(e -> addProduct());
        updateBtn.addActionListener(e -> updateProduct());
        removeBtn.addActionListener(e -> removeProduct());
        clearBtn.addActionListener(e -> clearCart());
        cartTable.getSelectionModel().addListSelectionListener(e -> fillFieldsFromTable());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        refreshCart();
    }

    private int getUserId() {
        try {
            return Integer.parseInt(userIdField.getText().trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private ProductItem getSelectedProduct() {
        return (ProductItem) productCombo.getSelectedItem();
    }

    private int getQuantity() {
        try {
            return Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void refreshCart() {
        int userId = getUserId();
        java.util.List<String> products = dashboard.getProductsInCart(userId);
        cartTableModel.setRowCount(0);
        double total = 0.0;
        for (String p : products) {
            // Parse: "Product ID: 1, Quantity: 2, Total Price: 1999.98"
            try {
                String[] parts = p.split(",");
                int pid = Integer.parseInt(parts[0].replaceAll("\\D+", ""));
                int qty = Integer.parseInt(parts[1].replaceAll("\\D+", ""));
                double price = Double.parseDouble(parts[2].replaceAll("[^\\d.]+", ""));
                String name = getProductName(pid);
                cartTableModel.addRow(new Object[]{pid, name, qty, String.format("%.2f", price)});
                total += price;
            } catch (Exception ex) {
                // ignore parse errors
            }
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
        statusLabel.setText("Cart refreshed.");
    }

    private void addProduct() {
        int userId = getUserId();
        ProductItem item = getSelectedProduct();
        int quantity = getQuantity();
        if (item == null || quantity <= 0) {
            statusLabel.setText("Select a product and enter a valid quantity.");
            return;
        }
        dashboard.addProductToCart(userId, item.id, quantity, item.price * quantity);
        statusLabel.setText("Product added.");
        refreshCart();
    }

    private void updateProduct() {
        int userId = getUserId();
        ProductItem item = getSelectedProduct();
        int quantity = getQuantity();
        if (item == null || quantity <= 0) {
            statusLabel.setText("Select a product and enter a valid quantity.");
            return;
        }
        dashboard.updateProductQuantity(userId, item.id, quantity);
        statusLabel.setText("Product updated.");
        refreshCart();
    }

    private void removeProduct() {
        int userId = getUserId();
        ProductItem item = getSelectedProduct();
        if (item == null) {
            statusLabel.setText("Select a product to remove.");
            return;
        }
        dashboard.removeProductFromCart(userId, item.id);
        statusLabel.setText("Product removed.");
        refreshCart();
    }

    private void clearCart() {
        int userId = getUserId();
        java.util.List<String> products = dashboard.getProductsInCart(userId);
        for (String p : products) {
            try {
                int pid = Integer.parseInt(p.split(",")[0].replaceAll("\\D+", ""));
                dashboard.removeProductFromCart(userId, pid);
            } catch (Exception ex) {
                // ignore
            }
        }
        statusLabel.setText("Cart cleared.");
        refreshCart();
    }

    private void fillFieldsFromTable() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            int pid = (int) cartTableModel.getValueAt(row, 0);
            int qty = Integer.parseInt(cartTableModel.getValueAt(row, 2).toString());
            for (int i = 0; i < productCombo.getItemCount(); i++) {
                if (productCombo.getItemAt(i).id == pid) {
                    productCombo.setSelectedIndex(i);
                    break;
                }
            }
            quantityField.setText(String.valueOf(qty));
        }
    }

    private String getProductName(int productId) {
        for (ProductItem p : PRODUCTS) {
            if (p.id == productId) return p.name;
        }
        return "Unknown";
    }

    // Helper class for product dropdown
    static class ProductItem {
        final int id;
        final String name;
        final double price;
        ProductItem(int id, String name, double price) {
            this.id = id; this.name = name; this.price = price;
        }
        public String toString() {
            return name + " ($" + String.format("%.2f", price) + ")";
        }
    }
}