# SU7.9 Professional POS

SU7.9 Professional POS is a feature-rich, user-friendly Point of Sale system developed using Java Swing. It provides a comprehensive solution for managing customers, inventory, and sales, all wrapped in a clean, modern interface. This application is designed to be intuitive for both cashiers and administrators.

-----

## ? Features

  * **User Authentication:** Secure login system with distinct roles for regular users and administrators.
  * **Customer Management:**
      * Add new customers with details like ID, name, address, and phone number.
      * Search for customers by ID or name.
      * View a list of all customers.
      * View detailed order history for a selected customer.
  * **Sales & Checkout:**
      * Add items to a sale from the product list.
      * Automatically calculates subtotal, tax, and total amount due.
      * Process payments and calculate change.
      * Hold or clear a sale.
  * **Admin Tools (Admin Role Only):**
      * Add new items to the inventory.
      * Update details of existing items.
      * Delete items from the inventory.

-----

## ??? Tech Stack

  * **Frontend:** Java Swing
  * **Database:** MySQL

-----

## ?? Getting Started

### Prerequisites

  * JDK 17 or higher
  * MySQL Server
  * An IDE like IntelliJ IDEA or Eclipse

### Setup

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/sengbuntheng/javaswing-pos.git
    ```

2.  **Database Setup:**

      * Create a database named `pos1` in your MySQL server.
      * Execute the following SQL commands to create the necessary tables:
        ```sql
        CREATE TABLE user (
            user_id VARCHAR(255) PRIMARY KEY,
            password VARCHAR(255),
            user_role_id INT
        );

        CREATE TABLE user_role (
            id INT PRIMARY KEY,
            role VARCHAR(255)
        );

        CREATE TABLE customer (
            customer_id VARCHAR(255) PRIMARY KEY,
            name VARCHAR(255),
            address VARCHAR(255),
            phone VARCHAR(255)
        );

        CREATE TABLE item (
            item_code VARCHAR(255) PRIMARY KEY,
            description VARCHAR(255),
            qty INT,
            unit_price DECIMAL(10, 2)
        );

        CREATE TABLE `order` (
            order_id VARCHAR(255) PRIMARY KEY,
            date DATE,
            user_id VARCHAR(255)
        );

        CREATE TABLE customer_order (
            customer_id VARCHAR(255),
            order_id VARCHAR(255)
        );

        CREATE TABLE order_item (
            item_code VARCHAR(255),
            order_id VARCHAR(255),
            qty INT,
            unit_price DECIMAL(10, 2)
        );

        -- Insert default users
        INSERT INTO user_role (id, role) VALUES (1, 'Admin'), (2, 'User');
        INSERT INTO user (user_id, password, user_role_id) VALUES ('admin', 'admin_pass', 1), ('user', 'user_pass', 2);
        ```

3.  **Configure Database Connection:**

      * Open the `src/globalValues/DBConnection.java` file.
      * Update the `connectionString`, `connectionUser`, and `connectionPassword` variables with your MySQL credentials.

4.  **Run the Application:**

      * Open the project in your favorite Java IDE.
      * Locate and run the `src/forms/Login.java` file to start the application.

-----

## ?? Usage

  * **Login:** Use the credentials you set up in the database. For example, `admin` / `admin_pass`.
  * **Main Dashboard:** After logging in, you'll see the main dashboard with customer information and the current sale panel.
  * **Admin Functions:** If you are logged in as an admin, you will have access to the "Admin Tools" menu to manage items.

-----

## ?? Contributing

Contributions are welcome\! If you'd like to contribute, please fork the repository and use a feature branch. Pull requests are warmly welcome.

### Areas for Contribution

  * **UI/UX Enhancements:** Improving the look and feel of the application.
  * **New Features:** Adding new functionalities like reporting, inventory tracking, or different payment methods.
  * **Bug Fixes:** Identifying and fixing bugs in the existing codebase.
  * **Database Migration:** Migrating from raw JDBC to an ORM like Hibernate.

-----

## ?? License

This project is open-source and available under the MIT License.
