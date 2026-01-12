# Sistema de Matriculación Vehicular y Emisión de Licencias (MVC)

![Estado](https://img.shields.io/badge/Estado-En%20Desarrollo-yellow?style=for-the-badge)
![Java Version](https://img.shields.io/badge/java-17%2B-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Platform](https://img.shields.io/badge/platform-windows%20%7C%20linux%20%7C%20mac-lightgrey?style=for-the-badge)
![Database](https://img.shields.io/badge/PostgreSQL-Supabase-316192?style=for-the-badge&logo=postgresql&logoColor=white)

> **Institución:** Escuela Politécnica Nacional (EPN)  
> **Facultad:** Ingeniería de Sistemas  
> **Carrera:** Tecnología en Desarrollo de Software  
> **Asignatura:** Programación Orientada a Objetos  
> **Semestre:** 2025-B  

---

El presente proyecto implementa una solución de software de escritorio robusta, desarrollada bajo el lenguaje **Java** y la biblioteca gráfica **Swing**. El sistema adopta una arquitectura en capas basada en el patrón de diseño **Modelo-Vista-Controlador (MVC)**, orientada a la automatización integral de los procesos de emisión de licencias de conducir.

La infraestructura de persistencia de datos reside en la nube mediante **PostgreSQL (Supabase)**, garantizando alta disponibilidad, integridad referencial y seguridad en la gestión de la información sensible del solicitante.

## 1. Demostración Funcional (Evidencia)
A continuación, se presenta la demostración operativa del sistema, abarcando el ciclo de vida completo del trámite: desde el registro del solicitante y validación de requisitos, hasta la aprobación de exámenes y generación del documento.

### Video Explicativo del Sistema
https://youtu.be/nMHTb2FO728
---

## 2. Arquitectura de Software
El diseño técnico prioriza la modularidad, el desacoplamiento de componentes y las buenas prácticas de la Programación Orientada a Objetos (POO):

* **MVC (Modelo-Vista-Controlador):** Separación estricta entre la lógica de negocio, la gestión de datos y la interfaz de usuario.
* **DAO (Data Access Object):** Patrón estructural que abstrae la persistencia, permitiendo la intercambiabilidad del motor de base de datos sin afectar la lógica de negocio.
* **Singleton:** Implementado en la gestión de conexiones (`DatabaseConfig`) para optimizar el consumo de recursos de red.
* **Role-Based Access Control (RBAC):** Sistema de seguridad que restringe funcionalidades críticas según el perfil del usuario (Administrador vs. Analista).

---

## 3. Stack Tecnológico

| Componente | Tecnología Seleccionada | Detalle / Versión |
| :--- | :--- | :--- |
| **Lenguaje Core** | Java JDK | Versión 17 (LTS) |
| **Interfaz Gráfica** | Java Swing / AWT | Componentes Nativos |
| **Gestión de Proyecto** | Apache Maven | 3.8.x |
| **Base de Datos** | PostgreSQL 15 | Alojada en Supabase (Cloud) |
| **Seguridad** |  | Hashing de contraseñas |

---

## 4. Guía de Instalación y Despliegue

### 4.1. Requisitos del Entorno
Para garantizar la correcta ejecución del software, asegúrese de cumplir con los siguientes prerrequisitos:
1.  **Java Runtime Environment (JRE):** Versión 17 o superior.
2.  **Conexión a Internet:** Requerida para la comunicación con el servidor de base de datos remoto.

### 4.2. Compilación desde Código Fuente
Si desea compilar el proyecto localmente para desarrollo:

1.  Clone el repositorio:
    ```bash
    git clone [https://github.com/OfficialMYKE/sistema-matriculacion-mvc.git](https://github.com/OfficialMYKE/sistema-matriculacion-mvc.git)
    ```
2.  Acceda al directorio del proyecto y ejecute Maven:
    ```bash
    mvn clean package
    ```
3.  El artefacto binario (`.jar`) se generará en el directorio `/target`.

### 4.3. Ejecución del Binario (Producción)
Para ejecutar la aplicación empaquetada lista para el usuario final, utilice el siguiente comando en su terminal:

```bash
java -jar build/app.jar
```

---

## 5. Credenciales de Acceso
El sistema se entrega con usuarios preconfigurados para facilitar la evaluación de los distintos roles y permisos:

| Rol | Usuario | Contraseña | Alcance Funcional |
| :--- | :--- | :--- | :--- |
| **ADMINISTRADOR** | admin | 12345 | Control total, gestión de usuarios (CRUD), auditoría y reportes estadísticos. |
| **ANALISTA** | analista | 12345 | Operación de trámites, validación de requisitos físicos, registro de notas y emisión. |

---

## 6. Autores
Este proyecto ha sido desarrollado por:

>Michael Alexander Carrillo Mendez
>
>Kevin Alexander Amagua Valenzuela

