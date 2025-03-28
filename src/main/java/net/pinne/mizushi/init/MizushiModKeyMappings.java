package net.pinne.mizushi.init;

import org.lwjgl.glfw.GLFW;

import net.pinne.mizushi.network.KaiMessage;
import net.pinne.mizushi.network.FugaMessage;
import net.pinne.mizushi.MizushiMod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class MizushiModKeyMappings {
	public static final KeyMapping KAI = new KeyMapping("key.mizushi.kai", GLFW.GLFW_KEY_R, "key.categories.misc") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				MizushiMod.PACKET_HANDLER.sendToServer(new KaiMessage(0, 0));
				KaiMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping FUGA = new KeyMapping("key.mizushi.fuga", GLFW.GLFW_KEY_Y, "key.categories.misc") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				MizushiMod.PACKET_HANDLER.sendToServer(new FugaMessage(0, 0));
				FugaMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(KAI);
		event.register(FUGA);
	}

	@Mod.EventBusSubscriber({Dist.CLIENT})
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (Minecraft.getInstance().screen == null) {
				KAI.consumeClick();
				FUGA.consumeClick();
			}
		}
	}
}
