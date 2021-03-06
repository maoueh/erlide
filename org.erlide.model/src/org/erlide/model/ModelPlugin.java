package org.erlide.model;

import org.eclipse.core.runtime.Plugin;
import org.erlide.runtime.IRpcSite;
import org.erlide.runtime.IRuntimeProvider;
import org.erlide.runtime.RuntimeVersion;
import org.erlide.util.ExtensionUtils;
import org.osgi.framework.BundleContext;


public class ModelPlugin extends Plugin {

    public static final String PLUGIN_ID = "org.erlide.model";
    private static final String CORE_PLUGIN_ID = "org.erlide.core";
    public static final String NATURE_ID = CORE_PLUGIN_ID + ".erlnature";
    public static final String BUILDER_ID = CORE_PLUGIN_ID + ".erlbuilder";

    private static BundleContext context;
    static ModelPlugin plugin;

    public ModelPlugin() {
        super();
        plugin = this;
    }

    public static ModelPlugin getDefault() {
        if (plugin == null) {
            plugin = new ModelPlugin();
        }
        return plugin;
    }

    static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        super.start(bundleContext);
        ModelPlugin.context = bundleContext;
    }

    @Override
    public void stop(final BundleContext bundleContext) throws Exception {
        ModelPlugin.context = null;
        super.stop(bundleContext);
    }

    private IRuntimeProvider getRuntimeProvider() {
        return ExtensionUtils.getSingletonExtension(
                "org.erlide.runtime.backend", IRuntimeProvider.class);
    }

    public IRpcSite getIdeBackend() {
        final IRuntimeProvider provider = getRuntimeProvider();
        return provider.get();
    }

    public IRpcSite getBackend(final RuntimeVersion version) {
        final IRuntimeProvider provider = getRuntimeProvider();
        return provider.get(version);
    }

    public IRpcSite getBackend(final String name) {
        final IRuntimeProvider provider = getRuntimeProvider();
        return provider.get(name);
    }

}
