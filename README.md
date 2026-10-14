# Document-Analyzer

Backend para el análisis de documentos corporativos. El proyecto utiliza Java, Spring Boot, FastAPI y PostgreSQL con pgvector como base para una arquitectura orientada a la extracción y consulta de información documental.

## Tecnologías

- Java 21 y Spring Boot
- Python y FastAPI
- PostgreSQL 16 con pgvector
- Docker y Docker Compose
- Maven

## Requisitos previos

- Docker Desktop instalado y en ejecución.
- Git.
- Un archivo `.env` configurado a partir de `.env.example`.

## Configuración inicial

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/BCpollito/Document-Analyzer-core.git
   cd Document-Analyzer
   ```

2. Crear el archivo de variables de entorno a partir del ejemplo.

   En PowerShell:

   ```powershell
   Copy-Item .env.example .env
   ```

3. Editar `.env` y configurar una contraseña de desarrollo para PostgreSQL. No utilizar credenciales reales en el repositorio.

4. Verificar que las variables de conexión de Spring Boot coincidan con las de PostgreSQL. Dentro de Docker, el backend debe conectarse al servicio `postgres` por el puerto `5432`.

## Ejecución con Docker Compose

Construir las imágenes e iniciar los servicios:

```bash
docker compose up --build -d
```

Consultar el estado:

```bash
docker compose ps
```

Consultar los logs:

```bash
docker compose logs --tail=100 backend ai-service postgres
```

Detener los servicios:

```bash
docker compose down
```

Los datos de PostgreSQL se almacenan en un volumen persistente. No utilizar `docker compose down -v` si se desea conservar ese volumen.

## Servicios disponibles

- Backend Spring Boot: http://localhost:9090
- Documentación de FastAPI: http://localhost:8000/docs
- Endpoint de salud de FastAPI: http://localhost:8000/health
- PostgreSQL: disponible desde el equipo local en el puerto `5433`, según la configuración predeterminada de `.env.example`.

La ruta raíz del backend puede responder con HTTP 404 si no se ha definido un controlador para `/`. Esto no significa necesariamente que Spring Boot haya fallado al iniciar.

## Pruebas básicas

Comprobar el endpoint de salud de FastAPI:

```powershell
Invoke-RestMethod http://localhost:8000/health
```

Verificar el estado de los contenedores:

```powershell
docker compose ps
```

Consultar los logs del backend:

```powershell
docker compose logs --tail=100 backend
```

Para comprobar la persistencia de PostgreSQL, reiniciar los servicios y verificar que los datos de prueba continúen disponibles.

## Variables de entorno

Las variables se configuran en `.env` y se inyectan a los servicios mediante Docker Compose.

- `POSTGRES_DB`: nombre de la base de datos.
- `POSTGRES_USER`: usuario de PostgreSQL.
- `POSTGRES_PASSWORD`: contraseña de PostgreSQL.
- `POSTGRES_PORT`: puerto de PostgreSQL publicado en el equipo local.
- `BACKEND_PORT`: puerto publicado para Spring Boot.
- `AI_PORT`: puerto publicado para FastAPI.
- `LLM_API_KEY`: clave del proveedor de modelos de lenguaje, si se requiere.

No guardar el archivo `.env` ni credenciales reales en el repositorio. Mantener `.env.example` con valores ficticios.

## Estado del proyecto

La infraestructura de desarrollo incluye PostgreSQL con pgvector, un backend Spring Boot y un microservicio FastAPI con endpoint de salud.

Las funcionalidades de análisis documental, extracción de texto, generación de embeddings y recuperación de información deben documentarse conforme se implementen y prueben.

## Migraciones de base de datos con Flyway

El esquema de PostgreSQL se administra mediante migraciones SQL versionadas con Flyway. Las migraciones se encuentran en `src/main/resources/db/migration/` y se ejecutan automáticamente cuando se inicia el backend.

### Verificar las migraciones aplicadas

Para consultar las migraciones registradas en la base de datos:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Todas las migraciones aplicadas deben tener `success = true`.

### Verificar las tablas del esquema

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

El esquema debe contener las tablas de usuarios, documentos, fragmentos, consultas, conversaciones y mensajes, además de `flyway_schema_history`.

### Verificar la extensión pgvector

```sql
SELECT extname
FROM pg_extension
WHERE extname = 'vector';
```

El resultado debe incluir la extensión `vector`.

### Prueba de inicialización sobre una base de datos nueva

Para comprobar que las migraciones pueden ejecutarse desde cero sin afectar la base de desarrollo, se utiliza una base de datos temporal independiente.

1. Verificar que PostgreSQL esté en ejecución:

   ```powershell
   docker compose ps postgres
   ```

2. Crear la base de datos temporal:

   ```powershell
   docker exec document-analyzer-db psql -U postgres -d postgres -c "CREATE DATABASE analizador_ca05;"
   ```

3. Ejecutar el backend contra la base temporal para aplicar las migraciones:

   ```powershell
   docker compose run --build --rm --no-deps `
     -e DB_URL=jdbc:postgresql://postgres:5432/analizador_ca05 `
     backend
   ```

4. Consultar el historial de migraciones:

   ```powershell
   docker exec document-analyzer-db psql -U postgres -d analizador_ca05 -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
   ```

5. Comprobar las tablas creadas:

   ```powershell
   docker exec document-analyzer-db psql -U postgres -d analizador_ca05 -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;"
   ```

6. Verificar que pgvector esté habilitado:

   ```powershell
   docker exec document-analyzer-db psql -U postgres -d analizador_ca05 -c "SELECT extname FROM pg_extension WHERE extname = 'vector';"
   ```

### Resultado esperado

La prueba se considera satisfactoria cuando Flyway aplica todas las migraciones pendientes sin errores, las versiones quedan registradas con `success = true`, se crean las tablas esperadas y la extensión `vector` está habilitada.

La inicialización del esquema no debe depender de la ejecución manual de scripts SQL fuera del mecanismo de migraciones.

La base de datos temporal se utiliza únicamente para validación. La base de desarrollo y su volumen persistente deben conservarse durante estas pruebas.