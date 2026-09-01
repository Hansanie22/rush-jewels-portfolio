# 💎 VELORA FINE JEWELLERY — Enterprise Luxury E-Commerce & Retail POS Suite

An enterprise-grade, full-stack luxury jewelry e-commerce platform and Point-of-Sale (POS) management suite built with **Spring Boot 3**, **Spring Security**, **Thymeleaf**, **Hibernate JPA**, and **MySQL**.

Developed and engineered by **Kalatuwawage Hansanie Prabodha** as a showcase of full-stack software architecture, cloud-ready database design, and secure retail workflow automation.

---

## 🌐 Live Demonstration

- **Live Storefront & POS:** [https://rush-jewels-portfolio.onrender.com](https://rush-jewels-portfolio.onrender.com)
- **Demo Admin Account:** `admin@velorajewellery.com` | `Admin@1234`
- **Demo POS Cashier Account:** `pos@velorajewellery.com` | `Cashier@1234`

---

## ✨ Features & Architecture

### 🛍️ Customer Storefront
- **Luxury Showcase & Catalog:** Responsive UI showcasing rings, necklaces, gemstones, bridal collections, and flash sales.
- **Product Variance & Customization:** Dynamic variance selectors for metals (18K, Platinum, Rose Gold), carats, ring sizes, and gemstones (Ceylon Sapphire, Diamond, Ruby, Emerald).
- **Interactive Shopping Cart & Checkout:** Multi-step seamless checkout with dynamic shipping calculation and country/city selection.
- **Payment & Invoicing:** Support for multiple payment methods including online gateways and bank slip verification.
- **Customer Portal:** Profile management, order tracking, order history, verified reviews, and customer support ticket desk.
- **Content & Editorial:** Blog and brand storytelling platform with tag filtering and curated product cross-selling.

### 🏢 Admin & POS Management Suite
- **Interactive Touch POS Terminal:** Real-time point-of-sale checkout, cash/card handling, barcode scanning, cashier shift management, and automated receipt generation.
- **Inventory & Multi-Warehouse Logistics:** Real-time stock tracking across flagship boutiques and distribution centers, stock adjustments, variance tracking, and inter-warehouse transfers.
- **Order & Shipment Lifecycle:** End-to-end order fulfillment pipeline, automated invoice generation, courier company assignment, and tracking numbers.
- **Discounts & Marketing Engine:** Promo code management, percentage/fixed discounts, usage caps, and dynamic promotional banner management.
- **Customer & Staff Administration:** Role-Based Access Control (RBAC), admin session management, immutable audit logs, and customer segmentation.
- **Analytics & Reporting:** Financial executive dashboards, sales trend metrics, POS performance analytics, and Excel report export.
- **Automated Communication:** Asynchronous email dispatching (Order confirmation, password reset, support tickets) via decoupled mail services.

---

## 🛠️ Tech Stack & Engineering Highlights

- **Backend Framework:** Java 17, Spring Boot 3.5.x (Spring MVC, Spring Data JPA, Spring Security, Spring Mail, Spring AMQP)
- **Frontend Architecture:** Modern Semantic HTML5, Vanilla CSS3, Responsive JavaScript, Thymeleaf
- **Database & Persistence:** MySQL 8.x / TiDB Cloud Serverless, Hibernate ORM, HikariCP Connection Pooling
- **Security:** Spring Security (RBAC, Session Management, Content Security Policy, XSS & CSRF protection)
- **Cloud & Media Storage:** Cloudinary CDN integration for high-definition jewelry media delivery
- **DevOps & Containerization:** Multi-stage Docker build, Cloud deployment via Render

---

## 🚀 Local Development Setup

### Prerequisites
- **JDK 17** or higher
- **Maven 3.8+** (or use bundled `./mvnw`)
- **MySQL Server 8.0+**

### 1. Clone the repository
```bash
git clone https://github.com/Hansanie22/rush-jewels-portfolio.git
cd rush-jewels-portfolio
```

### 2. Configure Database & Properties
Update `src/main/resources/application.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/velora_db?createDatabaseIfNotExist=true
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```
Access the storefront at `http://localhost:8080` and admin portal at `http://localhost:8080/admin-login.html`.

---

## ⚖️ Legal Notice & Copyright Disclaimer

**Copyright © 2025 Kalatuwawage Hansanie Prabodha. All Rights Reserved.**

### 1. Demonstration & Portfolio Purpose
This repository and its contents are published exclusively as a personal software engineering portfolio demonstrating system architecture, full-stack development capabilities, and coding standards. 

### 2. Intellectual Property & Non-Commercial License
- The source code, architectural patterns, user interface designs, and system workflows remain the intellectual property of the author.
- **No Commercial Use:** You may not copy, clone, distribute, sell, sublicense, or white-label this software for commercial purposes without explicit, prior written consent from the author.
- **Educational / Reviewer Access:** Recruiters, hiring managers, and educational reviewers are granted permission to review, compile, and test the software locally or view the live demo for evaluation purposes.

### 3. Data Privacy & Sanitization Notice
- All brand identities, product names, customer records, addresses, reviews, and transaction details included in this repository and its demo database are **100% fictionalized dummy data** created strictly for demonstration.
- No confidential client information, production credentials, or real customer Personally Identifiable Information (PII) is stored or exposed within this codebase.

---

### 📬 Author & Contact
- **Developer:** Kalatuwawage Hansanie Prabodha
- **GitHub:** [@Hansanie22](https://github.com/Hansanie22)
