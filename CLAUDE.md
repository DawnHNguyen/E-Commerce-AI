# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Role

You're an expert Android Kotlin Jetpack Compose developer, with more than 10 years of working experience in mobile development in the e-commerce domain

## Build and Run Commands

```bash
# Build the entire project
./gradlew build

# Run instrumentation tests
./gradlew connectedAndroidTest

# Run unit tests
./gradlew test

# Clean build
./gradlew clean build

# Run lint checks
./gradlew lint

# Install debug build on connected device
./gradlew installDebug

# Generate debug APK
./gradlew assembleDebug

# Generate release APK
./gradlew assembleRelease
```

## Project Architecture

This is an e-commerce Android application built using a modern architecture with the following key components:

### 1. Clean Architecture

The app follows Clean Architecture principles with clear separation between:

- **Presentation Layer**: Compose UI, ViewModels
- **Domain Layer**: Business logic, use cases, and repository interfaces
- **Data Layer**: Repository implementations, data sources, and API interfaces

### 2. Module Structure

The project is organized into multiple modules:
- `app`: Main application module, entry point
- `common`: Shared components, utilities, and constants
- `data`: Repository implementations, network APIs, and local storage
- `domain`: Business logic, entity models, and repository interfaces
- `navigation`: Navigation management and screen routing
- `feature`: Feature modules (auth, core, etc.)

### 3. Key Technologies

- **Kotlin**: Primary language
- **Jetpack Compose**: UI framework
- **Hilt**: Dependency injection
- **Retrofit**: Network communication
- **MMKV**: Key-value storage
- **SQLDelight**: Database
- **Jetpack Navigation**: Screen navigation with Compose integration
- **Flow/StateFlow**: Reactive programming

### 4. Data Flow Architecture

1. UI components observe StateFlow from ViewModels
2. ViewModels execute business logic via UseCase or Repository
3. Repositories coordinate between local and remote data sources
4. Network responses are mapped to domain entities
5. Results are wrapped in a `Resource<T>` class to handle loading, success, and error states

## Key Patterns and Conventions

### Resource Pattern

```kotlin
sealed class Resource<out T> {
    data object Idle : Resource<Nothing>()
    data class Loading<T>(val data: T? = null) : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val error: CustomException) : Resource<T>()
}
```

### ViewModels

ViewModels follow a standard pattern:
- Use Hilt `@HiltViewModel` annotation
- Expose UI state via StateFlow
- Handle user interactions through functions
- Update state with `_state.update { }` pattern

### Navigation

The app uses Jetpack Navigation Compose with:
- Route classes for type-safe navigation
- Composable destinations via `composable<Route>` extension
- Bottom navigation structure in MainActivity

### Component Styling

UI components in the common module should be used for consistent styling:
- `FilledButton`, `NeutralButton` for standard button styling
- Custom typography defined in `CustomTypography`
- Color system from `color_system.xml`

## Repository Structure

Repositories follow a standard interface-implementation pattern:
- Interface defined in `domain` module
- Implementation in `data` module
- Use Hilt for dependency injection through module bindings

## Error Handling

The app uses custom exceptions and error handling through the Resource pattern:
- Custom exceptions extend `CustomException`
- Network errors categorized by client/server error types
- UI displays appropriate error messages based on error type