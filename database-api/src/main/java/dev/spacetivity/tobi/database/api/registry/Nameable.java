package dev.spacetivity.tobi.database.api.registry;

import java.io.Serializable;

public interface Nameable extends Serializable {

    String getName();

    void setName(String name);

}
