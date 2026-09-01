# 💎 RUSH JEWELS — Luxury E-Commerce & Retail POS Suite

An enterprise-grade, full-stack luxury jewelry e-commerce platform and Point-of-Sale (POS) management suite built with **Spring Boot 3**, **Thymeleaf**, **Spring Security**, and **MySQL**.

---

## ✨ Features & Architecture

### 🛍️ Customer Storefront
- **Luxury Showcase & Catalog:** Responsive UI showcasing rings, necklaces, gemstones, collections, and flash sales.
- **Product Variance & Customization:** Detailed variance selectors for metals, carats, ring sizes, and gemstones.
- **Interactive Shopping Cart & Checkout:** Multi-step seamless checkout with dynamic shipping calculation and country/city selection.
- **Payment & Invoicing:** Support for multiple payment methods including online gateways and bank slip verification.
- **Customer Portal:** Profile management, order tracking, order history, reviews, and support ticket system.
- **Blog & Brand Storytelling:** Content publishing system with tag filtering and product associations.

### 🏢 Admin & POS Management Suite
- **Interactive POS Terminal:** Fast point-of-sale checkout, cash/card handling, barcode scanning, shift management, and digital receipt generation.
- **Inventory & Warehouse Logistics:** Multi-warehouse stock tracking, stock adjustments, variance tracking, and stock transfers.
- **Order & Shipment Lifecycle:** End-to-end order processing, invoice generation, courier company integrations, and tracking numbers.
- **Discounts & Marketing Engine:** Promo code management, percentage/fixed discounts, usage caps, and banner management.
- **Customer & Staff Administration:** Role-based access control (RBAC), admin session management, audit logs, and customer segmentation.
- **Reports & Analytics:** Financial dashboards, sales metrics, POS analytics, and Excel export reports.
- **Automated Communication:** Asynchronous email dispatching (Order confirmation, password reset, support tickets) via RabbitMQ / Spring Mail.

---

## 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot 3.5.x (Spring MVC, Spring Data JPA, Spring Security, Spring Mail, Spring AMQP)
- **Frontend:** Thymeleaf Templates, Modern CSS3 / JavaScript, Responsive Layouts
- **Database:** MySQL 8.x, Hibernate ORM
- **Media & Cloud Storage:** Cloudinary integration for product and banner media management
- **Messaging:** RabbitMQ for decoupled event processing and asynchronous notifications
- **Build Tool:** Maven

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17** or higher
- **Maven 3.8+** (or use bundled `./mvnw`)
- **MySQL Server**
- **RabbitMQ** (Optional for async email queues)

### 1. Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/velora-jewellery-portfolio.git
cd velora-jewellery-portfolio
```

### 2. Configure Database & Properties
Update `src/main/resources/application.properties` with your database credentials and configuration:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rush_jewels_db?createDatabaseIfNotExist=true
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```

Access the storefront at `http://localhost:8080` and admin portal at `http://localhost:8080/admin`.

---

## 📄 License
This project is for demonstration and developer portfolio purposes.
