package de.tomalbrc.filament.entity.skill;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Adapts values whose runtime type may differ from their declaration type.
 * Supports aliases for subtype labels.
 */
public final class RuntimeTypeAdapterFactoryWithAliases<T> implements TypeAdapterFactory {
  private final Class<?> baseType;
  private final String typeFieldName;
  private final boolean maintainType;

  /** map of label → subtype */
  private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
  /** map of subtype → canonical label */
  private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();
  /** map of alias → canonical label */
  private final Map<String, String> aliasToCanonical = new LinkedHashMap<>();

  private RuntimeTypeAdapterFactoryWithAliases(Class<?> baseType, String typeFieldName, boolean maintainType) {
    if (baseType == null || typeFieldName == null) {
      throw new NullPointerException();
    }
    this.baseType = baseType;
    this.typeFieldName = typeFieldName;
    this.maintainType = maintainType;
  }

  public static <T> RuntimeTypeAdapterFactoryWithAliases<T> of(
      Class<T> baseType, String typeFieldName, boolean maintainType) {
    return new RuntimeTypeAdapterFactoryWithAliases<>(baseType, typeFieldName, maintainType);
  }

  public static <T> RuntimeTypeAdapterFactoryWithAliases<T> of(
      Class<T> baseType, String typeFieldName) {
    return new RuntimeTypeAdapterFactoryWithAliases<>(baseType, typeFieldName, false);
  }

  public static <T> RuntimeTypeAdapterFactoryWithAliases<T> of(Class<T> baseType) {
    return new RuntimeTypeAdapterFactoryWithAliases<>(baseType, "type", false);
  }

  /**
   * Registers subtype with canonical label.
   */
  @CanIgnoreReturnValue
  public RuntimeTypeAdapterFactoryWithAliases<T> registerSubtype(Class<? extends T> type, String label) {
    if (type == null || label == null) {
      throw new NullPointerException();
    }
    if (subtypeToLabel.containsKey(type)) {
      throw new IllegalArgumentException("Subtype " + type + " already registered with label " + subtypeToLabel.get(type));
    }
    if (labelToSubtype.containsKey(label) || aliasToCanonical.containsKey(label)) {
      throw new IllegalArgumentException("Label '" + label + "' is already used as a label or alias");
    }

    labelToSubtype.put(label, type);
    subtypeToLabel.put(type, label);
    return this;
  }

  /**
   * Registers subtype with canonical label, and one or more alias labels.
   */
  @CanIgnoreReturnValue
  public RuntimeTypeAdapterFactoryWithAliases<T> registerSubtypeWithAliases(
      Class<? extends T> type, String canonicalLabel, String... aliases) {
    registerSubtype(type, canonicalLabel);
    for (String alias : aliases) {
      if (alias == null) {
        throw new NullPointerException("Alias cannot be null");
      }
      if (labelToSubtype.containsKey(alias) || aliasToCanonical.containsKey(alias)) {
        throw new IllegalArgumentException("Alias '" + alias + "' is already used as a label or alias");
      }
      aliasToCanonical.put(alias, canonicalLabel);
    }
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> typeToken) {
    if (typeToken == null) {
      return null;
    }
    Class<?> rawType = typeToken.getRawType();
    boolean handle =
        baseType.equals(rawType) || (rawType != null && baseType.isAssignableFrom(rawType));
    if (!handle) {
      return null;
    }

    final TypeAdapter<JsonElement> jsonElementAdapter = gson.getAdapter(JsonElement.class);
    final Map<String, TypeAdapter<?>> labelToDelegate = new LinkedHashMap<>();
    final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<>();

    for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
      TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
      labelToDelegate.put(entry.getKey(), delegate);
      subtypeToDelegate.put(entry.getValue(), delegate);
    }

    return new TypeAdapter<R>() {
      @Override
      public R read(JsonReader in) throws IOException {
        JsonElement jsonElement = jsonElementAdapter.read(in);
        JsonObject jsonObj = jsonElement.getAsJsonObject();

        JsonElement labelJsonElement;
        if (maintainType) {
          labelJsonElement = jsonObj.get(typeFieldName);
        } else {
          labelJsonElement = jsonObj.remove(typeFieldName);
        }

        if (labelJsonElement == null) {
          throw new JsonParseException(
              "Cannot deserialize " + baseType + " because it does not define a field named " + typeFieldName);
        }
        String label = labelJsonElement.getAsString();

        // resolve alias to canonical
        if (aliasToCanonical.containsKey(label)) {
          label = aliasToCanonical.get(label);
        }

        TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
        if (delegate == null) {
          throw new JsonParseException(
              "Cannot deserialize " + baseType + " subtype named '" + label
                  + "'; did you forget to register a subtype or alias?");
        }

        return delegate.fromJsonTree(jsonElement);
      }

      @Override
      public void write(JsonWriter out, R value) throws IOException {
        Class<?> srcType = value.getClass();
        String label = subtypeToLabel.get(srcType);
        TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);

        if (delegate == null) {
          throw new JsonParseException(
              "Cannot serialize " + srcType.getName() + "; did you forget to register a subtype?");
        }

        JsonObject jsonObj = delegate.toJsonTree(value).getAsJsonObject();

        if (maintainType) {
          jsonElementAdapter.write(out, jsonObj);
          return;
        }

        if (jsonObj.has(typeFieldName)) {
          throw new JsonParseException(
              "Cannot serialize " + srcType.getName()
                  + " because it already defines a field named " + typeFieldName);
        }

        JsonObject clone = new JsonObject();
        clone.add(typeFieldName, new JsonPrimitive(label));
        for (Map.Entry<String, JsonElement> e : jsonObj.entrySet()) {
          clone.add(e.getKey(), e.getValue());
        }

        jsonElementAdapter.write(out, clone);
      }
    }.nullSafe();
  }
}
