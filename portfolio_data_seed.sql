-- ========================================================
-- RUSH JEWELS - COMPLETE PORTFOLIO DATABASE SEED SCRIPT
-- Contains Schema & Luxury Jewelry Dummy Data
-- ========================================================

CREATE DATABASE IF NOT EXISTS `rush_jewels` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `rush_jewels`;

-- Disable Foreign Key Checks for clean insertions
SET FOREIGN_KEY_CHECKS = 0;

-- 1. STATUS
CREATE TABLE IF NOT EXISTS `status` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `status` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `status` (`id`, `status`) VALUES
(1, 'Active'),
(2, 'Inactive'),
(3, 'Pending'),
(4, 'Blocked')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- 2. STOCK STATUS
CREATE TABLE IF NOT EXISTS `stock_status` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `stock_status` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `stock_status` (`id`, `stock_status`) VALUES
(1, 'In Stock'),
(2, 'Out of Stock'),
(3, 'Low Stock')
ON DUPLICATE KEY UPDATE `stock_status` = VALUES(`stock_status`);

-- 3. REVIEW STATUS
CREATE TABLE IF NOT EXISTS `review_status` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `review_status` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `review_status` (`id`, `review_status`) VALUES
(1, 'Pending'),
(2, 'Approved'),
(3, 'Rejected')
ON DUPLICATE KEY UPDATE `review_status` = VALUES(`review_status`);

-- 4. ORDER STATUS
CREATE TABLE IF NOT EXISTS `order_status` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `status` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `order_status` (`id`, `status`) VALUES
(1, 'Pending'),
(2, 'Processing'),
(3, 'Shipped'),
(4, 'Delivered'),
(5, 'Cancelled')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- 5. PAYMENT STATUS
CREATE TABLE IF NOT EXISTS `payment_status` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `status` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `payment_status` (`id`, `status`) VALUES
(1, 'Pending'),
(2, 'Paid'),
(3, 'Failed'),
(4, 'Refunded')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- 6. SHIPMENT STATUS
CREATE TABLE IF NOT EXISTS `shipment_status` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `status` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `shipment_status` (`id`, `status`) VALUES
(1, 'Pending Pickup'),
(2, 'In Transit'),
(3, 'Out for Delivery'),
(4, 'Delivered')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- 7. CATEGORY
CREATE TABLE IF NOT EXISTS `category` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `category` VARCHAR(45) NOT NULL,
  `status_id` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `category` (`id`, `category`, `status_id`) VALUES
(1, 'Rings', 1),
(2, 'Necklaces & Pendants', 1),
(3, 'Earrings', 1),
(4, 'Bracelets & Bangles', 1),
(5, 'Precious Gemstones', 1),
(6, 'Bridal & Wedding', 1)
ON DUPLICATE KEY UPDATE `category` = VALUES(`category`);

-- 8. GEMSTONE
CREATE TABLE IF NOT EXISTS `gemstone` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `gem_stone` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `gemstone` (`id`, `gem_stone`) VALUES
(1, 'Ceylon Blue Sapphire'),
(2, 'Brilliant Cut Diamond'),
(3, 'Royal Ruby'),
(4, 'Colombian Emerald'),
(5, 'Padparadscha Sapphire'),
(6, 'South Sea Pearl'),
(7, 'Yellow Sapphire')
ON DUPLICATE KEY UPDATE `gem_stone` = VALUES(`gem_stone`);

-- 9. COLOR (Metal / Finish)
CREATE TABLE IF NOT EXISTS `color` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `color` VARCHAR(45) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `color` (`id`, `color`) VALUES
(1, '18K Yellow Gold'),
(2, '18K White Gold'),
(3, '18K Rose Gold'),
(4, 'Platinum 950'),
(5, 'Dual Tone Gold')
ON DUPLICATE KEY UPDATE `color` = VALUES(`color`);

