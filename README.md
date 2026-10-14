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