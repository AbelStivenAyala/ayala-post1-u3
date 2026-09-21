# Post-contenido — Unidad 3: Patrones Estructurales en ConfUDES

## Descripción
Repositorio del post-contenido de la Unidad 3 de Patrones de Diseño
de Software. Un único proyecto Spring Boot (confudes-patrones-
estructurales) que resuelve cuatro necesidades reales del backend
de ConfUDES, una plataforma de gestión de congresos académicos:
registro de asistencia con un proveedor externo, emisión de
certificados, mejoras opcionales sobre el certificado emitido y
control de acceso a la descarga masiva.

## Cómo ejecutar

$ mvn clean package
$ mvn spring-boot:run
$ mvn test


## Decisiones de diseño

### Necesidad 1 — Registro de asistencia

**Síntoma:** `QRCheckClient` es el SDK de un único proveedor externo
(QRCheckAPI), con un contrato incompatible con el que ya usa el
resto del sistema: exige `idEvento` como `long` (el sistema lo
maneja como `String`), un `payload` de texto en vez de una
credencial QR, y devuelve un código numérico (200/401) en vez del
`boolean exitoso` que espera `ResultadoCheckIn`. El contrato interno
`ServicioAsistencia` ya está en producción, usado por
`ControladorCheckIn` y por el módulo de reportes, y no puede
modificarse.

**Patrón aplicado: Adapter.** `QRCheckServicioAsistenciaAdapter`
implementa `ServicioAsistencia` y por dentro traduce cada llamada al
vocabulario de `QRCheckClient`.

**Por qué no Facade:** Facade resuelve el problema de "demasiados
colaboradores que orquestar"; aquí hay un único colaborador externo
cuyo problema es de **compatibilidad de contrato**, no de cantidad.
Usar Facade no resolvería la incompatibilidad de tipos y formatos
entre `QRCheckRequest`/`QRCheckResponse` y
`ServicioAsistencia`/`ResultadoCheckIn` — en la práctica se
terminaría escribiendo el mismo código de traducción, solo que mal
nombrado.

### Necesidad 2 — Emisión de certificados

**Síntoma:** `ControladorCertificados` conocía y orquestaba
directamente cuatro servicios (`ValidadorAsistencia`,
`GeneradorCertificadoPDF`, `FirmaDigitalService`,
`EnvioCorreoService`), con un constructor de 4 parámetros y un
método `emitir()` de más de 20 líneas coordinando la secuencia
manualmente. Los cuatro servicios se siguen usando directamente en
otros módulos de ConfUDES, así que no pueden fusionarse ni
modificarse.

**Patrón aplicado: Facade.** `ServicioEmisionCertificados` recibe
los cuatro servicios y expone una única operación (`emitir`) que
encapsula la secuencia completa (validar → generar → firmar →
enviar). El controlador ahora depende únicamente de ese colaborador
(a través del contrato `ServicioCertificados`), con un solo
constructor de un solo parámetro y un cuerpo de `emitir()` de 5
líneas.

**Por qué no Adapter (comparación con la Necesidad 1):** a
diferencia de `QRCheckClient`, ninguno de los cuatro servicios tiene
un contrato incompatible con lo que el sistema espera — todos ya se
usan tal cual en otros módulos. El problema no es de traducción de
contratos sino de cuántos colaboradores debe conocer el cliente.
Adapter no resolvería esto: seguiría existiendo un controlador con 4
dependencias inyectadas, solo que "traducidas" sin necesidad, porque
no había nada que traducir en primer lugar.

### Necesidad 3 — Mejoras opcionales del certificado

**Síntoma:** los organizadores quieren activar, por evento, hasta 3
mejoras sobre el PDF ya emitido (marca de agua, código QR de
verificación, traducción al inglés), en cualquier combinación (0 a
3, en cualquier orden — 8 combinaciones posibles con solo 3
mejoras), sin crear una clase por combinación ni modificar la lógica
base de emisión (`ServicioEmisionCertificados`).

**Patrón aplicado: Decorator.** Cada mejora
(`MejoraMarcaDeAgua`, `MejoraCodigoQRVerificacion`,
`MejoraTraduccionIngles`) implementa `ServicioCertificados` y
envuelve a otro `ServicioCertificados` (la base u otro decorador ya
aplicado): siempre delega en el envuelto y transforma el resultado.
Para combinar mejoras basta con anidar decoradores en tiempo de
ensamblaje, por ejemplo:
```java
ServicioCertificados conTodo =
    new MejoraMarcaDeAgua(
        new MejoraCodigoQRVerificacion(
            new MejoraTraduccionIngles(servicioBase)));
```
Cero clases nuevas por combinación.

