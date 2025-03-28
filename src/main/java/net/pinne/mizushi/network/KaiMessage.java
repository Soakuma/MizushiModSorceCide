package net.pinne.mizushi.network;

import net.pinne.mizushi.procedures.KaiOnKeyPressedProcedure;
import net.pinne.mizushi.MizushiMod;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class KaiMessage {
    int type, pressedms;

    public KaiMessage(int type, int pressedms) {
        this.type = type;
        this.pressedms = pressedms;
    }

    public KaiMessage(FriendlyByteBuf buffer) {
        this.type = buffer.readInt();
        this.pressedms = buffer.readInt();
    }

    public static void buffer(KaiMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.type);
        buffer.writeInt(message.pressedms);
    }

    public static void handler(KaiMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            pressAction(context.getSender(), message.type, message.pressedms);
        });
        context.setPacketHandled(true);
    }

    public static void pressAction(Player entity, int type, int pressedms) {
        Level world = entity.level();
        if (!world.hasChunkAt(entity.blockPosition()))
            return;
        if (type == 0) {
            KaiOnKeyPressedProcedure.execute(world, entity);
        }
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        MizushiMod.addNetworkMessage(KaiMessage.class, KaiMessage::buffer, KaiMessage::new, KaiMessage::handler);
    }
}