package org.cobalt.api.util

import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

@Suppress("UNUSED", "UNCHECKED_CAST")
object ReflectionUtils {

  /**
   * @param instance Object instance to get the field from
   * @param fieldName Name of the field
   * @return Value of the field cast to type T
   */
  @JvmStatic
  fun <T> getField(instance: Any, fieldName: String): T {
    val field = getField(instance::class.java, fieldName)
    field.makeAccessible()
    return field.get(instance) as T
  }

  /**
   * @param instance Object instance to set the field on
   * @param fieldName Name of the field
   * @param value Value to set
   */
  @JvmStatic
  fun setField(instance: Any, fieldName: String, value: Any?) {
    val field = getField(instance::class.java, fieldName)
    field.makeAccessible()
    field.set(instance, value)
  }

  @JvmStatic
  private fun getField(clazz: Class<*>, fieldName: String): Field {
    var current: Class<*>? = clazz

    while (current != null) {
      try {
        return current.getDeclaredField(fieldName)
      } catch (ignored: NoSuchFieldException) {
      }

      current = current.superclass
    }

    throw RuntimeException("Field $fieldName not found in class $clazz")
  }

  /**
   * @param instance Object instance to invoke the method on
   * @param methodName Name of the method
   * @param paramTypes Parameter types
   * @param args Arguments for the method
   * @return Result of method invocation cast to type T
   */
  @JvmStatic
  fun <T> invokeMethod(instance: Any, methodName: String, paramTypes: Array<Class<*>>, vararg args: Any?): T {
    val method = getMethod(instance::class.java, methodName, paramTypes)
    method.makeAccessible()
    return method.invoke(instance, *args) as T
  }

  @JvmStatic
  private fun getMethod(clazz: Class<*>, methodName: String, paramTypes: Array<Class<*>>): Method {
    var current: Class<*>? = clazz

    while (current != null) {
      try {
        return current.getDeclaredMethod(methodName, *paramTypes)
      } catch (ignored: NoSuchMethodException) {
      }

      current = current.superclass
    }

    throw RuntimeException("Method $methodName not found in class $clazz")
  }

  /**
   * @param clazz Class containing the static field
   * @param fieldName Name of the static field
   * @return Value of the static field cast to type T
   */
  @JvmStatic
  fun <T> getStaticField(clazz: Class<*>, fieldName: String): T {
    val field = getField(clazz, fieldName)
    field.makeAccessible()
    return field.get(null) as T
  }

  /**
   * @param clazz Class containing the static field
   * @param fieldName Name of the static field
   * @param value Value to set
   */
  @JvmStatic
  fun setStaticField(clazz: Class<*>, fieldName: String, value: Any?) {
    val field = getField(clazz, fieldName)
    field.makeAccessible()
    field.set(null, value)
  }

  /**
   * @param clazz Class to instantiate
   * @param paramTypes Constructor parameter types
   * @param args Arguments to pass
   * @return New instance of class
   */
  @JvmStatic
  fun <T> createInstance(clazz: Class<T>, paramTypes: Array<Class<*>>, vararg args: Any?): T {
    val constructor: Constructor<T> = clazz.getDeclaredConstructor(*paramTypes)
    constructor.makeAccessible()
    return constructor.newInstance(*args)
  }

  /**
   * @param clazz Class to inspect
   * @return List of all fields including inherited ones
   */
  @JvmStatic
  fun getAllFields(clazz: Class<*>): List<Field> {
    val fields = mutableListOf<Field>()
    var current: Class<*>? = clazz

    while (current != null) {
      fields += current.declaredFields
      current = current.superclass
    }

    return fields
  }

  /**
   * @param clazz Class to inspect
   * @return List of all methods including inherited ones
   */
  @JvmStatic
  fun getAllMethods(clazz: Class<*>): List<Method> {
    val methods = mutableListOf<Method>()
    var current: Class<*>? = clazz

    while (current != null) {
      methods += current.declaredMethods
      current = current.superclass
    }

    return methods
  }

  /**
   * @param clazz Class to inspect
   * @param annotation Annotation class to filter by
   * @return List of fields with the annotation
   */
  @JvmStatic
  fun <A : Annotation> getFieldsWithAnnotation(clazz: Class<*>, annotation: Class<A>): List<Field> {
    return getAllFields(clazz).filter { it.isAnnotationPresent(annotation) }
  }

  /**
   * @param clazz Class to inspect
   * @param annotation Annotation class to filter by
   * @return List of methods with the annotation
   */
  @JvmStatic
  fun <A : Annotation> getMethodsWithAnnotation(clazz: Class<*>, annotation: Class<A>): List<Method> {
    return getAllMethods(clazz).filter { it.isAnnotationPresent(annotation) }
  }

  /**
   * @param instance Object instance to get multiple fields from
   * @param fieldValues Map of fieldName -> value
   */
  @JvmStatic
  fun setFields(instance: Any, fieldValues: Map<String, Any?>) {
    for ((name, value) in fieldValues) {
      setField(instance, name, value)
    }
  }

  /**
   * @param instance Object instance to get multiple fields from
   * @param fieldNames List of field names
   * @return Map of fieldName -> value
   */
  @JvmStatic
  fun getFields(instance: Any, fieldNames: List<String>): Map<String, Any?> {
    return fieldNames.associateWith { getField(instance, it) }
  }

  /**
   * @param T Type parameter
   * @param instance Object instance
   * @param fieldName Name of the field
   * @return Value of the field cast to type T
   */
  @JvmStatic
  inline fun <reified T> getFieldTypeSafe(instance: Any, fieldName: String): T =
    getField(instance, fieldName)

  /**
   * @param T Type parameter
   * @param clazz Class containing the static field
   * @param fieldName Name of the static field
   * @return Value of the static field cast to type T
   */
  @JvmStatic
  inline fun <reified T> getStaticFieldTypeSafe(clazz: Class<*>, fieldName: String): T =
    getStaticField(clazz, fieldName)

  /**
   * @param T Type parameter
   * @param instance Object instance
   * @param methodName Name of the method
   * @param paramTypes Parameter types
   * @param args Arguments
   * @return Result of method invocation cast to type T
   */
  @JvmStatic
  inline fun <reified T> invokeMethodTypeSafe(
    instance: Any,
    methodName: String,
    paramTypes: Array<Class<*>>,
    vararg args: Any?,
  ): T =
    invokeMethod(instance, methodName, paramTypes, *args)

  /**
   * @param AccessibleObject (Field, Method, Constructor)
   * @return The same AccessibleObject with isAccessible = true
   */
  @JvmStatic
  fun <T : AccessibleObject> T.makeAccessible(): T {
    this.isAccessible = true
    return this
  }

}
