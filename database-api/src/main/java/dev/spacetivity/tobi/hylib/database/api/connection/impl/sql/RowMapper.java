package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Functional interface for mapping a single row from a {@link ResultSet} to a domain object.
 * 
 * <p>This interface provides a clean separation between SQL query execution and domain
 * object mapping. Implementations extract data from the current row of the ResultSet and
 * construct a domain object.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * // Using a lambda
 * RowMapper<User> mapper = (ResultSet rs) -> {
 *     return new User(
 *         rs.getInt("id"),
 *         rs.getString("name"),
 *         rs.getString("email")
 *     );
 * };
 * 
 * // Using a method reference
 * RowMapper<User> mapper = this::mapUser;
 * 
 * private User mapUser(ResultSet rs) throws SQLException {
 *     return new User(
 *         rs.getInt("id"),
 *         rs.getString("name"),
 *         rs.getString("email")
 *     );
 * }
 * }</pre>
 * 
 * <h3>ResultSet State</h3>
 * 
 * <p>The ResultSet cursor is positioned at the current row when this method is called.
 * You should only read data from the current row and not move the cursor. The ResultSet
 * is managed by the calling code and will be closed automatically.
 * 
 * <h3>Exception Handling</h3>
 * 
 * <p>If a database access error occurs, implementations should let the {@code SQLException}
 * propagate. The calling code will handle the exception appropriately.
 * 
 * @param <T> the type of the domain object to map to
 * @see ResultSet
 * @see dev.spacetivity.tobi.database.api.repository.impl.AbstractMariaDbRepository
 * @since 1.0
 */
@FunctionalInterface
public interface RowMapper<T> {
    
    /**
     * Maps the current row of the ResultSet to a domain object.
     * 
     * <p>The ResultSet cursor is positioned at the current row. This method should extract
     * data from the current row and construct a domain object. The cursor should not be moved.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * RowMapper<User> mapper = (ResultSet rs) -> {
     *     int id = rs.getInt("id");
     *     String name = rs.getString("name");
     *     String email = rs.getString("email");
     *     return new User(id, name, email);
     * };
     * }</pre>
     * 
     * @param rs the ResultSet positioned at the current row
     * @return the mapped domain object
     * @throws SQLException if a database access error occurs or the column index/name is invalid
     */
    T map(ResultSet rs) throws SQLException;
}
