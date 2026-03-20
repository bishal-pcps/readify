package controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import model.Order;
import model.Book;
import model.Report;

public class ReportController {
    private OrderController orderController;
    private BookController bookController;
    
    public ReportController() {
        this.orderController = new OrderController();
        this.bookController = new BookController();
    }
    
    public Report generateSalesReport() {
        Report salesReport = new Report();
        salesReport.setType("SALES");
        salesReport.setGeneratedDate(new java.util.Date());
        
        Map<String, Object> data = new HashMap<>();
        List<Order> orders = orderController.getAllOrders();
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order order : orders) {
            totalRevenue = totalRevenue.add(order.getTotalAmount());
        }
        
        data.put("totalOrders", orders.size());
        data.put("totalRevenue", totalRevenue);
        data.put("averageOrderValue", orders.isEmpty() ? BigDecimal.ZERO : totalRevenue.divide(new BigDecimal(orders.size()), 2, java.math.RoundingMode.HALF_UP));
        
        salesReport.setData(data);
        return salesReport;
    }
    
    public Report generateInventoryReport() {
        Report inventoryReport = new Report();
        inventoryReport.setType("INVENTORY");
        inventoryReport.setGeneratedDate(new java.util.Date());
        
        Map<String, Object> data = new HashMap<>();
        List<Book> books = bookController.getAllBooks();
        
        long lowStock = books.stream()
            .filter(b -> b.getStockLevel() < 10)
            .count();
        
        BigDecimal totalValue = BigDecimal.ZERO;
        for (Book book : books) {
            totalValue = totalValue.add(book.getPrice().multiply(new BigDecimal(book.getStockLevel())));
        }
        
        data.put("totalBooks", books.size());
        data.put("lowStockItems", lowStock);
        data.put("totalValue", totalValue);
        
        inventoryReport.setData(data);
        return inventoryReport;
    }
}
