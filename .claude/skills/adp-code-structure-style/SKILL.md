---
name: adp-code-structure-style
description: >
  Enforce AIAstro's coding conventions and architecture patterns when writing new code.
  This skill MUST trigger whenever Claude is asked to create new features, screens, composables,
  server routes, ViewModels, data classes, or any Kotlin code in the AIAstro project.
  Also trigger when modifying existing code — the same rules apply. Covers: feature-based folder
  structure, Compose design system reuse, server route patterns, shared module placement,
  ViewModel conventions, navigation, and general Kotlin style. If you're writing ANY code in
  this project, consult this skill.
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# AIAstro Code Style Enforcer

This project uses conventions specifically designed for AI-agent-friendly development. The core
idea: **feature locality** — everything an agent (or developer) needs to understand a feature lives
in one folder with one CLAUDE.md. This beats the traditional layered structure (ui/, viewModels/,
repositories/) because it eliminates cross-folder context gathering.

Internalize these patterns before writing any code.

## 1. Feature-Based Folder Architecture

### composeApp (Client)

Every feature gets its own folder under `composeApp/src/commonMain/kotlin/.../`:

```
featureName/
├── FeatureComposable.kt            # UI (screen composable)
├── FeatureViewModel.kt             # State management (if needed)
├── FeatureKtorApi.kt               # HTTP client calls (if needed)
├── FeaturePagingDataManager.kt     # Pagination (if needed)
├── FeatureComponents.kt            # Sub-components (if needed)
├── FeatureFunctions.kt             # Repository/business logic (if needed)
└── CLAUDE.md                       # Describes ALL files in this folder
```

**Co-locate everything**: UI, ViewModel, API client, repository logic — all in the same folder.
Never split a feature across separate `ui/`, `viewModels/`, `repositories/` folders.

### server (Backend)

Same principle — each feature is a folder:

```
featureName/
├── FeatureService.kt               # Business logic + DB operations
├── getFeatureItem.kt               # One route per file
├── createFeatureItem.kt            # One route per file
├── updateFeatureItem.kt            # One route per file
└── CLAUDE.md                       # Describes ALL files in this folder
```

Each route handler is its own file (not one massive `FeatureRoutes.kt`). The service class sits
alongside its routes.

### shared (Data Models)

Data classes and DTOs that both client and server need go in
`shared/src/commonMain/kotlin/.../classes/`. This is the only code that crosses the client/server
boundary.

```
shared/classes/
├── User.kt
├── Chat.kt
├── Notification.kt      # Used by both composeApp and server
└── ...
```

### CLAUDE.md Per Feature Folder

Every feature folder MUST have a `CLAUDE.md` that lists all files with one-line descriptions. This
is what gives AI agents instant context. Format:

```markdown
# Feature Name

Brief description of what this feature does.

## Files

- **FeatureComposable.kt** - Main screen with list and detail views
- **FeatureViewModel.kt** - State management, API calls, pagination
- **FeatureKtorApi.kt** - Ktor HTTP client for /getFeature, /updateFeature endpoints
```

---

## 2. Compose UI Conventions

### Text — Use Wrappers, Not Vanilla `Text()`

```kotlin
// CORRECT
HeadlineMedium(text = "Welcome")
BodyMedium(text = "Details here")
ErrorText(text = "Something went wrong")
SecondaryText(text = "Hint text")

// WRONG — never use vanilla Text()
Text(text = "Welcome", style = MaterialTheme.typography.headlineMedium)
```

Available: `HeadlineLarge`, `HeadlineMedium`, `HeadlineSmall`, `TitleLarge`, `TitleMedium`,
`TitleSmall`, `BodyLarge`, `BodyMedium`, `BodySmall`, `LabelLarge`, `LabelMedium`, `LabelSmall`,
`DisplayLarge/Medium/Small`, `ErrorText`, `SecondaryText`, `AutoResizableText`

### Buttons — Use Wrappers, Not Vanilla `Button()`

```kotlin
// CORRECT
PrimaryButton(text = "Submit", onClick = { viewModel.submit() }, isLoading = uiState.isSubmitting)
SecondaryButton(text = "Cancel", onClick = onCancel)

// WRONG
Button(onClick = { }) { Text("Submit") }
```

