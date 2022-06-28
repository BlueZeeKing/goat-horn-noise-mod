package dev.blueish.ghnm;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoatHornNoiseMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Goat Horn Noise Mod");

	@Override
	public void onInitialize() {
		LOGGER.info("The best mod, Goat Horn Noise Mod, is now active");
	}
}
