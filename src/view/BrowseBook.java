package view;

import controller.BookController;
import controller.CartController;
import model.Book;
import view.components.BookCard;
import view.components.NavigationBar;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BrowseBooksView extends JFrame {
    private BookController bookController;
    private CartController cartController;
    private JPanel booksPanel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JPanel mainContent;
    
    public BrowseBooksView() {
        this.bookController = new BookController();
        this.cartController = new CartController();
        bookController.setBrowseView(this);
        initComponents();
        loadBooks();
    }
    
    private void initComponents() {
        setTitle("Readify - Browse Books");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Main layout
        setLayout(new BorderLayout());
        
        // Navigation bar (from your UI)
        NavigationBar navBar = new NavigationBar(cartController);
        add(navBar, BorderLayout.NORTH);
        
        // Main content with sidebar and book grid
        mainContent = new JPanel(new BorderLayout());
        
        // Sidebar filters (from your UI)
        JPanel sidebar = createSidebar();
        mainContent.add(sidebar, BorderLayout.WEST);
        
        // Books panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(30);
        searchField.setPreferredSize(new Dimension(400, 35));
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchBooks());
        
        searchPanel.add(new JLabel("Q ")); // Search icon
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Books grid
        booksPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        booksPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(booksPanel);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainContent.add(centerPanel, BorderLayout.CENTER);
        
        add(mainContent, BorderLayout.CENTER);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(new Color(245, 245, 245));
        
        // Filters label (from your UI)
        JLabel filtersLabel = new JLabel("Filters");
        filtersLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Category buttons (from your UI)
        String[] categories = {
            "Classic Literature",
            "Contemporary Fiction",
            "Mystery & Thriller",
            "Cooking & Food",
            "Science & Education",
            "Fantasy & Sci-Fi"
        };
        
        sidebar.add(filtersLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        
        for (String category : categories) {
            JButton categoryBtn = new JButton(category);
            categoryBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            categoryBtn.setBackground(new Color(245, 245, 245));
            categoryBtn.setBorderPainted(false);
            categoryBtn.addActionListener(e -> filterByCategory(category));
            sidebar.add(categoryBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        return sidebar;
    }
    
    private void loadBooks() {
        List<Book> books = bookController.getAllBooks();
        displayBooks(books);
    }
    
    public void displayBooks(List<Book> books) {
        booksPanel.removeAll();
        
        for (Book book : books) {
            BookCard card = new BookCard(book, cartController);
            booksPanel.add(card);
        }
        
        booksPanel.revalidate();
        booksPanel.repaint();
    }
    
    private void filterByCategory(String category) {
        bookController.loadBooksByCategory(category);
    }
    
    private void searchBooks() {
        String keyword = searchField.getText();
        bookController.search(keyword);
    }
}