Available: `PrimaryButton`, `SecondaryButton`, `DrawTextButton`, `AIAstroIconButton`,
`SmallPrimaryButton`, `SmallSecondaryButton`, `ButtonWithLeadingIcon`, `ButtonWithTrailingIcon`

### Dialogs — Use `DrawBottomSheetAlert`, Not `AlertDialog`

```kotlin
// CORRECT
DrawBottomSheetAlert(
    showBottomSheet = showDialog,
    setShowBottomSheet = { showDialog = it },
    title = "Delete this item?",
    positiveButtonText = "Delete",
    negativeButtonText = "Cancel",
    positiveClick = { onDelete() }
)

// WRONG
AlertDialog(onDismissRequest = { }, title = { Text("Delete?") }, ...)
```

### Spacing — Use `AddSpace()`, Not `Spacer()`

```kotlin
// CORRECT — works in both Row and Column
AddSpace(sixteenDp)

// WRONG
Spacer(modifier = Modifier.height(16.dp))
```

### Dimensions — Use `dimens.kt` Values, Not Hardcoded dp

```kotlin
// CORRECT
Modifier.padding(paddingDefault)    // 16dp
Modifier.height(buttonHeightDefault) // 48dp
Modifier.size(avatarSizeMedium)

// WRONG
Modifier.padding(16.dp)
Modifier.height(48.dp)
```

Key values: `paddingTiny` (4dp), `paddingSmall` (8dp), `paddingDefault` (16dp), `paddingLarge`
(20dp), `paddingXLarge` (24dp), `spacingSmall` (8dp), `spacingDefault` (16dp), `cornerRadiusDefault`
(12dp), `cornerRadiusPill` (50dp)

### Colors — Use Theme, Not Hardcoded

```kotlin
// CORRECT
color = MaterialTheme.colorScheme.primary
color = MaterialTheme.colorScheme.onSurface

// WRONG
color = Color.Blue
color = Color(0xFF123456)
```

Primary (red) is for CTAs and interactive elements only — don't overuse it. Secondary (yellow) is
for UI accents — always pair with `MaterialTheme.colorScheme.onSecondary` for content on it.

### Loading States — Use `.placeholder()` Modifier

```kotlin
// CORRECT
BodyMedium(
    text = if (isLoading) "" else actualText,
    modifier = Modifier.fillMaxWidth().placeholder(visible = isLoading)
)

// WRONG — no custom shimmer/skeleton implementations
Box(modifier = Modifier.background(shimmerBrush))
```

### Toasts — Use Custom Toast System, Not Snackbar

```kotlin
// CORRECT
showShortToast("Saved successfully")
showLongToast("Please enter your name")
showDismissableToast("Important message")

// WRONG
snackbarHostState.showSnackbar("message")
```

### Shapes

```kotlin
// CORRECT
shape = MaterialTheme.shapes.medium
shape = RoundedCornerShape(cornerRadiusDefault)

// WRONG
shape = RoundedCornerShape(12.dp)
```

---

## 3. ViewModel Conventions

### Structure

```kotlin
@KoinViewModel
class FeatureViewModel(
    private val repository: FeatureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState = _uiState.asStateFlow()  // type inference, no explicit type

    // Direct functions — NO sealed class actions
    fun submit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // ...
        }
    }

    fun updateField(value: String) {
        _uiState.update { it.copy(fieldValue = value) }
    }
}

// Simple data class for state — NOT sealed class
data class FeatureUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: String = ""
)
```

Key rules:

- **`@KoinViewModel`** annotation on every ViewModel
- Register in `ViewModelModule.kt`
- **No sealed classes** for actions, events, or UI states — use direct function calls
- **Type inference preferred** — don't write `val uiState: StateFlow<FeatureUiState> =` when
  `.asStateFlow()` makes it obvious
- Use `_backing` + `.asStateFlow()` pattern

### Composable Usage — Stateless Content Pattern

```kotlin
@Composable
fun FeatureScreen(viewModel: FeatureViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureContent(
        uiState = uiState,
        onSubmit = viewModel::submit,
        onFieldChange = viewModel::updateField
    )
}

@Composable
private fun FeatureContent(
    uiState: FeatureUiState,
    onSubmit: () -> Unit,
    onFieldChange: (String) -> Unit
) {
    // Stateless — no ViewModel dependency here
}
```

