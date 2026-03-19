package view;

import controller.AdminController;
import model.Order;
import view.components.StatCard;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AdminView extends JFrame {
    private AdminController adminController;
    private JLabel totalRevenueLabel;
    private JLabel totalOrdersLabel;
    private JLabel totalCustomersLabel;
    private JLabel avgOrderLabel;
    private JPanel ordersPanel;
    
    public AdminView() {
        this.adminController = new AdminController();
        adminController.setAdminView(this);
        initComponents();
        loadDashboardData();
    }
    
    private void initComponents() {
        setTitle("Readify - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        // Main layout
        setLayout(new BorderLayout());
        
        // Top navigation
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setBackground(new Color(30, 30, 30));
        topNav.setPreferredSize(new Dimension(0, 60));
        
        JLabel titleLabel = new JLabel("Readify Admin");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JPanel navLinks = new JPanel(new FlowLayout());
        navLinks.setBackground(new Color(30, 30, 30));
        String[] links = {"Dashboard", "Inventory", "Reports", "Users", "Store"};
        for (String link : links) {
            JButton btn = new JButton(link);
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(30, 30, 30));
            btn.setBorderPainted(false);
            navLinks.add(btn);
        }
        
        topNav.add(titleLabel, BorderLayout.WEST);
        topNav.add(navLinks, BorderLayout.CENTER);
        
        add(topNav, BorderLayout.NORTH);
        
        // Main content
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Stats cards (from your UI)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        
        totalRevenueLabel = new JLabel();
        totalOrdersLabel = new JLabel();
        totalCustomersLabel = new JLabel();
        avgOrderLabel = new JLabel();
        
        statsPanel.add(new StatCard("Total Revenue", "$37,100", "↑12.5% from last month"));
        statsPanel.add(new StatCard("Total Orders", "1,278", "↑8.2% from last month"));
        statsPanel.add(new StatCard("Total Customers", "892", "↑15.3% from last month"));
        statsPanel.add(new StatCard("Avg Order Value", "$29.03", "↓3.1% from last month"));
        
        mainContent.add(statsPanel, BorderLayout.NORTH);
        
        // Charts and tables
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Sales trend panel
        JPanel salesPanel = new JPanel(new BorderLayout());
        salesPanel.setBorder(BorderFactory.createTitledBorder("Sales Trend"));
        JLabel salesChart = new JLabel("Sales chart would go here", SwingConstants.CENTER);
        salesChart.setPreferredSize(new Dimension(400, 200));
        salesPanel.add(salesChart);
        
        // Categories panel
        JPanel categoriesPanel = new JPanel(new BorderLayout());
        categoriesPanel.setBorder(BorderFactory.createTitledBorder("Categories"));
        JLabel categoriesChart = new JLabel("Categories chart would go here", SwingConstants.CENTER);
        categoriesChart.setPreferredSize(new Dimension(400, 200));
        categoriesPanel.add(categoriesChart);
        
        bottomPanel.add(salesPanel);
        bottomPanel.add(categoriesPanel);
        
        mainContent.add(bottomPanel, BorderLayout.CENTER);
        
        add(mainContent, BorderLayout.CENTER);
    }
    
    private void loadDashboardData() {
        Map<String, Object> stats = adminController.getDashboardStats();
        // Update UI with real data
    }
    
    public void updateDashboard(Map<String, Object> stats, List<Order> recentOrders) {
        // Update stats cards with real data
        totalRevenueLabel.setText(String.valueOf(stats.get("totalRevenue")));
        totalOrdersLabel.setText(String.valueOf(stats.get("totalOrders")));
        totalCustomersLabel.setText(String.valueOf(stats.get("totalCustomers")));
        avgOrderLabel.setText(String.valueOf(stats.get("avgOrderValue")));
    }
}