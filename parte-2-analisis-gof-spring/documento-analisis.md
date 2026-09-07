# Análisis de Patrones GoF en Spring Framework

**Estudiante:** Nicolás Sánchez
**Código:** 02220131023
**Curso:** Patrones de Diseño de Software
**Unidad:** 1 — Fundamentos de Patrones de Diseño y Buenas Prácticas
**Programa:** Ingeniería de Sistemas, Universidad de Santander
**Fecha:** 6 de septiembre de 2026

## 1. Introducción

Spring Framework es el marco de trabajo más extendido para el desarrollo de aplicaciones empresariales en Java, y Spring Boot es la capa que lo hace directamente ejecutable mediante autoconfiguración y dependencias preempaquetadas. Detrás de esa aparente simplicidad hay un diseño interno que se apoya de forma sistemática en los patrones catalogados por Gamma et al. (1994), lo que convierte a su código fuente en un objeto de estudio adecuado para observar cómo se aplican los patrones fuera de un ejemplo académico.

El presente documento analiza tres patrones GoF identificados directamente en el código fuente del framework, seleccionados de categorías distintas: Singleton como patrón creacional, Proxy como patrón estructural y Template Method como patrón de comportamiento. Para cada uno se indica la clase concreta y el módulo donde reside, el problema específico que el framework necesitaba resolver, un extracto del código que evidencia la implementación y el principio SOLID que el patrón refuerza en ese contexto. El objetivo es mostrar que la elección de un patrón en un framework maduro responde a una restricción técnica concreta y no a una preferencia de estilo.

## 2. Análisis de Patrón 1: Singleton

**Categoría:** Creacional

Singleton garantiza que una clase tenga una sola instancia y proporciona un punto de acceso global a ella (Gamma et al., 1994). Su propósito general es controlar la creación de objetos cuya existencia múltiple sería costosa, inconsistente o carente de sentido dentro del sistema.

**Ubicación en Spring Framework.** El patrón se encuentra en la clase `org.springframework.beans.factory.support.DefaultSingletonBeanRegistry`, que reside en el módulo `spring-beans` y de la cual heredan las implementaciones del contenedor de inversión de control, entre ellas `DefaultListableBeanFactory`. Esta clase implementa la interfaz `SingletonBeanRegistry` y es el componente que sostiene el ámbito por defecto de todos los beans de Spring.

**Problema que resuelve.** Una aplicación Spring Boot registra decenas o cientos de componentes anotados con `@Service`, `@Repository` o `@Component`. La mayoría de ellos no guarda estado propio de un usuario y su construcción implica resolver dependencias, leer configuración o abrir recursos. Si el contenedor creara un objeto nuevo cada vez que otro componente lo solicita, el costo de inicialización se multiplicaría y, sobre todo, se perdería la garantía de que dos partes de la aplicación comparten el mismo repositorio, la misma caché o el mismo pool de conexiones. La alternativa directa, que consistiría en implementar el Singleton clásico con un campo estático dentro de cada clase de servicio, tendría dos consecuencias graves: obligaría al programador a escribir el control de instancia en cada componente y ataría el código de la aplicación a una llamada estática imposible de sustituir en pruebas. Spring resuelve el problema desplazando la responsabilidad del Singleton fuera de las clases de negocio y hacia el contenedor, que mantiene un registro central de instancias únicas indexadas por nombre de bean.

**Evidencia de código.**

```java
// org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
    synchronized (this.singletonLock) {
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null) {
            singletonObject = singletonFactory.getObject();
            addSingleton(beanName, singletonObject);
        }
        return singletonObject;
    }
}
```

El mapa `singletonObjects` es la caché de instancias únicas. El método consulta primero si el bean ya existe y solo invoca la fábrica cuando no lo encuentra, tras lo cual lo registra. El bloque sincronizado protege esa comprobación frente a accesos concurrentes durante el arranque del contexto.