-- 10. SIZE
CREATE TABLE IF NOT EXISTS `size` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `size` VARCHAR(45) NOT NULL,
  `category_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `size` (`id`, `size`, `category_id`) VALUES
(1, 'US 5', 1),
(2, 'US 6', 1),
(3, 'US 7', 1),
(4, 'US 8', 1),
(5, '16 Inches', 2),
(6, '18 Inches', 2),
(7, '7 Inches', 4),
(8, 'Standard', 3)
ON DUPLICATE KEY UPDATE `size` = VALUES(`size`);

-- 11. WAREHOUSE
CREATE TABLE IF NOT EXISTS `warehouse` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `warehouse` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `warehouse` (`id`, `warehouse`) VALUES
(1, 'Colombo Flagship Vault'),
(2, 'Online Distribution Center'),
(3, 'Kandy Boutique Store')
ON DUPLICATE KEY UPDATE `warehouse` = VALUES(`warehouse`);

-- 12. ADMIN
CREATE TABLE IF NOT EXISTS `admin` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100),
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `password` VARCHAR(100) NOT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
  `image_path` VARCHAR(500),
  `last_login` DATETIME,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `status_id` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `admin` (`id`, `name`, `email`, `password`, `role`, `image_path`, `created_at`, `status_id`) VALUES
(1, 'Hansanie Prabodha (Super Admin)', 'admin@velorajewellery.com', 'Admin@1234', 'ADMIN', '/uploads/profile-images/18/profile.png', NOW(), 1),
(2, 'Kasun Perera (POS Cashier)', 'pos@velorajewellery.com', 'Cashier@1234', 'CASHIER', '/uploads/profile-images/19/profile.png', NOW(), 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 13. USER
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `fname` VARCHAR(45) NOT NULL,
  `lname` VARCHAR(45) NOT NULL,
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `mobile` VARCHAR(15),
  `verification` VARCHAR(225),
  `password` VARCHAR(100),
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `verification_expiry` DATETIME,
  `status_id` INT NOT NULL DEFAULT 1,
  `login_provider` VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
  `provider_id` VARCHAR(100),
  `subscribed` TINYINT(1) NOT NULL DEFAULT 0,
  `type` VARCHAR(10) NOT NULL DEFAULT 'USER',
  `image_path` VARCHAR(500),
  PRIMARY KEY (`id`),
  FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (`id`, `fname`, `lname`, `email`, `mobile`, `password`, `created_at`, `status_id`, `type`, `image_path`) VALUES
(1, 'Amaya', 'Senanayake', 'customer@gmail.com', '0771234567', 'Customer@1234', NOW(), 1, 'USER', '/uploads/profile-images/27/profile.png'),
(2, 'Dilshan', 'Wickramasinghe', 'dilshan@gmail.com', '0719876543', 'Customer@1234', NOW(), 1, 'USER', '/uploads/profile-images/65/profile.png')
ON DUPLICATE KEY UPDATE `fname` = VALUES(`fname`);

-- 14. PRODUCT
CREATE TABLE IF NOT EXISTS `product` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `description` TEXT NOT NULL,
  `specifications` TEXT NOT NULL,
  `warranty` VARCHAR(100) NOT NULL,
  `category_id` INT NOT NULL,
  `status_id` INT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `image1` VARCHAR(500),
  `image2` VARCHAR(500),
  `image3` VARCHAR(500),
  `image4` VARCHAR(500),
  PRIMARY KEY (`id`),
  FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `product` (`id`, `name`, `title`, `description`, `specifications`, `warranty`, `category_id`, `status_id`, `created_at`, `image1`, `image2`, `image3`, `image4`) VALUES
(1, 'Royal Blue Sapphire Ring', '18K Gold Natural Ceylon Sapphire Ring', 'Handcrafted with a certified 2.5ct Royal Blue Ceylon Sapphire surrounded by conflict-free diamond accents in pure 18K Yellow Gold.', 'Stone: Natural Ceylon Sapphire\nMetal: 18K Yellow Gold\nClarity: VVS1\nCut: Oval Brilliant', 'Lifetime Guarantee & Certificate of Authenticity', 1, 1, NOW(), '/uploads/product-images/1/image1.png', NULL, NULL, NULL),
(2, 'Elysian Diamond Solitaire', 'Signature 1.5 Carat Diamond Solitaire Ring', 'An eternal symbol of love featuring a stunning round brilliant diamond nestled in a 6-prong platinum crown setting.', 'Diamond: 1.50 Carats\nColor: D (Colorless)\nClarity: VVS2\nBand: Platinum 950', 'GIA Certified & Lifetime Polish Warranty', 1, 1, NOW(), '/uploads/product-images/2/image1.png', '/uploads/product-images/2/image2.png', '/uploads/product-images/2/image3.png', '/uploads/product-images/2/image4.png'),
(3, 'Celestial Diamond Pendant', 'Floating Halo Diamond Teardrop Pendant', 'A mesmerizing pear-cut diamond surrounded by a micropave halo, hanging gracefully from an 18K white gold box chain.', 'Pendant: 0.85ct Diamond\nChain: 18K White Gold 18 inch\nTotal Weight: 4.8g', '5-Year International Warranty', 2, 1, NOW(), '/uploads/product-images/3/image1.png', '/uploads/product-images/3/image2.png', NULL, NULL),
(4, 'Imperial Emerald Bracelet', 'Colombian Emerald & Diamond Tennis Bracelet', 'An opulent tennis bracelet featuring 24 natural vivid green Colombian emeralds alternating with brilliant-cut diamonds.', 'Emeralds: 6.20 Total Carats\nDiamonds: 2.10 Carats\nLock: Double Safety Clasp', 'Lifetime Craftsmanship Guarantee', 4, 1, NOW(), '/uploads/product-images/4/image1.png', NULL, NULL, NULL),
(5, 'Rose Gold Blossom Earrings', '18K Rose Gold Floral Diamond Drop Earrings', 'Delicate blossoms sculpted in warm rose gold, sparkling with premium pave diamonds for effortless day-to-night glamour.', 'Metal: 18K Rose Gold\nDiamonds: 0.65 Total Carats\nBacking: Secure Push-Back', '3-Year Warranty with Annual Cleaning', 3, 1, NOW(), '/uploads/product-images/5/image1.png', NULL, NULL, NULL),
(6, 'Heritage Ruby Choker', 'Burmese Ruby & Gold Filigree Choker', 'Intricate royal filigree artistry paired with pigeon-blood red rubies, inspired by regal Sri Lankan heritage.', 'Rubies: Certified Natural Burmese\nGold: 22K Solid Gold\nWeight: 32.5g', 'Official Gemmological Certificate', 2, 1, NOW(), '/uploads/product-images/6/image1.png', NULL, NULL, NULL),
(7, 'Padparadscha Sunset Ring', 'Rare Ceylon Padparadscha Sapphire Ring', 'The king of sapphires showcasing the rare delicate blend of lotus pink and sunset orange in a diamond-encrusted band.', 'Center Stone: 3.10ct Padparadscha\nDiamonds: 0.50ct Halo\nMetal: Platinum & Rose Gold', 'GIA & NGJA Certified', 1, 1, NOW(), '/uploads/product-images/7/image1.png', '/uploads/product-images/7/image2.png', NULL, NULL),
(8, 'South Sea Pearl Drop', 'Lustrous Golden Pearl Diamond Necklace', 'A flawless 13mm Golden South Sea Pearl suspended beneath a cluster of marquise-cut diamonds in 18K yellow gold.', 'Pearl: 13mm AAA Grade South Sea\nDiamonds: 0.40ct Marquise\nChain: 18K Yellow Gold', 'Pearl Authenticity Guarantee', 2, 1, NOW(), '/uploads/product-images/8/image1.png', NULL, NULL, NULL),
(11, 'Eternity Platinum Wedding Band', 'Full Diamond Eternity Band in Pure Platinum', 'Continuous circle of channel-set diamonds representing unending devotion and sophistication.', 'Diamonds: 1.80 Total Carats\nMetal: Platinum 950\nWidth: 3.5mm', 'Lifetime Resize & Maintenance', 1, 1, NOW(), '/uploads/product-images/11/image1.png', NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 15. PRODUCT VARIANCE
CREATE TABLE IF NOT EXISTS `product_variance` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `product_id` INT NOT NULL,
  `size_id` INT,
  `color_id` INT,
  `gemstone_id` INT,
  `status_id` INT NOT NULL DEFAULT 1,
  `price` DOUBLE,
  `discount_percentage` DOUBLE DEFAULT 0,
  `regular_price` DOUBLE NOT NULL,
  `stock_limit` INT NOT NULL DEFAULT 10,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  FOREIGN KEY (`size_id`) REFERENCES `size` (`id`),
  FOREIGN KEY (`color_id`) REFERENCES `color` (`id`),
  FOREIGN KEY (`gemstone_id`) REFERENCES `gemstone` (`id`),
  FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `product_variance` (`id`, `product_id`, `size_id`, `color_id`, `gemstone_id`, `status_id`, `price`, `discount_percentage`, `regular_price`, `stock_limit`) VALUES
(1, 1, 2, 1, 1, 1, 295000, 10, 328000, 15),
(2, 1, 3, 2, 1, 1, 310000, 5, 328000, 10),
(3, 2, 2, 4, 2, 1, 480000, 15, 565000, 8),
(4, 2, 3, 4, 2, 1, 480000, 15, 565000, 12),
(5, 3, 6, 2, 2, 1, 195000, 0, 195000, 20),
(6, 4, 7, 1, 4, 1, 620000, 10, 689000, 5),
(7, 5, 8, 3, 2, 1, 145000, 20, 182000, 25),
(8, 6, 5, 1, 3, 1, 850000, 0, 850000, 3),
(9, 7, 3, 3, 5, 1, 750000, 10, 835000, 4),
(10, 8, 6, 1, 6, 1, 230000, 0, 230000, 14),
(11, 11, 2, 4, 2, 1, 320000, 10, 355000, 18)
ON DUPLICATE KEY UPDATE `regular_price` = VALUES(`regular_price`);

-- 16. STOCK
CREATE TABLE IF NOT EXISTS `stock` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `qty` INT NOT NULL,
  `stock_status_id` INT NOT NULL DEFAULT 1,
  `product_variance_id` INT,
  `collection_id` INT,
  `warehouse_id` INT,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`stock_status_id`) REFERENCES `stock_status` (`id`),
  FOREIGN KEY (`product_variance_id`) REFERENCES `product_variance` (`id`),
  FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `stock` (`id`, `qty`, `stock_status_id`, `product_variance_id`, `collection_id`, `warehouse_id`) VALUES
(1, 25, 1, 1, NULL, 1),
(2, 18, 1, 2, NULL, 1),
(3, 12, 1, 3, NULL, 1),
(4, 15, 1, 4, NULL, 2),
(5, 30, 1, 5, NULL, 1),
(6, 8, 1, 6, NULL, 1),
(7, 35, 1, 7, NULL, 2),
(8, 5, 1, 8, NULL, 1),
(9, 6, 1, 9, NULL, 1),
(10, 20, 1, 10, NULL, 2),
(11, 22, 1, 11, NULL, 1)
ON DUPLICATE KEY UPDATE `qty` = VALUES(`qty`);

-- 17. COLLECTION
CREATE TABLE IF NOT EXISTS `collection` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `title` VARCHAR(100),
  `description` TEXT,
  `specifications` TEXT,
  `warranty` VARCHAR(45),
  `material` VARCHAR(45),
  `price` DOUBLE,
  `discount_percentage` DOUBLE,
  `regular_price` DOUBLE,
  `stock_limit` INT,
  `type` VARCHAR(45),
  `image1` VARCHAR(500),
  `image2` VARCHAR(500),
  `image3` VARCHAR(500),
  `image4` VARCHAR(500),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `status_id` INT DEFAULT 1,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `collection` (`id`, `name`, `title`, `description`, `specifications`, `warranty`, `material`, `price`, `discount_percentage`, `regular_price`, `stock_limit`, `type`, `image1`, `image2`, `image3`, `image4`, `created_at`, `status_id`) VALUES
(1, 'Royal Heritage Set', 'Regal Sapphire & Diamond Bridal Set', 'A curated bridal suite featuring matching Ceylon Sapphire necklace, drop earrings, and signature cocktail ring.', '3-Piece Set: Necklace, Earrings, Ring\nMetal: 18K White Gold', 'Lifetime Guarantee', '18K White Gold', 890000, 15, 1050000, 5, 'Bridal Suite', '/uploads/collection-images/1/image1.png', '/uploads/collection-images/1/image2.png', NULL, NULL, NOW(), 1),
(2, 'Imperial Radiance', 'Full Diamond Pavé Evening Collection', 'Bespoke diamond luxury designed for red carpet sophistication and gala evenings.', 'Includes Tennis Necklace & Bracelet', 'Certified Authenticity', 'Platinum 950', 1250000, 10, 1390000, 3, 'Fine Jewelry', '/uploads/collection-images/2/image1.png', NULL, NULL, NULL, NOW(), 1),
(3, 'Celestial Blossom', 'Rose Gold Floral Blossom Suite', 'An ode to spring featuring delicate rose gold petals and radiant diamond centers.', 'Earrings & Pendant Pair', '3-Year Warranty', '18K Rose Gold', 340000, 20, 425000, 8, 'Seasonal Set', '/uploads/collection-images/3/image1.png', NULL, NULL, NULL, NOW(), 1),
(4, 'Serene Ocean Pearl', 'South Sea Golden Pearl Set', 'Rare Golden South Sea Pearls matched in color, luster, and size across a necklace and drop earrings.', 'AAA Grade Golden Pearls', '5-Year Warranty', '18K Yellow Gold', 520000, 10, 578000, 6, 'Pearl Exclusive', '/uploads/collection-images/4/image1.png', NULL, NULL, NULL, NOW(), 1),
(7, 'Eternal Romance', 'His & Hers Platinum Wedding Band Pair', 'Hand-finished comfort fit platinum wedding bands with precision micro-diamond channel detailing.', 'Matching Couple Bands Set', 'Lifetime Resize Support', 'Platinum 950', 450000, 15, 530000, 10, 'Wedding Bands', '/uploads/collection-images/7/image1.png', '/uploads/collection-images/7/image2.png', NULL, NULL, NOW(), 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 18. BANNER
CREATE TABLE IF NOT EXISTS `banner` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `media_path` VARCHAR(500) NOT NULL,
  `media_type` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `banner` (`id`, `media_path`, `media_type`) VALUES
(1, '/uploads/banner-images/banner_11.mp4', 'VIDEO'),
(2, '/uploads/banner-images/banner_7.png', 'IMAGE'),
(3, '/uploads/banner-images/banner_9.png', 'IMAGE')
ON DUPLICATE KEY UPDATE `media_path` = VALUES(`media_path`);

-- 19. BLOG POST
CREATE TABLE IF NOT EXISTS `blog_post` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `slug` VARCHAR(200) NOT NULL UNIQUE,
  `snippet` TEXT NOT NULL,
  `content` LONGTEXT NOT NULL,
  `image_path` VARCHAR(500),
  `category` VARCHAR(100),
  `read_time` VARCHAR(20),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_published` TINYINT(1) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `blog_post` (`id`, `title`, `slug`, `snippet`, `content`, `image_path`, `category`, `read_time`, `created_at`, `is_published`) VALUES
