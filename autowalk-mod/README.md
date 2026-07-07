# Auto Walk Toggle (Fabric, Minecraft 26.2)

Mod muy simple: presiona una tecla (por defecto **G**) y tu personaje
camina hacia adelante solo, hasta que:

- vuelvas a presionar esa misma tecla, o
- presiones **W, A, S o D** de verdad.

Solo funciona en el cliente (no requiere nada especial del servidor,
es 100% local, como caminar tú mismo).

## Estructura del proyecto

```
autowalk-mod/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── src/main/java/com/example/autowalk/AutoWalkClient.java
└── src/main/resources/
    ├── fabric.mod.json
    └── assets/autowalk/lang/{en_us.json, es_es.json}
```

## Cómo compilarlo

1. Necesitas **JDK 21** instalado.
2. Este proyecto usa el plugin de Gradle de Fabric Loom, pero **no
   incluye el "gradle wrapper"** (los binarios `gradlew`/`gradlew.bat`
   y el jar de Gradle), porque este entorno no tiene acceso a
   internet para descargarlos. Genera el wrapper tú mismo la primera
   vez, dentro de la carpeta del proyecto:

   ```bash
   gradle wrapper --gradle-version 9.5.1
   ```

   (Necesitas tener Gradle instalado una vez para este paso; después
   podrás usar siempre `./gradlew`.)

3. Compila el mod:

   ```bash
   ./gradlew build
   ```

   El `.jar` resultante aparecerá en `build/libs/`.

4. Copia ese `.jar` a la carpeta `mods` de tu instalación de
   **Minecraft 26.2 con Fabric Loader 0.19.3+** (necesitas también
   **Fabric API 0.154.0+26.2** instalado en esa carpeta `mods`).

## ⚠️ Nota importante sobre versiones exactas

Verifiqué que para Minecraft 26.2 se recomienda:
- Fabric Loader 0.19.3
- Fabric Loom 1.17
- Gradle 9.5.1
- Fabric API 0.154.0+26.2

El nombre exacto de las **mappings de Yarn** (`yarn_mappings` en
`gradle.properties`) puede variar según el build específico
disponible en el momento en que compiles. Revisa la versión más
reciente en:

https://fabricmc.net/develop/

o consultando directamente:

https://maven.fabricmc.net/net/fabricmc/yarn/

y ajusta la línea `yarn_mappings=26.2+build.X` en `gradle.properties`
con el número de build que encuentres ahí.

## Cómo funciona el código (resumen)

- Se registra una keybinding personalizada con `KeyBindingHelper`.
- En cada tick del cliente (`ClientTickEvents.END_CLIENT_TICK`):
  - Si se presionó la tecla de toggle, se activa/desactiva el modo
    "auto caminar".
  - Si el modo está activo, se fuerza
    `client.options.forwardKey.setPressed(true)` cada tick — esto
    hace que el juego crea que W está presionada, sin necesidad de
    mixins.
  - Para detectar si el jugador presiona W/A/S/D **de verdad** (y así
    cancelar el auto-caminar), se consulta el estado real del
    teclado vía GLFW (`InputUtil.isKeyPressed`), en vez de mirar las
    keybindings (que nosotros mismos podríamos haber alterado).
- Al desactivar el modo, se suelta la tecla forward que habíamos
  forzado, pero solo si fuimos nosotros quienes la habíamos forzado
  (para no interferir con una pulsación real del jugador).

## Personalizar la tecla de activación

Puedes cambiarla ingame en **Opciones > Controles > Auto Caminar**,
o cambiar el valor por defecto en el código
(`GLFW.GLFW_KEY_G` en `AutoWalkClient.java`) por cualquier otra tecla
de GLFW.
