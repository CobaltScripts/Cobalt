package org.cobalt.api.module.setting

import com.google.gson.JsonElement
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import org.cobalt.api.module.Module

abstract class Setting<T>(
  val name: String,
  val description: String,
  var value: T,
) : ReadWriteProperty<Module, T>, PropertyDelegateProvider<Module, ReadWriteProperty<Module, T>> {

  override operator fun provideDelegate(thisRef: Module, property: KProperty<*>): ReadWriteProperty<Module, T> {
    thisRef.addSetting(this)
    return this
  }

  override operator fun getValue(thisRef: Module, property: KProperty<*>): T {
    return value
  }

  override operator fun setValue(thisRef: Module, property: KProperty<*>, value: T) {
    this.value = value
  }

  abstract fun read(element: JsonElement)
  abstract fun write(): JsonElement

}
