package de.tomalbrc.filament.entity.skill.condition;

import de.tomalbrc.filament.entity.skill.SkillContext;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

class HoldingCondition implements Condition { private final ItemStack sample; HoldingCondition(ItemStack s){this.sample=s;} public boolean test(SkillContext ctx, LivingEntity target){ return ItemStack.isSameItem(target.getItemBySlot(EquipmentSlot.MAINHAND), sample); } }
