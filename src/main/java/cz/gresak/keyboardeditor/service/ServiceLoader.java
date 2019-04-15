package cz.gresak.keyboardeditor.service;

import cz.gresak.keyboardeditor.service.impl.CommandExecutorImpl;
import cz.gresak.keyboardeditor.service.impl.FontProviderImpl;
import cz.gresak.keyboardeditor.service.impl.GroupStateImpl;
import cz.gresak.keyboardeditor.service.impl.KeyboardModelLoaderImpl;
import cz.gresak.keyboardeditor.service.impl.KeysymMapperImpl;
import cz.gresak.keyboardeditor.service.impl.LayoutExporterImpl;
import cz.gresak.keyboardeditor.service.impl.xkbconfig.CurrentConfigLoaderImpl;
import cz.gresak.keyboardeditor.service.impl.xkbconfig.KeyboardModelUpdaterImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceLoader {
    private static final Set<Object> context;

    static {
        context = new HashSet<>();
        //dependency configuration
        //order matters - load dependent classes after loading dependencies
        context.add(new CommandExecutorImpl());
        context.add(CurrentConfigLoaderImpl.getInstance());
        context.add(new KeyboardModelUpdaterImpl());
        context.add(FontProviderImpl.getInstance());
        context.add(GroupStateImpl.getInstance());
        context.add(new KeyboardModelLoaderImpl());
        context.add(new KeysymMapperImpl());
        context.add(new LayoutExporterImpl());
    }

    @SuppressWarnings("unchecked")
    public static <T> T lookup(Class<T> clazz) {
        List<T> candidates = context.stream()
                .filter(o -> clazz.isAssignableFrom(o.getClass()))
                .map(o -> (T) o)
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Failed to lookup object. Following class is not registered: " + clazz);
        }
        if (candidates.size() > 1) {
            throw new IllegalStateException("Failed to lookup object. Multiple candidates found for following class: " + clazz + ". Candidates: " + candidates);
        }
        return candidates.iterator().next();
    }

    public static void register(Object o) {
        context.add(o);
    }

}
