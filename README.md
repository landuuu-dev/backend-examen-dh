# 🌎 DH Tours API

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=for-the-badge&logo=spring-boot)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green?style=for-the-badge&logo=mongodb)

**DH Tours** es una solución robusta para la gestión de servicios turísticos. Esta API permite administrar un catálogo de tours, gestionar usuarios con diferentes niveles de acceso y manejar archivos multimedia de forma eficiente.

---

## 🚀 Tecnologías Utilizadas

* **Backend:** Java 17 con Spring Boot 3.
* **Seguridad:** Spring Security + JSON Web Tokens (JWT).
* **Base de Datos:** MongoDB (NoSQL) para persistencia flexible.
* **Multimedia:** Cloudinary (Almacenamiento de imágenes en la nube).
* **Despliegue:** Configurado para **Render**.

---

## 🛠️ Guía de Despliegue

Sigue estos pasos para poner en marcha la API en tu entorno local o en la nube. Recuerda que hay un archivo .env.example donde hay una plantilla solo debes rellenar con tus datos y credenciales para ocupar la API

### 1. Clonar el repositorio
```bash
git clone https://github.com/landuuu-dev/backend-examen-dh.git
```


# Conexión a Base de Datos
```bash
MONGODB_URI=mongodb+srv://<usuario>:<password>@cluster.mongodb.net/dh-tours
```

# Seguridad JWT
```bash
JWT_SECRET=tu_clave_secreta_muy_larga_y_segura
JWT_EXPIRATION_MS=????????
```
# Cloudinary (Credenciales de API)
```bash
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret
```

---

## Endpoints de Tours

### 1. Crear Tour
**POST** `/tours`

**Descripción:** Crea un nuevo tour en el sistema (requiere permisos de administrador).

**Body (JSON) respuesta esperada:**
```json
{
    "id": 5,
    "nombre": "Cocina Fusión Andina",
    "categoria": "cocina",
    "descripcion": "Experimenta la mezcla de sabores tradicionales con técnicas modernas. Donde aprenderas platos tipicos como Calapurca, Chairo, Sango y Picante de guata",
    "ubicacion": "Putre",
    "precio": 28000,
    "imagenes": [
        "https://dynamic-media-cdn.tripadvisor.com/media/photo-o/15/42/45/f4/das-essen-war-lecker.jpg?w=900&h=500&s=1",
        "https://d2kkzshb6n9g86.cloudfront.net/wp-content/uploads/2021/07/recetas-chilenas-calapurca-1-768x500.jpg.webp",
        "https://www.marcachile.cl/cocina-chilena/salsa-pebre-tradicional/"
    ]
}
```

### 2. Listar Tour
**GET** `/tours`

**Descripción:** Obtiene la lista completa de tours disponibles.

**Body (JSON) respuesta esperada:**
```json
[
    {
        "id": 1,
        "nombre": "Tour 1",
        "categoria": "aventura",
        "precio": 15000,
        ...
    },
    {
        "id": 2,
        "nombre": "Tour 2",
        "categoria": "cultura",
        "precio": 20000,
        ...
    }
]
```

### 3. Actualizar Tour
**PUT** `/tours/{id}`

**Descripción:** Actualiza un tour existente por su ID.

**Parametros de ruta:** ID del tour a actualizar

**Body (JSON) respuesta esperada:**
```json
{
    "nombre": "Taller de Telar Avanzado",
    "categoria": "tejido",
    "descripcion": "Taller avanzado para crear tapices profesionales.",
    "imagenes": [
        "https://example.com/nueva_imagen1.jpg"
    ]
}
```
### 4.  Eliminar Tour
**DELETE** `/tours/{id}`

**Descripción:** Elimina un tour por su ID.

**Parametros de ruta:** ID del tour a eliminar.


## Endpoints de Autenticación y Usuarios

### 1. Login de Administrador/Usuario
**POST** `/auth/login`

**Descripción:** Autentica a un usuario y devuelve un token JWT.

**Headers:**
```json
Content-Type: application/json
```
**Body (JSON):**
```json
{
    "correo": "correo@ejemplo.com",
    "password": "tuClaveAdmin"
}
```
**Body (JSON) respuesta esperada:**
```json
{
    "token": "223JhbGciOiJIUzI3345InR5cCI6IkpXVCJ9...",
    "usuario": {
        "id": "223457890833222",
        "nombre": "Usuario Prueba",
        "correo": "correo@ejemplo.com",
        "rol": "ADMIN"
    },
    "expiraEn": 3600
}
```

### 2. Registrar usuario
**POST** `/auth/register`

**Descripción:**  Registra un nuevo usuario en el sistema.

