package dev.spacetivity.tobi.hylib.hytale.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods as event handlers that should be automatically registered.
 * 
 * <p>Methods annotated with {@code @EventHandler} will be automatically discovered and
 * registered with the Hytale event system. The method must have exactly one parameter,
 * which must be a subclass of the event type you want to listen to.
 * 
 * <h3>Example Usage</h3>
 * 
 * <pre>{@code
 * public class MyEventListener {
 *     
 *     @EventHandler
 *     public void onPlayerReady(PlayerReadyEvent event) {
 *         Player player = event.getPlayer();
 *         player.sendMessage(Message.raw("Welcome " + player.getDisplayName()));
 *     }
 * }
 * 
 * // In your plugin setup:
 * EventRegistrar.registerEvents(plugin, new MyEventListener());
 * }</pre>
 * 
 * <h3>Requirements</h3>
 * 
 * <ul>
 *   <li>The method must have exactly one parameter (the event object)</li>
 *   <li>The parameter type must be a valid Hytale event class</li>
 *   <li>The method can be private, protected, or public</li>
 * </ul>
 * 
 * @see dev.spacetivity.tobi.hylib.hytale.common.api.event.EventRegistrar
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
}
