package dev.spacetivity.tobi.hylib.hytale.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event handler. Method must have exactly one parameter (the event type).
 *
 * @see dev.spacetivity.tobi.hylib.hytale.common.api.event.EventRegistrar
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
}
