# Testing Documentation for ECommerceAI

This document outlines the testing strategy and documentation for the ECommerceAI Android application.

## Test Structure

The application follows the standard Android testing approach with:

1. **Unit Tests** - Located in each module's `src/test/java` directory
2. **Instrumentation Tests** - Located in each module's `src/androidTest/java` directory

## Running Tests

```bash
# Run all unit tests
./gradlew test

# Run all instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run tests for a specific module
./gradlew :feature:core:test

# Run instrumentation tests for a specific module
./gradlew :app:connectedAndroidTest
```

## Test Coverage

### Unit Tests

| Module | Test Coverage Areas |
|--------|---------------------|
| app | Application configuration tests |
| common | Utility and component tests |
| data | Repository implementations, data source tests |
| domain | Business logic and domain entity tests |
| feature/auth | Authentication flow tests |
| feature/core | Payment configuration and UI state tests |
| navigation | Navigation route tests |

### Feature Tests

#### Payment Configuration

The payment configuration feature has extensive unit tests covering:

1. **CardNumberVisualTransformation** 
   - Formatting of different card number patterns
   - Offset mapping for cursor positioning
   - Handling of various card types

2. **PaymentConfigViewModel**
   - Card number validation
   - Expiration date validation
   - CVV validation
   - State management and updates
   - Default card handling

3. **PaymentConfigUiState**
   - Default state validation
   - Button activation logic
   - State copying and modification

### Instrumentation Tests

Basic instrumentation tests validate:
- Application context
- Component rendering
- UI interactions

## Mocking Strategy

The application uses Mockito for mocking dependencies in tests:

```kotlin
@Mock
private lateinit var paymentMethodRepository: PaymentMethodRepository

@Before
fun setUp() {
    MockitoAnnotations.openMocks(this)
    // Additional setup
}
```

## Testing Coroutines

For testing coroutines, the application uses the kotlinx-coroutines-test library:

```kotlin
private val testDispatcher = StandardTestDispatcher()

@Before
fun setUp() {
    // Setup code
    Dispatchers.setMain(testDispatcher)
}

@After
fun tearDown() {
    Dispatchers.resetMain()
}

@Test
fun `test coroutine functionality`() = runTest {
    // Test coroutines
    testDispatcher.scheduler.advanceUntilIdle()
}
```

## Testing Flow

For testing Flow, the application uses the Turbine library:

```kotlin
viewModel.saveCardState.test {
    val emission = awaitItem()
    assertTrue(emission is Resource.Idle)
}
```

## Test Naming Convention

Tests follow the backtick naming convention for better readability:

```kotlin
@Test
fun `updateCardNumber sets card type and validates correctly`()
```

## Future Test Coverage Areas

1. **Repository Integration Tests** - Test the interaction between repositories and their data sources
2. **UI Component Tests** - Test UI components in isolation
3. **End-to-End Feature Tests** - Test complete user flows from UI to data layer
4. **API Integration Tests** - Test integration with backend APIs

## Best Practices

1. Test one concept per test method
2. Use descriptive test names
3. Follow the Arrange-Act-Assert pattern
4. Mock external dependencies
5. Use test utilities for common test operations
6. Keep tests independent of each other