**Principio SOLID que refuerza.** El patrón refuerza aquí el principio de responsabilidad única, porque la gestión del ciclo de vida y de la unicidad de las instancias queda concentrada en el registro del contenedor y desaparece de las clases de negocio, que solo se ocupan de su propia lógica. De forma indirecta habilita también el principio de inversión de dependencias, ya que al no exponer un acceso estático permite que los componentes reciban la instancia compartida por constructor y dependan de una interfaz en lugar de una clase concreta. Sin este patrón, Spring Boot tendría que construir un objeto por cada punto de inyección, lo que haría inviable compartir estado de infraestructura y elevaría el tiempo de arranque de forma proporcional al número de dependencias.

## 3. Análisis de Patrón 2: Proxy

**Categoría:** Estructural

Proxy proporciona un sustituto o representante de otro objeto para controlar el acceso a él (Gamma et al., 1994). El sustituto expone la misma interfaz que el objeto real, de manera que quien lo usa no distingue entre ambos, y aprovecha esa posición intermedia para añadir comportamiento antes o después de delegar la llamada.

**Ubicación en Spring Framework.** El patrón aparece en la clase `org.springframework.aop.framework.JdkDynamicAopProxy`, del módulo `spring-aop`, que implementa las interfaces `AopProxy` e `InvocationHandler`. Es el mecanismo que Spring emplea cuando el objeto a interceptar implementa al menos una interfaz; cuando no lo hace, el framework recurre a la variante basada en subclases de la clase `CglibAopProxy`, dentro del mismo módulo.

**Problema que resuelve.** Funcionalidades como la gestión de transacciones con `@Transactional`, el control de acceso de Spring Security o el registro de auditoría deben ejecutarse alrededor de métodos de negocio, pero no pertenecen a la lógica de negocio. Escribirlas dentro de cada método produciría código repetido en toda la aplicación y mezclaría preocupaciones distintas en la misma clase. La alternativa directa, que sería obligar al programador a abrir y cerrar la transacción manualmente en cada servicio, es exactamente el problema que el framework busca eliminar. Spring lo resuelve envolviendo el bean real en un proxy que implementa sus mismas interfaces y registrando ese proxy en el contenedor. Cuando otro componente invoca un método, en realidad invoca al proxy, que ejecuta la cadena de interceptores configurada y solo entonces delega en el objeto real. El resultado es que una anotación sobre un método basta para envolverlo en una transacción sin que su cuerpo cambie una sola línea.

**Evidencia de código.**

```java
// org.springframework.aop.framework.JdkDynamicAopProxy
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
        if (chain.isEmpty()) {
            retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
        } else {
            MethodInvocation invocation =
                    new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
            retVal = invocation.proceed();
        }
        return retVal;
    }
}
```

La clase implementa `InvocationHandler`, de modo que toda llamada al objeto sustituido pasa por el método `invoke`. Allí se consulta qué interceptores aplican al método concreto que se está invocando. Si no hay ninguno, la llamada se delega tal cual al objeto real. Si los hay, se construye una invocación que los ejecuta en orden y termina alcanzando el método original.

**Principio SOLID que refuerza.** El patrón implementa el principio abierto/cerrado, ya que permite añadir comportamiento a un servicio existente sin modificar su código fuente: basta con registrar un nuevo aspecto. Refuerza además el principio de responsabilidad única, porque las preocupaciones transversales quedan en clases propias y no invaden el cuerpo de los métodos de negocio. Si Spring Boot no usara este patrón, cada método transaccional tendría que gestionar de forma explícita la conexión, el commit y el rollback, con lo que la anotación `@Transactional` no podría existir.

## 4. Análisis de Patrón 3: Template Method

**Categoría:** Comportamiento

Template Method define el esqueleto de un algoritmo en una operación y delega algunos de sus pasos en las subclases o en objetos colaboradores, permitiendo variar esos pasos sin alterar la estructura general del algoritmo (Gamma et al., 1994).

**Ubicación en Spring Framework.** El patrón se observa en la clase `org.springframework.jdbc.core.JdbcTemplate`, del módulo `spring-jdbc`, y en particular en su familia de métodos `execute`, que reciben un objeto de tipo `StatementCallback` o `PreparedStatementCallback`. El mismo enfoque se repite en otras clases del framework cuyo nombre termina en `Template`, como `RestTemplate` o `TransactionTemplate`.

