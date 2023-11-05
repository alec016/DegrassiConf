package es.degrassi.degrassiconf.conf;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import es.degrassi.degrassiconf.util.DegrassiLogger;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class DegrassiConfSpec {
  private final File configFile;
  private JsonObject config = new JsonObject();
  private JsonObject tempConfig = new JsonObject();
  private Map<String, JsonElement> map;
  private final Builder builder;

  private DegrassiConfSpec(File file, @NotNull Map<String, JsonElement> config, Builder builder) {
    this.configFile = file;
    this.map = config;
    this.builder = builder;
    config.forEach(this.config::add);
  }

  private DegrassiConfSpec(String filename, Map<String, JsonElement> config, Builder builder) {
    this(new File(Minecraft.getInstance().gameDirectory.getPath() + "/config/" + filename + ".json"), config, builder);
  }

  public void init() {
    try(JsonReader reader = new JsonReader(new FileReader(configFile))) {
      DegrassiLogger.INSTANCE.info("Reading configFile {}", configFile.getName());
      tempConfig = config;
      config = new GsonBuilder().create().fromJson(reader, JsonObject.class);
      if (map == null) map = Maps.newHashMap();
      config.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
      builder.map = map;

      DegrassiLogger.INSTANCE.info("Config {} currently has {} config paths", configFile.getName(), config.entrySet().size());
    } catch (IOException e) {
      DegrassiLogger.INSTANCE.error("Couldn't save config in file: {}",configFile.getName());
    }
  }

  public void addPropertiesThatNotExistsInConfig () {
    AtomicInteger countAdded = new AtomicInteger(0);
    Set<Map.Entry<String, JsonElement>> toAdd = Sets.newHashSet();
    tempConfig.entrySet().forEach(entry -> {
      AtomicBoolean has = new AtomicBoolean(false);
      config.entrySet().forEach(e -> {
        if (e.getKey().equals(entry.getKey())) has.set(true);
      });
      if (!has.get()) {
        toAdd.add(entry);
        countAdded.getAndIncrement();
      }
    });

    if (!toAdd.isEmpty()) {
      DegrassiLogger.INSTANCE.info("Adding new properties to {}...", configFile.getName());
      toAdd.forEach(entry -> config.add(entry.getKey(), entry.getValue()));
      write();
      DegrassiLogger.INSTANCE.info("Added {} properties to {}", countAdded.get(), configFile.getName());
      if (countAdded.get() > 0) DegrassiLogger.INSTANCE.info("Config file {} now has {} config paths", configFile.getName(), config.entrySet().size());
    }
  }

  public void write() {
    try(JsonWriter jw = new JsonWriter(new FileWriter(configFile))) {
      DegrassiLogger.INSTANCE.info("Saving config {}...", configFile.getName());
      jw.setLenient(true);
      jw.setIndent("\t");
      Streams.write(config, jw);
      DegrassiLogger.INSTANCE.info("Config {} saved successfully", configFile.getName());
    } catch (IOException e) {
      DegrassiLogger.INSTANCE.error("Couldn't save config in file: {}", configFile.getName());
    }
  }

  public void save() {
    if (configFile.exists()) {
      init();
      addPropertiesThatNotExistsInConfig();
      return;
    }
    write();
  }

  public static class Builder {
    private Map<String, JsonElement> map = Maps.newHashMap();
    private final Map<String, ConfigValue<?>> configMap = Maps.newHashMap();
    private JsonObject config = new JsonObject();
    private String currentPath = "";
    private String prevPath = "";
    private String acc = "";
    private final List<String> comments = new LinkedList<>();
    public DegrassiConfSpec build(String fileName) {
      return new DegrassiConfSpec(fileName, map, this);
    }

    public void push(String path) {
      acc = currentPath.isEmpty() ? path : currentPath + "." + path;
      prevPath = currentPath;
      config = new JsonObject();
      currentPath = path;
    }

    public void pop() {
      JsonArray array = new JsonArray();
      comments.forEach(array::add);
      config.add("comments", array);
      map.put(currentPath, config);
      comments.clear();
      currentPath = prevPath;
      acc = acc.split("\\.").length >= 2 ? Arrays.stream(acc.split("\\.")).toList().subList(0, acc.split("\\.").length - 1).stream().reduce((value, acc) -> {
        if (acc.isEmpty()) acc = value;
        else acc += "." + value;
        return acc;
      }).orElse(currentPath) : currentPath;
    }

    public Builder comment(String comment) {
      if (comment == null) comment = "";
      comments.add(comment);
      return this;
    }

    public ConfigValue<Boolean> define(String path, boolean value) {
      config.addProperty(path, value);
      var x = new ConfigValue<>(path, value, this);
      configMap.put(acc + "." + path, x);
      return x;
    }

    public ConfigValue<String> define(String path, String value) {
      config.addProperty(path, value);
      var x = new ConfigValue<>(path, value, this);
      configMap.put(acc + "." + path, x);
      return x;
    }

    public ConfigValue<Integer> define(String path, int value) {
      config.addProperty(path, value);
      var x = new ConfigValue<>(path, value, this);
      configMap.put(acc + "." + path, x);
      return x;
    }

    public ConfigValue<Double> define(String path, double value) {
      config.addProperty(path, value);
      var x = new ConfigValue<>(path, value, this);
      configMap.put(acc + "." + path, x);
      return x;
    }

    public ConfigValue<Long> define(String path, long value) {
      config.addProperty(path, value);
      var x = new ConfigValue<>(path, value, this);
      configMap.put(acc + "." + path, x);
      return x;
    }

    public ConfigValue<Float> define(String path, float value) {
      config.addProperty(path, value);
      var x = new ConfigValue<>(path, value, this);
      configMap.put(acc + "." + path, x);
      return x;
    }

    public ConfigValue<Short> define(String path, short value) {
      config.addProperty(path, value);
      var x = new ConfigValue<>(path, value, this);
      configMap.put(acc + "." + path, x);
      return x;
    }
  }

  @SuppressWarnings("unchecked")
  public static class ConfigValue<T> {
    protected Builder delegate;
    protected T value;
    protected String path;

    private JsonObject current;

    public ConfigValue(String path, T value, Builder builder) {
      this.path = path;
      this.value = value;
      this.delegate = builder;
    }

    public String getPath() {
      return this.path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public T get() {
      DegrassiLogger.INSTANCE.info("path: {}", path);
//      return (T) delegate.config.get(path);
       T value = get(path);
       current = null;
       return value;
    }

    public void set(T value) {
      if (!path.contains(".")) {
        if (value instanceof Boolean v) {
          if (delegate.map.get(path).isJsonObject()) {
            delegate.map.get(path).getAsJsonObject().addProperty(path, v);
          } else if (delegate.map.get(path).isJsonArray()) {
            delegate.map.get(path).getAsJsonArray().add(v);
          } else {
            delegate.map.put(path, new JsonPrimitive(v));
          }
        }
        if (value instanceof String v) {
          if (delegate.map.get(path).isJsonObject()) {
            delegate.map.get(path).getAsJsonObject().addProperty(path, v);
          } else if (delegate.map.get(path).isJsonArray()) {
            delegate.map.get(path).getAsJsonArray().add(v);
          } else {
            delegate.map.put(path, new JsonPrimitive(v));
          }
        }
        if (value instanceof Number v) {
          if (delegate.map.get(path).isJsonObject()) {
            delegate.map.get(path).getAsJsonObject().addProperty(path, v);
          } else if (delegate.map.get(path).isJsonArray()) {
            delegate.map.get(path).getAsJsonArray().add(v);
          } else {
            delegate.map.put(path, new JsonPrimitive(v));
          }
        }
        if (value instanceof Character v) {
          if (delegate.map.get(path).isJsonObject()) {
            delegate.map.get(path).getAsJsonObject().addProperty(path, v);
          } else if (delegate.map.get(path).isJsonArray()) {
            delegate.map.get(path).getAsJsonArray().add(v);
          } else {
            delegate.map.put(path, new JsonPrimitive(v));
          }
        }
      }
      this.value = value;
    }

    private @Nullable T get(@NotNull String path) {
      AtomicReference<T> value = new AtomicReference<>(null);
      List<String> paths = Arrays.stream(path.split("\\.")).toList();
      if (current == null) {
        path = paths.get(0);
        if (path.contains(".")) current = delegate.map.get(path).getAsJsonObject();
        else {
          if (delegate.map.get(path).isJsonPrimitive()) {
            JsonPrimitive p = delegate.map.get(path).getAsJsonPrimitive();
            if (p.isBoolean()) return (T) ((Boolean) p.getAsBoolean());
            else if (p.isNumber()) return (T) p.getAsNumber();
            else if (p.isJsonNull()) return null;
            else return (T) delegate.map.get(path).getAsString();
          } else if (delegate.map.get(path).isJsonObject())
            current = delegate.map.get(path).getAsJsonObject();
          else if (delegate.map.get(path).isJsonArray())
            return (T) delegate.map.get(path).getAsJsonArray();
          else if (delegate.map.get(path).isJsonNull())
            return null;
        }
      }
      paths = paths.subList(1, paths.size());
      paths.forEach(p -> {
        if (value.get() != null) return;
        if (p.contains(".")) {
          current = current.get(p).getAsJsonObject();
          value.set(get(p));
          return;
        }
        value.set((T) current.get(p).getAsString());
      });
      return value.get();
    }
  }
}
