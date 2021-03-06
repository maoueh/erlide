package org.erlide.model.internal.root;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.xtext.xbase.lib.Pair;
import org.erlide.model.erlang.IErlModule;
import org.erlide.model.root.ErlModelManager;
import org.erlide.model.root.IErlElement;
import org.erlide.model.root.IErlModelChangeListener;
import org.erlide.model.root.IErlProject;
import org.erlide.model.services.search.ErlideOpen.ExternalTreeEntry;
import org.erlide.model.util.ErlideUtil;
import org.erlide.utils.IDisposable;
import org.erlide.utils.LRUCache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ErlModelCache implements IDisposable {

    private static final int CACHE_SIZE = 100;
    // TODO make a more educated guess here...
    // private static final int NAME_CACHE_SIZE = 300;
    private static ErlModelCache fgInstance = null;

    private final Map<IErlModule, List<IErlModule>> moduleIncludeCache;
    private final Map<String, IErlModule> pathToModuleCache;
    private final Map<String, Pair<IErlProject, List<ExternalTreeEntry>>> externalTreeCache;
    private final Map<String, IErlModule> editedModulesMap;
    // private final LRUCache<String, Set<IErlModule>> nameToModuleCache;
    private final ModelChangeListener modelChangeListener;
    private final Map<IErlProject, List<IErlModule>> projectModuleCache;
    private final Map<IErlProject, List<IErlModule>> projectIncludeCache;
    private final Map<IErlProject, String> projectExternalModulesStringCache;
    private final Map<IErlProject, String> projectExternalIncludesStringCache;
    private final Map<IErlProject, Collection<IPath>> projectSourceDirsCache;
    private final Map<IErlProject, Collection<IPath>> projectIncludeDirsCache;
    private final static boolean disabled = ErlideUtil.isCacheDisabled();

    public static ErlModelCache getDefault() {
        if (fgInstance == null) {
            fgInstance = disabled ? new DisabledErlModelCache()
                    : new ErlModelCache();
        }
        return fgInstance;
    }

    private class ModelChangeListener implements IErlModelChangeListener {
        // TODO should we handle changes of projects and includes too?
        // Which is hard, since the IOldErlangProjectProperties doesn't have
        // listeners
        @Override
        public void elementChanged(final IErlElement element) {
            if (element instanceof IErlModule) {
                final IErlModule module = (IErlModule) element;
                moduleIncludeCache.remove(module);
            }
        }
    }

    private static <K, V> Map<K, V> newCache() {
        return new LRUCache<K, V>(CACHE_SIZE).asSynchronized();
    }

    private ErlModelCache() {
        pathToModuleCache = newCache();
        editedModulesMap = Maps.newHashMap();
        // nameToModuleCache = newCache();
        moduleIncludeCache = newCache();
        externalTreeCache = newCache();
        projectModuleCache = newCache();
        projectIncludeCache = newCache();
        projectExternalModulesStringCache = newCache();
        projectExternalIncludesStringCache = newCache();
        projectSourceDirsCache = newCache();
        projectIncludeDirsCache = newCache();
        modelChangeListener = new ModelChangeListener();
        ErlModelManager.getErlangModel().addModelChangeListener(
                modelChangeListener);
    }

    public void putModule(final IErlModule module) {
        // final String moduleName = module.getModuleName();
        // Set<IErlModule> modules = getModulesByName(moduleName);
        // if (modules == null) {
        // modules = Sets.newHashSet();
        // }
        // modules.add(module);
        // nameToModuleCache.put(moduleName, modules);
        final String path = module.getFilePath();
        pathToModuleCache.put(path, module);
    }

    public void removeModule(final IErlModule module) {
        // final String moduleName = module.getModuleName();
        // nameToModuleCache.remove(moduleName);
        final String path = module.getFilePath();
        if (path != null) {
            pathToModuleCache.remove(path);
        }
    }

    // public Set<IErlModule> getModulesByName(final String moduleName) {
    // final Set<IErlModule> modules = nameToModuleCache.get(moduleName);
    // if (modules == null) {
    // return Sets.newHashSet();
    // }
    // return Sets.newHashSet(modules);
    // }

    // public void put(final String path, final IErlModule module) {
    // if (noModelCache) {
    // return;
    // }
    // pathToModuleCache.put(path, module);
    // }

    public void putEdited(final String path, final IErlModule module) {
        if (module == null) {
            editedModulesMap.remove(path);
        } else {
            editedModulesMap.put(path, module);
        }
    }

    public IErlModule getModuleByPath(final String path) {
        final IErlModule module = editedModulesMap.get(path);
        if (module != null) {
            return module;
        }
        return pathToModuleCache.get(path);
    }

    public void putIncludedFilesForModule(final IErlModule module,
            final Collection<IErlModule> result) {
        if (result == null) {
            moduleIncludeCache.remove(module);
        } else {
            moduleIncludeCache.put(module, Lists.newArrayList(result));
        }
    }

    public List<IErlModule> getIncludedFilesForModule(final IErlModule module) {
        final List<IErlModule> modules = moduleIncludeCache.get(module);
        if (modules == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(modules);
    }

    @Override
    public void dispose() {
        ErlModelManager.getErlangModel().removeModelChangeListener(
                modelChangeListener);
    }

    public void putExternalTree(final String externalPath,
            final IErlProject project,
            final List<ExternalTreeEntry> externalTree) {
        if (externalTree == null) {
            externalTreeCache.remove(externalPath);
        } else {
            externalTreeCache.put(externalPath,
                    new Pair<IErlProject, List<ExternalTreeEntry>>(project,
                            Lists.newArrayList(externalTree)));
        }
    }

    public List<ExternalTreeEntry> getExternalTree(final String externalPath) {
        final Pair<IErlProject, List<ExternalTreeEntry>> tuple = externalTreeCache
                .get(externalPath);
        if (tuple == null) {
            return null;
        }
        final List<ExternalTreeEntry> entries = tuple.getValue();
        if (entries == null) {
            return null;
        }
        return Lists.newArrayList(entries);
    }

    public List<IErlModule> getModulesForProject(final IErlProject project) {
        final List<IErlModule> modules = projectModuleCache.get(project);
        if (modules == null) {
            return null;
        }
        return Lists.newArrayList(modules);
    }

    public List<IErlModule> getIncludesForProject(final IErlProject project) {
        final List<IErlModule> includes = projectIncludeCache.get(project);
        if (includes == null) {
            return null;
        }
        return Lists.newArrayList(includes);
    }

    public void putModulesForProject(final IErlProject project,
            final List<IErlModule> modules) {
        final List<String> moduleNames = Lists.newArrayList();
        for (final IErlModule module : modules) {
            moduleNames.add(module.getName());
        }
        // ErlLogger.debug("setModulesForProject %s %s", project.getName(),
        // moduleNames);
        projectModuleCache.put(project, Lists.newArrayList(modules));
    }

    public void putIncludesForProject(final IErlProject project,
            final List<IErlModule> includes) {
        final List<String> moduleNames = Lists.newArrayList();
        for (final IErlModule module : includes) {
            moduleNames.add(module.getName());
        }
        // ErlLogger.debug("setIncludesForProject %s %s", project.getName(),
        // moduleNames);
        projectIncludeCache.put(project, Lists.newArrayList(includes));
    }

    public void removeProject(final IErlProject project) {
        // ErlLogger.debug("removeForProject %s", project.getName());
        final List<IErlModule> includes = projectIncludeCache.get(project);
        if (includes != null) {
            for (final IErlModule module : includes) {
                moduleIncludeCache.put(module, null);
            }
        }
        final List<IErlModule> modules = projectModuleCache.get(project);
        if (modules != null) {
            for (final IErlModule module : modules) {
                moduleIncludeCache.put(module, null);
            }
        }
        projectIncludeCache.remove(project);
        projectModuleCache.remove(project);
        final Set<String> keysToRemove = Sets.newHashSet();
        final Set<Entry<String, Pair<IErlProject, List<ExternalTreeEntry>>>> entrySet = externalTreeCache
                .entrySet();
        for (final Entry<String, Pair<IErlProject, List<ExternalTreeEntry>>> entry : entrySet) {
            if (entry.getValue().getKey() == project) {
                keysToRemove.add(entry.getKey());
            }
        }
        for (final String keyToRemove : keysToRemove) {
            externalTreeCache.remove(keyToRemove);
        }
        projectExternalModulesStringCache.remove(project);
        projectExternalIncludesStringCache.remove(project);
        projectIncludeDirsCache.remove(project);
        projectSourceDirsCache.remove(project);
    }

    public void pathVarsChanged() {
        // FIXME we need to clear some stuff here...
    }

    public String getExternalModulesString(final IErlProject project) {
        return projectExternalModulesStringCache.get(project);
    }

    public void putExternalModulesString(final IErlProject project,
            final String externalModulesString) {
        if (externalModulesString == null) {
            projectExternalModulesStringCache.remove(project);
        } else {
            projectExternalModulesStringCache.put(project,
                    externalModulesString);
        }
    }

    public String getExternalIncludesString(final IErlProject project) {
        return projectExternalIncludesStringCache.get(project);
    }

    public void putExternalIncludesString(final IErlProject project,
            final String externalModulesString) {
        if (externalModulesString == null) {
            projectExternalIncludesStringCache.remove(project);
        } else {
            projectExternalIncludesStringCache.put(project,
                    externalModulesString);
        }
    }

    public void putSourceDirs(final IErlProject project,
            final Collection<IPath> dirs) {
        if (dirs == null) {
            projectSourceDirsCache.remove(project);
        } else {
            projectSourceDirsCache.put(project, dirs);
        }
    }

    public Collection<IPath> getSourceDirs(final IErlProject project) {
        return projectSourceDirsCache.get(project);
    }

    public void putIncludeDirs(final IErlProject project,
            final Collection<IPath> dirs) {
        if (dirs == null) {
            projectIncludeDirsCache.remove(project);
        } else {
            projectIncludeDirsCache.put(project, dirs);
        }
    }

    public Collection<IPath> getIncludeDirs(final IErlProject project) {
        return projectIncludeDirsCache.get(project);
    }

    public void newProjectCreated() {
        pathToModuleCache.clear();
        // nameToModuleCache.clear();
    }

    public void putModules(final Collection<IErlModule> modules) {
        for (final IErlModule module : modules) {
            putModule(module);
        }
    }

    public void clearModelCache() {
        moduleIncludeCache.clear();
        pathToModuleCache.clear();
        externalTreeCache.clear();
        // editedModulesMap.clear();
        // nameToModuleCache.clear();
        projectModuleCache.clear();
        projectIncludeCache.clear();
        projectExternalModulesStringCache.clear();
        projectExternalIncludesStringCache.clear();
        projectSourceDirsCache.clear();
        projectIncludeDirsCache.clear();
    }

    private static class DisabledErlModelCache extends ErlModelCache {
        @Override
        public void putEdited(final String path, final IErlModule module) {
        }

        @Override
        public void putExternalIncludesString(final IErlProject project,
                final String externalModulesString) {
        }

        @Override
        public void putExternalModulesString(final IErlProject project,
                final String externalModulesString) {
        }

        @Override
        public void putExternalTree(final String externalPath,
                final IErlProject project,
                final List<ExternalTreeEntry> externalTree) {
        }

        @Override
        public void putIncludedFilesForModule(final IErlModule module,
                final Collection<IErlModule> result) {
        }

        @Override
        public void putIncludeDirs(final IErlProject project,
                final Collection<IPath> dirs) {
        }

        @Override
        public void putIncludesForProject(final IErlProject project,
                final List<IErlModule> includes) {
        }

        @Override
        public void putModule(final IErlModule module) {
        }

        @Override
        public void putModules(final Collection<IErlModule> modules) {
        }

        @Override
        public void putModulesForProject(final IErlProject project,
                final List<IErlModule> modules) {
        }

        @Override
        public void putSourceDirs(final IErlProject project,
                final Collection<IPath> dirs) {
        }
    }
}
