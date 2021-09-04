package org.cardboardpowered.mixin.block;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.dispenser.ShearsDispenserBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPointer;

@Mixin(ShearsDispenserBehavior.class)
public class MixinShearsDispenserBehavior {

    // todo: nonstatic
    private static Block cardboard_block;
    private static CraftItemStack cardboard_saved;

    @Inject(at = @At("HEAD"), method = "dispenseSilently")
    protected void dispenseSilently(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable ci) {
        cardboard_block = ((IMixinWorld)pointer.getWorld()).getWorldImpl().getBlockAt(pointer.getPos().getX(), pointer.getPos().getY(), pointer.getPos().getZ());
        cardboard_saved = CraftItemStack.asCraftMirror(stack);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Shearable;sheared(Lnet/minecraft/sound/SoundCategory;)V"),
            method = "tryShearEntity")
    private static void doEvent(Shearable s, SoundCategory cat) {
        if (!callBlockShearEntityEvent((LivingEntity)s, cardboard_block, cardboard_saved).isCancelled()) {
            s.sheared(cat);
        }
    }

    private static BlockShearEntityEvent callBlockShearEntityEvent(Entity animal, org.bukkit.block.Block dispenser, CraftItemStack is) {
        BlockShearEntityEvent bse = new BlockShearEntityEvent(dispenser, ((IMixinEntity)animal).getBukkitEntity(), is);
        Bukkit.getPluginManager().callEvent(bse);
        return bse;
    }

}