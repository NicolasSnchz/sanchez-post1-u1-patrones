# Post-contenido — Unidad 1: Fundamentos de Patrones de Diseño y Buenas Prácticas

Repositorio del post-contenido de la Unidad 1 de Patrones de Diseño de Software, sexto semestre. Contiene las dos partes de la actividad: la refactorización SOLID de un God Object en `parte-1-refactorizacion-solid/` y el análisis de patrones GoF en Spring Framework en `parte-2-analisis-gof-spring/`.

## Análisis de Violaciones SOLID

| Principio | Método/Sección afectada | Descripción de la violación |
|-----------|-------------------------|-----------------------------|
| SRP | calculateTotal + applyDiscount + saveOrder + sendEmail + printReport | La clase concentra cinco responsabilidades que cambian por razones distintas: cálculo tributario, política comercial de descuentos, persistencia, notificación y presentación. Cualquier cambio en una de esas reglas obliga a modificar y recompilar la misma clase, con riesgo de afectar las demás. |
| OCP | applyDiscount (if/else sobre customerType) | El descuento se decide con una cadena de condicionales sobre un String. Incorporar un nuevo tipo de cliente exige editar el cuerpo de un método ya probado en lugar de agregar código nuevo, de modo que la clase no está cerrada a modificación. |
| DIP | Toda la clase (dependencias internas sin abstracciones) | La clase instancia y usa directamente sus colaboradores, la lista concreta de órdenes y la salida por consola, sin depender de abstracciones ni recibirlas por constructor. Esto impide sustituir la persistencia o el canal de notificación y hace imposible probar la lógica de forma aislada. |

## Parte 1 — Refactorización SOLID

Proyecto Maven que refactoriza `OrderProcessor` aplicando SRP, OCP y DIP. La responsabilidad única se logra separando `TaxCalculator`, `OrderRepository`, `EmailNotifier` y `OrderReporter`. La apertura a extensión se logra con la interfaz `DiscountStrategy` y sus implementaciones. La inversión de dependencias se logra inyectando los colaboradores por constructor en `OrderService`.

La clase `OrderProcessor` se conserva sin modificar como línea base del análisis, marcada como obsoleta y fuera del flujo que ejecuta `Main`.

### Cómo ejecutarlo

```
cd parte-1-refactorizacion-solid
mvn compile
mvn exec:java
```

Requiere Java 17 o superior y Apache Maven 3.8 o superior.

## Parte 2 — Análisis de Patrones GoF en Spring

| # | Patrón | Categoría | Clase en Spring |
|---|--------|-----------|-----------------|
| 1 | Singleton | Creacional | org.springframework.beans.factory.support.DefaultSingletonBeanRegistry |
| 2 | Proxy | Estructural | org.springframework.aop.framework.JdkDynamicAopProxy |
| 3 | Template Method | Comportamiento | org.springframework.jdbc.core.JdbcTemplate |

Ver `parte-2-analisis-gof-spring/documento-analisis.md`. Los extractos de código fuente usados como evidencia están en `parte-2-analisis-gof-spring/evidencia/`.

## Herramientas utilizadas

- Java 17, Apache Maven, VS Code, Git, GitHub
- Código fuente de Spring Framework (investigación)

## Conclusiones

La refactorización de la Parte 1 mostró que los principios SOLID no actúan por separado: separar responsabilidades fue la condición previa para poder extraer la estrategia de descuento y para poder inyectar las dependencias por constructor. El paso de una cadena de condicionales a una interfaz convirtió una modificación de código en una simple extensión, que es el efecto práctico del principio abierto/cerrado. El análisis de la Parte 2 confirmó ese mismo razonamiento en un framework real, donde cada patrón identificado responde a un problema concreto de Spring y sostiene un principio SOLID identificable. El aprendizaje central es que un patrón se justifica por el problema que resuelve y no por su nombre, criterio que se aplicó al elegir Strategy en lugar de ampliar el condicional original.
