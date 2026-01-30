package dev.spacetivity.tobi.hylib.database.api.repository;

/**
 * Marker interface for database repositories.
 * 
 * <p>This interface serves as a marker for classes that implement database repository
 * patterns. Repositories provide a clean abstraction layer between your application
 * logic and database access code.
 * 
 * <h3>Usage Pattern</h3>
 * 
 * <p>Repositories are typically registered with a {@link RepositoryLoader} and can
 * be retrieved by their class type:
 * 
 * <pre>{@code
 * public class UserRepository extends AbstractMariaDbRepository<User> implements Repository {
 *     // Repository implementation
 * }
 * 
 * RepositoryLoader loader = DatabaseProvider.getApi().getRepositoryLoader();
 * loader.register(new UserRepository(...));
 * 
 * UserRepository repo = loader.getRepository(UserRepository.class);
 * }</pre>
 * 
 * <h3>Implementation</h3>
 * 
 * <p>For MariaDB repositories, extend {@link dev.spacetivity.tobi.database.api.repository.impl.AbstractMariaDbRepository}
 * which provides common database operations.
 * 
 * @see RepositoryLoader
 * @see dev.spacetivity.tobi.database.api.repository.impl.AbstractMariaDbRepository
 * @since 1.0
 */
public interface Repository {
}
