package es.degrassi.degrassiconf;

import es.degrassi.degrassiconf.conf.DegrassiConfSpec;
import es.degrassi.degrassiconf.util.DegrassiLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DegrassiConf {
  private static final Map<String, DegrassiConfSpec> CONFIGS = new HashMap<>();
  public static final String MODID = "degrassiconf";
  public static Logger LOGGER = LogManager.getLogger("DegrassiConf");

  public static void init() {
    DegrassiLogger.init("degrassi", "degrassi_conf", "DegrassiConf");
    addConf(TestConfig.SPEC, "test");
    register();
  }

  private DegrassiConf() {}

  public static void register() {
    CONFIGS.forEach(DegrassiConf::registerConfig);
  }

  public static void addConf(@NotNull DegrassiConfSpec spec, @NotNull String filename) {
    CONFIGS.put(filename, spec);
  }

  private static void registerConfig(@NotNull String filename, @NotNull DegrassiConfSpec spec) {
    spec.save();
  }
}