---

## 4. Server Route Conventions

### Flat Routes Only

```kotlin
// CORRECT — flat, descriptive name, query params
get("/getUser") {
    val userId = call.request.queryParameters[KtorVariables.USER_ID]
}

// WRONG — no nesting, no path parameters
get("/api/v1/user/{id}")
```

### One Route Per File

Each route handler is its own `.kt` file:

```kotlin
// getNotifications.kt
fun Route.getNotifications() {
    val service by inject<NotificationService>()

    authenticated {
        get("/getNotifications") {
            val userId = call.authenticatedUser().uid
            val page = call.request.queryParameters[RequestKeys.PAGE]?.toIntOrNull() ?: 0
            val result = service.getNotificationsPaginated(userId, page, 20)
            call.respond(ApiResponse(success = true, data = result))
        }
    }
}
```

### Service Layer

- All DB operations go through a `FeatureService` class, never directly in routes
- Inject via Koin: `val service by inject<FeatureService>()`
- Register services in `util/KoinModule.kt`

### Auth

- Wrap routes in `authenticated { }` for Firebase token verification
- Use `call.authenticatedUser().uid` to get the user ID

---

## 5. Shared Module Rules

### Constants — Single File

ALL `const val` declarations go in `shared/.../utils/Constants.kt`. No constants scattered in other
files.

```kotlin
// CORRECT — in Constants.kt
const val MAX_MESSAGE_LENGTH = 2000

// WRONG — in some other file
private const val MAX_MESSAGE_LENGTH = 2000
```

### API Contracts

- Use `RequestKeys.FIELD_NAME` for JSON keys (from `ApiContracts.kt`)
- Use `KtorVariables.PARAM_NAME` for HTTP query parameter keys
- Use `ApiRoutes` for route path constants
- Never hardcode these strings

### Navigation Routes

Defined as `@Serializable` objects/data classes in `shared/.../navigation/NavRoutes.kt`:

```kotlin
@Serializable
object FeatureDestination

@Serializable
data class FeatureDetailDestination(val itemId: String)
```

When referencing destinations from strings (notifications, deep links):

```kotlin
// CORRECT — reflection
FeatureDestination::class.simpleName!!
FeatureDetailDestination::itemId.name

// WRONG — hardcoded
"FeatureDestination"
"itemId"
```

### Data Classes

All shared data classes use `kotlinx.serialization`:

```kotlin
@Serializable
data class Feature(
    @SerialName("_id") val id: String = "",
    var name: String = ""
)
```

Room entities additionally get `@Entity`, `@PrimaryKey`, `@Index` annotations.

---

## 6. General Kotlin Style

### Logging — Kermit Logger with Function Name

```kotlin
import co.touchlab.kermit.Logger

Logger.d { "fetchUser Debug: loaded $count items" }
Logger.e { "fetchUser Error: ${exception.message}" }

// WRONG
println("loaded items")
```

Always prefix log messages with the function name for filtering.

### Data Classes at Top of File

If a file has both data classes and functions, data classes come first.

### No KDoc Comments

```kotlin
// WRONG
/** Fetches user data. @param id The user ID @return User object */
fun fetchUser(id: String): User

// CORRECT — self-documenting names, brief inline comments only when logic is non-obvious
fun fetchUser(id: String): User
```

### No Sealed Classes

Don't use `sealed class` or `sealed interface` for actions, events, or UI states. Use direct
function calls on the ViewModel and simple data classes for state.

### Region Comments for Organization

```kotlin
// ==================== MAIN COMPOSABLE ====================

// ==================== CONTENT ====================

// ==================== UI COMPONENTS ====================
```

### String Resource Apostrophes

In `composeResources` XML strings, wrap in double quotes instead of escaping:

```xml
<!-- CORRECT -->
<string name="msg">"I don't know"</string>

    <!-- WRONG — backslash appears literally -->
<string name="msg">I don\'t know</string>
```

### IST Timezone

All time calculations use `TimeUtils` locked to `Asia/Kolkata`. Never use UTC or device-local
timezone for business logic.

### Astrologer Persona Name

Use `getAstrologerName(language)` — never hardcode "Navya" or "नव्या".

### App Name

Use `Strings.app_name.resolve()` for language-aware display — never hardcode "Astro Navya".
