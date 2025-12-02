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

- **Kotlin**: Primary language (targeting JVM 11)
- **Jetpack Compose**: UI framework (BOM 2025.04.00)
- **Hilt**: Dependency injection (v2.52)
- **Retrofit**: Network communication with GSON converter
- **MMKV**: Key-value storage (v1.3.11)
- **SQLDelight**: Database with coroutines support (v2.0.2)
- **Jetpack Navigation**: Screen navigation with Compose integration (v2.8.9)
- **Flow/StateFlow**: Reactive programming
- **Paging 3**: Pagination support (v3.3.6)
- **Glide Compose**: Image loading (v1.0.0-beta01)
- **Recurly SDK**: Payment processing
- **Biometric**: Authentication (v1.4.0-alpha03)

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

The Resource class includes companion functions (`success()`, `error()`, `loading()`, `idle()`), extension functions for state handling (`onIdle`, `onLoading`, `onSuccess`, `onError`), and data transformation with the `map()` function.

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
- Custom exceptions extend `CustomException` (including `CustomRemoteException`, `UnknownException`)
- Network errors categorized by client/server error types via `BaseErrorResponse` mapping
- UI displays appropriate error messages based on error type
- Repository implementations handle API errors and map to domain exceptions

## Additional Architecture Details

### Secure Storage
- MMKV used for secure key-value storage (access tokens, refresh tokens, user ID)
- Storage keys defined in `SecureStorageKey` constants
- Token management handled in repository implementations

### Payment Integration
- Recurly SDK integration for payment processing
- Card validation and tokenization in ViewModels
- Payment method persistence via local storage
- Biometric authentication support for secure payments

### API Architecture
- Base response patterns: `BaseSuccessResponse` and `BaseErrorResponse`
- Automatic token refresh via `RefreshTokenAuthenticator`
- Authorization headers added via `HeaderAuthorizationInterceptor`
- Call adapter factory for Resource wrapping