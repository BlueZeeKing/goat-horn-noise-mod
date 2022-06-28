package dev.blueish.ghnm.mixin;

import dev.blueish.ghnm.GoatHornNoiseMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Instrument;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.Main;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GoatHornItem.class)
public class GoatHornItemMixin {
    private static final Random threadSafeRandom = Random.createThreadSafe();

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/GoatHornItem;playSound(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/Instrument;)V"))
    private void playSound(World world, PlayerEntity player, Instrument instrument) {
        SoundEvent soundEvent = instrument.soundEvent();
        float volume = instrument.range() / 16.0F;

        for (ServerPlayerEntity receiver : world.getServer().getPlayerManager().getPlayerList()) {
            float distance = (float) Math.sqrt(Math.pow(receiver.getX() - player.getX(), 2) + Math.pow(receiver.getY() - player.getY(), 2) + Math.pow(receiver.getZ() - player.getZ(), 2));
            receiver.networkHandler.sendPacket(new PlaySoundS2CPacket(soundEvent, SoundCategory.RECORDS, receiver.getX(), receiver.getY(), receiver.getZ(), Math.max(0.5F, volume - (distance/33)), 1, threadSafeRandom.nextLong()));
        }

        world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, player.getPos(), GameEvent.Emitter.of(player));
    }
}