**Headers:**
```json
Content-Type: application/json
```
**Body (JSON):**
```json
{
    "nombre": "Usuario Prueba",
    "correo": "correo@ejemplo.com",
    "password": "claveDeUsuario"
}
```
**Body (JSON) respuesta esperada:**
```json
{
    "mensaje": "Usuario registrado exitosamente",
    "usuario": {
        "id": "6978477eb25eeee6ae769410",
        "nombre": "usuario ejemplo",
        "correo": "correo@ejemplo.com",
        "rol": "USER"
    }
}
```

### 3. Listar Usuarios
**GET** `/usuarios`

**Descripción:** Obtiene la lista de todos los usuarios (requiere autenticación de administrador).

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Respuesta Exitosa (200):**
```json
[
    {
        "id": "697835a7671d6f2223344d",
        "nombre": "usuario",
        "correo": "pssss@correo.com",
        "rol": "ADMIN",
        "fechaRegistro": "2024-01-15T09:00:00Z"
    }
]
```

---

### 4. Actualizar Usuario
**PUT** `/usuarios/{id}`

**Descripción:** Actualiza la información de un usuario.

**Parámetros de Ruta:**
- `id` (string): ID del usuario a actualizar

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body (JSON):**
```json
{
    "nombre": "Nuevo Nombre",
    "correo": "correo@example.com"
}
```

**Respuesta Exitosa (200):**
```json
{
    "mensaje": "Usuario actualizado exitosamente",
    "usuario": {
        "id": "6978477eb25eeee6ae769410",
        "nombre": "Nuevo Nombre",
        "correo": "correo@example.com",
        "rol": "USER"
    }
}
```

---

### 5. Eliminar Usuario
**DELETE** `/usuarios/{id}`

**Descripción:** Elimina un usuario del sistema.

**Parámetros de Ruta:**
- `id` (string): ID del usuario a eliminar

**Headers:**
```
Authorization: Bearer <token>
```

**Respuesta Exitosa (200):**
```json
{
    "mensaje": "Usuario eliminado exitosamente",
    "idEliminado": "697844b2651b2b3ec9f9b324"
}
```

---

### 6. Promover a Administrador
**POST** `/admin/promote/{id}`

**Descripción:** Promueve un usuario a administrador (requiere super administrador).

**Parámetros de Ruta:**
- `id` (string): ID del usuario a promover

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body (JSON):**
```json
{
    "correo": "correoAdminNuevo@correo.com",
    "password": "claveAdminNuevo"
}
```

**Respuesta Exitosa (200):**
```json
{
    "mensaje": "Usuario promovido a ADMIN exitosamente",
    "usuario": {
        "id": "69783512334567878432",
        "nombre": "Usuario Admin Nuevo",
        "rol": "ADMIN"
    }
}
```

---

# Tour Favoritos por usuario

### 1. Agregar Favorito
**POST** `/usuarios/{id}/favoritos/{tourId}`

**Descripción:** Agrega un tour a la lista de favoritos de un usuario. Solo el usuario autenticado puede modificar sus favoritos.

**Parámetros de Ruta:**
- `id` (string): ID del usuario
- `tourId` (string): ID del tour a agregar

**Headers:**
```
Authorization: Bearer <token>
```

**Respuesta Exitosa (200):**
```json
{
  "id": "697835a7671d6f2def6c208d",
  "nombre": "Usuario ejemplo",
  "correo": "correoEjemplo@correo.com",
  "rol": "USER",
  "favoritos": [
    {
      "id": "123456",
      "nombre": "Cocina Fusión Andina",
      "categoria": "cocina"
    }
  ]
}
```

---

### 2. Quitar Favorito
**DELETE** `/usuarios/{id}/favoritos/{tourId}`

**Descripción:** Elimina un tour de la lista de favoritos del usuario. Solo el usuario autenticado puede modificar sus favoritos.

**Parámetros de Ruta:**
- `id` (string): ID del usuario
- `tourId` (string): ID del tour a eliminar

**Headers:**
```
Authorization: Bearer <token>
```

**Respuesta Exitosa (200):**
```json
{
  "id": "697835a134567678d334446c208d"
}

```

## Códigos de Estado HTTP

| Código | Descripción |
|--------|-------------|
| 200    | OK - Solicitud exitosa |
| 201    | Created - Recurso creado exitosamente |
| 400    | Bad Request - Error en los datos enviados |
| 401    | Unauthorized - No autenticado |
| 403    | Forbidden - No tiene permisos para la acción |
| 404    | Not Found - Recurso no encontrado |
| 500    | Internal Server Error - Error del servidor |

---

