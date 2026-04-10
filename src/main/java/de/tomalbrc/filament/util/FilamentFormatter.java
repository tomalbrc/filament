package de.tomalbrc.filament.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.tomalbrc.filament.Filament;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.net.URI;
import java.util.*;
import java.util.function.UnaryOperator;

public final class FilamentFormatter {
    private static final Map<String, TagHandler> REGISTRY = new HashMap<>();

    static {
        register(List.of("b", "bold"), (args, ctx) -> ctx.applyStyle(s -> s.withBold(parseBool(args, 0, true))));
        register(List.of("i", "italic", "em"), (args, ctx) -> ctx.applyStyle(s -> s.withItalic(parseBool(args, 0, true))));
        register(List.of("u", "underlined", "underline"), (args, ctx) -> ctx.applyStyle(s -> s.withUnderlined(parseBool(args, 0, true))));
        register(List.of("st", "strikethrough", "strike"), (args, ctx) -> ctx.applyStyle(s -> s.withStrikethrough(parseBool(args, 0, true))));
        register(List.of("obf", "obfuscated", "magic"), (args, ctx) -> ctx.applyStyle(s -> s.withObfuscated(parseBool(args, 0, true))));

        register(List.of("c", "color", "colour"), (args, ctx) -> {
            TextColor color = parseTextColor(String.join(" ", args));
            return color != null ? ctx.applyStyle(s -> s.withColor(color)) : null;
        });
        register("font", (args, ctx) -> {
            Identifier id = Identifier.tryParse(String.join(" ", args));
            return id != null ? ctx.applyStyle(s -> s.withFont(new FontDescription.Resource(id))) : null;
        });

        register(List.of("key", "keybind"), (args, ctx) -> {
            if (!args.isEmpty()) ctx.insert(Component.keybind(args.getFirst()));
            return null;
        });
        register(List.of("lang", "translate"), (args, ctx) -> {
            if (args.isEmpty()) return null;
            if (args.size() == 1) ctx.insert(Component.translatable(args.getFirst()));
            else {
                Object[] transArgs = args.subList(1, args.size()).stream().map(FilamentFormatter::parse).toArray();
                ctx.insert(Component.translatable(args.getFirst(), transArgs));
            }
            return null;
        });
        register(List.of("selector", "sel"), (args, ctx) -> {
            if (!args.isEmpty()) {
                try {
                    ctx.insert(new EntitySelectorParser(new StringReader(args.getFirst()), true).parse().findSingleEntity(Filament.SERVER.createCommandSourceStack()).getDisplayName());
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
        register("score", (args, ctx) -> {
            if (args.size() >= 2) ctx.insert(Component.score(args.get(0), args.get(1)));
            return null;
        });

        register("insertion", (args, ctx) -> ctx.applyStyle(s -> s.withInsertion(String.join(" ", args))));
        register("click", (args, ctx) -> {
            if (args.size() < 2) return null;
            String action = args.getFirst().toLowerCase(Locale.ROOT);
            String val = String.join(" ", args.subList(1, args.size()));
            return ctx.applyStyle(s -> s.withClickEvent(createClick(action, val)));
        });
        register("hover", (args, ctx) -> {
            if (args.size() < 2) return null;
            String action = args.getFirst().toLowerCase(Locale.ROOT);
            String val = String.join(" ", args.subList(1, args.size()));
            HoverEvent event = createHover(action, val);
            return event != null ? ctx.applyStyle(s -> s.withHoverEvent(event)) : null;
        });

        register("shadow", (args, ctx) -> {
            if (args.isEmpty()) return ctx.applyStyle(s -> applyShadow(s, "current", 0.25));
            double alpha = 0.25;
            String colorStr = args.getFirst();
            if (args.size() >= 2 && isNumber(args.getLast())) {
                alpha = Math.clamp(parseDouble(args.getLast(), 0.25), 0, 1);
                colorStr = String.join(" ", args.subList(0, args.size() - 1));
            }
            String finalColor = colorStr;
            double finalAlpha = alpha;
            return ctx.applyStyle(s -> applyShadow(s, finalColor, finalAlpha));
        });

        register("gradient", (args, ctx) -> applyComplexEffect(args, ctx, false));
        register("transition", (args, ctx) -> applyComplexEffect(args, ctx, true));
        register("rainbow", (args, ctx) -> {
            double phase = 0.0;
            boolean rev = false;
            if (!args.isEmpty()) {
                String arg = args.getFirst();
                if (arg.startsWith("!")) {
                    rev = true;
                    arg = arg.substring(1);
                }
                phase = parseDouble(arg, 0.0);
            }
            double fP = phase;
            boolean fR = rev;
            return ctx.transformSegments(segs -> applyRainbow(segs, fP, fR));
        });

        register("reset", (args, ctx) -> {
            ctx.reset();
            return null;
        });
        register(List.of("newline", "br"), (args, ctx) -> {
            ctx.insert(Component.literal("\n"));
            return null;
        });
        register("sprite", (args, ctx) -> {
            if (args.size() < 2) return null;
            String type = args.get(0).toLowerCase(Locale.ROOT);
            if (type.equals("atlas") && args.size() >= 3) {
                Identifier atlas = Identifier.tryParse(args.get(1));
                Identifier sprite = Identifier.tryParse(args.get(2));
                if (atlas != null && sprite != null)
                    return ctx.applyStyle(s -> s.withFont(new FontDescription.AtlasSprite(atlas, sprite)));
            } else if (type.equals("player")) {
                return ctx.applyStyle(s -> s.withFont(new FontDescription.PlayerSprite(ResolvableProfile.createUnresolved(args.get(1)), parseBool(args, 2, true))));
            }
            return null;
        });
    }

    public static void register(String name, TagHandler handler) {
        REGISTRY.put(name.toLowerCase(Locale.ROOT), handler);
    }

    public static void register(List<String> names, TagHandler handler) {
        names.forEach(n -> register(n, handler));
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) return Component.empty();
        return new Parser(input).parse();
    }

    public static String toString(Component component) {
        StringBuilder out = new StringBuilder();
        if (component != null) serialize(component, Style.EMPTY, out);
        return out.toString();
    }

    private static void serialize(Component component, Style parent, StringBuilder out) {
        Style style = component.getStyle();

        appendStyleOpen(parent, style, out);

        if (component.getContents() instanceof PlainTextContents.LiteralContents(String text)) {
            out.append(escape(text));
        } else {
            out.append(component.getString());
        }

        for (Component sibling : component.getSiblings()) {
            serialize(sibling, style, out);
        }

        appendStyleClose(parent, style, out);
    }

    private static void appendStyleOpen(Style parent, Style style, StringBuilder out) {
        if (!Objects.equals(parent.getColor(), style.getColor()) && style.getColor() != null) {
            out.append("<#").append(String.format("%06X", style.getColor().getValue())).append(">");
        }

        if (style.isBold() && !parent.isBold()) out.append("<bold>");
        if (style.isItalic() && !parent.isItalic()) out.append("<italic>");
        if (style.isUnderlined() && !parent.isUnderlined()) out.append("<underlined>");
        if (style.isStrikethrough() && !parent.isStrikethrough()) out.append("<strikethrough>");
        if (style.isObfuscated() && !parent.isObfuscated()) out.append("<obfuscated>");

        if (!Objects.equals(parent.getShadowColor(), style.getShadowColor()) && style.getShadowColor() != null) {
            int argb = style.getShadowColor();
            int rgb = argb & 0xFFFFFF;
            double alpha = ((argb >> 24) & 0xFF) / 255.0;
            out.append("<shadow:#")
                    .append(String.format("%06X", rgb))
                    .append(" ")
                    .append(alpha)
                    .append(">");
        }

        if (!Objects.equals(parent.getClickEvent(), style.getClickEvent()) && style.getClickEvent() != null) {
            ClickEvent click = style.getClickEvent();

            String action;
            String value;

            switch (click) {
                case ClickEvent.OpenUrl open -> {
                    action = "open_url";
                    value = open.uri().toString();
                }
                case ClickEvent.RunCommand run -> {
                    action = "run_command";
                    value = run.command();
                }
                case ClickEvent.SuggestCommand suggest -> {
                    action = "suggest_command";
                    value = suggest.command();
                }
                case ClickEvent.CopyToClipboard copy -> {
                    action = "copy_to_clipboard";
                    value = copy.value();
                }
                case ClickEvent.ChangePage page -> {
                    action = "change_page";
                    value = Integer.toString(page.page());
                }
                default -> {
                    action = click.getClass().getSimpleName().toLowerCase(Locale.ROOT);
                    value = "";
                }
            }

            out.append("<click:")
                    .append(action)
                    .append(" ")
                    .append(escape(value))
                    .append(">");
        }

        if (!Objects.equals(parent.getHoverEvent(), style.getHoverEvent()) && style.getHoverEvent() != null) {
            HoverEvent hover = style.getHoverEvent();

            if (hover instanceof HoverEvent.ShowText(Component value)) {
                out.append("<hover:show_text ")
                        .append(escape(toString(value)))
                        .append(">");
            }

            if (hover instanceof HoverEvent.ShowItem(ItemStackTemplate item)) {
                Identifier id = BuiltInRegistries.ITEM.getKey(item.item().value());
                out.append("<hover:show_item ")
                        .append(id)
                        .append(" ")
                        .append(item.count())
                        .append(">");
            }
        }

        if (!Objects.equals(parent.getInsertion(), style.getInsertion()) && style.getInsertion() != null) {
            out.append("<insertion:")
                    .append(escape(style.getInsertion()))
                    .append(">");
        }
    }

    private static void appendStyleClose(Style parent, Style style, StringBuilder out) {
        if (!Objects.equals(parent.getInsertion(), style.getInsertion()) && style.getInsertion() != null)
            out.append("</insertion>");

        if (!Objects.equals(parent.getHoverEvent(), style.getHoverEvent()) && style.getHoverEvent() != null)
            out.append("</hover>");

        if (!Objects.equals(parent.getClickEvent(), style.getClickEvent()) && style.getClickEvent() != null)
            out.append("</click>");

        if (!Objects.equals(parent.getShadowColor(), style.getShadowColor()) && style.getShadowColor() != null)
            out.append("</shadow>");

        if (style.isObfuscated() && !parent.isObfuscated()) out.append("</obfuscated>");
        if (style.isStrikethrough() && !parent.isStrikethrough()) out.append("</strikethrough>");
        if (style.isUnderlined() && !parent.isUnderlined()) out.append("</underlined>");
        if (style.isItalic() && !parent.isItalic()) out.append("</italic>");
        if (style.isBold() && !parent.isBold()) out.append("</bold>");

        if (!Objects.equals(parent.getColor(), style.getColor()) && style.getColor() != null)
            out.append("</color>");
    }

    private static String escape(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("<", "\\<")
                .replace(">", "\\>");
    }

    private static final class Parser implements Context {
        private final String input;
        private final Deque<Frame> stack = new ArrayDeque<>();

        private Parser(String input) {
            this.input = input;
            this.stack.push(new Frame());
        }

        public Component parse() {
            StringBuilder literal = new StringBuilder();
            for (int i = 0; i < input.length(); ) {
                char ch = input.charAt(i);
                if (ch == '\\' && i + 1 < input.length()) {
                    literal.append(input.charAt(i + 1));
                    i += 2;
                    continue;
                }
                if (ch == '<') {
                    int end = findTagEnd(i);
                    if (end > i) {
                        processTag(input.substring(i + 1, end), literal);
                        i = end + 1;
                        continue;
                    }
                }
                literal.append(ch);
                i++;
            }
            flush(literal);
            while (stack.size() > 1) finalizeFrame();
            return render(stack.isEmpty() ? List.of() : stack.pop().nodes);
        }

        private void processTag(String raw, StringBuilder literal) {
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) return;
            List<String> parts = splitArgs(trimmed);
            String head = parts.getFirst().toLowerCase(Locale.ROOT);
            flush(literal);

            if (head.startsWith("/")) closeTag(head.substring(1));
            else {
                boolean negate = head.startsWith("!");
                String tagName = negate ? head.substring(1) : head;
                TagHandler handler = REGISTRY.get(tagName);
                if (handler == null) {
                    TextColor color = parseTextColor(tagName);
                    if (color != null) handler = (args, ctx) -> ctx.applyStyle(s -> s.withColor(color));
                }
                if (handler != null) {
                    List<String> args = negate ? List.of("false") : parts.subList(1, parts.size());
                    Frame newFrame = new Frame(tagName, handler, args);
                    stack.push(newFrame);
                    TagFrame res = handler.handle(newFrame.args, this);
                    if (res == null) {
                        Frame f = stack.pop();
                        if (!stack.isEmpty()) stack.peek().nodes.addAll(f.nodes);
                    } else newFrame.result = res;
                }
            }
        }

        @Override
        public TagFrame applyStyle(UnaryOperator<Style> op) {
            return new TagFrame(op, null);
        }

        @Override
        public TagFrame transformSegments(UnaryOperator<List<Segment>> op) {
            return new TagFrame(null, op);
        }

        @Override
        public void insert(Component comp) {
            if (!stack.isEmpty()) stack.peek().nodes.add(new ComponentNode(comp));
        }

        @Override
        public void reset() {
            while (stack.size() > 1) finalizeFrame();
        }

        private void flush(StringBuilder sb) {
            if (!sb.isEmpty() && !stack.isEmpty()) {
                stack.peek().nodes.add(new ComponentNode(Component.literal(sb.toString())));
                sb.setLength(0);
            }
        }

        private void closeTag(String name) {
            boolean found = false;
            for (Frame f : stack)
                if (name.equalsIgnoreCase(f.name)) {
                    found = true;
                    break;
                }
            if (!found) return;
            while (stack.size() > 1) {
                Frame top = stack.peek();
                boolean match = name.equalsIgnoreCase(top.name);
                finalizeFrame();
                if (match) break;
            }
        }

        private void finalizeFrame() {
            if (stack.size() <= 1) return;
            Frame frame = stack.pop();
            if (!stack.isEmpty()) stack.peek().nodes.add(new TagNode(frame.result, List.copyOf(frame.nodes)));
        }

        private int findTagEnd(int start) {
            boolean q = false;
            char qc = '\0';
            boolean esc = false;
            for (int i = start + 1; i < input.length(); i++) {
                char c = input.charAt(i);
                if (esc) {
                    esc = false;
                    continue;
                }
                if (c == '\\') {
                    esc = true;
                    continue;
                }
                if (q) {
                    if (c == qc) q = false;
                    continue;
                }
                if (c == '\'' || c == '"') {
                    q = true;
                    qc = c;
                    continue;
                }
                if (c == '>') return i;
            }
            return -1;
        }

        private Component render(List<Node> nodes) {
            List<Segment> segments = renderSegments(nodes, Style.EMPTY);
            MutableComponent out = Component.empty();
            for (Segment s : segments) out.append(s.component);
            return out;
        }

        private List<Segment> renderSegments(List<Node> nodes, Style base) {
            List<Segment> out = new ArrayList<>();
            for (Node node : nodes) {
                if (node instanceof ComponentNode(Component component)) {
                    out.add(new Segment(component.copy().withStyle(base)));
                    continue;
                }

                TagNode tn = (TagNode) node;
                Style active = (tn.res != null && tn.res.styleApplier != null) ? tn.res.styleApplier.apply(base) : base;
                List<Segment> children = renderSegments(tn.children, active);
                if (tn.res != null && tn.res.segmentTransformer != null)
                    children = tn.res.segmentTransformer.apply(children);
                out.addAll(children);
            }
            return merge(out);
        }
    }

    private static List<String> splitArgs(String raw) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean q = false;
        char qc = '\0';
        boolean esc = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (esc) {
                sb.append(c);
                esc = false;
                continue;
            }
            if (c == '\\') {
                esc = true;
                continue;
            }
            if (q) {
                if (c == qc) q = false;
                else sb.append(c);
                continue;
            }
            if (c == '\'' || c == '"') {
                q = true;
                qc = c;
                continue;
            }
            if (c == ':' || (c == ' ' && !isIdContext(raw, i))) {
                if (!sb.isEmpty()) {
                    out.add(sb.toString());
                    sb.setLength(0);
                }
                continue;
            }
            sb.append(c);
        }
        if (!sb.isEmpty()) out.add(sb.toString());
        return out;
    }

