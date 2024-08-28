package de.tomalbrc.filament.trim;

import java.util.List;
import java.util.Map;

public class ResourcePackTrimPatternAtlas {
    public List<Source> sources;

    public static class Source {
        public String type;
        public List<String> textures;
        public String palette_key;
        public Map<String, String> permutations;
    }
}
