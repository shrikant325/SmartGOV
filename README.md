SmartGov Connect – Government Scheme Recommendation System

**SmartGov Connect** is an Android application designed to help citizens discover government schemes relevant to their needs, preferences, and eligibility criteria.

The application uses a **rule-based recommendation system** to filter government schemes according to user information, selected categories, government level, state, and income requirements where applicable.

By organizing scheme information in one place, SmartGov Connect aims to reduce manual searching and make government welfare information easier to discover.

---

## 📌 Project Overview

India offers numerous government schemes covering education, agriculture, healthcare, employment, financial assistance, and social welfare. However, identifying the schemes relevant to an individual's circumstances can be difficult because eligibility requirements and application information vary between schemes.

SmartGov Connect addresses this problem through a personalized Android application that matches user profiles with available government scheme records.

Users can create profiles, select their interests, view recommended schemes, explore all available schemes, review eligibility requirements, and access application links.

An administrator module allows authorized administrators to add and manage scheme information.

### 🎯 Project Objectives

* Simplify the discovery of government welfare schemes.
* Provide personalized scheme recommendations based on user profiles.
* Filter schemes according to categories, government level, state, and applicable income limits.
* Display scheme benefits and eligibility requirements.
* Provide direct access to scheme application links.
* Maintain government scheme information through an administrator module.
* Evaluate the effectiveness of a transparent, rule-based recommendation approach.

---

## ✨ Key Features

### 👤 1. User Registration and Authentication

* Create a user account.
* Log in using registered credentials.
* Reset forgotten passwords.
* Access personalized application features after authentication.

### 📝 2. User Profile Management

* Maintain personal profile information.
* Select preferred government scheme categories.
* Store relevant information such as age, state, and income where applicable.
* Update selected priorities and profile information.
* Use profile information to personalize scheme recommendations.

### 🎯 3. Personalized Scheme Recommendations

* Recommend schemes based on selected categories and priorities.
* Match schemes against applicable government levels.
* Filter state-specific schemes according to the user's state.
* Apply income eligibility conditions where relevant.
* Remove duplicate scheme entries.
* Display matching schemes in the application.

### 🔍 4. Browse All Government Schemes

* Explore available government schemes.
* Browse schemes beyond the initial recommendations.
* View scheme categories and related information.
* Search for schemes using the available search functionality.

### 📄 5. Scheme Details

Users can view available information about individual schemes, including:

* Scheme name and category.
* Central or State Government classification.
* Applicable state.
* Eligibility requirements.
* Benefits offered.
* Income criteria, where applicable.
* Scheme-related images.
* Application links.

### 🛡️ 6. Administrator Module

* Access the administrator interface.
* Add new government schemes.
* View available schemes.
* Update existing scheme information.
* View registered users.
* Maintain structured scheme records.

### ☁️ 7. Firebase Integration

* Firebase Authentication for user account management.
* Cloud Firestore for storing and retrieving user profiles and scheme information.
* Centralized cloud-based data management.

### 🔔 8. Notification-Related Functionality

The project includes notification-related functionality intended to improve awareness of newly available schemes that may be relevant to users.

The actual notification behavior depends on the functionality implemented in the application.

---

## 🛠️ Technologies Used

| Technology              | Purpose                                            |
| ----------------------- | -------------------------------------------------- |
| Kotlin                  | Android application logic and recommendation rules |
| Android Studio          | Android application development                    |
| XML                     | User interface layouts                             |
| Firebase Authentication | User registration, login, and password recovery    |
| Cloud Firestore         | Cloud database for user and scheme information     |
| Material Design         | Interface components and visual consistency        |
| Glide                   | Loading and displaying scheme-related images       |
| Android SDK             | Android development APIs and tools                 |

---

## 🏗️ System Architecture

SmartGov Connect follows a layered application architecture consisting of the Android interface, application logic, and cloud services.

```text
                  SMARTGOV CONNECT
                          |
             +------------+------------+
             |                         |
         User Module             Admin Module
             |                         |
       User Registration          Admin Login
       User Login                 Add New Scheme
       Profile Management         View All Schemes
       Select Priorities          Update Schemes
             |                    View Users
             |                         |
             +------------+------------+
                          |
                  APPLICATION LAYER
                          |
              Rule-Based Recommendation
                          |
             +------------+------------+
             |                         |
     Firebase Authentication     Cloud Firestore
             |                         |
      User Authentication       Users Collection
      Account Management        Schemes Collection
                                       |
                              Scheme Information
                                       |
                              Eligibility Filtering
                                       |
                              Recommended Schemes
                                       |
                              Scheme Details
                                       |
                              Application Links
```