(1, 'The Timeless Allure of Ceylon Blue Sapphires', 'timeless-allure-ceylon-blue-sapphires', 'Discover why Ceylon Sapphires remain the ultimate choice for royalty and world-renowned connoisseurs across centuries.', 'Sri Lanka, historically known as Ratna-Dweepa (The Island of Jewels), has produced the worlds finest cornflower blue and royal blue sapphires. In this guide, we explore the distinct optical properties, the unique heat-treatment certification, and what makes Ceylon sapphires hold their value across generations.', '/uploads/blog-images/blog_1.png', 'Gemstone Education', '4 min read', NOW(), 1),
(2, 'The 4Cs: An Expert Guide to Buying Diamonds', 'expert-guide-to-diamond-4cs', 'Learn how Carat, Cut, Clarity, and Color determine the true brilliance and investment value of your dream diamond.', 'When choosing a diamond for an engagement ring or signature heirloom, the Cut is undeniably the most crucial factor determining fire and scintillation. Discover our gemmologists tips on selecting eye-clean stones and maximizing your budget without compromising on breathtaking beauty.', '/uploads/blog-images/blog_2.png', 'Buying Guide', '6 min read', NOW(), 1),
(3, 'Caring for High-Jewelry: Maintenance & Polishing', 'caring-for-high-jewelry-guide', 'Preserve the sparkling brilliance and structural longevity of fine gold, platinum, and gemstone jewelry with professional tips.', 'From ultrasonic cleaning cautions with emeralds and pearls to safe daily storage practices, learn how to keep your cherished Velora Fine Jewellery creations looking as radiant as the day they left our boutique atelier.', '/uploads/blog-images/blog_3.png', 'Jewelry Care', '3 min read', NOW(), 1)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- 20. COUNTRY, PROVINCE, CITY
CREATE TABLE IF NOT EXISTS `country` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `country` VARCHAR(100) NOT NULL,
  `code` VARCHAR(45) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `country` (`id`, `country`, `code`) VALUES
