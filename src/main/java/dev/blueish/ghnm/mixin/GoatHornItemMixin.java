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
import net.minecraft.util.math.Vec3d;
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
            if (receiver.world.getRegistryKey() == world.getRegistryKey()) {
                double distance = receiver.getPos().distanceTo(player.getPos());
                Vec3d difference = player.getPos().subtract(receiver.getPos());

                if (distance > 220) {
                    double ratio = 220 / distance;
                    difference = difference.multiply(ratio);
                }
                Vec3d finished = receiver.getPos().add(difference);

                GoatHornNoiseMod.LOGGER.info(String.format("Player: %s, X: %f, Y: %f, Z: %f, D: %f", receiver.getName().getString(), finished.getX(), finished.getY(), finished.getZ(), distance));

                receiver.networkHandler.sendPacket(new PlaySoundS2CPacket(soundEvent, SoundCategory.RECORDS, finished.getX(), finished.getY(), finished.getZ(), volume, 1, threadSafeRandom.nextLong()));
            } else {
                receiver.networkHandler.sendPacket(new PlaySoundS2CPacket(soundEvent, SoundCategory.RECORDS, receiver.getX(), receiver.getY(), receiver.getZ(), 0.5F, 1, threadSafeRandom.nextLong()));
            }
        }

        world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, player.getPos(), GameEvent.Emitter.of(player));
    }
}