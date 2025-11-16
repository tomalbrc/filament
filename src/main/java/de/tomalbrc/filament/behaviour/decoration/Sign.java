package de.tomalbrc.filament.behaviour.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.DecorationBehaviour;
import de.tomalbrc.filament.behaviour.Behaviours;
import de.tomalbrc.filament.decoration.block.entity.DecorationBlockEntity;
import de.tomalbrc.filament.item.FilamentItem;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.sgui.api.gui.SignGui;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Brightness;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Sign implements DecorationBehaviour<Sign.Config> {
    Config config;

    Map<ConfiguredSignElement, SignData> signData = new Reference2ObjectOpenHashMap<>();
    boolean isWaxed = false;
    UUID editingPlayer = null;

    public Sign(Config config) {
        this.config = config;
    }

    @Override
    public @NotNull Config getConfig() {
        return config;
    }

    @Override
    public InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationBlockEntity decorationBlockEntity) {
        var e = getClosestTextElement(decorationBlockEntity, location);
        var sd = getSignData(e);

        if (config.canEdit) {
            var itemStack = player.getItemInHand(hand);
            if (config.waxable && !isWaxed && (itemStack.is(Items.HONEYCOMB) || itemStack.getItem() instanceof FilamentItem filamentItem && filamentItem.has(Behaviours.WAX))) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, decorationBlockEntity.getBlockPos(), itemStack);
                player.level().levelEvent(null, LevelEvent.PARTICLES_AND_SOUND_WAX_ON, decorationBlockEntity.getBlockPos(), 0);

                if (!itemStack.isDamageableItem())
                    itemStack.consume(1, player);
                else itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

                this.isWaxed = true;
                decorationBlockEntity.setChanged();

                return InteractionResult.CONSUME;
            }

            var empty = sd.isEmpty();

            if (config.dyeable && !empty && itemStack.getItem() instanceof DyeItem dyeItem && apply(player.level(), decorationBlockEntity, e, dyeItem.getDyeColor(), sd.glow)) {
                if (!itemStack.isDamageableItem())
                    itemStack.consume(1, player);
                else itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

                return InteractionResult.CONSUME;
            }

            if (!empty && itemStack.is(Items.GLOW_INK_SAC) && !sd.glow) {
                sd.glow = true;
                player.level().playSound(null, decorationBlockEntity.getBlockPos(), SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                changed(decorationBlockEntity, e);

                if (!itemStack.isDamageableItem())
                    itemStack.consume(1, player);
                else itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

                return InteractionResult.CONSUME;
            }

            if (!isWaxed && !player.isSecondaryUseActive() && (editingPlayer == null || Filament.SERVER.getPlayerList().getPlayer(editingPlayer) == null)) {
                if (e != null) {
                    editingPlayer = player.getUUID();
                    var signGui = new SignGui(player) {
                        @Override
                        public void onClose() {
                            for (int i = 0; i < e.lines; i++) {
                                sd.text[i] = getLine(i);
                            }
                            changed(decorationBlockEntity, e);
                            editingPlayer = null;
                            super.onClose();
                        }
                    };

                    for (int i = 0; i < e.lines; i++) {
                        signGui.setLine(i, getLine(e, i));
                    }

                    signGui.setSignType(config.block);
                    signGui.open();
                    return InteractionResult.CONSUME;
                }
            }
        }

        if (executeClickCommandsIfPresent(player.serverLevel(), player, decorationBlockEntity.getBlockPos(), sd.text)) {
            return InteractionResult.CONSUME;
        }

        return DecorationBehaviour.super.interact(player, hand, location, decorationBlockEntity);
    }


    public boolean executeClickCommandsIfPresent(ServerLevel level, Player player, BlockPos blockPos, Component[] components) {
        boolean bl2 = false;

        for(Component component : components) {
            Style style = component.getStyle();
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                player.getServer().getCommands().performPrefixedCommand(createCommandSourceStack(player, level, blockPos), clickEvent.getValue());
                bl2 = true;
            }
        }

        return bl2;
    }

    private static CommandSourceStack createCommandSourceStack(@Nullable Player player, ServerLevel serverLevel, BlockPos blockPos) {
        String string = player == null ? "Decoration" : player.getName().getString();
        Component component = player == null ? Component.literal("Decoration") : player.getDisplayName();
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(blockPos), Vec2.ZERO, serverLevel, 2, string, component, serverLevel.getServer(), player);
    }

    @Override
    public void read(CompoundTag output, HolderLookup.Provider lookup, DecorationBlockEntity blockEntity) {
        this.isWaxed = output.contains("IsWaxed") && output.getBoolean("IsWaxed");

        if (output.contains("SignData")) {
            var tag = output.get("SignData");
            var dec = SignData.CODEC.listOf().decode(lookup.createSerializationContext(NbtOps.INSTANCE), tag);
            dec.ifSuccess(pair -> {
                var x = pair.getFirst();
                for (int i = 0; i < config.elements.size(); i++) {
                    ConfiguredSignElement signElement = config.elements.get(i);
                    this.signData.put(signElement, x.get(i));
                    changed(blockEntity, signElement);
                }
            });
        }
    }

    @Override
    public void write(CompoundTag input, HolderLookup.Provider lookup, DecorationBlockEntity blockEntity) {
        input.putBoolean("IsWaxed", this.isWaxed);
        List<SignData> data = new ObjectArrayList<>();
        for (int i = 0; i < this.config.elements.size(); i++) {
            ConfiguredSignElement signElement = config.elements.get(i);
            data.add(i, getSignData(signElement));
        }
        input.put("SignData", SignData.CODEC.listOf().encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE), data).getOrThrow());
    }

    @Override
    public void applyImplicitComponents(DecorationBlockEntity decorationBlockEntity, BlockEntity.DataComponentInput dataComponentGetter) {
        var data = dataComponentGetter.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            if (data.contains("SignData")) {
                var t = data.copyTag();
                var sd = t.get("SignData");
                SignData.CODEC.listOf().decode(RegistryOps.create(NbtOps.INSTANCE, Filament.SERVER.registryAccess()), sd).ifSuccess(pair -> {
                    var res = pair.getFirst();
                    for (int i = 0; i < config.elements.size(); i++) {
                        ConfiguredSignElement signElement = config.elements.get(i);
                        this.signData.put(signElement, res.get(i));
                        changed(decorationBlockEntity, signElement);
                    }
                });
            }

        } else {
            for (int i = 0; i < config.elements.size(); i++) {
                ConfiguredSignElement signElement = config.elements.get(i);
                changed(decorationBlockEntity, signElement);
            }
        }
    }

    @Override
    public void collectImplicitComponents(DecorationBlockEntity decorationBlockEntity, DataComponentMap.Builder builder) {
        List<SignData> data = new ObjectArrayList<>();
        for (int i = 0; i < this.config.elements.size(); i++) {
            ConfiguredSignElement signElement = config.elements.get(i);
            data.add(i, getSignData(signElement));
        }

        builder.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY.update(x -> {
            SignData.CODEC.listOf().encodeStart(NbtOps.INSTANCE, data).ifSuccess(res -> {
                x.put("SignData", res);
            });
        }));
    }

    private Component getLine(ConfiguredSignElement element, int i) {
        return getSignData(element).text[i];
    }

    private SignData getSignData(ConfiguredSignElement element) {
        return this.signData.computeIfAbsent(element, x -> new SignData());
    }

    private void changed(DecorationBlockEntity decorationBlockEntity, ConfiguredSignElement configuredSignElement) {
        decorationBlockEntity.setChanged();
        var sd = getSignData(configuredSignElement);

        MutableComponent c = Component.empty();
        if (sd.isEmpty() && configuredSignElement.text != null) {
            for (int i = 0; i < configuredSignElement.text.length; i++) {
                c.append("\n").append(configuredSignElement.text[i]);
            }
        } else {
            for (int i = 0; i < configuredSignElement.lines; i++) {
                c.append("\n").append(getLine(configuredSignElement, i));
            }
        }

        Matrix4f matrix4f = new Matrix4f();
        matrix4f.rotate(configuredSignElement.rotation);
        matrix4f.scale(configuredSignElement.scale);

        if (sd.display == null) {
            sd.display = new SignLikeTextDisplay();
        }
        var element = sd.display;
        element.setOffset(new Vec3(configuredSignElement.offset.rotateY(Mth.DEG_TO_RAD * (-decorationBlockEntity.getVisualRotationYInDegrees()), new Vector3f())));
        element.setSeeThrough(configuredSignElement.seeThrough);
        element.setBackground(configuredSignElement.backgroundColor);
        element.setYaw(decorationBlockEntity.getVisualRotationYInDegrees() + 180);
        element.setDisplaySize(3, 3);
        element.setTransformation(matrix4f);
        element.setBillboardMode(configuredSignElement.billboardMode);
        element.setTextAlignment(configuredSignElement.alignment);

        if (element.getHolder() == null)
            decorationBlockEntity.getOrCreateHolder().addElement(element);

        element.setText(c, sd.color, sd.glow);
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

    public boolean apply(Level level, DecorationBlockEntity blockEntity, ConfiguredSignElement element, DyeColor color, boolean glow) {
        boolean changedColor = false;
        var sd = getSignData(element);
        if (color != null && sd.color != color) {
            sd.color = color;
            changedColor = true;
            level.playSound(null, blockEntity.getBlockPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        sd.glow = glow;

        changed(blockEntity, element);

        return changedColor;
    }

    public static class Config {
        public boolean canEdit = true;
        public boolean waxable = true;
        public boolean dyeable = true;
        public Block block = Blocks.OAK_SIGN;
        public List<ConfiguredSignElement> elements = List.of();
    }

    public static class ConfiguredSignElement {
        public Vector3f offset = new Vector3f(0, 0, 0.5f);
        public Vector3f scale = new Vector3f(0.5f);
        public Quaternionf rotation = new Quaternionf();
        public int lines = 4;
        public Component[] text;
        public Display.BillboardConstraints billboardMode = Display.BillboardConstraints.FIXED;
        public int backgroundColor = 0x00_000000;
        public boolean seeThrough = false;
        public Display.TextDisplay.Align alignment = Display.TextDisplay.Align.CENTER;
    }

    public static class SignData {
        public Component[] text = new Component[]{Component.empty(), Component.empty(), Component.empty(), Component.empty()};
        public SignLikeTextDisplay display;
        public boolean glow = false;
        public DyeColor color = DyeColor.WHITE;

        public static final Codec<SignData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ComponentSerialization.CODEC.listOf().fieldOf("text").forGetter(signData -> List.of(signData.text)),
                        Codec.BOOL.fieldOf("glow").forGetter(signData -> signData.glow),
                        DyeColor.CODEC.fieldOf("color").forGetter(signData -> signData.color)
                ).apply(instance, (textList, glow, color) -> {
                    SignData data = new SignData();
                    data.text = textList.toArray(new Component[0]);
                    data.glow = glow;
                    data.color = color;
                    return data;
                })
        );

        private boolean isEmpty() {
            for (Component line : text) {
                if (line != null && !line.toFlatList().isEmpty())
                    return false;
            }

            return true;
        }
    }

    public static class SignLikeTextDisplay extends TextDisplayElement {
        private static final int CENTER = 4;
        private final TextDisplayElement[] displayText = new TextDisplayElement[9];

        private boolean glow = false;
        private Matrix4f transformation;

        public SignLikeTextDisplay() {
            for (int i = 0; i < this.displayText.length; i++) {
                var text = new TextDisplayElement();
                text.setBackground(0);
                text.setInvisible(true);
                this.displayText[i] = text;
            }
        }

        public void setText(Component text, DyeColor color, boolean glow) {
            var brightness = glow ? Brightness.FULL_BRIGHT : null;
            this.displayText[CENTER].setText(Component.empty().append(text).withColor(color.getTextureDiffuseColor()));
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
                int i = color.getTextureDiffuseColor();
                int j = (int) ((double) FastColor.ARGB32.red(i) * 0.4);
                int k = (int) ((double) FastColor.ARGB32.green(i) * 0.4);
                int l = (int) ((double) FastColor.ARGB32.blue(i) * 0.4);
                return FastColor.ARGB32.color(0, j, k, l);
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

        public void setOffset(Vec3 offset) {
            for (var e : this.displayText) e.setOffset(offset);
        }
    }
}
