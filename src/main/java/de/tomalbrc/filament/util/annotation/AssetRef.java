package de.tomalbrc.filament.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE_USE})
public @interface AssetRef {
    /**
     * reg. id
     */
    Type value();

    enum Type {
        ITEM_ASSET("items/", ".json"),
        MODEL("models/", ".json"),
        TEXTURE("textures/", ".png");

        private final String prefix;
        private final String suffix;

        Type(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String prefix() {
            return prefix;
        }

        public String suffix() {
            return suffix;
        }
    };
}
