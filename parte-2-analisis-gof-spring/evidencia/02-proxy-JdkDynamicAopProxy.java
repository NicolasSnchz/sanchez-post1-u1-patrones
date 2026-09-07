// Fuente: spring-projects/spring-framework, modulo spring-aop
// Clase: org.springframework.aop.framework.JdkDynamicAopProxy
// URL: https://github.com/spring-projects/spring-framework/blob/main/spring-aop/src/main/java/org/springframework/aop/framework/JdkDynamicAopProxy.java

// El proxy implementa InvocationHandler: toda llamada al objeto real pasa por aqui
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Cadena de interceptores aplicables a este metodo concreto
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
        if (chain.isEmpty()) {
            // Sin aspectos: se delega directamente al objeto real
            retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
        } else {
            // Con aspectos: se ejecutan antes y despues de la llamada real
            MethodInvocation invocation =
                    new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
            retVal = invocation.proceed();
        }
        return retVal;
    }
}
