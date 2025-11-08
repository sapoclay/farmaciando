# 💊 FarmaCiando - Sistema de Gestión de Farmacia

Sistema de escritorio multiplataforma para la gestión integral de farmacias, desarrollado con **JavaFX** y **Spring Boot**.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-blue?style=flat)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen?style=flat&logo=spring)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=flat&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat)

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Requisitos del Sistema](#-requisitos-del-sistema)
- [Instalación](#-instalación)
- [Ejecución](#-ejecución)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologías](#-tecnologías)
- [Base de Datos](#-base-de-datos)
- [Distribución](#-distribución)
- [Contribución](#-contribución)
- [Licencia](#-licencia)

---

## ✨ Características

✅ **Sistema de Autenticación de Usuarios**
- Login seguro con usuario y contraseña
- Contraseñas encriptadas con BCrypt
- Dos roles: Administrador y Cajero
- Usuario admin por defecto: `admin` / `admin123`
- Todas las ventas asociadas al usuario que las realiza

✅ **Gestión de Usuarios (Solo Administrador)**
- Crear, editar y desactivar usuarios
- Cambiar contraseñas
- Asignar roles (ADMIN / CAJERO)
- Ver historial de accesos
- Control de usuarios activos/inactivos

✅ **Gestión Completa de Inventario**
- Control de stock con alertas automáticas
- Registro de productos con múltiples atributos
- Alertas de productos próximos a caducar
- Búsqueda y filtrado avanzado

✅ **Sistema de Ventas**
- Punto de venta intuitivo
- Múltiples métodos de pago (Efectivo, Tarjeta, Transferencia)
- Historial completo de ventas con usuario responsable
- Cálculo automático de totales e IVA

✅ **Gestión de Clientes**
- Registro de clientes con historial de compras
- Búsqueda rápida por nombre o documento
- Estadísticas de consumo
- Productos favoritos por cliente

✅ **Gestión de Proveedores y Pedidos**
- Base de datos completa de proveedores
- Creación de pedidos de compra
- Seguimiento de estado de pedidos
- Contacto y condiciones comerciales

✅ **Sistema de Alertas Inteligente**
- Alertas de stock bajo (< 10 unidades)
- Productos caducados o próximos a caducar (30 días)
- Pedidos pendientes y retrasados
- Actualización automática cada 2 minutos

✅ **Reportes y Gráficos**
- Evolución de ventas diarias (gráfico de línea)
- Top 10 productos más vendidos (gráfico de barras)
- Distribución por método de pago (gráfico circular)
- Filtrado por fechas personalizado

✅ **Interfaz Moderna**
- Diseño intuitivo con JavaFX
- Pestañas organizadas por módulos
- Iconos y badges informativos
- Responsive y adaptable

---

## Sistema de Autenticación

### Inicio de Sesión

Al ejecutar la aplicación, se muestra primero una **ventana de login** donde debes autenticarte:

**Credenciales por defecto:**
- **Usuario:** `admin`
- **Contraseña:** `admin123`
- **Rol:** Administrador

> ⚠️ **IMPORTANTE:** Cambia la contraseña del administrador en el primer inicio de sesión.

### Roles y Permisos

#### 👑 Administrador (ADMIN)
- **Acceso total** a todos los módulos
- **Gestión de usuarios:** Crear, editar, desactivar usuarios y cambiar contraseñas
- **Pestaña exclusiva:** "👥 Usuarios"
- Puede realizar todas las operaciones del cajero

#### 💼 Cajero (CAJERO)
- **Acceso a módulos operativos:**
  - ✅ Inventario (consulta y edición)
  - ✅ Ventas (realizar ventas)
  - ✅ Clientes (consulta y registro)
  - ✅ Proveedores y Pedidos
  - ✅ Reportes
  - ✅ Alertas
- **Sin acceso a:** Gestión de usuarios

### Trazabilidad

- Todas las **ventas quedan asociadas** al usuario que las realizó
- Se registra la **fecha y hora del último acceso** de cada usuario
- El **nombre del usuario** aparece en el título de la ventana principal
- La **columna "Usuario"** en el historial de ventas muestra quién realizó cada venta

---

## ���️ Requisitos del Sistema

### Software Necesario:

| Componente | Versión Mínima | Recomendada | Verificar |
|------------|----------------|-------------|-----------|
| **Java JDK** | 21.0.0 | 21.0.8 LTS | `java -version` |
| **Maven** | 3.8.0 | 3.8.7+ | `mvn -version` |
| **Entorno Gráfico** | X11 / Windows GUI | - | - |
| **Memoria RAM** | 512 MB | 1 GB | - |
| **Espacio en Disco** | 100 MB | 200 MB | - |

### Sistemas Operativos Soportados:
- ✅ **Windows** 10/11 (64-bit)
- ✅ **Linux** (Ubuntu 20.04+, Debian 11+, Fedora 35+)
- ✅ **macOS** 11+ (Big Sur o superior)

### Verificar Instalación de Java:

```bash
java -version
```

**Salida esperada:**
```
openjdk version "21.0.8" 2024-07-16 LTS
OpenJDK Runtime Environment (build 21.0.8+7-LTS)
OpenJDK 64-Bit Server VM (build 21.0.8+7-LTS, mixed mode, sharing)
```

---

## 📦 Instalación

### Opción 1: Descargar Release (Recomendado)

1. Descarga la última versión desde [Releases](../../releases)
2. Descomprime el archivo ZIP
3. Ejecuta el script de inicio según tu sistema operativo

### Opción 2: Compilar desde Código Fuente

```bash
# 1. Clonar el repositorio
git clone https://github.com/sapoclay/farmaciando
cd farmacia

# 2. Compilar el proyecto
mvn clean package -DskipTests

# 3. Compilar el launcher
bash compilar-launcher.sh       # Linux/Mac
compilar-launcher.bat           # Windows

# 4. Ejecutar
./iniciar.sh                    # Linux/Mac
iniciar.bat                     # Windows
```

---

## 🚀 Ejecución

### **Método 1: Scripts de Inicio (RECOMENDADO)**

#### En Windows:
```cmd
# Opción A: Doble clic en el archivo
iniciar.bat

# Opción B: Desde CMD
cd C:\ruta\a\Farmacia
iniciar.bat
```

#### En Linux/Mac:
```bash
cd /ruta/a/Farmacia
./iniciar.sh
```

**💡 Los scripts automáticamente:**
- ✅ Verifican si existe el JAR compilado
- ✅ Compilan el proyecto si es necesario
- ✅ Compilan el launcher si es necesario
- ✅ Ejecutan la aplicación

---

### **Método 2: Ejecución Directa del JAR**

Si ya tienes todo compilado:

```bash
# Con el launcher (recomendado)
java -jar target/launcher.jar

# O directamente el JAR principal
java -jar target/gestion-farmacia-1.0.0.jar
```

---

### **Método 3: Desde Maven (Desarrollo)**

```bash
# Ejecutar sin compilar JAR
mvn spring-boot:run
```

---

## 📁 Estructura del Proyecto

```
📁 Farmacia/
├── 📄 pom.xml                              # Configuración Maven
├── 📄 README.md                            # Este archivo
├── 📄 INICIO_RAPIDO.md                     # Guía rápida
├── 📄 README_LAUNCHER.md                   # Documentación del launcher
├── 📄 .gitignore                           # Archivos ignorados por Git
│
├── 🚀 iniciar.bat                          # Launcher Windows
├── 🚀 iniciar.sh                           # Launcher Linux/Mac
├── 🔧 compilar-launcher.bat                # Compilar launcher (Windows)
├── 🔧 compilar-launcher.sh                 # Compilar launcher (Linux/Mac)
│
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/farmacia/
│   │   │   ├── 📄 FarmaciaApplication.java         # Aplicación principal
│   │   │   │
│   │   │   ├── 📁 model/                           # Modelos de datos
│   │   │   │   ├── 📄 Cliente.java
│   │   │   │   ├── 📄 Producto.java
│   │   │   │   ├── 📄 Venta.java
│   │   │   │   ├── 📄 DetalleVenta.java
│   │   │   │   ├── 📄 Proveedor.java
│   │   │   │   ├── 📄 Pedido.java
│   │   │   │   ├── 📄 DetallePedido.java
│   │   │   │   └── 📄 Alerta.java
│   │   │   │
│   │   │   ├── 📁 repository/                      # Acceso a datos (JPA)
│   │   │   │   ├── 📄 ClienteRepository.java
│   │   │   │   ├── 📄 ProductoRepository.java
│   │   │   │   ├── 📄 VentaRepository.java
│   │   │   │   ├── 📄 ProveedorRepository.java
│   │   │   │   └── 📄 PedidoRepository.java
│   │   │   │
│   │   │   ├── 📁 service/                         # Lógica de negocio
│   │   │   │   ├── 📄 ClienteService.java
│   │   │   │   ├── 📄 ProductoService.java
│   │   │   │   ├── 📄 VentaService.java
│   │   │   │   ├── 📄 ProveedorService.java
│   │   │   │   ├── 📄 PedidoService.java
│   │   │   │   └── 📄 AlertaService.java
│   │   │   │
│   │   │   └── 📁 ui/                              # Interfaz gráfica (JavaFX)
│   │   │       ├── 📄 SplashScreen.java
│   │   │       ├── 📄 InventarioPanel.java
│   │   │       ├── 📄 InventarioWindow.java
│   │   │       ├── 📄 ProductoFormWindow.java
│   │   │       ├── 📄 VentasPanel.java
│   │   │       ├── 📄 ClientesPanel.java
│   │   │       ├── 📄 ProveedoresPanel.java
│   │   │       ├── 📄 CrearPedidoWindow.java
│   │   │       ├── 📄 AlertasPanel.java
│   │   │       ├── 📄 ReportesPanel.java
│   │   │       └── 📄 GraficosPanel.java
│   │   │
│   │   └── 📁 resources/
│   │       ├── 📄 application.properties           # Configuración de producción
│   │       ├── 📄 application-dev.properties       # Configuración de desarrollo
│   │       └── 📁 images/                          # Recursos gráficos
│   │
│   └── 📁 launcher/java/
│       └── 📄 Launcher.java                        # Launcher multiplataforma
│
├── 📁 target/                                      # Archivos compilados
│   ├── 📄 gestion-farmacia-1.0.0.jar              # Aplicación principal (50 MB)
│   └── 📄 launcher.jar                             # Launcher (8 KB)
│
└── 📁 data/                                        # Base de datos H2
    └── 📄 farmacia_db.mv.db                        # Archivo de base de datos
```

---

## 🎯 Funcionalidades

### 1. 📦 Gestión de Inventario

- **Agregar/Editar/Eliminar** productos
- **Campos del producto:**
  - Nombre, principio activo, laboratorio
  - Precio de compra y venta
  - Stock actual y mínimo
  - Fecha de vencimiento
  - Categoría (Medicamento, Genérico, Cosmético, etc.)
  - Presentación, lote, ubicación
  - ¿Requiere receta? ¿Refrigeración?
  
- **Búsqueda y filtrado** en tiempo real
- **Vista de tabla** con todos los productos
- **Alertas automáticas** de stock bajo

### 2. 💰 Sistema de Ventas

- **Punto de venta** con búsqueda de productos
- **Carrito de compras** con cantidades ajustables
- **Métodos de pago:** Efectivo, Tarjeta, Transferencia
- **Cálculo automático** de subtotales, IVA y totales
- **Asociación con cliente** (opcional)
- **Actualización automática** del stock
- **Historial de ventas** con filtros
- **Ver detalle** de cada venta

### 3. 👥 Gestión de Clientes

- **Registro completo** de clientes
- **Datos:** Nombre, documento, email, teléfono, dirección
- **Historial de compras** por cliente
- **Búsqueda rápida** por nombre o documento
- **Estadísticas** de consumo
- **Productos favoritos** automáticos

### 4. 🏭 Gestión de Proveedores

- **Base de datos** completa de proveedores
- **Información:** Razón social, contacto, condiciones comerciales
- **Catálogo de productos** por proveedor
- **Historial de pedidos**
- **Estadísticas** de compras

### 5. 📋 Pedidos de Compra

- **Crear pedidos** a proveedores
- **Selección múltiple** de productos
- **Cálculo automático:** Subtotal, IVA (21%), Descuento, Total
- **Estados:** Borrador, Enviado, Confirmado, En Tránsito, Recibido, Cancelado
- **Actualización automática** de stock al recibir
- **Seguimiento** de pedidos pendientes

### 6. 🔔 Sistema de Alertas

- **5 tipos de alertas:**
  1. 🔴 **Stock Bajo** - Productos con menos de 10 unidades
  2. ❌ **Producto Caducado** - Ya venció
  3. ⚠️ **Próximo a Caducar** - Vence en menos de 30 días
  4. 📦 **Pedido Pendiente** - Pedidos no recibidos
  5. ⏰ **Pedido Retrasado** - Pedidos con más de 7 días de demora

- **Filtros** por tipo de alerta
- **Actualización automática** cada 2 minutos
- **Badge** con contador en la pestaña
- **Estadísticas** resumidas

### 7. 📊 Reportes y Gráficos

#### Reportes Estáticos:
- **Ventas del día**
- **Productos más vendidos**
- **Stock bajo**
- **Clientes frecuentes**
- **Ganancias del mes**

#### Gráficos Interactivos (JFreeChart):
- 📈 **Evolución de ventas diarias** (Gráfico de línea)
- 📊 **Top 10 productos** más vendidos (Gráfico de barras)
- 🥧 **Distribución por método de pago** (Gráfico circular)
- **Filtrado por fechas** personalizado

---

## 🛠️ Tecnologías

### Backend:
- **Spring Boot** 3.5.0 - Framework principal
- **Spring Data JPA** - Persistencia de datos
- **Spring Security Crypto** - Encriptación de contraseñas (BCrypt)
- **Hibernate** 6.6.15.Final - ORM
- **H2 Database** 2.3.232 - Base de datos embebida
- **Lombok** - Reducción de boilerplate

### Frontend:
- **JavaFX** 21.0.1 - Interfaz gráfica
- **JavaFX Controls** - Componentes UI
- **JavaFX FXML** - Diseño declarativo
- **JavaFX Swing** - Integración con Swing

### Gráficos:
- **JFreeChart** 1.5.4 - Generación de gráficos

### Build & Deploy:
- **Maven** 3.8.7 - Gestión de dependencias
- **Spring Boot Maven Plugin** - Empaquetado
- **Launcher Universal** - Detección de SO

---

## 💾 Base de Datos

### Configuración:

**Tipo:** H2 Database (Embebida en archivo)  
**Ubicación:** `./data/farmacia_db.mv.db`  
**Modo:** FILE (persistente)  

### Credenciales:
```properties
URL: jdbc:h2:file:./data/farmacia_db
Usuario: admin
Contraseña: farmacia2024
```

### Características:
- ✅ **Sin instalación** - Base de datos embebida
- ✅ **Persistente** - Los datos se guardan en disco
- ✅ **Portable** - Copia la carpeta `data/` y tienes backup
- ✅ **Auto-creación** - Se crea automáticamente al iniciar
- ✅ **DDL automático** - Hibernate crea las tablas

### Tablas Principales:
- `usuario` - Usuarios del sistema (admin, cajeros)
- `cliente` - Clientes registrados
- `producto` - Inventario de productos
- `venta` - Registro de ventas (con usuario responsable)
- `detalle_venta` - Líneas de cada venta
- `proveedor` - Proveedores
- `pedido` - Órdenes de compra
- `detalle_pedido` - Líneas de cada pedido

### Console H2 (Desarrollo):

Si necesitas acceder a la consola H2 para debugging:

1. Habilitar en `application-dev.properties`:
```properties
spring.h2.console.enabled=true
```

2. Acceder a: `http://localhost:8080/h2-console`

---

## 📦 Distribución

### Empaquetar para Distribución:

```bash
# 1. Compilar todo
mvn clean package -DskipTests

# 2. Compilar launcher
bash compilar-launcher.sh  # Linux/Mac
compilar-launcher.bat      # Windows

# 3. Crear carpeta de distribución
mkdir Farmacia-Release
cp -r target/*.jar iniciar.* compilar-launcher.* README.md INICIO_RAPIDO.md Farmacia-Release/

# 4. Comprimir
zip -r Farmacia-v1.0.0.zip Farmacia-Release/
```

### Contenido del Paquete:

```
📦 Farmacia-v1.0.0.zip
└── 📁 Farmacia/
    ├── 📄 gestion-farmacia-1.0.0.jar    (50 MB)
    ├── 📄 launcher.jar                   (8 KB)
    ├── 🚀 iniciar.bat                   (Windows)
    ├── 🚀 iniciar.sh                    (Linux/Mac)
    ├── 📄 README.md
    └── 📄 INICIO_RAPIDO.md
```

**Tamaño total:** ~52 MB

### Instrucciones para el Usuario:

1. Descomprimir el archivo ZIP
2. Instalar Java 21 JDK (si no lo tiene)
3. Ejecutar `iniciar.bat` (Windows) o `./iniciar.sh` (Linux/Mac)
4. ¡Listo! 🎉

---

## 🤝 Contribución

Las contribuciones son bienvenidas! Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

---

## 👨‍💻 Autor

**Tu Nombre**  
📧 Email: tu.email@ejemplo.com  
🐙 GitHub: [@tu-usuario](https://github.com/tu-usuario)

---

## 🙏 Agradecimientos

- [Spring Framework](https://spring.io/) - Framework backend
- [JavaFX](https://openjfx.io/) - Interfaz gráfica
- [JFreeChart](http://www.jfree.org/jfreechart/) - Generación de gráficos
- [H2 Database](https://www.h2database.com/) - Base de datos embebida
- [Lombok](https://projectlombok.org/) - Reducción de código boilerplate

---

## 📸 Screenshots

### Pantalla Principal
![Inicio](docs/screenshots/inicio.png)

### Gestión de Inventario
![Inventario](docs/screenshots/inventario.png)

### Sistema de Ventas
![Ventas](docs/screenshots/ventas.png)

### Reportes con Gráficos
![Gráficos](docs/screenshots/graficos.png)

---

## 🔮 Roadmap

- [ ] **Autenticación y roles** de usuario
- [ ] **Backup automático** de base de datos
- [ ] **Exportar reportes** a PDF/Excel
- [ ] **Impresión** de tickets de venta
- [ ] **API REST** para integración con otros sistemas
- [ ] **Modo offline** con sincronización
- [ ] **Módulo de contabilidad**
- [ ] **Gestión de empleados** y turnos

---

## 📞 Soporte

¿Tienes problemas o preguntas?

- 📖 Lee la [Guía de Inicio Rápido](INICIO_RAPIDO.md)
- 🔧 Revisa la [Documentación del Launcher](README_LAUNCHER.md)
- 🐛 Reporta bugs en [Issues](../../issues)
- 💬 Discusiones en [Discussions](../../discussions)

---

<div align="center">

Hecho con ❤️ y ☕ por entreunosyceros.net

</div>
