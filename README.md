# 🎨 Kalasetu – Art Marketplace Platform

**Kalasetu** is an Android-based digital art marketplace that connects artists with art buyers through a centralized platform. Artists can upload their paintings, administrators can review and approve artwork, and users can browse and purchase approved paintings.

The application aims to promote artists, improve artwork visibility, and provide a structured purchasing experience through an admin-controlled approval system.

---

## 📌 Project Overview

Traditional artists often face difficulties reaching potential buyers and showcasing their artwork to a wider audience. Buyers may also find it difficult to verify the quality and authenticity of paintings.

Kalasetu addresses these challenges by providing a digital marketplace with role-based access, artwork approval, painting management, and order tracking.

### 🎯 Project Objectives

* Provide a centralized digital platform for artists and buyers.
* Allow artists to upload and manage their paintings.
* Enable administrators to approve or reject submitted artwork.
* Display only approved paintings to buyers.
* Allow users to purchase paintings and view their order history.
* Maintain structured records of users, artists, paintings, and orders.
* Promote art and creativity through a digital marketplace.

---

## ✨ Key Features

### 👤 1. User Module

* User registration and login.
* Authentication using username and password.
* Account recovery using phone number and secret code.
* Browse approved paintings.
* View painting details, descriptions, prices, and images.
* Place orders for paintings.
* View order history and order status.

### 🎨 2. Artist Module

* Artist registration and login.
* Upload paintings with titles, descriptions, prices, and images.
* Track painting approval status.
* View pending, approved, and rejected paintings.
* View orders received for uploaded paintings.
* Access order details, including buyer information and delivery address.
* Update order status.

### 🛡️ 3. Admin Module

* Administrative access to the application.
* View registered users and artists.
* View paintings submitted by artists.
* Approve or reject uploaded paintings.
* Manage user and artist records.
* Control which paintings are displayed in the marketplace.
* Maintain artwork quality and marketplace trust.

### 🛒 4. Painting Management

* Store painting information in the SQLite database.
* Maintain painting titles, descriptions, prices, and image references.
* Track painting approval status.
* Manage painting availability.
* Display approved artwork to users.

### 📦 5. Order Management

* Create order records when users purchase paintings.
* Maintain buyer, artist, and painting references.
* Track order status and creation date.
* Allow artists to view received orders.
* Support order status updates.

### 🗄️ 6. Database Management

* Local database management using SQLite.
* Database creation and version management.
* Insert, retrieve, update, and delete operations.
* Structured storage of user, artist, painting, and order information.

---

## 🛠️ Technologies Used

| Technology          | Purpose                                              |
| ------------------- | ---------------------------------------------------- |
| Kotlin              | Primary programming language for Android development |
| Android Studio      | Android application development environment          |
| XML                 | User interface layout design                         |
| SQLite              | Local database storage                               |
| SQLiteOpenHelper    | Database creation, upgrades, and management          |
| Cursor              | Retrieving and reading database query results        |
| ContentValues       | Inserting and updating database records              |
| Android SDK         | Android APIs and development tools                   |
| Draw.io / UML Tools | System design and UML diagrams                       |

---

## 🏗️ System Architecture

Kalasetu follows a modular Android application architecture in which different modules interact with a centralized SQLite database through the `DBHelper` class.

```text
                    KALASETU
                       |
          +------------+------------+
          |            |            |
        Admin        Artist        User
          |            |            |
          |            |            |
       Approve      Upload       Browse
       Artwork      Paintings    Paintings
          |            |            |
          +------------+------------+
                       |
               Painting Management
                       |
                Order Management
                       |
                  DBHelper
                       |
                 SQLite Database
                       |
          +------------+------------+
          |            |            |
        Users        Artists     Paintings
                       |
                     Orders
```

### Application Workflow

1. Artists register and log in to the application.
2. Artists upload paintings with the required details.
3. Newly uploaded paintings receive a `PENDING` status.
4. The administrator reviews the submitted paintings.
5. The administrator changes the painting status to `APPROVED` or `REJECTED`.
6. Users browse paintings approved by the administrator.
7. Users select paintings and place orders.
8. Artists view received orders and update their status.
9. The application stores relevant information in the SQLite database.

---

## 🗃️ Database Design

Kalasetu uses a local SQLite database named `KalaSetu.db`.

The database is managed through a custom `DBHelper` class that extends `SQLiteOpenHelper`.

**Database version:** 7

### 1. Users Table

Stores information about registered buyers.

| Column        | Description            |
| ------------- | ---------------------- |
| `user_id`     | Unique user identifier |
| `name`        | User's name            |
| `username`    | Login username         |
| `password`    | Login password         |
| `phone`       | Phone number           |
| `address`     | User's address         |
| `secret_code` | Account recovery code  |

### 2. Artists Table

Stores artist registration and account information.

| Column        | Description              |
| ------------- | ------------------------ |
| `artist_id`   | Unique artist identifier |
| `name`        | Artist's name            |
| `username`    | Artist login username    |
| `password`    | Login password           |
| `phone`       | Phone number             |
| `address`     | Artist's address         |
| `secret_code` | Account recovery code    |

### 3. Paintings Table

Stores paintings uploaded by artists.