(1, 'Sri Lanka', 'LK'),
(2, 'United States', 'US'),
(3, 'United Kingdom', 'GB'),
(4, 'Australia', 'AU'),
(5, 'United Arab Emirates', 'AE')
ON DUPLICATE KEY UPDATE `country` = VALUES(`country`);

CREATE TABLE IF NOT EXISTS `province` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `province` VARCHAR(100) NOT NULL,
  `country_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`country_id`) REFERENCES `country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `province` (`id`, `province`, `country_id`) VALUES
(1, 'Western Province', 1),
(2, 'Central Province', 1),
(3, 'Southern Province', 1),
(4, 'North Western Province', 1)
ON DUPLICATE KEY UPDATE `province` = VALUES(`province`);

CREATE TABLE IF NOT EXISTS `city` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `city` VARCHAR(100) NOT NULL,
  `postal_code` VARCHAR(20),
  `province_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`province_id`) REFERENCES `province` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `city` (`id`, `city`, `postal_code`, `province_id`) VALUES
(1, 'Colombo', '00100', 1),
(2, 'Dehiwala - Mount Lavinia', '10350', 1),
(3, 'Negombo', '11500', 1),
(4, 'Kandy', '20000', 2),
(5, 'Galle', '80000', 3)
ON DUPLICATE KEY UPDATE `city` = VALUES(`city`);

-- 21. PAYMENT METHODS
CREATE TABLE IF NOT EXISTS `payments_method` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `method` VARCHAR(45) NOT NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `payments_method` (`id`, `method`, `is_active`) VALUES
(1, 'Credit / Debit Card (PayHere Online)', 1),
(2, 'Direct Bank Slip Transfer', 1),
(3, 'Cash on Delivery (Courier COD)', 1),
(4, 'Store POS Terminal Card / Cash', 1)
ON DUPLICATE KEY UPDATE `method` = VALUES(`method`);

-- 22. DISCOUNT CODES
CREATE TABLE IF NOT EXISTS `discount_code` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(45) NOT NULL UNIQUE,
  `discount_percentage` DOUBLE NOT NULL,
  `max_discount_amount` DOUBLE,
  `min_order_amount` DOUBLE,
  `valid_from` DATETIME,
  `valid_until` DATETIME,
  `usage_limit` INT DEFAULT 100,
  `usage_count` INT DEFAULT 0,
  `status_id` INT DEFAULT 1,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`status_id`) REFERENCES `status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `discount_code` (`id`, `code`, `discount_percentage`, `max_discount_amount`, `min_order_amount`, `valid_from`, `valid_until`, `usage_limit`, `usage_count`, `status_id`) VALUES
