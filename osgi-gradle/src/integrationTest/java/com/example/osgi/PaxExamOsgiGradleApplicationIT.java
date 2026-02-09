package com.example.osgi;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.IOException;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

@RunWith(PaxExam.class)
public class PaxExamOsgiGradleApplicationIT {

  @Inject
  private BundleContext bundleContext;

  @Configuration
  public Option[] config() throws IOException {
    var bundlePath = BundleTestHelper.bundlePath("build/libs", "osgi-");
    var bundleUri = bundlePath.toUri().toString();
    return options(
//        org.ops4j.pax.exam.CoreOptions.felix(), // Tells Pax Exam to use the Felix container
        junitBundles(),
        // Loads your freshly built bundle into the test container
        bundle("reference:" + bundleUri)
    );
  }

  @Test
  public void testBundleIsActive() throws BundleException {
    assertNotNull("BundleContext should be injected", bundleContext);

    // Verify your bundle is actually installed and active
    var bundle = java.util.Arrays.stream(bundleContext.getBundles())
        .filter(b -> b.getSymbolicName().equals("com.example.osgi-gradle"))
        .findFirst().orElse(null);
    assertNotNull("Our bundle should be present in Felix", bundle);

    bundle.start();

    var svcRef = bundleContext.getServiceReference(Greeter.class);
    assertNotNull(svcRef);

    var svc = bundleContext.getService(svcRef);
    assertNotNull(svc);

    var output = svc.hello("test");
    assertNotNull(output);

    bundle.stop();
  }
}
