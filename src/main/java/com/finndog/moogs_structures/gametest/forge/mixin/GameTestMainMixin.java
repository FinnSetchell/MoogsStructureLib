//? if >=1.21.5 <1.21.10 {
/*package com.finndog.moogs_structures.gametest.forge.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTestMainUtil;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.gametest.GameTestMain;
import net.minecraftforge.server.loading.ServerModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Forge 55 to 58 (Minecraft 1.21.5 to 1.21.8) start a plain dedicated server from runGameTestServer: their
// GameTestMain hands over to the server's Main, whose GameTest server branch is commented out. Forge 59 runs
// Minecraft's own GameTest entry point instead and loads mods in it, so this does the same.
@Mixin(value = GameTestMain.class, remap = false)
public class GameTestMainMixin {

	@Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/Main;main([Ljava/lang/String;)V"))
	private static void moogs_structures$runGameTestServer(String[] args) throws Exception {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		ServerModLoader.load();
		GameTestMainUtil.runGameTestServer(args, universe -> {});
	}
}
*///?}
