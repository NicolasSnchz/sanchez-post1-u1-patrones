// Fuente: spring-projects/spring-framework, modulo spring-beans
// Clase: org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
// URL: https://github.com/spring-projects/spring-framework/blob/main/spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultSingletonBeanRegistry.java

// Cache de instancias unicas: nombre del bean -> instancia compartida
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
    synchronized (this.singletonLock) {
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null) {
            // Solo se crea si el contenedor aun no tiene la instancia
            singletonObject = singletonFactory.getObject();
            addSingleton(beanName, singletonObject);
        }
        return singletonObject;
    }
}
