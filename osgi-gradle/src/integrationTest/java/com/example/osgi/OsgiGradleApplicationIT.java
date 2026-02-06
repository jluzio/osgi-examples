package com.example.osgi;

import static org.junit.Assert.assertNotNull;

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
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

class OsgiGradleApplicationIT {

  private Framework felix;
  private Path storageDir;
  private Bundle bundle;

  @BeforeEach
  void setUp() throws Exception {
    storageDir = Path.of("build/felix-storage");
    Files.createDirectories(storageDir);

    Map<String, Object> config = new HashMap<>();
    config.put(Constants.FRAMEWORK_STORAGE, storageDir.toString());
    config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

    // Tell Felix to use the host's version of the API package
    config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "com.example.osgi");

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

    var bundleBundleContext = bundle.getBundleContext();
    var serviceReference = bundleBundleContext.getServiceReference(Greeter.class);
    assertNotNull(serviceReference);
//    var service = bundleBundleContext.getService(serviceReference);
//    var output = service.hello("IT test");
//    assertThat(output)
//        .isNotEmpty();
  }

}
