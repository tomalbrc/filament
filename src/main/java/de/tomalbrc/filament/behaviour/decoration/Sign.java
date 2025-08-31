package de.tomalbrc.filament.behaviour.decoration;

import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.item.FilamentItem;
import de.tomalbrc.filament.registry.WaxableRegistry;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.sgui.api.gui.SignGui;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Sign implements DecorationBehaviour<Sign.Config> {
    Config config;

    Map<ConfiguredSignElement, Component[]> lines = new Reference2ObjectOpenHashMap<>();
    Map<ConfiguredSignElement, SignLikeText> textDisplayElement = new Reference2ObjectOpenHashMap<>();

    public Sign(Config config) {
        this.config = config;
    }

    @Override
    public @NotNull Config getConfig() {
        return config;
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        if (!config.canEdit)
            return InteractionResult.PASS;

        boolean isWaxed = WaxableRegistry.getPrevious(decorationBlockEntity.getBlock()) != null;

        var itemStack = player.getItemInHand(hand);
        if (!isWaxed && itemStack.is(Items.HONEYCOMB) || itemStack.getItem() instanceof FilamentItem filamentItem && filamentItem.has(Behaviours.WAX)) {
            return InteractionResult.PASS;
        }

        if (!isWaxed && !player.isSecondaryUseActive()) {
            var e = getClosestTextElement(decorationBlockEntity, location);
            if (e != null) {
                var signGui = new SignGui(player) {
                    @Override
                    public void onClose() {
                        for (int i = 0; i < Math.min(e.lines, lines.get(e).length); i++) {
                            lines.get(e)[i] = getLine(i);
                        }
                        decorationBlockEntity.setChanged();
                        changed(decorationBlockEntity, e);

                        super.onClose();
                    }
                };

                for (int i = 0; i < lines.computeIfAbsent(e, x -> new Component[4]).length; i++) {
                    signGui.setLine(i, getLine(e, i));
                }

                signGui.setSignType(config.block);
                signGui.open();
                return InteractionResult.CONSUME;
            }

        }

        return DecorationBehaviour.super.interact(player, hand, location, decorationBlockEntity);
    }

    @Override
    public void read(ValueInput output, DecorationBlockEntity blockEntity) {
        for (int i = 0; i < this.config.elements.size(); i++) {
            var list = output.list("Lines_" + i, ComponentSerialization.CODEC);
            var el = this.config.elements.get(i);
            list.ifPresent(x -> {
                int idx = 0;
                for (Component component : x) {
                    this.lines.computeIfAbsent(el, e -> new Component[4])[idx] = component;
                    idx++;
                }

                changed(blockEntity, el);
            });
        }
    }

    @Override
    public void write(ValueOutput input, DecorationBlockEntity blockEntity) {
        for (int i = 0; i < this.config.elements.size(); i++) {
            var list = input.list("Lines_" + i, ComponentSerialization.CODEC);
            var el = this.config.elements.get(i);
            for (int j = 0; j < this.lines.computeIfAbsent(el, x -> new Component[4]).length; j++) {
                list.add(getLine(el, j));
            }
        }
    }

    private Component getLine(ConfiguredSignElement element, int i) {
        var l = this.lines.computeIfAbsent(element, x -> new Component[4])[i];
        if (l == null) {
            return Component.empty();
        }

        return l;
    }

    private void changed(DecorationBlockEntity decorationBlockEntity, ConfiguredSignElement configuredSignElement) {
        MutableComponent c = Component.empty();
        for (Component line : this.lines.get(configuredSignElement)) {
            c.append("\n").append(line);
        }

        Matrix4f matrix4f = new Matrix4f();
        matrix4f.rotate(configuredSignElement.rotation);
        matrix4f.scale(configuredSignElement.scale);

        var element = textDisplayElement.computeIfAbsent(configuredSignElement, x -> new SignLikeText());

        element.setOffset(new Vec3(configuredSignElement.offset.rotateY(Mth.DEG_TO_RAD * (-decorationBlockEntity.getVisualRotationYInDegrees()), new Vector3f())));
        element.setSeeThrough(configuredSignElement.seeThrough);
        element.setBackground(configuredSignElement.backgroundColor);
        element.setYaw(decorationBlockEntity.getVisualRotationYInDegrees() + 180);
        element.setTransformation(matrix4f);
        element.setBillboardMode(configuredSignElement.billboardMode);
        element.setTextAlignment(configuredSignElement.alignment);

        if (element.getHolder() == null)
            decorationBlockEntity.getOrCreateHolder().addElement(element);

        element.setText(c, DyeColor.BLUE, true);
        decorationBlockEntity.getOrCreateHolder().tick();
    }

    public Sign.ConfiguredSignElement getClosestTextElement(DecorationBlockEntity decorationBlockEntity, Vec3 location) {
        if (config.elements.size() == 1) {
            return config.elements.getFirst();
        } else {
            double dist = Double.MAX_VALUE;
            ConfiguredSignElement nearest = null;
            for (var element : config.elements) {
                Vec3 q = decorationBlockEntity.getBlockPos().getCenter().add(new Vec3(this.getTranslation(element).rotateY((-decorationBlockEntity.getVisualRotationYInDegrees() + 180) * Mth.DEG_TO_RAD)));
                double distance = q.distanceTo(location);

                if (distance < dist) {
                    dist = distance;
                    nearest = element;
                }
            }

            return nearest;
        }
    }

    private Vector3f getTranslation(ConfiguredSignElement element) {
        return new Vector3f(element.offset).sub(0, 0.475f, 0).rotate(element.rotation).rotateY(Mth.PI);
    }

    public static class Config {
        public List<ConfiguredSignElement> elements = List.of();
        public boolean canEdit;
        public Block block = Blocks.OAK_SIGN;
    }

    public static class ConfiguredSignElement {
        public Vector3f offset = new Vector3f(0, 0, 0.5f);
        public Vector3f scale = new Vector3f(0.5f);
        public Quaternionf rotation = new Quaternionf();
        public int lines = 4;
        public Display.BillboardConstraints billboardMode = Display.BillboardConstraints.FIXED;
        public int backgroundColor = 0x00_000000;
        public boolean seeThrough = false;
        public Display.TextDisplay.Align alignment = Display.TextDisplay.Align.CENTER;
    }

    public static class SignLikeText extends TextDisplayElement {
        private static final int CENTER = 4;
        private final TextDisplayElement[] displayText = new TextDisplayElement[9];

        private Component text = Component.empty();

        private boolean glow = false;
        private float viewRange = 1;
        private Matrix4f transformation;

        public SignLikeText() {
            for (int i = 0; i < this.displayText.length; i++) {
                var text = new TextDisplayElement();
                text.setBackground(0);
                text.setInvisible(true);
                this.displayText[i] = text;
            }
        }

        public void setText(Component text, DyeColor color, boolean glow) {
            this.text = text;
            var brightness = glow ? Brightness.FULL_BRIGHT : null;
            this.displayText[CENTER].setText(Component.empty().append(text).withColor(color.getTextColor()));
            this.displayText[CENTER].setBrightness(brightness);

            if (glow) {
                var background = Component.empty().append(text).withColor(getOutlineColor(color));
                for (int i = 0; i < this.displayText.length; i++) {
                    if (i != CENTER) {
                        this.displayText[i].setText(background);
                        this.displayText[i].setBrightness(brightness);
                        if (!this.glow && this.getHolder() != null) {
                            this.getHolder().addElement(this.displayText[i]);
                        }
                    }
                }
            } else if (this.glow && this.getHolder() != null) {
                for (int i = 0; i < this.displayText.length; i++) {
                    if (i != CENTER) {
                        this.getHolder().removeElement(this.displayText[i]);
                    }
                }
            }
            this.glow = glow;
        }

        public static int getOutlineColor(DyeColor color) {
            if (color == DyeColor.BLACK) {
                return -988212;
            } else {
                int i = color.getTextColor();
                int j = (int) ((double) ARGB.red(i) * 0.4);
                int k = (int) ((double) ARGB.green(i) * 0.4);
                int l = (int) ((double) ARGB.blue(i) * 0.4);
                return ARGB.color(0, j, k, l);
            }
        }

        @Override
        public void setHolder(ElementHolder holder) {
            var old = this.getHolder();
            super.setHolder(holder);
            if (holder != null) {
                if (this.glow) {
                    holder.addElement(this.displayText[CENTER]);
                } else {
                    for (var x : this.displayText) {
                        holder.addElement(x);
                    }
                }
            } else if (old != null) {
                for (var x : this.displayText) {
                    old.removeElement(x);
                }
            }
        }

        @Override
        public void startWatching(ServerPlayer serverPlayer, Consumer<Packet<ClientGamePacketListener>> consumer) {

        }

        @Override
        public void stopWatching(ServerPlayer serverPlayer, Consumer<Packet<ClientGamePacketListener>> consumer) {

        }

        @Override
        public void tick() {
            if (this.glow) {
                for (var x : this.displayText) {
                    x.tick();
                }
            } else {
                this.displayText[CENTER].tick();
            }
        }

        public void setViewRange(float v) {
            this.viewRange = v;
            for (var x : this.displayText) {
                x.setViewRange(v);
            }
        }

        public void setYaw(float yaw) {
            for (var x : this.displayText) {
                x.setYaw(yaw);
            }

            if (transformation != null) this.setTransformation(transformation);
        }

        public void setDisplaySize(int x, int y) {
            for (var e : this.displayText) {
                e.setDisplaySize(x, y);
            }
        }

        @Override
        public IntList getEntityIds() {
            return IntList.of();
        }

        @Override
        public void notifyMove(Vec3 oldPos, Vec3 currentPos, Vec3 delta) {

        }

        public void setSeeThrough(boolean seeThrough) {
            for (var e : this.displayText) e.setSeeThrough(seeThrough);
        }

        public void setBackground(int backgroundColor) {
            for (var e : this.displayText) e.setBackground(backgroundColor);
        }

        public void setTransformation(Matrix4f matrix4f) {
            this.transformation = matrix4f;
            for (int i = 0; i < this.displayText.length; i++) {
                var mat = new Matrix4f(matrix4f);

                if (CENTER == i) {
                    this.displayText[i].setTransformation(mat.translate(new Vector3f(0, 0, 0.002f)));
                } else {
                    int x = i % 3 - 1;
                    int y = i / 3 - 1;
                    this.displayText[i].setTransformation(mat.translate(new Vector3f(x / 40f, y / 40f, 0)));
                }
            }
        }

        public void setBillboardMode(Display.BillboardConstraints billboardMode) {
            for (var e : this.displayText) e.setBillboardMode(billboardMode);
        }

        public void setTextAlignment(Display.TextDisplay.Align alignment) {
            for (var e : this.displayText) e.setTextAlignment(alignment);
        }
    }
}