### Application Workflow

1. The user registers or logs in.
2. The user provides profile information and selects scheme priorities.
3. The application retrieves scheme records from Cloud Firestore.
4. The recommendation module compares scheme information with the user's profile.
5. Applicable category, government-level, state, and income conditions are evaluated.
6. Duplicate scheme records are removed.
7. Matching schemes are displayed to the user.
8. The user opens a scheme to view its details and application link.

---

## 🧠 Recommendation Algorithm

SmartGov Connect uses a **rule-based recommendation algorithm** rather than a machine-learning model.

The system applies predefined conditions to identify government schemes that may be relevant to a user.

### Input Parameters

* User-selected categories or priorities.
* User's state.
* User's income, where applicable.
* Government scheme records stored in Cloud Firestore.

### Algorithm Steps

1. Retrieve the user's profile and selected priorities.
2. Fetch the available scheme records from Cloud Firestore.
3. Compare each scheme's category with the user's selected categories.
4. Exclude schemes that do not match the applicable category conditions.
5. Identify the scheme's government level.
6. For state-specific schemes, compare the scheme's target state with the user's state.
7. For income-based schemes, compare the user's income with the defined income limit.
8. Add schemes satisfying the applicable conditions to the recommendation list.
9. Remove duplicate records using unique Firestore document identifiers.
10. Sort matching schemes by creation time, where applicable.
11. Display the resulting recommendations.

### Recommendation Flow

```text
         User Profile
              |
              v
      Retrieve Scheme Data
              |
              v
      Category Matching
              |
              v
    Government-Level Check
              |
              v
       State Eligibility
              |
              v
      Income Eligibility
        Where Applicable
              |
              v
      Remove Duplicates
              |
              v
       Sort Matching Data
              |
              v
     Recommended Schemes
```

**Important:** Recommendations depend on the scheme information and eligibility rules stored in the application. They are intended to assist scheme discovery and do not constitute official confirmation of eligibility.

---

## 🗄️ Database Design

SmartGov Connect uses **Cloud Firestore**, a cloud-hosted NoSQL database.

The project report identifies two primary collections.

### 1. Users Collection

Stores user account and profile information used for personalization.

Depending on the fields implemented, user records may contain:

| Field               | Description                          |
| ------------------- | ------------------------------------ |
| Name                | User's name                          |
| Email               | User's email address                 |
| State               | User's state                         |
| Selected Priorities | Preferred scheme categories          |
| Age                 | User's age                           |
| Income              | Income information, where applicable |

Firebase Authentication manages authentication credentials separately from the user profile records stored in Firestore.

### 2. Schemes Collection

Stores government scheme information used by the recommendation system.

| Field         | Description                    |
| ------------- | ------------------------------ |
| `id`          | Scheme identifier              |
| `title`       | Name of the scheme             |
| `domain`      | Scheme category                |
| `level`       | Central or State Government    |
| `state`       | Applicable state               |
| `eligibility` | Eligibility requirements       |
| `benefits`    | Benefits offered               |
| `applyLink`   | Application URL                |
| `imageUrl`    | Scheme image URL               |
| `incomeCap`   | Income limit, where applicable |
| `createdAt`   | Record creation timestamp      |

The application compares user profile information with the attributes of scheme records to identify potentially relevant recommendations.

---

## 📱 Application Screens

The project report documents the following user interface screens.

### User Interface

* Splash Screen
* About Page
* Privacy Policy Screen
* Login Screen
* Registration Screen
* Select Priority Screen
* Home Screen
* User Profile Screen
* All Schemes Screen
* Scheme Details Screen
* Reset Password Screen

### Administrator Interface

* Admin Login Screen
* Admin Home Screen
* Add Scheme Screen
* View All Schemes Screen
* View Users Screen
* View User Details Screen

These screens support the main workflows of account management, scheme discovery, personalized recommendations, and scheme administration.

---

## 🚀 Getting Started

Follow these steps to set up the project locally.

### Prerequisites

Install the following:

* Android Studio
* Android SDK
* Kotlin support
* A Google account for Firebase setup
* An Android emulator or physical Android device

### Step 1: Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

Replace `<YOUR_GITHUB_REPOSITORY_URL>` with your repository's actual URL.

### Step 2: Open the Project

1. Launch Android Studio.
2. Select **Open**.
3. Choose the cloned SmartGov Connect project folder.
4. Wait for Gradle synchronization to finish.

### Step 3: Configure Firebase

