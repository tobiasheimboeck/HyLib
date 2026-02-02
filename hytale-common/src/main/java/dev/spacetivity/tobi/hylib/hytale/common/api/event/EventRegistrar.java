package dev.spacetivity.tobi.hylib.hytale.common.api.event;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import dev.spacetivity.tobi.hylib.hytale.api.event.EventHandler;

import java.lang.reflect.Method;

/**
 * Registers all methods annotated with {@link EventHandler} from a listener object.
 * Handler methods must have exactly one parameter (the event type).
 *
 * @see EventHandler
 * @since 1.0
 */
public final class EventRegistrar {

    private EventRegistrar() {
        // Utility class
    }

    /**
     * Registers all {@link EventHandler} methods from the listener with the plugin.
     *
     * @param plugin   the plugin to register with
     * @param listener the object with event handler methods
     * @throws IllegalArgumentException if an @EventHandler method has not exactly one parameter
     * @throws NullPointerException if plugin or listener is null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerEvents(JavaPlugin plugin, Object listener) {
        if (plugin == null) {
            throw new NullPointerException("plugin cannot be null");
        }
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }

        Class<?> clazz = listener.getClass();
        
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                // Validate method signature
                if (method.getParameterCount() != 1) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Method %s.%s annotated with @EventHandler must have exactly one parameter (the event), but has %d parameters",
                            clazz.getName(),
                            method.getName(),
                            method.getParameterCount()
                        )
                    );
                }

                Class<?> eventType = method.getParameterTypes()[0];
                
                // Register the event handler
                try {
                    method.setAccessible(true);
                    
                    // Create a method reference handler
                    plugin.getEventRegistry().registerGlobal(
                        (Class) eventType,
                        (event) -> {
                            try {
                                method.invoke(listener, event);
                            } catch (Exception e) {
                                System.err.println(
                                    String.format("[%s] Error invoking event handler %s.%s: %s", 
                                        plugin.getName(),
                                        clazz.getName(), 
                                        method.getName(),
                                        e.getMessage()
                                    )
                                );
                                e.printStackTrace();
                            }
                        }
                    );
                    
                    // Debug logging (if needed, uncomment)
                    // plugin.getLogger().info(
                    //     String.format("Registered event handler %s.%s for event type %s",
                    //         clazz.getSimpleName(),
                    //         method.getName(),
                    //         eventType.getSimpleName()
                    //     )
                    // );
                } catch (Exception e) {
                    System.err.println(
                        String.format("[%s] Failed to register event handler %s.%s: %s", 
                            plugin.getName(),
                            clazz.getName(), 
                            method.getName(),
                            e.getMessage()
                        )
                    );
                    e.printStackTrace();
                }
            }
        }
    }
}
