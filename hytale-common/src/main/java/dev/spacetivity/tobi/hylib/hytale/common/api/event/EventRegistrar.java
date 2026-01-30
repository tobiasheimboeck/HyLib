package dev.spacetivity.tobi.hylib.hytale.common.api.event;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import dev.spacetivity.tobi.hylib.hytale.api.event.EventHandler;

import java.lang.reflect.Method;

/**
 * Utility class for automatically registering event handlers annotated with {@link EventHandler}.
 * 
 * <p>This class scans an object for methods annotated with {@code @EventHandler} and
 * automatically registers them with the Hytale event system. This eliminates the need
 * for manual event registration.
 * 
 * <h3>Example Usage</h3>
 * 
 * <pre>{@code
 * public class MyPlugin extends JavaPlugin {
 *     
 *     @Override
 *     protected void setup() {
 *         super.setup();
 *         
 *         // Register events from this class
 *         EventRegistrar.registerEvents(this, this);
 *         
 *         // Or from another listener class
 *         MyEventListener listener = new MyEventListener();
 *         EventRegistrar.registerEvents(this, listener);
 *     }
 * }
 * 
 * public class MyEventListener {
 *     
 *     @EventHandler
 *     public void onPlayerReady(PlayerReadyEvent event) {
 *         // Handle event
 *     }
 * }
 * }</pre>
 * 
 * <h3>Method Requirements</h3>
 * 
 * <p>Methods annotated with {@code @EventHandler} must:
 * <ul>
 *   <li>Have exactly one parameter (the event object)</li>
 *   <li>The parameter type must be a valid Hytale event class</li>
 * </ul>
 * 
 * @see EventHandler
 * @since 1.0
 */
public final class EventRegistrar {

    private EventRegistrar() {
        // Utility class
    }

    /**
     * Automatically registers all methods annotated with {@link EventHandler} from the
     * given listener object.
     * 
     * <p>This method scans the listener object's class for all methods annotated with
     * {@code @EventHandler} and registers them with the plugin's event registry.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * MyEventListener listener = new MyEventListener();
     * EventRegistrar.registerEvents(plugin, listener);
     * }</pre>
     * 
     * @param plugin   the plugin instance to register events with
     * @param listener the object containing event handler methods
     * @throws IllegalArgumentException if a method annotated with {@code @EventHandler}
     *                                  doesn't have exactly one parameter
     * @throws NullPointerException     if plugin or listener is null
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
