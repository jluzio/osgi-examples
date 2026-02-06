package com.example.osgi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.felix.framework.Felix;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

class OsgiGradleApplicationIT {

  private Felix felix;
  private Path storageDir;
  private Bundle bundle;

  @BeforeEach
  void setUp() throws Exception {
    storageDir = Path.of("build/felix-storage");
    Files.createDirectories(storageDir);

    Map<String, Object> config = new HashMap<>();
    config.put("org.osgi.framework.storage", storageDir.toString());
    config.put("org.osgi.framework.storage.clean", "onFirstInit");

    felix = new Felix(config);
    felix.init();
    felix.start();
  }

  @AfterEach
  void tearDown() throws BundleException, InterruptedException {
    // Stop and uninstall bundle if installed
    if (bundle != null) {
      try {
        bundle.stop();
      } catch (Exception ignored) {
        // best-effort cleanup: ignore exceptions when stopping the bundle
      }
      try {
        bundle.uninstall();
      } catch (Exception ignored) {
        // best-effort cleanup: ignore exceptions when uninstalling the bundle
      }
      bundle = null;
    }

    // Stop the framework
    if (felix != null) {
      felix.stop();
      felix.waitForStop(TimeUnit.SECONDS.toMillis(10));
      felix = null;
    }
  }

  @Test
  void testBundle() throws Exception {
    var jarOpt = Files.list(Path.of("build/libs"))
        .filter(p -> {
          String n = p.getFileName().toString();
          return n.startsWith("osgi-gradle-") && n.endsWith(".jar");
        }).findFirst();
    if (jarOpt.isEmpty()) {
      Assertions.fail("Could not find osgi-maven JAR in target/. Build the project first (mvn package).");
      return;
    }
    Path jarPath = jarOpt.get().toAbsolutePath();

    // Install the bundle from the built JAR using the framework started in @BeforeEach
    String bundleLocation = jarPath.toUri().toString();
    bundle = felix.getBundleContext().installBundle(bundleLocation);

    // Start the bundle
    bundle.start();

    // Wait for ACTIVE state using Awaitility (timeout after 30 seconds)
    Awaitility.await()
        .atMost(Duration.ofSeconds(30))
        .pollInterval(Duration.ofMillis(200))
        .untilAsserted(() -> Assertions.assertEquals(Bundle.ACTIVE, bundle.getState(), "Bundle did not reach ACTIVE state"));
  }

}