(1, 'WELCOME10', 10, 50000, 100000, NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 500, 24, 1),
(2, 'LUXURY2026', 15, 100000, 250000, NOW(), DATE_ADD(NOW(), INTERVAL 180 DAY), 200, 12, 1),
(3, 'VIPGOLD', 20, 200000, 500000, NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 50, 5, 1)
ON DUPLICATE KEY UPDATE `discount_percentage` = VALUES(`discount_percentage`);

-- 23. REVIEWS
CREATE TABLE IF NOT EXISTS `review` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `rating` INT NOT NULL,
  `comment` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `approved_at` DATETIME,
  `review_status_id` INT NOT NULL DEFAULT 2,
  `product_variance_id` INT,
  `collection_id` INT,
  `user_id` INT,
  `admin_id` INT,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`review_status_id`) REFERENCES `review_status` (`id`),
  FOREIGN KEY (`product_variance_id`) REFERENCES `product_variance` (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `review` (`id`, `rating`, `comment`, `created_at`, `approved_at`, `review_status_id`, `product_variance_id`, `collection_id`, `user_id`) VALUES
(1, 5, 'The Royal Ceylon Sapphire Ring is simply breathtaking in person! The craftsmanship and gold finish exceeded all my expectations.', NOW(), NOW(), 2, 1, NULL, 1),
(2, 5, 'Ordered the Elysian Diamond Solitaire for my engagement. Certificate verified, packaging was royal, and she said YES!', NOW(), NOW(), 2, 3, NULL, 2),
(3, 5, 'Superb quality and customer service from Velora Fine Jewellery. The delivery was fast and secure with tamper-proof seal.', NOW(), NOW(), 2, 5, NULL, 1)
ON DUPLICATE KEY UPDATE `rating` = VALUES(`rating`);

-- 24. SYSTEM SETTINGS
CREATE TABLE IF NOT EXISTS `system_setting` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `setting_key` VARCHAR(100) NOT NULL UNIQUE,
  `setting_value` TEXT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `system_setting` (`id`, `setting_key`, `setting_value`) VALUES
(1, 'STORE_NAME', 'Velora Fine Jewellery Boutique'),
(2, 'STORE_CURRENCY', 'LKR'),
(3, 'STORE_EMAIL', 'info@velorajewellery.com'),
(4, 'STORE_PHONE', '+94 11 234 5678'),
(5, 'FREE_SHIPPING_THRESHOLD', '150000'),
(6, 'TAX_RATE', '0')
ON DUPLICATE KEY UPDATE `setting_value` = VALUES(`setting_value`);

-- Re-enable Foreign Key Checks
SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================
-- End of Seed Script
-- ========================================================