**Por qué no herencia (una subclase por combinación):** con 3
mejoras ya son 8 subclases posibles
(`CertificadoConMarcaDeAgua`, `CertificadoConMarcaDeAguaYQR`...); si
ConfUDES agrega una cuarta mejora, las combinaciones se duplican a
16. Es una explosión combinatoria de clases que el enunciado prohíbe
explícitamente.

**Por qué no parámetros booleanos en `emitir()`:** funcionaría para
3 mejoras (`activarMarcaDeAgua, activarQR, activarTraduccion`), pero
cada mejora nueva exigiría modificar la firma del método y su lógica
interna — violando la restricción de no tocar la lógica base de
emisión, y el método crecería indefinidamente con condicionales
anidados.

**Por qué no el patrón de la Necesidad 4 (Proxy) para esto:** un
Proxy está pensado para decidir si delega o no, no para apilarse ni
transformar el resultado de forma combinable. Usar Proxy aquí
dejaría sin resolver justamente el requisito central: combinar
mejoras libremente.

### Necesidad 4 — Control de acceso a la descarga masiva

**Síntoma:** la descarga masiva de certificados es costosa (el
proveedor de firma digital limita a 60 llamadas por minuto, y cada
certificado requiere al menos una) y debe restringirse a usuarios
con rol `ORGANIZADOR` o `ADMIN`. El resto del sistema — incluido el
flujo de emisión individual de la Necesidad 2 — debe seguir
inyectando `ServicioCertificados` sin conocer roles ni límites del
proveedor.

**Patrón aplicado: Proxy (de protección).**
`ServicioCertificadosProxyControlAcceso` implementa
`ServicioCertificados`, envuelve un `ServicioCertificados` real, y
verifica el rol con `ContextoUsuario` **antes** de delegar. Si el
rol no es válido, lanza `AccesoDenegadoException` sin que el
servicio real llegue a ejecutarse.

**Por qué no el patrón de la Necesidad 3 (Decorator) para esto:**
estructuralmente ambos envuelven un objeto que implementa la misma
interfaz, pero un Decorator **siempre delega primero** y luego
transforma el resultado — nunca decide si la llamada ocurre. Usar
Decorator aquí ejecutaría igual la operación costosa de emisión
antes de decidir si el usuario tiene permiso, violando exactamente
la restricción de "rechazar sin llegar a invocar la lógica costosa".
El Proxy, en cambio, puede sustituir por completo el acceso al
objeto real sin que este se ejecute — es la diferencia de intención
entre "añadir capacidades siempre delegando" (Decorator) y "decidir
si delegar" (Proxy).

### Reflexión — Composite y Flyweight (opcional)

La agenda de cada congreso (tracks, sesiones y actividades dentro de
cada sesión) es una estructura jerárquica árbol-de-partes, así que
**Composite** encajaría de forma natural: cada nodo (track, sesión,
actividad) podría tratarse de manera uniforme a través de una
interfaz común, permitiendo operaciones recursivas como "calcular la
duración total del track" sin distinguir si el nodo es una sesión
individual o un contenedor de actividades.

**Flyweight**, en cambio, no aplicaría a las miles de credenciales
QR generadas: Flyweight sirve para compartir estado **común** entre
muchos objetos similares y así ahorrar memoria, pero cada credencial
QR tiene datos únicos e irrepetibles (el `payload` de cada
participante es distinto) — no hay estado compartible que extraer,
así que no habría ahorro de memoria real que justificar el patrón.

## Herramientas utilizadas
- Java 17, Spring Boot 3.2, Apache Maven, JUnit 5
- VS Code, Git, GitHub

## Conclusiones
Este laboratorio dejó claro que el nombre de un patrón no se elige
por cómo "se ve" el código (una clase que envuelve a otra), sino por
el problema real que resuelve: Adapter traduce contratos
incompatibles, Facade reduce colaboradores conocidos, Decorator
añade capacidades siempre delegando, y Proxy decide si delega o no.
La parte más difícil fue distinguir Decorator de Proxy en las
Necesidades 3 y 4, porque ambos comparten la misma estructura
(implementan y envuelven la misma interfaz); la diferencia solo se
nota mirando la intención y el efecto sobre la ejecución — si
siempre se llega al objeto real, o si se puede evitar llegar a él.
Comparar cada necesidad con su alternativa más cercana, antes de
escribir código, ayudó a evitar soluciones que "funcionan" pero no
corresponden al patrón correcto para el problema.