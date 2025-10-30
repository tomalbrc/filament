package de.tomalbrc.filament.entity.skill;

public class Variable {


    enum Type {
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

    enum Scope {
        GLOBAL,
        WORLD
    }

}