**Problema que resuelve.** El acceso a una base de datos mediante JDBC exige una secuencia rígida de pasos: obtener la conexión, crear la sentencia, ejecutarla, procesar el resultado, capturar la excepción y liberar los recursos en un bloque final. De esos pasos, solo uno cambia entre una consulta y otra, que es la sentencia concreta y el tratamiento del resultado. Todo lo demás es idéntico y, además, es la parte donde se concentran los errores más costosos, porque olvidar cerrar una conexión agota el pool de la aplicación. Escribir esa secuencia completa en cada consulta multiplicaría el código repetido y el riesgo de fuga de recursos. Spring invierte el reparto: el framework escribe una vez la secuencia completa y el programador aporta únicamente el fragmento variable a través de una función de retorno.

**Evidencia de código.**

```java
// org.springframework.jdbc.core.JdbcTemplate
public <T> T execute(StatementCallback<T> action) throws DataAccessException {
    Connection con = DataSourceUtils.getConnection(obtainDataSource());  // paso fijo
    Statement stmt = null;
    try {
        stmt = con.createStatement();                                    // paso fijo
        applyStatementSettings(stmt);                                    // paso fijo
        T result = action.doInStatement(stmt);                           // paso variable
        handleWarnings(stmt);
        return result;
    } catch (SQLException ex) {
        throw translateException("StatementCallback", sql, ex);          // paso fijo
    } finally {
        JdbcUtils.closeStatement(stmt);                                  // paso fijo
        DataSourceUtils.releaseConnection(con, getDataSource());         // paso fijo
    }
}
```

Los pasos de obtención de la conexión, creación de la sentencia, traducción de la excepción y liberación de recursos son invariantes y están escritos una sola vez. La única línea que cambia entre una operación y otra es la invocación de `doInStatement`, que corresponde al paso variable aportado por quien llama. Conviene señalar que Spring aplica aquí la variante del patrón basada en delegación mediante una interfaz de retorno en lugar de la herencia clásica descrita por el catálogo original, lo que evita obligar al programador a extender la clase.

**Principio SOLID que refuerza.** El patrón sostiene el principio abierto/cerrado, porque permite definir operaciones de acceso a datos nuevas sin tocar la clase que contiene el algoritmo. También refuerza el principio de inversión de dependencias, ya que la parte fija del algoritmo depende de la abstracción `StatementCallback` y no de ninguna consulta concreta. En ausencia de este patrón, cada método de repositorio en una aplicación Spring Boot debería repetir la gestión completa de la conexión y del cierre de recursos, con el consiguiente aumento de código duplicado y de errores de agotamiento del pool.

## 5. Conclusiones

El análisis muestra que en Spring Framework los patrones no aparecen como adorno de diseño sino como respuesta a restricciones concretas: el Singleton evita la multiplicación de instancias de infraestructura, el Proxy permite añadir comportamiento transversal sin tocar el código de negocio y el Template Method elimina la repetición de secuencias frágiles de manejo de recursos. En los tres casos el patrón traslada una responsabilidad desde el código de la aplicación hacia el framework, y en los tres casos esa decisión se traduce en un principio SOLID identificable. La lección para el diseño propio es que la elección de un patrón debe partir de nombrar primero el problema y el principio que se quiere sostener, tal como se hizo en la Parte 1 de este post contenido al reemplazar la cadena de condicionales de los descuentos por una interfaz de estrategia.

## 6. Referencias

Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design patterns: Elements of reusable object-oriented software*. Addison-Wesley.

Martin, R. C. (2017). *Clean architecture: A craftsman's guide to software structure and design*. Prentice Hall.

Spring Team. (2026). *Spring Framework documentation: Core technologies*. https://docs.spring.io/spring-framework/reference/core.html

Spring Team. (2026). *Spring Framework source code* [Repositorio de código]. GitHub. https://github.com/spring-projects/spring-framework
