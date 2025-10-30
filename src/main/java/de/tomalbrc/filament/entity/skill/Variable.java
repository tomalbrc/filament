package de.tomalbrc.filament.entity.skill;

import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Variable {
    public static final Variable EMPTY = new Variable();

    Object val;
    Type type;

    public Variable() {}

    public Variable(String val) {
        this.val = val;
        this.type = Type.STRING;
    }

    public Variable(Integer val) {
        this.val = val;
        this.type = Type.INTEGER;
    }

    public Variable(Float val) {
        this.val = val;
        this.type = Type.FLOAT;
    }

    public Variable(Double val) {
        this.val = val;
        this.type = Type.DOUBLE;
    }

    public Variable(Boolean val) {
        this.val = val;
        this.type = Type.BOOLEAN;
    }

    public Variable(Set<?> val) {
        this.val = val;
        this.type = Type.SET;
    }

    public Variable(List<?> val) {
        this.val = val;
        this.type = Type.LIST;
    }

    public Variable(Vec3 val) {
        this.val = val;
        this.type = Type.POSITION;
    }

    public Variable(Map<?, ?> val) {
        this.val = val;
        this.type = Type.MAP;
    }

    public Object getRaw() {
        return val;
    }

    public Integer asInteger() { return type == Type.INTEGER ? ((Number) val).intValue() : null; }
    public Double asDouble() { return type == Type.DOUBLE ? ((Number) val).doubleValue() : null; }
    public Float asFloat() { return type == Type.FLOAT ? ((Number) val).floatValue() : null; }

    public Number asNumber() {
        return (val instanceof Number) ? ((Number) val) : null;
    }

    public String asString() { return type == Type.STRING ? (String) val : null; }
    public Boolean asBoolean() { return type == Type.BOOLEAN ? (Boolean) val : null; }
    public Vec3 asVec3() { return type == Type.POSITION ? (Vec3) val : null; }
    public List<?> asList() { return type == Type.LIST ? (List<?>) val : null; }
    public Map<?,?> asMap() { return type == Type.MAP ? (Map<?,?>) val : null; }
    public Set<?> asSet() { return type == Type.SET ? (Set<?>) val : null; }

    public enum Type {
        INTEGER,
        FLOAT,
        DOUBLE,
        STRING,
        BOOLEAN,
        SET,
        LIST,
        MAP,
        POSITION
    }

    public enum Scope {
        SKILL,
        CASTER,
        TARGET,
        WORLD,
        GLOBAL
    }
}