The application uses Firebase Authentication and Cloud Firestore.

1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Create or select the Firebase project configured for the application.
3. Register the Android application using its actual package name.
4. Download the corresponding `google-services.json` file.
5. Place the file in the Android app module directory, normally `app/`.
6. Enable Firebase Authentication methods required by the application.
7. Create the required Cloud Firestore database.
8. Configure Firestore collections and security rules according to the application's data structure.

**Important:** Use your own Firebase project configuration. Never upload service-account private keys, passwords, or other confidential credentials to GitHub.

### Step 4: Build and Run

1. Allow Android Studio to synchronize the project.
2. Resolve any missing SDK or dependency requirements.
3. Connect an Android device with USB debugging enabled, or launch an emulator.
4. Select the target device.
5. Click **Run ▶** to build and install the application.

> The exact Gradle version, Android SDK requirements, Firebase configuration, and launch settings depend on the source project.

---

## 🧪 Testing and Evaluation

The project follows a system-based testing approach using different user profiles and scheme eligibility conditions.

Testing can cover the following scenarios:

| Test Scenario                         | Expected Result                         |
| ------------------------------------- | --------------------------------------- |
| User registration with valid details  | Account is created successfully         |
| Login with valid credentials          | User can access the application         |
| Login with invalid credentials        | Access is denied                        |
| Update user profile                   | Relevant profile information is updated |
| Select scheme categories              | Selected priorities are saved           |
| Match a scheme category               | Matching schemes are considered         |
| Check a state-specific scheme         | State eligibility is evaluated          |
| Check an income-based scheme          | Income condition is evaluated           |
| Browse all schemes                    | Available scheme records are displayed  |
| View scheme details                   | Scheme information is displayed         |
| Open an application link              | The configured link is opened           |
| Add a scheme through the admin module | Scheme information is saved             |
| Update an existing scheme             | Updated information is reflected        |
| Process duplicate scheme records      | Duplicate entries are excluded          |

The expected results should be verified against the actual application during testing.

---

## ⚠️ Limitations

The current project has the following limitations:

* Recommendations depend on the accuracy and completeness of stored scheme information.
* Eligibility is evaluated only against the conditions implemented in the application.
* Scheme records require maintenance to reflect changes in government policies.
* The system does not automatically verify eligibility with every government department.
* The recommendation mechanism uses predefined rules rather than machine learning.
* Recommendation results do not guarantee that a user will qualify for or receive a government benefit.
* The current implementation does not claim to cover every government scheme available in India.

Users should verify the latest eligibility requirements and application procedures through the relevant official government sources.

---

## 🔮 Future Enhancements

The project could be extended with the following features:

* Integration with verified government scheme data sources.
* Automatic notifications about newly added or updated schemes.
* Advanced search and filtering options.
* More detailed eligibility rules.
* Support for additional regional languages.
* Improved accessibility for users with disabilities.
* Personalized recommendations using machine learning.
* User feedback and recommendation-quality evaluation.
* Improved administrative analytics and reporting.
* Automatic identification of outdated scheme records.
* Enhanced security and privacy controls.
* More comprehensive testing using real-world user profiles.

These are potential improvements and should not be considered implemented features unless they are present in the application.

---

## 🎓 Learning Outcomes

The project demonstrates practical application of:

* Android application development using Kotlin.
* XML-based interface design.
* Firebase Authentication integration.
* Cloud Firestore database operations.
* User profile management.
* Rule-based recommendation algorithms.
* Eligibility-based filtering.
* Modular application development.
* Functional testing and evaluation.
* System architecture and technical documentation.

---

## 👨‍💻 Project Information

| Field                    | Details                                 |
| ------------------------ | --------------------------------------- |
| Project Name             | SmartGov Connect                        |
| Project Title            | Government Scheme Recommendation System |
| Project Type             | Android Application                     |
| Programming Language     | Kotlin                                  |
| User Interface           | XML                                     |
| Authentication           | Firebase Authentication                 |
| Database                 | Cloud Firestore                         |
| Development Environment  | Android Studio                          |
| Recommendation Technique | Rule-Based Matching                     |
| Academic Course          | B.Sc. Computer Science                  |
| College                  | Bhavan's College, Andheri West, Mumbai  |
| Academic Year            | 2026–2027                               |

---

## 📄 License

This project is intended for educational and academic purposes.

If you plan to distribute or reuse the source code publicly, consider adding an appropriate open-source license after reviewing your project's ownership and sharing requirements.

---

**⭐ If you find this project useful, consider starring the repository!**
