package org.erlide.runtime;

import org.erlide.runtime.runtimeinfo.IRuntimeInfoCatalog;
import org.erlide.runtime.runtimeinfo.IRuntimeInfoSerializer;
import org.erlide.runtime.runtimeinfo.RuntimeInfoCatalog;
import org.erlide.runtime.runtimeinfo.RuntimeInfoCatalogData;

public class RuntimeCore {

    private static RuntimeInfoCatalog runtimeInfoCatalog;

    public static final IRuntimeInfoCatalog getRuntimeInfoCatalog(
            final IRuntimeInfoSerializer serializer) {
        if (runtimeInfoCatalog == null) {
            final RuntimeInfoCatalogData data = serializer.load();

            runtimeInfoCatalog = new RuntimeInfoCatalog();
            runtimeInfoCatalog.setRuntimes(data.runtimes,
                    data.defaultRuntimeName, data.erlideRuntimeName);
            HostnameUtils.detectHostNames(runtimeInfoCatalog.erlideRuntime);
        }
        return runtimeInfoCatalog;
    }

    public static IRuntimeInfoCatalog getRuntimeInfoCatalog() {
        if (runtimeInfoCatalog != null) {
            return runtimeInfoCatalog;
        }
        return null;
    }

}
