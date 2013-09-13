package at.ac.tuwien.dsg.scaledom.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.ConstructorUtils;

/**
 * A simple component factory.<br/>
 * Users may either:
 * <ul>
 * <li>bind an instance to a type, or</li>
 * <li>bind an implementation type to a type</li>
 * </ul>
 * If instances are bound, the user may request the bound instance any time by calling {@link #getInstance(Class)}. If
 * an implementation type is bound, the user may request a new instance of that implementation type by calling
 * {@link #getNewInstance(Class, Object...)}, providing possible constructor parameters as ellipsis arguments.
 * 
 * @author Dominik Rauch
 */
public class ComponentFactory {

	/** Bound instances. */
	private final Map<Class<?>, Object> instances;
	/** Bound implementation classes. */
	private final Map<Class<?>, Class<?>> bindings;

	/**
	 * Default constructor.
	 */
	public ComponentFactory() {
		instances = new HashMap<Class<?>, Object>();
		bindings = new HashMap<Class<?>, Class<?>>();
	}

	/**
	 * Binds an existing instance to a type.
	 * 
	 * @param type the type.
	 * @param instance the instance.
	 */
	public <T, U extends T> void bind(final Class<T> type, final U instance) {
		checkNotNull(type, "Argument type must not be null.");
		checkNotNull(instance, "Argument instance must not be null.");

		instances.put(type, instance);
	}

	/**
	 * Binds a new instance to a type.
	 * 
	 * @param type the type.
	 * @param implType the implementation type.
	 * @param args possible constructor arguments.
	 * @throws InstantiationException If the instantiation of the new instance fails.
	 */
	public <T> void bind(final Class<T> type, final Class<? extends T> implType, final Object... args)
			throws InstantiationException {
		bind(type, getNewInstanceInternal(implType, args));
	}

	/**
	 * Binds an implementation type to a type.
	 * 
	 * @param type the type.
	 * @param implType the implementation type.
	 */
	public <T> void bind(final Class<T> type, final Class<? extends T> implType) {
		checkNotNull(type, "Argument type must not be null.");
		checkNotNull(implType, "Argument implType must not be null.");

		bindings.put(type, implType);
	}

	/**
	 * Returns the bound instance.
	 * 
	 * @param type the type.
	 * @return the bound instance.
	 * @throws IllegalArgumentException If no instance is bound to the given type.
	 */
	public <T> T getInstance(final Class<T> type) {
		checkNotNull(type, "Argument type must not be null.");
		checkArgument(instances.containsKey(type), "No instance is bound to type '%s'.", type.getName());

		@SuppressWarnings("unchecked")
		final T instance = (T) instances.get(type);
		return instance;
	}

	/**
	 * Returns a new instance of the bound implementation type as a reference to type.
	 * 
	 * @param type the type.
	 * @param args possible constructor parameters.
	 * @return the new instance.
	 * @throws IllegalArgumentException If no implementation type is bound to the given type.
	 * @throws InstantiationException If the instantiation of the new instance fails.
	 */
	public <T> T getNewInstance(final Class<T> type, final Object... args) throws InstantiationException {
		checkNotNull(type, "Argument type must not be null.");
		checkArgument(bindings.containsKey(type), "No implementation class is bound to type '%s'.", type.getName());

		@SuppressWarnings("unchecked")
		final Class<T> implType = (Class<T>) bindings.get(type);
		return getNewInstanceInternal(implType, args);
	}

	private <T> T getNewInstanceInternal(final Class<? extends T> implType, final Object... args)
			throws InstantiationException {
		try {
			return ConstructorUtils.invokeConstructor(implType, args);
		} catch (final IllegalAccessException ex) {
			throw new InstantiationException("Type '" + implType.getName() + "' could not be instantiated: "
					+ ex.getMessage());
		} catch (final InvocationTargetException ex) {
			throw new InstantiationException("Type '" + implType.getName() + "' could not be instantiated: "
					+ ex.getMessage());
		} catch (final NoSuchMethodException ex) {
			throw new InstantiationException("Type '" + implType.getName() + "' could not be instantiated: "
					+ ex.getMessage());
		}
	}
}
