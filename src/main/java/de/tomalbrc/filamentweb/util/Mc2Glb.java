package de.tomalbrc.filamentweb.util;

import com.google.gson.*;
import de.tomalbrc.filament.Filament;
import net.minecraft.resources.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public record Mc2Glb(Function<Identifier, InputStream> modelProvider, Function<Identifier, InputStream> textureProvider) {
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final float SCALE_FACTOR = 16f;

    private static final int ROT_90 = 90;
    private static final int ROT_180 = 180;
    private static final int ROT_270 = 270;

    private static final int MAG_FILTER_NEAREST = 9728;
    private static final int MIN_FILTER_NEAREST = 9728;
    private static final int WRAP_S_REPEAT = 10497;
    private static final int WRAP_T_REPEAT = 10497;

    private static final int TARGET_ARRAY_BUFFER = 34962;
    private static final int TARGET_ELEMENT_ARRAY_BUFFER = 34963;
    private static final int TYPE_FLOAT = 5126;
    private static final int TYPE_UNSIGNED_SHORT = 5123;

    private static final int GLTF_MAGIC = 0x46546C67;
    private static final int GLTF_VERSION = 2;
    private static final int GLB_HEADER_SIZE = 12;
    private static final int CHUNK_HEADER_SIZE = 8;
    private static final int CHUNK_TYPE_JSON = 0x4E4F534A;
    private static final int CHUNK_TYPE_BIN = 0x004E4942;

    private static final int ALIGNMENT = 4;
    private static final int PAD_SPACE = 0x20;
    private static final int PAD_NULL = 0x00;

    private static final int FLOAT_BYTES = 4;
    private static final int SHORT_BYTES = 2;

    private static final int IO_BUFFER_SIZE = 16384;

    private static final int DEFAULT_TEX_SIZE = 16;
    private static final int SHIFT_24 = 24;
    private static final int SHIFT_16 = 16;
    private static final int SHIFT_8 = 8;
    private static final int BYTE_MASK = 0xFF;

    public static Map<Identifier, byte[]> cache = new ConcurrentHashMap<>();

    public byte[] toGlb(Identifier modelId, boolean forceNew) throws Exception {
        var cached = cache.get(modelId);
        if (cached != null && !forceNew) {
            return cached;
        }

        JsonObject resolvedModel = resolveModelHierarchy(modelId);
        Map<String, MeshBuilder> meshesByTexture = extractGeometry(resolvedModel);
        return packToGlb(meshesByTexture);
    }

    public byte[] toGlb(Identifier modelId) throws Exception {
        return toGlb(modelId, false);
    }

    private JsonObject resolveModelHierarchy(Identifier modelId) throws IOException {
        JsonObject current = loadJson(modelId);
        if (current == null) return new JsonObject();

        if (current.has("parent")) {
            String parentId = current.get("parent").getAsString();
            if (!parentId.contains("builtin/")) {
                JsonObject parent = resolveModelHierarchy(Identifier.parse(parentId));
                return mergeModels(parent, current);
            }
        }
        return current;
    }

    private JsonObject mergeModels(JsonObject parent, JsonObject child) {
        JsonObject merged = parent.deepCopy();
        if (child.has("textures")) {
            if (!merged.has("textures")) merged.add("textures", new JsonObject());
            JsonObject mTex = merged.getAsJsonObject("textures");
            JsonObject cTex = child.getAsJsonObject("textures");
            for (String key : cTex.keySet()) mTex.add(key, cTex.get(key));
        }
        if (child.has("elements")) merged.add("elements", child.get("elements"));
        return merged;
    }

    private JsonObject loadJson(Identifier id) throws IOException {
        try (InputStream is = modelProvider.apply(id)) {
            if (is == null) return null;
            return JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private String resolveTexture(String ref, JsonObject textures) {
        String current = ref;
        Set<String> visited = new HashSet<>();
        while (current.startsWith("#")) {
            String key = current.substring(1);
            if (!textures.has(key) || !visited.add(key)) return "missingno";
            current = textures.get(key).getAsString();
        }
        return current;
    }

    private void generateItemElements(JsonObject model) {
        if (model.has("elements")) return;
        if (!model.has("textures")) return;

        JsonObject texturesMap = model.getAsJsonObject("textures");
        JsonArray elements = new JsonArray();

        for (int layer = 0; layer < 10; layer++) {
            String layerKey = "layer" + layer;
            if (!texturesMap.has(layerKey)) continue;

            String texRef = texturesMap.get(layerKey).getAsString();
            String texPath = resolveTexture(texRef, texturesMap);
            if ("missingno".equals(texPath)) continue;

            byte[] imgData = null;
            try (InputStream is = textureProvider.apply(Identifier.parse(texPath))) {
                if (is != null) imgData = is.readAllBytes();
            } catch (Exception ignored) {
                Filament.LOGGER.error("GLB Converter: Error reading {}", texPath);
            }

            if (imgData == null) continue;

            int width = 16, height = 16;
            boolean[][] opaque = null;

            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgData));
                if (img != null) {
                    width = img.getWidth();
                    height = img.getHeight();
                    opaque = new boolean[width][height];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int alpha = (img.getRGB(x, y) >> 24) & 0xFF;
                            opaque[x][y] = alpha > 25; // 10% alpha cutoff like vanilla
                        }
                    }
                }
            } catch (Exception ignored) {
                Filament.LOGGER.error("GLB Converter: Error reading pixels of {}", texPath);
            }

            if (opaque == null) continue;

            float scaleX = 16f / width;
            float scaleY = 16f / height;
            float zOffset = layer * 0.01f;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!opaque[x][y]) continue;

                    JsonObject element = new JsonObject();

                    JsonArray from = new JsonArray();
                    from.add(x * scaleX);
                    from.add((height - 1 - y) * scaleY);
                    from.add(7.5f - zOffset);
                    element.add("from", from);

                    JsonArray to = new JsonArray();
                    to.add((x + 1) * scaleX);
                    to.add((height - y) * scaleY);
                    to.add(8.5f + zOffset);
                    element.add("to", to);

                    JsonObject faces = new JsonObject();
                    float u0 = (x * 16f) / width;
                    float v0 = (y * 16f) / height;
                    float u1 = ((x + 1) * 16f) / width;
                    float v1 = ((y + 1) * 16f) / height;

                    JsonArray uv = new JsonArray();
                    uv.add(u0);
                    uv.add(v0);
                    uv.add(u1);
                    uv.add(v1);

                    // todo: is this correct?
                    JsonArray uvFlipped = new JsonArray();
                    uvFlipped.add(u1);
                    uvFlipped.add(v0);
                    uvFlipped.add(u0);
                    uvFlipped.add(v1);

                    JsonObject southFace = new JsonObject();
                    southFace.add("uv", uv);
                    southFace.addProperty("texture", "#" + layerKey);
                    faces.add("south", southFace);

                    JsonObject northFace = new JsonObject();
                    northFace.add("uv", uvFlipped);
                    northFace.addProperty("texture", "#" + layerKey);
                    faces.add("north", northFace);

                    if (x == 0 || !opaque[x - 1][y]) {
                        JsonObject westFace = new JsonObject();
                        westFace.add("uv", uv);
                        westFace.addProperty("texture", "#" + layerKey);
                        faces.add("west", westFace);
                    }
                    if (x == width - 1 || !opaque[x + 1][y]) {
                        JsonObject eastFace = new JsonObject();
                        eastFace.add("uv", uv);
                        eastFace.addProperty("texture", "#" + layerKey);
                        faces.add("east", eastFace);
                    }
                    if (y == 0 || !opaque[x][y - 1]) {
                        JsonObject upFace = new JsonObject();
                        upFace.add("uv", uv);
                        upFace.addProperty("texture", "#" + layerKey);
                        faces.add("up", upFace);
                    }
                    if (y == height - 1 || !opaque[x][y + 1]) {
                        JsonObject downFace = new JsonObject();
                        downFace.add("uv", uv);
                        downFace.addProperty("texture", "#" + layerKey);
                        faces.add("down", downFace);
                    }

                    element.add("faces", faces);
                    elements.add(element);
                }
            }
        }

        if (!elements.isEmpty()) {
            model.add("elements", elements);
        }
    }

    private Map<String, MeshBuilder> extractGeometry(JsonObject model) {
        generateItemElements(model);

        Map<String, MeshBuilder> meshes = new HashMap<>();
        JsonObject texturesMap = model.has("textures") ? model.getAsJsonObject("textures") : new JsonObject();
        JsonArray elements = model.has("elements") ? model.getAsJsonArray("elements") : new JsonArray();

        for (JsonElement elem : elements) {
            JsonObject block = elem.getAsJsonObject();
            JsonArray from = block.getAsJsonArray("from");
            JsonArray to = block.getAsJsonArray("to");

            float fx = from.get(0).getAsFloat() / SCALE_FACTOR;
            float fy = from.get(1).getAsFloat() / SCALE_FACTOR;
            float fz = from.get(2).getAsFloat() / SCALE_FACTOR;
            float tx = to.get(0).getAsFloat() / SCALE_FACTOR;
            float ty = to.get(1).getAsFloat() / SCALE_FACTOR;
            float tz = to.get(2).getAsFloat() / SCALE_FACTOR;

            JsonObject rotation = block.has("rotation") ? block.getAsJsonObject("rotation") : null;

            if (block.has("faces")) {
                JsonObject faces = block.getAsJsonObject("faces");
                for (String dir : faces.keySet()) {
                    JsonObject face = faces.getAsJsonObject(dir);
                    String texPath = resolveTexture(face.get("texture").getAsString(), texturesMap);
                    MeshBuilder builder = meshes.computeIfAbsent(texPath, k -> new MeshBuilder());
                    addFace(builder, dir, fx, fy, fz, tx, ty, tz, face, rotation);
                }
            }
        }
        return meshes;
    }

    private void addFace(MeshBuilder builder, String dir, float fx, float fy, float fz, float tx, float ty, float tz, JsonObject faceData, JsonObject rotation) {
        float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
        if (faceData.has("uv")) {
            JsonArray uv = faceData.getAsJsonArray("uv");
            u0 = uv.get(0).getAsFloat() / SCALE_FACTOR;
            v0 = uv.get(1).getAsFloat() / SCALE_FACTOR;
            u1 = uv.get(2).getAsFloat() / SCALE_FACTOR;
            v1 = uv.get(3).getAsFloat() / SCALE_FACTOR;
        }

        int start = builder.positions.size() / 3;
        float[][] pos = new float[4][3];
        float[] norm = new float[3];

        switch (dir) {
            case "up" -> {
                pos[0] = new float[]{fx, ty, fz};
                pos[1] = new float[]{fx, ty, tz};
                pos[2] = new float[]{tx, ty, tz};
                pos[3] = new float[]{tx, ty, fz};
                norm = new float[]{0, 1, 0};
            }
            case "down" -> {
                pos[0] = new float[]{fx, fy, tz};
                pos[1] = new float[]{fx, fy, fz};
                pos[2] = new float[]{tx, fy, fz};
                pos[3] = new float[]{tx, fy, tz};
                norm = new float[]{0, -1, 0};
            }
            case "north" -> {
                pos[0] = new float[]{tx, ty, fz};
                pos[1] = new float[]{tx, fy, fz};
                pos[2] = new float[]{fx, fy, fz};
                pos[3] = new float[]{fx, ty, fz};
                norm = new float[]{0, 0, -1};
            }
            case "south" -> {
                pos[0] = new float[]{fx, ty, tz};
                pos[1] = new float[]{fx, fy, tz};
                pos[2] = new float[]{tx, fy, tz};
                pos[3] = new float[]{tx, ty, tz};
                norm = new float[]{0, 0, 1};
            }
            case "west" -> {
                pos[0] = new float[]{fx, ty, fz};
                pos[1] = new float[]{fx, fy, fz};
                pos[2] = new float[]{fx, fy, tz};
                pos[3] = new float[]{fx, ty, tz};
                norm = new float[]{-1, 0, 0};
            }
            case "east" -> {
                pos[0] = new float[]{tx, ty, tz};
                pos[1] = new float[]{tx, fy, tz};
                pos[2] = new float[]{tx, fy, fz};
                pos[3] = new float[]{tx, ty, fz};
                norm = new float[]{1, 0, 0};
            }
        }

        if (rotation != null) {
            for (int i = 0; i < 4; i++) {
                pos[i] = applyElementRotation(pos[i], rotation);
            }
            norm = applyNormalRotation(norm, rotation);
        }

        builder.addQuadPos(pos[0][0], pos[0][1], pos[0][2], pos[1][0], pos[1][1], pos[1][2], pos[2][0], pos[2][1], pos[2][2], pos[3][0], pos[3][1], pos[3][2]);
        builder.addNorm(norm[0], norm[1], norm[2]);
        builder.addIndices(start, start + 1, start + 2, start, start + 2, start + 3);

        int uvRot = faceData.has("rotation") ? faceData.get("rotation").getAsInt() : 0;
        float[] uvs;
        if (uvRot == ROT_90) {
            uvs = new float[]{u0, v1, u1, v1, u1, v0, u0, v0};
        } else if (uvRot == ROT_180) {
            uvs = new float[]{u1, v1, u1, v0, u0, v0, u0, v1};
        } else if (uvRot == ROT_270) {
            uvs = new float[]{u1, v0, u0, v0, u0, v1, u1, v1};
        } else {
            uvs = new float[]{u0, v0, u0, v1, u1, v1, u1, v0};
        }
        builder.addQuadUVs(uvs);
    }

    private float[] applyElementRotation(float[] p, JsonObject rot) {
        JsonArray origin = rot.getAsJsonArray("origin");
        float ox = origin.get(0).getAsFloat() / SCALE_FACTOR;
        float oy = origin.get(1).getAsFloat() / SCALE_FACTOR;
        float oz = origin.get(2).getAsFloat() / SCALE_FACTOR;
        String axis = rot.get("axis").getAsString();
        float angle = rot.get("angle").getAsFloat();
        boolean rescale = rot.has("rescale") && rot.get("rescale").getAsBoolean();

        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        float dx = p[0] - ox, dy = p[1] - oy, dz = p[2] - oz;
        float rx = dx, ry = dy, rz = dz;

        final float v = Math.abs(cos) + Math.abs(sin);
        switch (axis) {
            case "x" -> {
                ry = dy * cos - dz * sin;
                rz = dy * sin + dz * cos;
                if (rescale) {
                    ry *= v;
                    rz *= v;
                }
            }
            case "y" -> {
                rx = dx * cos + dz * sin;
                rz = -dx * sin + dz * cos;
                if (rescale) {
                    rx *= v;
                    rz *= v;
                }
            }
            case "z" -> {
                rx = dx * cos - dy * sin;
                ry = dx * sin + dy * cos;
                if (rescale) {
                    rx *= v;
                    ry *= v;
                }
            }
        }
        return new float[]{rx + ox, ry + oy, rz + oz};
    }

    private float[] applyNormalRotation(float[] n, JsonObject rot) {
        String axis = rot.get("axis").getAsString();
        float angle = rot.get("angle").getAsFloat();
        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        float rx = n[0], ry = n[1], rz = n[2];
        switch (axis) {
            case "x" -> {
                ry = n[1] * cos - n[2] * sin;
                rz = n[1] * sin + n[2] * cos;
            }
            case "y" -> {
                rx = n[0] * cos + n[2] * sin;
                rz = -n[0] * sin + n[2] * cos;
            }
            case "z" -> {
                rx = n[0] * cos - n[1] * sin;
                ry = n[0] * sin + n[1] * cos;
            }
        }
        return new float[]{rx, ry, rz};
    }

    private byte[] packToGlb(Map<String, MeshBuilder> meshesByTexture) throws Exception {
        ByteArrayOutputStream bin = new ByteArrayOutputStream();
        JsonObject gltf = new JsonObject();

        gltf.add("asset", gson.fromJson("{\"version\":\"2.0\"}", JsonObject.class));
        gltf.addProperty("scene", 0);
        gltf.add("scenes", gson.fromJson("[{\"nodes\":[0]}]", JsonArray.class));
        gltf.add("nodes", gson.fromJson("[{\"mesh\":0}]", JsonArray.class));

        JsonArray accessors = new JsonArray(), bufferViews = new JsonArray(), materials = new JsonArray();
        JsonArray textures = new JsonArray(), images = new JsonArray(), samplers = new JsonArray();
        JsonArray primitives = new JsonArray();

        JsonObject samplerObj = new JsonObject();
        samplerObj.addProperty("magFilter", MAG_FILTER_NEAREST);
        samplerObj.addProperty("minFilter", MIN_FILTER_NEAREST);
        samplerObj.addProperty("wrapS", WRAP_S_REPEAT);
        samplerObj.addProperty("wrapT", WRAP_T_REPEAT);
        samplers.add(samplerObj);

        Map<String, Integer> pathToMatIdx = new HashMap<>();
        List<byte[]> imageBlobs = new ArrayList<>();

        for (String path : meshesByTexture.keySet()) {
            try (var is = textureProvider.apply(Identifier.parse(path))) {
                if (is == null)
                    continue;

                byte[] imgData = is.readAllBytes();

                int matIdx = materials.size();
                pathToMatIdx.put(path, matIdx);
                imageBlobs.add(imgData);

                int frameCount = 1;
                int[] dims = getPngDimensions(imgData);
                if (dims[0] > 0 && dims[1] > dims[0] && dims[1] % dims[0] == 0) {
                    frameCount = dims[1] / dims[0];
                }

                JsonObject img = new JsonObject();
                images.add(img);

                JsonObject tex = new JsonObject();
                tex.addProperty("source", images.size() - 1);
                tex.addProperty("sampler", 0);
                textures.add(tex);

                JsonObject mat = new JsonObject();
                mat.addProperty("alphaMode", "MASK");
                JsonObject pbr = new JsonObject();
                JsonObject bct = new JsonObject();
                bct.addProperty("index", textures.size() - 1);
                pbr.add("baseColorTexture", bct);
                pbr.addProperty("metallicFactor", 0.0f);
                pbr.addProperty("roughnessFactor", 1.0f);
                mat.add("pbrMetallicRoughness", pbr);
                materials.add(mat);

                if (frameCount > 1) {
                    MeshBuilder mb = meshesByTexture.get(path);
                    for (int i = 1; i < mb.uvs.size(); i += 2) {
                        float v = mb.uvs.get(i);
                        mb.uvs.set(i, v / frameCount);
                    }
                }
            }
        }

        int bvIdx = 0;
        int accIdx = 0;
        for (var entry : meshesByTexture.entrySet()) {
            MeshBuilder mb = entry.getValue();
            String path = entry.getKey();

            bufferViews.add(createBV(addToBin(bin, floatsToBytes(mb.positions)), mb.positions.size() * FLOAT_BYTES, TARGET_ARRAY_BUFFER));
            bufferViews.add(createBV(addToBin(bin, floatsToBytes(mb.normals)), mb.normals.size() * FLOAT_BYTES, TARGET_ARRAY_BUFFER));
            bufferViews.add(createBV(addToBin(bin, floatsToBytes(mb.uvs)), mb.uvs.size() * FLOAT_BYTES, TARGET_ARRAY_BUFFER));
            bufferViews.add(createBV(addToBin(bin, intsToShortBytes(mb.indices)), mb.indices.size() * SHORT_BYTES, TARGET_ELEMENT_ARRAY_BUFFER));

            accessors.add(createAcc(bvIdx, TYPE_FLOAT, mb.positions.size() / 3, "VEC3", getMinMax(mb.positions)));
            accessors.add(createAcc(bvIdx + 1, TYPE_FLOAT, mb.normals.size() / 3, "VEC3", null));
            accessors.add(createAcc(bvIdx + 2, TYPE_FLOAT, mb.uvs.size() / 2, "VEC2", null));
            accessors.add(createAcc(bvIdx + 3, TYPE_UNSIGNED_SHORT, mb.indices.size(), "SCALAR", null));

            JsonObject prim = new JsonObject();
            JsonObject attr = new JsonObject();
            attr.addProperty("POSITION", accIdx);
            attr.addProperty("NORMAL", accIdx + 1);
            attr.addProperty("TEXCOORD_0", accIdx + 2);
            prim.add("attributes", attr);
            prim.addProperty("indices", accIdx + 3);

            Integer mId = pathToMatIdx.get(path);
            if (mId != null) prim.addProperty("material", mId);
            primitives.add(prim);

            bvIdx += 4;
            accIdx += 4;
        }

        for (int i = 0; i < imageBlobs.size(); i++) {
            byte[] data = imageBlobs.get(i);
            int offset = addToBin(bin, data);
            bufferViews.add(createBV(offset, data.length, null));
            images.get(i).getAsJsonObject().addProperty("bufferView", bvIdx++);
            images.get(i).getAsJsonObject().addProperty("mimeType", "image/png");
        }

        JsonObject meshObj = new JsonObject();
        meshObj.add("primitives", primitives);
        gltf.add("meshes", gson.fromJson("[" + meshObj.toString() + "]", JsonArray.class));

        gltf.add("accessors", accessors);
        gltf.add("bufferViews", bufferViews);
        gltf.add("materials", materials);
        gltf.add("textures", textures);
        gltf.add("images", images);
        gltf.add("samplers", samplers);

        JsonObject buffer = new JsonObject();
        buffer.addProperty("byteLength", bin.size());
        gltf.add("buffers", gson.fromJson("[" + buffer.toString() + "]", JsonArray.class));

        return createGlbFile(gltf, bin.toByteArray());
    }

    private int[] getPngDimensions(byte[] data) {
        if (data != null && data.length > 0) {
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
                if (image != null) {
                    return new int[]{image.getWidth(), image.getHeight()};
                }
            } catch (IOException ignored) {
                Filament.LOGGER.error("GLB Converter: Error reading image data!");
            }
        }
        return new int[]{DEFAULT_TEX_SIZE, DEFAULT_TEX_SIZE};
    }

    private byte[] createGlbFile(JsonObject gltf, byte[] bin) throws IOException {
        byte[] json = gltf.toString().getBytes(StandardCharsets.UTF_8);
        int jPad = (ALIGNMENT - (json.length % ALIGNMENT)) % ALIGNMENT;
        int bPad = (ALIGNMENT - (bin.length % ALIGNMENT)) % ALIGNMENT;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            writeLE(dos, GLTF_MAGIC);
            writeLE(dos, GLTF_VERSION);
            writeLE(dos, GLB_HEADER_SIZE + CHUNK_HEADER_SIZE + json.length + jPad + CHUNK_HEADER_SIZE + bin.length + bPad);

            writeLE(dos, json.length + jPad);
            writeLE(dos, CHUNK_TYPE_JSON);
            dos.write(json);
            for (int i = 0; i < jPad; i++) dos.write(PAD_SPACE);

            writeLE(dos, bin.length + bPad);
            writeLE(dos, CHUNK_TYPE_BIN);
            dos.write(bin);
            for (int i = 0; i < bPad; i++) dos.write(PAD_NULL);
        }

        return bos.toByteArray();
    }

    private JsonObject createBV(int offset, int len, Integer target) {
        JsonObject o = new JsonObject();
        o.addProperty("buffer", 0);
        o.addProperty("byteOffset", offset);
        o.addProperty("byteLength", len);
        if (target != null) o.addProperty("target", target);
        return o;
    }

    private JsonObject createAcc(int bv, int compType, int count, String type, float[][] minMax) {
        JsonObject o = new JsonObject();
        o.addProperty("bufferView", bv);
        o.addProperty("componentType", compType);
        o.addProperty("count", count);
        o.addProperty("type", type);
        if (minMax != null) {
            JsonArray min = new JsonArray(), max = new JsonArray();
            for (float v : minMax[0]) min.add(v);
            for (float v : minMax[1]) max.add(v);
            o.add("min", min);
            o.add("max", max);
        }
        return o;
    }

    private int addToBin(ByteArrayOutputStream bin, byte[] data) throws IOException {
        int offset = bin.size();
        bin.write(data);
        int pad = (ALIGNMENT - (data.length % ALIGNMENT)) % ALIGNMENT;
        for (int i = 0; i < pad; i++) bin.write(0);
        return offset;
    }

    private void writeLE(DataOutputStream d, int v) throws IOException {
        d.write(v & BYTE_MASK);
        d.write((v >> SHIFT_8) & BYTE_MASK);
        d.write((v >> SHIFT_16) & BYTE_MASK);
        d.write((v >> SHIFT_24) & BYTE_MASK);
    }

    private float[][] getMinMax(List<Float> p) {
        float minX = 1, minY = 1, minZ = 1, maxX = 0, maxY = 0, maxZ = 0;
        for (int i = 0; i < p.size(); i += 3) {
            float x = p.get(i), y = p.get(i + 1), z = p.get(i + 2);
            if (i == 0) {
                minX = maxX = x;
                minY = maxY = y;
                minZ = maxZ = z;
            } else {
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
            }
        }
        return new float[][]{{minX, minY, minZ}, {maxX, maxY, maxZ}};
    }

    private byte[] floatsToBytes(List<Float> f) {
        ByteBuffer b = ByteBuffer.allocate(f.size() * FLOAT_BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (float x : f) b.putFloat(x);
        return b.array();
    }

    private byte[] intsToShortBytes(List<Integer> i) {
        ByteBuffer b = ByteBuffer.allocate(i.size() * SHORT_BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (int x : i) b.putShort((short) x);
        return b.array();
    }

    private static class MeshBuilder {
        List<Float> positions = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> uvs = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        void addQuadPos(float... p) {
            for (float x : p) positions.add(x);
        }

        void addQuadUVs(float... u) {
            for (float x : u) uvs.add(x);
        }

        void addIndices(int... i) {
            for (int x : i) indices.add(x);
        }

        void addNorm(float x, float y, float z) {
            for (int i = 0; i < 4; i++) {
                normals.add(x);
                normals.add(y);
                normals.add(z);
            }
        }
    }
}