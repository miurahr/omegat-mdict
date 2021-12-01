package tokyo.northside.omegat.mdict;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.dictionaries.IDictionary;
import org.omegat.core.dictionaries.IDictionaryFactory;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.util.Language;

import java.io.File;

public class MDict implements IDictionaryFactory {

    /**
     * Plugin loader.
     */
    public static void loadPlugins() {
        CoreEvents.registerApplicationEventListener(new MDictApplicationEventListener());
    }

    /**
     * Plugin unloader.
     */
    public static void unloadPlugins() {
    }

    /**
     * Determine whether or not the supplied file is supported by this factory.
     * This is intended to be a lightweight check, e.g. looking for a file
     * extension.
     *
     * @param file The file to check
     * @return Whether or not the file is supported
     */
    @Override
    public boolean isSupportedFile(File file) {
        return file.getPath().endsWith(".MDX") || file.getPath().endsWith(".mdx");
    }

    /**
     * Load the given file and return an {@link IDictionary} that wraps it.
     * Deprecated; use {@link #loadDict(File, Language)} instead.
     *
     * @param file The file to load
     * @return An IDictionary file that can read articles from the file
     * @throws Exception If the file could not be loaded for reasons that were not
     *                   determined by {@link #isSupportedFile(File)}
     */
    @Override
    public IDictionary loadDict(File file) throws Exception {
        return this.loadDict(file, null);
    }

    /**
     * Load the given file and return an {@link IDictionary} that wraps it.
     *
     * @param file The file to load
     * @param lang language
     * @return An IDictionary file that can read articles from the file
     * @throws Exception If the file could not be loaded for reasons that were not
     *                   determined by {@link #isSupportedFile(File)}
     */
    @Override
    public IDictionary loadDict(final File file, final Language lang) throws Exception {
        return new MDictImpl(file);
    }

    /**
     * registration of dictionary factory.
     */
    static class MDictApplicationEventListener implements IApplicationEventListener {
        @Override
        public void onApplicationStartup() {
            Core.getDictionaries().addDictionaryFactory(new MDict());
        }

        @Override
        public void onApplicationShutdown() {
        }
    }
}
