package org.erlide.backend.events;

import java.util.Dictionary;
import java.util.Hashtable;

import org.erlide.backend.BackendPlugin;
import org.erlide.backend.IBackend;
import org.erlide.runtime.IRpcSite;
import org.erlide.utils.IDisposable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public abstract class ErlangEventHandler implements EventHandler, IDisposable {
    private final IBackend backend;
    private final String topic;
    private ServiceRegistration registration;

    public ErlangEventHandler(final String topic, final IBackend backend) {
        this.topic = topic;
        this.backend = backend;
    }

    public void register() {
        final String fullTopic = ErlangEventPublisher.getFullTopic(topic,
                backend);
        // ErlLogger.info("Register event handler for " + topic + ": " + this);
        final BundleContext context = BackendPlugin.getDefault().getBundle()
                .getBundleContext();
        if (registration == null) {
            final Dictionary<String, String> properties = new Hashtable<String, String>();
            properties.put(EventConstants.EVENT_TOPIC, fullTopic);
            registration = context.registerService(
                    EventHandler.class.getName(), this, properties);
        }
    }

    public IRpcSite getBackend() {
        return backend.getRpcSite();
    }

    @Override
    public void dispose() {
        registration.unregister();
    }
}
