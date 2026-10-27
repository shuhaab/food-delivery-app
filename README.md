# Food Ordering & Delivery Management System

A JavaFX desktop application that brings food ordering, vendor management, delivery tasks, and administration into one system. Built as a Java assignment, it provides dedicated dashboards for customers, vendors, delivery runners, administrators, and managers.

## Features

| Role | Features |
| --- | --- |
| Customer | Browse vendor menus, manage a shopping cart, place delivery/dine-in/takeaway orders, view order and transaction history, request credit top-ups, submit reviews and complaints, and view notifications. |
| Vendor | Manage menu items and prices, accept orders, view order history and customer reviews, monitor revenue, and receive notifications. |
| Delivery runner | Accept or decline delivery tasks, mark deliveries complete, view task history and customer reviews, and monitor revenue. |
| Administrator | Create, edit, and delete customer, vendor, and runner records, and approve customer credit top-up requests. |
| Manager | Review vendor and runner performance, respond to and resolve complaints, issue complaint-related credit refunds, and remove vendor items. |

## Tech Stack

- **Java** for application logic and object-oriented data models.
- **JavaFX 23** for desktop controls and dashboards.
- **FXML and CSS** for layouts and styling.
- **Maven** for dependency management and running the application.
- **org.json** for reading and writing local JSON data.

## Getting Started

### Prerequisites

- JDK 23 to match the project's JavaFX 23 dependency.
- Apache Maven, available as `mvn` in your terminal.

The Maven compiler configuration targets Java 11 bytecode; this does not mean the JavaFX 23 application can run on Java 11.

### Run Locally

1. Clone or download this repository.
2. Open a terminal in the project root, where `pom.xml` is located.
3. Check your Java and Maven installations:

   ```bash
   java -version
   mvn -version
   ```

   Ensure Maven uses the intended JDK; check the Java version shown by `mvn -version`.

4. Start the application:

   ```bash
   mvn clean javafx:run
   ```

   Maven downloads the declared dependencies on the first run.

5. Sign in using an existing account from `src/main/resources/com/g25/db/users.txt`. The login matches the account's `name` and `password`, then opens the dashboard for its `role`.

Run from the project root: data file paths are relative to that directory.

## Project Structure

```text
.
├── pom.xml
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── com/g25/
    │       ├── App.java                 # JavaFX entry point and scene switching
    │       ├── LoginController.java     # Login and role-based navigation
    │       ├── *Controller.java         # Dashboards for the five user roles
    │       ├── db/                      # Data access classes and CRUD interfaces
    │       ├── customcomponent/         # Shared UI components
    │       ├── admin/                   # Administrator view models
    │       ├── runner/                  # Delivery task models
    │       ├── vendor/                  # Vendor item models
    │       ├── Shared/                  # Shared complaint model
    │       ├── Icons/                   # Custom icons
    │       └── utils/                   # Date formatting helper
    └── resources/com/g25/
        ├── *.fxml                       # Login and dashboard layouts
        ├── *.css                        # Application styles
        ├── burger.jpg                   # Image asset
        └── db/*.txt                     # JSON data files
```

## Data Storage

The application stores JSON arrays in `.txt` files under `src/main/resources/com/g25/db/`. These contain users, role-specific records, menu items, orders, reviews, complaints, notifications, and credit top-up requests. No separate database server is required.

The shared `DB` class handles reading and writing these files. Role-specific data classes provide operations such as creating orders, updating credits, assigning delivery tasks, and recording reviews.

Application actions update these files directly. Keep a backup of the supplied data if you want to restore the initial demonstration state after testing.

## Scope & Limitations

This is a local academic demonstration. Credits and refunds are recorded within the application; there is no external payment gateway integration.

Passwords are stored and compared in plain text. Use demonstration accounts only. Production use would require password hashing, stronger authorization, and a storage layer designed for concurrent access.

## Development Notes

- Update layouts in `src/main/resources/com/g25/*.fxml` and styles in the corresponding CSS files.
- Dashboard behavior lives in the role-specific controllers under `src/main/java/com/g25/`.
- Data access and persistence logic live in `src/main/java/com/g25/db/`.
- Data files contain JSON despite their `.txt` extension; preserve valid JSON when editing them manually.
