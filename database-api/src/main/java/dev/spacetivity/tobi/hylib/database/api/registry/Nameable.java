package dev.spacetivity.tobi.hylib.database.api.registry;

import java.io.Serializable;

/**
 * Objects with a name property. Extends {@link Serializable}.
 *
 * @see Serializable
 * @since 1.0
 */
public interface Nameable extends Serializable {

    /**
     * Returns the name.
     *
     * @return the name, may be null
     */
    String getName();

    /**
     * Sets the name.
     *
     * @param name the new name, may be null
     */
    void setName(String name);

}
