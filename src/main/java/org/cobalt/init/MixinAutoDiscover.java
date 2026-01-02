package org.cobalt.init;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinAutoDiscover implements IMixinConfigPlugin {

  private final List<String> mixins = new ArrayList<>();

  @Override
  public void onLoad(String mixinPackage) {
    try {
      URL location = MixinAutoDiscover.class.getProtectionDomain().getCodeSource().getLocation();
      Path path = Paths.get(location.toURI());

      if (Files.isDirectory(path)) {
        scanDirectory(path, mixinPackage);
      } else {
        scanJar(path, mixinPackage);
      }

      for (int i = 0; i < mixins.size(); i++) {
        String cls = mixins.get(i);

        if (cls.startsWith(mixinPackage + ".")) {
          cls = cls.substring(mixinPackage.length() + 1);
          mixins.set(i, cls);
        }
      }
    } catch (IOException | URISyntaxException e) {
      System.err.println("Failed to auto-discover mixins: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void scanDirectory(Path root, String mixinPackage) throws IOException {
    String mixinPath = mixinPackage.replace(".", "/");
    Path mixinDir = root.resolve(mixinPath);

    if (!Files.exists(mixinDir)) {
      System.out.println("Mixin directory does not exist: " + mixinDir);
      return;
    }

    try (Stream<Path> paths = Files.walk(mixinDir)) {
      paths.filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".class"))
        .filter(p -> !p.toString().endsWith("package-info.class"))
        .forEach(p -> {
          String relativePath = root.relativize(p).toString();
          String className = relativePath
            .replace("/", ".")
            .replace("\\", ".")
            .replace(".class", "");

          if (!className.isEmpty()) {
              mixins.add(className);
          }
        });
    }
  }

  private void scanJar(Path jarPath, String mixinPackage) throws IOException {
    String mixinPath = mixinPackage.replace(".", "/") + "/";

    try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(jarPath))) {
      ZipEntry entry;

      while ((entry = zip.getNextEntry()) != null) {
        String entryName = entry.getName();

        if (
          entryName.startsWith(mixinPath) &&
          entryName.endsWith(".class") &&
          !entryName.endsWith("package-info.class")
        ) {
          String className = entryName
            .replace("/", ".")
            .replace(".class", "");

          if (!className.isEmpty()) {
            mixins.add(className);
          }
        }

        zip.closeEntry();
      }
    }
  }

  @Override
  public List<String> getMixins() {
    return mixins;
  }

  @Override
  public String getRefMapperConfig() {
    return null;
  }

  @Override
  public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
    return true;
  }

  @Override
  public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

  @Override
  public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

  @Override
  public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

}
