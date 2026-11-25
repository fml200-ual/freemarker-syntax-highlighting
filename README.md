# FreeMarker Highlight

Plugin de IntelliJ IDEA que añade resaltado de sintaxis para plantillas **FreeMarker**.

> **Contexto.** JetBrains distribuye un plugin de FreeMarker, pero forma parte de la parte de pago.
> Sin licencia, un `.ftl` se abre como texto plano. Este plugin cubre ese hueco con un lexer y un
> esquema de color propios, y usa IDs distintos (`FREEMARKER_HIGHLIGHT`) para no colisionar con el
> de JetBrains si algún día se activa.

[Licencia: MIT](LICENSE) · Requiere IntelliJ IDEA 2025.3 (build `253`) o superior

---

## Qué resalta

| Constructo | Ejemplo |
|---|---|
| Interpolación | `${usuario.nombre}`, `#{1.5}`, `[=usuario.nombre]` |
| Directivas | `<#if>`, `<#list>`, `<#assign>`, `<#macro>`, `</#if>`, `</#list>` |
| Macro de usuario | `<@miBoton texto="Guardar"/>`, `<@liferay.portlet url=...>` |
| Comentarios | `<#-- no se renderiza -->`, `[#-- tampoco --]` |
| Builtins | `?upper_case`, `?size`, `?has_content`, `??`, `?string("0.00")` |
| Operadores y literales | `..`, `==`, `&&`, `!`, `"texto"`, `r"raw"`, `[1,2]`, `{a: 1}` |
| HTML alrededor | etiquetas, atributos, entidades, comentarios HTML |
| Sintaxis de corchetes | `[#if]`, `[/#if]`, `[@macro]`, `[/@macro]`, `[=expr]` |

Extensiones asociadas: `.ftl`, `.ftlh` (auto-escape HTML), `.ftlx` (auto-escape XML).

## Requisitos

- IntelliJ IDEA **2025.3** (build `253`) o superior, hasta `263.*`.
- JDK **21** para compilar.

## Compilar

```powershell
$env:JAVA_HOME = "<ruta a un JDK 21>"
.\gradlew.bat buildPlugin
```

El ZIP instalable queda en `build/distributions/`.

### Modo offline (plataforma local)

Si la máquina no puede resolver el API de releases de JetBrains:

```powershell
.\gradlew.bat buildPlugin "-PidePath=<ruta al IDE descomprimido>"
```

o guarda la ruta en `%USERPROFILE%\.gradle\gradle.properties`:

```properties
idePath=C:/ruta/al/ide253
```

`local(...)` exige el IDE **descomprimido** (carpeta con `bin/`, `lib/` y `product-info.json`), no
el ZIP.

## Instalar

1. `Settings > Plugins > ⚙ > Install Plugin from Disk…`
2. Selecciona el ZIP de `build/distributions/`
3. Reinicia el IDE

Comprobación: con un `.ftl` abierto, la barra de estado debe decir **FreeMarker (highlight)** y en
`Settings > Editor > Color Scheme` debe aparecer **FreeMarker (highlight)**.

## Tests

```powershell
.\gradlew.bat test
```

`FreemarkerLexerTest` es JUnit puro (sin *platform test framework*, funciona offline). Cada `.ftl` de
`src/test/testData/lexer` se lexa y se compara con el volcado de tokens de su `.txt`. Si falta el
`.txt`, el test lo escribe y falla: hay que revisarlo. Para regenerarlos tras un cambio intencionado:

```powershell
.\gradlew.bat test "-Dftl.golden.update=true"
```

## Diseño

Dos sintaxis conviven en FreeMarker (`<#if>` y `[#if]`), y la forma de interpolación (`${x}` o
`[=x]`) es independiente de la de las etiquetas. El lexer guarda, dentro del `int` de
`getState()`, qué terminador espera (`>`, `]` o `}`) y a qué estado volver, de modo que el relexado
incremental del editor no pierde contexto:

```
S_TEXT → S_FTL_PENDING → S_FTL_BODY → (">" | "]" | "}") → estado de retorno
S_TEXT → S_TAG_NAME → S_TAG_BODY → S_TAG_VALUE_* → (FTL dentro de un atributo) → retorno
```

Sin parser/PSI: `FreemarkerParserDefinition` produce un único nodo plano `FTL_FILE`. El resaltado no
depende de él, así que no hay navegación, referencias ni inspecciones.

## Límites conocidos (v1)

- Sin parser: no hay validación de expresiones ni de directivas.
- Las cadenas son atómicas: `${...}` dentro de `"..."` no se separa (sí es válido en FreeMarker).
- Sin literales etiquetados de fecha/hora más allá de `r"..."` / `f"..."`.
- Si algún día se activa el plugin de JetBrains, su file type puede reclamar `.ftl`; elige el
  nuestro en `Settings > Editor > File Types`.

## Roadmap sugerido

1. PSI real: nodos de directiva, macro e interpolación.
2. `BraceMatcher` para `<#if>…</#if>` y `[#if]…[/#if]`.
3. `CompletionContributor` con directivas, builtins y macros conocidas.
4. Resolución de `<#import>` / `<#include>` a otros `.ftl` del proyecto.
