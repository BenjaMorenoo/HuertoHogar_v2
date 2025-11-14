# Huerto Hogar — Documentación Técnica

## Resumen

Huerto Hogar es una aplicación móvil de comercio electrónico para la venta de productos orgánicos y frescos. Ofrece una experiencia de usuario moderna y minimalista para explorar un catálogo, gestionar cuentas y realizar compras de forma segura.

## Tabla de contenido

- [Introducción](#introducción)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Arquitectura de la aplicación](#arquitectura-de-la-aplicación)
- [Gestión de datos con Room](#gestión-de-datos-con-room)
- [Funcionalidades principales](#funcionalidades-principales)
- [Dificultades y aprendizajes](#dificultades-y-aprendizajes)
- [Conclusiones y mejoras futuras](#conclusiones-y-mejoras-futuras)

## Introducción

**Propósito:** Huerto Hogar facilita la compra de productos orgánicos directamente desde productores locales. La app está orientada a usuarios interesados en alimentos saludables y en una experiencia móvil clara y rápida.

**Usuarios objetivo:** Consumidores conscientes de la alimentación saludable, que valoran interfaces móviles sencillas y procesos de compra transparentes.

## Tecnologías utilizadas

- **Lenguaje:** Kotlin (100% nativo)
- **UI:** Jetpack Compose (Material Design 3, tema verde personalizado)
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Persistencia local:** Room
- **Gestión de dependencias:** Gradle con catálogo (`libs.versions.toml`)
- **Navegación:** Jetpack Navigation for Compose
- **Asincronía:** Corrutinas + StateFlow

## Arquitectura de la aplicación

La aplicación sigue MVVM para separar la lógica de negocio de la UI.

- **Modelo:** Entidades y DAOs (Room). Ej.: `User.kt`, `Product.kt`, `AppDatabase.kt`, `UserDao.kt`.
- **Vista:** Composables de Jetpack Compose (ej.: `HomeScreen.kt`, `CartScreen.kt`, `ProfileScreen.kt`).
- **ViewModel:** Lógica de presentación, validaciones y comunicación con la capa de datos (ej.: `HomeViewModel.kt`, `CartViewModel.kt`).

Principios aplicados:

- Ciclo de vida consciente: los ViewModel sobreviven a cambios de configuración.
- Reactividad: uso de `StateFlow` para notificar la UI sobre cambios.

## Gestión de datos con Room

Room se usa para persistencia local (usuarios, carrito, productos). Permite consultas comprobadas en compilación y se integra con corrutinas y Flows.

### Ejemplo de entidad `User`

```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val address: String,
    val phone: String,
    val password: String
)
```

### Ejemplo de `UserDao`

```kotlin
@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
}
```

### Beneficios de Room

1. **Seguridad en compilación:** verifica consultas SQL en tiempo de compilación.
2. **Abstracción:** reduce boilerplate.
3. **Compatibilidad con corrutinas/Flow:** permite operaciones asíncronas y UI reactiva.

## Funcionalidades principales

- **Catálogo de productos:** exploración por categoría.
- **Búsqueda:** filtrado por nombre en tiempo real.
- **Detalles de producto:** modal con información completa.
- **Carrito:** añadir/modificar cantidades y ver desglose (subtotal, IVA, total).
- **Cuentas de usuario:** registro, inicio de sesión, actualización de perfil y cierre de sesión.
- **Flujo de compra:** pasarela simulada (requiere sesión), selección de dirección y confirmación.

## Dificultades y aprendizajes

- **Configuración de Gradle:** alinear versiones de Kotlin, KSP y Room fue crítico; limpiar cachés de Gradle ayudó a resolver problemas.
- **Animaciones en `LazyColumn`:** se ajustó el uso a `animateItemPlacement()` para evitar errores con APIs experimentales.

Lecciones clave:

- **StateFlow:** centraliza el estado en ViewModel y simplifica la UI.
- **Room + Flow:** actualizaciones automáticas de la UI cuando cambian los datos.
- **Testabilidad:** MVVM facilita pruebas aisladas de la lógica.

## Conclusiones y mejoras futuras

La app ya es funcional y escalable. Siguientes mejoras propuestas:

1. **Cargar imágenes desde URLs** para reemplazar placeholders.
2. **Persistir sesión** con DataStore.
3. **Backend real** (por ejemplo Firebase) para centralizar datos.
4. **Notificaciones push** para informar a usuarios.

Cambios de proceso sugeridos al continuar el desarrollo:

- Definir desde el inicio las dependencias (p. ej. `material-icons-extended`).
- Probar animaciones en pantallas de ejemplo antes de integrarlas en producción.

---
