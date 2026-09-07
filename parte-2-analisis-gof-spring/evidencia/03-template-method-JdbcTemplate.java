// Fuente: spring-projects/spring-framework, modulo spring-jdbc
// Clase: org.springframework.jdbc.core.JdbcTemplate
// URL: https://github.com/spring-projects/spring-framework/blob/main/spring-jdbc/src/main/java/org/springframework/jdbc/core/JdbcTemplate.java

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
