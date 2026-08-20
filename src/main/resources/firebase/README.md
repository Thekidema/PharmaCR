# Credenciales de Firebase Storage

En esta carpeta va el archivo JSON de la cuenta de servicio de Firebase.
**Ese archivo no está en el repositorio** — el `.gitignore` lo excluye, porque da
acceso de escritura al bucket de almacenamiento.

## Cómo obtenerlo

1. Entrar a [firebase.google.com](https://firebase.google.com) → **Ir a la consola**.
2. Crear el proyecto (por ejemplo `pharmacr`) y activar **Storage**.
3. Ir a **Configuración del proyecto → Cuentas de servicio → Generar nueva clave privada**.
4. Guardar el archivo descargado en esta carpeta.

## Cómo activarlo

En `application.properties`, poner el nombre real del archivo y activar Firebase:

```properties
firebase.activo=true
firebase.bucket.name=<tu-proyecto>.firebasestorage.app
firebase.json.file=<nombre-del-archivo-descargado>.json
```

El nombre del bucket aparece en la consola de Firebase, en **Storage**, con el
formato `gs://<tu-proyecto>.firebasestorage.app` (se copia sin el `gs://`).

## Si no se configura

La aplicación arranca y funciona igual. `StorageConfig` está anotado con
`@ConditionalOnProperty(name = "firebase.activo", havingValue = "true")`, así que
sin credenciales simplemente no se crea el bean y los usuarios se guardan sin
fotografía.
