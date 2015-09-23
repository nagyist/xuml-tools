package xuml.tools.model.compiler.runtime.message;

import java.io.Serializable;

import com.google.common.base.Optional;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import xuml.tools.model.compiler.runtime.Entity;
import xuml.tools.model.compiler.runtime.Event;

public class Signal<T> {

    private final Class<Entity<T>> entityClass;
    private final Event<T> event;
    private final long id;
    // epoch time ms to process signal
    private final Long timeMs;
    private final Optional<FiniteDuration> repeatInterval;
    private final String fromEntityUniqueId;
    private final Serializable entityId;
    private final String entityUniqueId;

    public Signal(String fromEntityUniqueId, Class<Entity<T>> entityClass, Event<T> event, long id,
            Long timeMs, Optional<FiniteDuration> repeatInterval, Serializable entityId,
            String entityUniqueId) {
        if (entityId instanceof Optional)
            throw new RuntimeException("unexpected");
        this.fromEntityUniqueId = fromEntityUniqueId;
        this.entityClass = entityClass;
        this.event = event;
        this.id = id;
        this.timeMs = timeMs;
        this.repeatInterval = repeatInterval;
        this.entityId = entityId;
        this.entityUniqueId = entityUniqueId;
    }

    public Signal(String fromEntityUniqueId, Class<Entity<T>> entityClass, Event<T> event, long id,
            Long timeMs, Serializable entityId, String entityUniqueId) {
        this(fromEntityUniqueId, entityClass, event, id, timeMs, null, entityId, entityUniqueId);
    }

    public Signal(String fromEntityUniqueId, Class<Entity<T>> entityClass, Event<T> event, long id,
            Duration delay, FiniteDuration repeatInterval, Serializable entityId,
            String entityUniqueId) {
        this(fromEntityUniqueId, entityClass, event, id, getTime(delay),
                Optional.of(repeatInterval), entityId, entityUniqueId);
    }

    private static Long getTime(Duration delay) {
        if (delay == null)
            return null;
        else
            return System.currentTimeMillis() + delay.toMillis();
    }

    public Signal(String fromEntityUniqueId, Class<Entity<T>> entityClass, Event<T> event, long id,
            Serializable entityId, String entityUniqueId) {
        this(fromEntityUniqueId, entityClass, event, id, null, entityId, entityUniqueId);
    }

    public long getId() {
        return id;
    }

    public Optional<FiniteDuration> getRepeatInterval() {
        return repeatInterval;
    }

    public Class<Entity<T>> getEntityClass() {
        return entityClass;
    }

    public Event<T> getEvent() {
        return event;
    }

    public Long getTime() {
        return timeMs;
    }

    public Serializable getEntityId() {
        return entityId;
    }

    public String getFromEntityUniqueId() {
        return fromEntityUniqueId;
    }

    public String getEntityUniqueId() {
        return entityUniqueId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Signal [id=");
        builder.append(id);
        builder.append(", event=");
        builder.append(event);
        builder.append(", entityClass");
        builder.append(entityClass.getName());
        builder.append(", timeMs=");
        builder.append(timeMs);
        builder.append(", repeatInterval=");
        builder.append(repeatInterval);
        builder.append("]");
        return builder.toString();
    }

}
