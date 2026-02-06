package com.example.osgi;

import java.util.Hashtable;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@Slf4j
public class OsgiApplicationActivator implements BundleActivator {

  private ServiceRegistration<Greeter> registration;

  @Override
  public void start(BundleContext context) throws Exception {
    IO.println("Registering service");
    log.info("Registering service");
    registration = context.registerService(
        Greeter.class,
        new DefaultGreeter(),
        new Hashtable<>());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    IO.println("Unregistering service");
    log.info("Unregistering service");
    registration.unregister();
  }
}
