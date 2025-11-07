package de.tomalbrc.filament.entity.skill;

// variable format: "<scope>.var.<name>"
// ex: "skill.var.mything"

import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unused")
public class Resolvable<T> {
    private final boolean isReference;
    private final Variable.Scope scope;
    private final String variable;
    private final T literalValue;

    private Resolvable(boolean isReference, Variable.Scope scope, String variable, T literalValue) {
        this.isReference = isReference;
        this.scope = scope;
        this.variable = variable;
        this.literalValue = literalValue;
    }

    public static <T> Resolvable<T> reference(Variable.Scope scope, String variable) {
        return new Resolvable<>(true, scope, variable, null);
    }

    public static <T> Resolvable<T> literal(T value) {
        return new Resolvable<>(false, null, null, value);
    }

    public boolean isReference() { return isReference; }
    public Variable.Scope getScope() { return scope; }
    public String getVariable() { return variable; }
    public T getLiteralValue() { return literalValue; }

    @SuppressWarnings("unchecked")
    public T resolve(SkillTree context) {
        if (!isReference) return literalValue;
        Map<String, Variable> scopedMap = switch (scope) {
            case SKILL -> context.vars();
            case CASTER -> context.caster.getVariables();
            case TARGET -> throw new UnsupportedOperationException("target variable scope not implemented");
            case WORLD -> MobSkills.getWorldVariables(context.level().dimension());
            case GLOBAL -> MobSkills.getGlobalVariables();
        };

        Object val = scopedMap.get(variable);
        if (val == null)
            return null;
        try {
            return (T) val;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Type mismatch for variable '" + variable + "'", e);
        }
    }

    @Override
    public String toString() {
        if (isReference) return "<" + scope.name().toLowerCase() + ".var." + variable + ">";
        return String.valueOf(literalValue);
    }

    public static class Deserializer<T> implements JsonDeserializer<Resolvable<T>> {
        @Override
        public Resolvable<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            Type paramType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];

            if (json.isJsonPrimitive()) {
                JsonPrimitive primitive = json.getAsJsonPrimitive();

                if (primitive.isString()) {
                    String s = primitive.getAsString();
                    if (s.startsWith("<") && s.endsWith(">")) {
                        String inner = s.substring(1, s.length() - 1);
                        String[] parts = inner.split("\\.");
                        if (parts.length < 3 || !"var".equalsIgnoreCase(parts[1])) {
                            throw new JsonParseException("Invalid reference: " + s);
                        }
                        Variable.Scope scope = Variable.Scope.valueOf(parts[0].toUpperCase(Locale.ROOT));
                        String variable = inner.substring(inner.indexOf("var.") + 4);
                        return Resolvable.reference(scope, variable);
                    }
                }
            }

            T val = context.deserialize(json, paramType);
            return Resolvable.literal(val);
        }
    }
}