| Column            | Description                 |
| ----------------- | --------------------------- |
| `painting_id`     | Unique painting identifier  |
| `artist_username` | Username of the artist      |
| `title`           | Painting title              |
| `description`     | Painting description        |
| `price`           | Painting price              |
| `image_uri`       | Painting image reference    |
| `status`          | Approval status             |
| `in_stock`        | Painting availability       |
| `created_at`      | Painting creation timestamp |

**Painting status:** `PENDING`, `APPROVED`, or `REJECTED`.

### 4. Orders Table

Stores painting purchase and order information.

| Column            | Description                         |
| ----------------- | ----------------------------------- |
| `order_id`        | Unique order identifier             |
| `painting_id`     | Reference to the purchased painting |
| `user_username`   | Buyer's username                    |
| `artist_username` | Artist's username                   |
| `status`          | Current order status                |
| `created_at`      | Order creation timestamp            |

---

## 🔐 Authentication and Approval Workflow

Kalasetu provides separate registration and login functionality for users and artists, along with administrative access.

The application checks login credentials against the corresponding database records. Account recovery is supported through a phone number and secret code.

Artwork approval is an important part of the application's workflow:

* **PENDING:** The painting has been uploaded and is awaiting review.
* **APPROVED:** The painting has been approved for display in the marketplace.
* **REJECTED:** The painting has been rejected by the administrator.

Only approved paintings are intended to be available for users to browse and purchase.

**Security note:** Database-backed login functionality is implemented, but production deployment should include password hashing, stronger authentication controls, and appropriate protection of personal information.

---

## 📱 Application Modules

The project is organized into the following modules:

1. User Management
2. Artist Management
3. Admin Management
4. Painting Management
5. Order Management
6. Database Management

This modular structure helps separate responsibilities and makes the application easier to maintain and extend.

---

## 🧪 Testing

The project documentation includes test cases for the main application workflows.

Testing covers:

* Valid and invalid admin login.
* Empty login credentials.
* Valid and invalid artist login.
* Painting uploads with valid and missing information.
* Admin approval of paintings.
* Display of approved paintings.
* Painting order placement.
* Viewing received orders.
* Updating order status.

The documented test cases report successful results for the scenarios listed in the project report.

---

## 🚀 Getting Started

Follow these steps to open and run the project locally.

### Prerequisites

Install the following software:

* Android Studio
* Android SDK
* Kotlin support for Android development
* An Android emulator or physical Android device

### Installation

**Step 1: Clone the repository**

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

Replace `<YOUR_GITHUB_REPOSITORY_URL>` with your actual GitHub repository URL.

**Step 2: Open the project**

1. Open Android Studio.
2. Select **Open**.
3. Choose the cloned Kalasetu project folder.
4. Wait for Gradle synchronization to complete.

**Step 3: Configure the Android SDK**

Ensure the required Android SDK and build tools are installed in Android Studio.

**Step 4: Run the application**

1. Connect an Android phone using USB debugging or start an Android emulator.
2. Select the target device in Android Studio.
3. Click **Run ▶**.
4. Wait for the application to build and install.

**Step 5: Initialize the database**

The application uses SQLite, with database creation and management handled through the `DBHelper` class when the database is initialized.

> Note: Exact SDK versions, Gradle requirements, and launch configuration depend on the project files.

---

## 🔮 Future Enhancements

The following improvements could extend the application's functionality:

* Online payments using UPI, debit cards, and credit cards.
* Delivery and shipment tracking.
* Painting ratings and customer reviews.
* AI-based artwork recommendations.
* Artist sales dashboards and analytics.
* Administrative reports and sales statistics.
* Push notifications for order updates and painting approvals.
* Wishlist and favorite paintings.
* Advanced search and filtering.
* Digital artwork certificates and authenticity tags.
* Multi-language support.
* Automated inventory management.
* Stronger authentication and password security.
* Cloud database integration for improved scalability.

These are proposed enhancements and should not be considered existing features unless implemented.

---

## 🎓 Learning Outcomes

The project demonstrates practical application of:

* Android application development using Kotlin.
* XML-based interface design.
* SQLite database design and management.
* Role-based application workflows.
* CRUD operations.
* Software requirement analysis and system design.
* UML diagrams and documentation.
* Application testing and debugging.

---

## 🏁 Conclusion

Kalasetu is a digital art marketplace designed to connect artists and buyers through an organized Android application.

By combining artist uploads, administrator-controlled artwork approval, painting discovery, and order management, the project demonstrates how mobile application development and local database management can address practical challenges in the art marketplace.

The modular design also provides opportunities for future improvements, including online payments, analytics, recommendations, and cloud integration.

---

## 👨‍💻 Project Information

| Field            | Details                             |
| ---------------- | ----------------------------------- |
| Project Name     | Kalasetu – Art Marketplace Platform |
| Project Type     | Android Application                 |
| Primary Language | Kotlin                              |
| UI Design        | XML                                 |
| Database         | SQLite                              |
| IDE              | Android Studio                      |
| College          | Bhavan's College, Mumbai            |
| Academic Year    | 2026                                |

---

## 📄 License

This project is intended for educational and academic purposes.

If you plan to distribute or reuse the source code publicly, consider adding an appropriate open-source license.

---

**⭐ If you find this project interesting, consider starring the repository!**
