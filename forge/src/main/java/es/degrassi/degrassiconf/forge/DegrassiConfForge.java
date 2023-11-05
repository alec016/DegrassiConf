package es.degrassi.degrassiconf.forge;

import dev.architectury.platform.forge.EventBuses;
import es.degrassi.degrassiconf.DegrassiConf;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DegrassiConf.MODID)
public class DegrassiConfForge {
  public DegrassiConfForge() {
    // Submit our event bus to let architectury register our content on the right time
    EventBuses.registerModEventBus(DegrassiConf.MODID, FMLJavaModLoadingContext.get().getModEventBus());
    DegrassiConf.init();
  }
}