    private static boolean isIdContext(String raw, int pos) {
        int lastColon = raw.lastIndexOf(':', pos - 1);
        if (lastColon == -1) return false;
        String prev = raw.substring(lastColon + 1, pos);
        return !prev.contains(" ") && !prev.contains("<") && !prev.contains(">");
    }

    private static boolean parseBool(List<String> args, int idx, boolean def) {
        if (args.size() <= idx) return def;
        String v = args.get(idx).toLowerCase(Locale.ROOT);
        return !List.of("false", "0", "off", "none").contains(v);
    }

    private static TextColor parseTextColor(String raw) {
        if (raw == null || raw.isEmpty()) return null;

        if (raw.startsWith("#")) {
            String hex = raw.substring(1);

            if (hex.length() == 3) {
                char r = hex.charAt(0);
                char g = hex.charAt(1);
                char b = hex.charAt(2);
                hex = "" + r + r + g + g + b + b;
            }

            try {
                return TextColor.fromRgb(Integer.parseInt(hex, 16));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        try {
            return TextColor.parseColor(raw).getOrThrow();
        } catch (Exception e) {
            return null;
        }
    }

    private static Style applyShadow(Style base, String colorVal, double alpha) {
        if (List.of("none", "off", "clear", "false").contains(colorVal))
            return base.withShadowColor(0);

        int rgb;
        if (colorVal.equals("current")) {
            rgb = base.getColor() != null ? base.getColor().getValue() & 0xFFFFFF : 0;
        }
        else {
            TextColor tc = parseTextColor(colorVal);
            if (tc == null) return base;
            rgb = tc.getValue() & 0xFFFFFF;
        }

        return base.withShadowColor((Math.clamp((int) (alpha * 255), 0, 255) << 24) | rgb);
    }

    private static TagFrame applyComplexEffect(List<String> args, Context ctx, boolean isTransition) {
        if (args.isEmpty())
            return null;

        List<String> mutableArgs = new ArrayList<>(args);
        double phase = isNumber(mutableArgs.getLast()) ? parseDouble(mutableArgs.removeLast(), 0) : 0;
        List<TextColor> colors = mutableArgs.stream().map(FilamentFormatter::parseTextColor).filter(Objects::nonNull).toList();
        if (colors.isEmpty())
            return null;

        return ctx.transformSegments(segs -> isTransition ? applyTransition(segs, colors, phase) : applyGradient(segs, colors, phase));
    }

    private static List<Segment> applyGradient(List<Segment> input, List<TextColor> colors, double phase) {
        int total = countCP(input);
        if (total == 0) return input;
        int[] map = new int[total];
        for (int i = 0; i < total; i++) map[i] = sample(colors, wrap((i / (double) Math.max(1, total - 1)) + phase));
        return recolor(input, map);
    }

    private static List<Segment> applyTransition(List<Segment> input, List<TextColor> colors, double phase) {
        int total = countCP(input);
        if (total == 0) return input;
        int color = sample(colors, wrap(phase));
        int[] map = new int[total];
        Arrays.fill(map, color);
        return recolor(input, map);
    }

    private static List<Segment> applyRainbow(List<Segment> input, double phase, boolean rev) {
        int total = countCP(input);
        if (total == 0) return input;
        int[] map = new int[total];
        for (int i = 0; i < total; i++) {
            double t = rev ? 1.0 - (i / (double) Math.max(1, total - 1)) : (i / (double) Math.max(1, total - 1));
            map[i] = java.awt.Color.HSBtoRGB((float) wrap(t + phase), 1.0f, 1.0f) & 0xFFFFFF;
        }
        return recolor(input, map);
    }

    private static List<Segment> recolor(List<Segment> in, int[] colors) {
        List<Segment> out = new ArrayList<>();
        int idx = 0;
        for (Segment s : in) {
            if (s.component.getContents() instanceof PlainTextContents.LiteralContents(String text)) {
                for (int cp : text.codePoints().toArray()) {
                    out.add(new Segment(Component.literal(new String(Character.toChars(cp))).withStyle(s.component.getStyle().withColor(TextColor.fromRgb(colors[idx++])))));
                }
            } else
                out.add(new Segment(s.component.copy().withStyle(s.component.getStyle().withColor(TextColor.fromRgb(colors[idx++])))));
        }
        return merge(out);
    }

    private static int sample(List<TextColor> colors, double t) {
        if (colors.size() == 1) return colors.getFirst().getValue();
        double scaled = t * (colors.size() - 1);
        int i = (int) scaled;
        int c1 = colors.get(i).getValue();
        int c2 = colors.get(Math.min(i + 1, colors.size() - 1)).getValue();
        double f = scaled - i;
        return lerp(c1, c2, f);
    }

    private static int lerp(int c1, int c2, double t) {
        int r = (int) ((c1 >> 16 & 0xFF) + ((c2 >> 16 & 0xFF) - (c1 >> 16 & 0xFF)) * t);
        int g = (int) ((c1 >> 8 & 0xFF) + ((c2 >> 8 & 0xFF) - (c1 >> 8 & 0xFF)) * t);
        int b = (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t);
        return (r << 16) | (g << 8) | b;
    }

    private static int countCP(List<Segment> segs) {
        int count = 0;
        for (Segment s : segs) {
            if (s.component.getContents() instanceof PlainTextContents.LiteralContents(String text))
                count += text.codePointCount(0, text.length());
            else count++;
        }
        return count;
    }

    private static double wrap(double v) {
        v %= 1.0;
        return v < 0 ? v + 1.0 : v;
    }

    private static boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static double parseDouble(String s, double def) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static List<Segment> merge(List<Segment> in) {
        if (in.size() <= 1)
            return in;

        List<Segment> out = new ArrayList<>();
        Segment cur = in.getFirst();
        for (int i = 1; i < in.size(); i++) {
            Segment next = in.get(i);
            if (cur.component.getContents() instanceof PlainTextContents.LiteralContents(String text) && next.component.getContents() instanceof PlainTextContents.LiteralContents(String text1) && Objects.equals(cur.component.getStyle(), next.component.getStyle())) {
                cur = new Segment(Component.literal(text + text1).withStyle(cur.component.getStyle()));
            } else {
                out.add(cur);
                cur = next;
            }
        }
        out.add(cur);
        return out;
    }

    private static ClickEvent createClick(String action, String val) {
        return switch (action) {
            case "open_url", "url" -> new ClickEvent.OpenUrl(URI.create(val));
            case "run_command", "command", "cmd" -> new ClickEvent.RunCommand(val);
            case "suggest_command", "suggest" -> new ClickEvent.SuggestCommand(val);
            case "copy_to_clipboard", "copy" -> new ClickEvent.CopyToClipboard(val);
            case "change_page" -> new ClickEvent.ChangePage(Integer.parseInt(val));
            default -> null;
        };
    }

    private static HoverEvent createHover(String action, String val) {
        if (action.equals("show_text")) return new HoverEvent.ShowText(parse(val));
        if (action.equals("show_item")) {
            List<String> p = splitArgs(val);
            if (p.isEmpty()) return null;
            Identifier id = Identifier.tryParse(p.getFirst());
            if (id == null) return null;

            Optional<Holder.Reference<Item>> itemOpt = BuiltInRegistries.ITEM.get(id);
            Item item = itemOpt.map(Holder.Reference::value).orElse(Items.STONE);
            return new HoverEvent.ShowItem(ItemStackTemplate.fromNonEmptyStack(new ItemStack(item, p.size() > 1 ? (int) parseDouble(p.get(1), 1) : 1)));
        }
        return null;
    }

    public interface TagHandler {
        TagFrame handle(List<String> args, Context ctx);
    }

    public interface Context {
        TagFrame applyStyle(UnaryOperator<Style> op);

        TagFrame transformSegments(UnaryOperator<List<Segment>> op);

        void insert(Component component);

        void reset();
    }

    public record TagFrame(UnaryOperator<Style> styleApplier, UnaryOperator<List<Segment>> segmentTransformer) { }

    private interface Node { }

    private record ComponentNode(Component component) implements Node { }

    private record TagNode(TagFrame res, List<Node> children) implements Node { }

    public record Segment(Component component) { }

    private static class Frame {
        String name;
        TagHandler handler;
        List<String> args;
        TagFrame result;
        List<Node> nodes = new ArrayList<>();

        Frame() {
        }

        Frame(String name, TagHandler handler, List<String> args) {
            this.name = name;
            this.handler = handler;
            this.args = args;
        }
    }
}
