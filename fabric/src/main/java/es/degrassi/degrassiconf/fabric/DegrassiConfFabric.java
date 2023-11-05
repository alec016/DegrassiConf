package es.degrassi.degrassiconf.fabric;

import es.degrassi.degrassiconf.DegrassiConf;
import net.fabricmc.api.ModInitializer;

public class DegrassiConfFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    DegrassiConf.init();
  }
}