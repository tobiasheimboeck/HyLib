package dev.spacetivity.tobi.hylib.database.api.registry;

import java.io.Serializable;

/**
 * Interface for objects that have a name property.
 * 
 * <p>This interface provides a simple contract for objects that have a name that can
 * be retrieved and modified. It extends {@link Serializable} to allow objects implementing
 * this interface to be serialized.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * public class NamedEntity implements Nameable {
 *     private String name;
 *     
 *     @Override
 *     public String getName() {
 *         return name;
 *     }
 *     
 *     @Override
 *     public void setName(String name) {
 *         this.name = name;
 *     }
 * }
 * }</pre>
 * 
 * <h3>Serialization</h3>
 * 
 * <p>Since this interface extends {@link Serializable}, implementing classes should
 * ensure they are properly serializable. Consider using Lombok's {@code @Getter} and
 * {@code @Setter} annotations for convenience.
 * 
 * @see Serializable
 * @since 1.0
 */
public interface Nameable extends Serializable {

    /**
     * Gets the name of this object.
     * 
     * @return the name, may be null
     */
    String getName();

    /**
     * Sets the name of this object.
     * 
     * @param name the new name, may be null
     */
    void setName(String name);

}
