package es.degrassi.degrassiconf;

import es.degrassi.degrassiconf.conf.DegrassiConfSpec;

public class TestConfig {

  public static final DegrassiConfSpec.Builder BUILDER = new DegrassiConfSpec.Builder();
  public static final DegrassiConfSpec SPEC;

  public static final DegrassiConfSpec.ConfigValue<Integer> testInt;
  public static final DegrassiConfSpec.ConfigValue<Short> testShort;
  public static final DegrassiConfSpec.ConfigValue<Double> testDouble;
  public static final DegrassiConfSpec.ConfigValue<Float> testFloat;
  public static final DegrassiConfSpec.ConfigValue<Long> testLong;
  public static final DegrassiConfSpec.ConfigValue<String> testString;
  public static final DegrassiConfSpec.ConfigValue<Boolean> testBoolean;
  public static final DegrassiConfSpec.ConfigValue<String> newProperty;

  static {
    BUILDER.push("short");
    testShort = BUILDER.comment("short test value").define("testShort", (short) 1);
    BUILDER.pop();
    BUILDER.push("int");
    testInt = BUILDER.comment("integer test value").define("testInt", 1);
    BUILDER.pop();
    BUILDER.push("double");
    testDouble = BUILDER.comment("double test value").define("testDouble", 1d);
    BUILDER.pop();
    BUILDER.push("float");
    testFloat = BUILDER.comment("float test value").define("testFloat", 1f);
    BUILDER.pop();
    BUILDER.push("long");
    testLong = BUILDER.comment("long test value").define("testLong", 1L);
    BUILDER.pop();
    BUILDER.push("string");
    testString = BUILDER.comment("string test value").define("testString", "String");
    BUILDER.pop();
    BUILDER.push("boolean");
    testBoolean = BUILDER.comment("boolean test value").define("testBoolean", false);
    BUILDER.pop();
    BUILDER.push("newProperty");
    newProperty = BUILDER.comment("adding multiple").comment("comments to try").define("property", "adding new property to existing config");
    BUILDER.pop();
  }

  static {
    SPEC = BUILDER.build("test");
  }
}
