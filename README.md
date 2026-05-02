# BizSync

BizSync is an Android mobile application designed to support workforce management in small and medium-sized businesses.

The app centralizes shift planning, employee availability, absence requests, and electronic clock-in management. It helps managers reduce manual errors through automated validation, department coverage checks, and AI-assisted shift scheduling.

## Main Features

- Manager dashboard with daily business overview
- Weekly shift planning and publication workflow
- Department-based employee organization
- Manual shift creation with conflict and availability checks
- AI-assisted shift generation based on department coverage
- Absence request management with approval rules
- Electronic clock-in system for employees
- Virtual employee badge with QR code
- Geolocation-based validation for on-site shifts
- Local data storage and synchronization with the server

## Technologies Used

- Kotlin
- Jetpack Compose
- Firebase
- Room Database
- Gradle Kotlin DSL
- Clean Architecture

## Project Structure

```text
BizSync/
├── app/                 # Main Android application module
├── backend/             # Backend and remote data management logic
├── cache/               # Local persistence and cache management
├── domain/              # Business logic, models, and use cases
├── sync/                # Data synchronization module
├── ui/                  # User interface components and screens
├── gradle/              # Gradle configuration files
├── build.gradle.kts     # Root Gradle build configuration
├── settings.gradle.kts  # Gradle project settings
├── gradle.properties    # Global Gradle properties
├── gradlew              # Gradle wrapper for Unix systems
├── gradlew.bat          # Gradle wrapper for Windows
└── README.md            # Project documentation
```

## Architecture

BizSync follows a **Clean Architecture** approach. The project is divided into multiple modules to separate responsibilities and improve maintainability, scalability, and testability.

The main architectural layers are:

- **UI layer**: manages screens, components, and user interaction.
- **Domain layer**: contains business rules, entities, and use cases.
- **Data/cache layer**: handles local storage and persistence.
- **Backend layer**: manages communication with remote services.
- **Sync layer**: coordinates synchronization between local and remote data.

This structure makes the application easier to extend with future features such as payroll management, task assignment, and company hierarchy support.

## Getting Started

### Prerequisites

Before running the project, make sure you have installed:

- Android Studio
- JDK compatible with the Android Gradle Plugin
- Gradle Wrapper included in the project
- A configured Firebase project

### Installation

1. Clone the repository:

```bash
git clone https://github.com/<username>/<repository-name>.git
```

2. Open the project in Android Studio.

3. Sync the Gradle project.

4. Configure Firebase by adding the required Firebase configuration file to the Android project.

5. Build and run the application on an emulator or physical Android device.

## Future Improvements

Possible future extensions include:

- Payroll and salary management
- Task assignment for departments and employees
- Advanced company hierarchy management
- Improved reporting and analytics
- Extended AI-based planning support

## License

This project is currently developed for academic and educational purposes.
