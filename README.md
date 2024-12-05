
# Forecastery - Compose Clean Architecture with Hilt

A modern Android application demonstrating a clean architecture approach, Jetpack Compose, and Hilt for dependency injection. This repository showcases best practices in Android development for building scalable, testable, and maintainable applications.

---

## ✨ Features

- **Jetpack Compose**: Fully declarative UI powered by Jetpack Compose.
- **Clean Architecture**: Separation of concerns with domain, data, and presentation layers.
- **Dependency Injection**: Powered by Hilt for easy and efficient dependency management.
- **Coroutines**: Asynchronous programming and reactive streams.
- **Material Design 3**: Adheres to modern UI/UX principles.
- **Error Handling**: Graceful error management with reusable patterns.

---

## 🚀 Technologies Used

- **Kotlin**: Primary programming language.
- **Jetpack Compose**: For declarative UI development.
- **Hilt**: Dependency injection framework.
- **Coroutines**: For background tasks and threading.
- **Retrofit**: Networking library for API communication.
- **Material Design 3**: Modern UI components and theming.

---

## 🛠 Architecture

This project follows **Clean Architecture**, ensuring the following:

1. **Presentation (UI) Layer**:
   - Contains `ViewModels` and UI-related logic using Jetpack Compose.
   - Handles state and events for the user interface.

2. **Domain Layer**:
   - Contains business logic, use cases, and interfaces.
   - Independent of any framework or platform-specific implementations.

3. **Data Layer**:
   - Handles data sources (e.g., REST APIs, databases).
   - Implements repository interfaces defined in the domain layer.

---

## 📂 Project Structure

```
Forecastery_Jetpack_Compose_Clean_Arch_Hilt/
├── app/                      # Application module
│   ├── base/                 # Dependency injection setup, Utilities
│   ├── data/                 # Data layer (repositories, data sources)
│   ├── domain/               # Domain layer (use cases, interfaces)
│   ├── ui/                   # Presentation layer (UI, ViewModels, states)
└── build.gradle              # Build configuration
```

---

## 📱 App Screens & Features

1. **Home Screen**
   - Location Permission: If location access is required, users are prompted to grant permissions, and if denied, they are shown a custom dialog that directs them to the app settings to enable permissions.
   - Weather Overview: The main screen of the app shows the weather details for the user’s location or a selected district. It provides essential information like temperature, humidity, weather conditions, etc., in a clear, easy-to-read format.
   - Search Functionality: Users can search for a specific district to get weather details by tapping the search icon (magnifying glass) at the bottom. This opens the Search Screen where users can search and select a district.
   - Pull-to-Refresh: The screen supports pull-to-refresh to fetch the latest weather data.
   - Error Handling: In case of an error while fetching weather data, a dialog is displayed to notify the user of the failure, and retrying is possible.

2. **Search Screen**
   - District Search: This screen allows users to search for a specific district (e.g., city, town) to get weather information. It contains a search bar at the top where users can type in the district name.
   - District List: A list of districts is displayed below the search bar. The list is filtered as the user types in the search bar, and it allows selecting a district to view its weather.
   - Current Location Option: The list also includes an option to use the user’s current location for weather details.

---

## 🛡 SOLID Principles in Action

### Single Responsibility Principle (SRP)
- **Definition**: A class should have only one reason to change.
- **Application**:
  - Each class or component in the architecture has a distinct responsibility:
    - **ViewModel**: Manages UI-related state and handles user interactions.
    - **Use Case**: Encapsulates a single piece of business logic (e.g., fetching weather data).
    - **Repository**: Abstracts data sources and centralizes data handling.
    - **API**: Handles network communication.
  - This separation ensures that changes in one layer don’t affect others unnecessarily.

### Open-Closed Principle (OCP)
- **Definition**: Software entities should be open for extension but closed for modification.
- **Application**:
  - Adding new features (e.g., caching data or a new weather data source) can be done by extending the Repository implementation or introducing a new Use Case without modifying existing code.
  - The sealed class (e.g., `WeatherState`) for state management allows extending new states without directly altering existing states.

### Liskov Substitution Principle (LSP)
- **Definition**: Subtypes must be substitutable for their base types.
- **Application**:
  - The `WeatherRepository` interface ensures any implementation (e.g., network-based, local database) can replace the default implementation (`WeatherRepositoryImpl`) without breaking the application logic.
  - The domain layer depends only on abstractions (e.g., `WeatherRepository`), not concrete implementations.

### Interface Segregation Principle (ISP)
- **Definition**: Clients should not be forced to depend on methods they do not use.
- **Application**:
  - The `WeatherRepository` interface defines only the specific methods required by the domain layer (e.g., `getWeather()`), avoiding unnecessary methods.
  - Each layer has its specific responsibilities and interfaces, keeping them focused and minimal.

### Dependency Inversion Principle (DIP)
- **Definition**: High-level modules should not depend on low-level modules. Both should depend on abstractions.
- **Application**:
  - The ViewModel depends on the Use Case, not the repository directly.
  - The Use Case depends on the `WeatherRepository` abstraction, not the concrete implementation.
  - Hilt injects dependencies, ensuring high-level modules are decoupled from low-level details.

---

## 📦 Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/maksudmubin/Forecastery_Jetpack_Compose_Clean_Arch_Hilt.git
   ```
2. Open the project in **Android Studio** (Arctic Fox or later).
3. Sync the project with Gradle files.
4. Run the app on an emulator or physical device.

---

## 📝 Usage

- Explore the `ui` package to understand UI development with Jetpack Compose.
- Check out the `domain` layer to see how use cases and business logic are structured.
- Dive into the `data` layer for REST API and database implementations.

---

## 💬 Contact

For any queries or suggestions, feel free to reach out:

- **Md. Maksudur Rahaman**
- [LinkedIn Profile](https://www.linkedin.com/in/maksudmubin/)
- [GitHub Profile](https://github.com/maksudmubin)
