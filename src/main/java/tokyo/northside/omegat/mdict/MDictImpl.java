package tokyo.northside.omegat.mdict;

import io.github.eb4j.mdict.MDException;
import io.github.eb4j.mdict.MDictDictionary;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.omegat.core.dictionaries.DictionaryEntry;
import org.omegat.core.dictionaries.IDictionary;
import org.omegat.util.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class MDictImpl implements IDictionary {

    private final MDictDictionary mdictionary;
    private final MDictDictionary mData;

    public MDictImpl(final File mdxFile) throws MDException, IOException {
        String mdxPath = mdxFile.getPath();
        mdictionary = MDictDictionary.loadDicitonary(mdxPath);
        MDictDictionary temp = null;
        try {
            if (mdictionary.getMdxVersion().equals("2.0")) {
                temp = MDictDictionary.loadDictionaryData(mdxPath);
            }
        } catch (MDException | IOException ignored) {
        }
        mData = temp;
    }

    /**
     * Read article's text. Matching is predictive, so e.g. supplying "term"
     * will return articles for "term", "terminology", "termite", etc. The
     * default implementation simply calls {@link #readArticles(String)} for
     * backwards compatibility.
     *
     * @param word The word to look up in the dictionary
     * @return List of entries. May be empty, but cannot be null.
     */
    @Override
    public List<DictionaryEntry> readArticlesPredictive(String word) throws Exception {
        List<DictionaryEntry> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : mdictionary.getEntriesPredictive(word)) {
            addEntry(result, entry);
        }
        return result;
    }

    /**
     * Read article's text.
     *
     * @param word The word to look up in the dictionary
     * @return List of entries. May be empty, but cannot be null.
     */
    @Override
    public List<DictionaryEntry> readArticles(final String word) throws Exception {
        List<DictionaryEntry> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : mdictionary.getEntries(word)) {
            addEntry(result, entry);
        }
        return result;
    }

    private void addEntry(final List<DictionaryEntry> result, final Map.Entry<String, Object> entry) throws MDException {
        if (entry.getValue() instanceof Long) {
            result.add(new DictionaryEntry(entry.getKey(),
                    retrieveDataAndUpdateLink(cleaHtmlArticle(mdictionary.getText((Long) entry.getValue())))));
        } else {
            Long[] values = (Long[]) entry.getValue();
            for (Long value : values) {
                result.add(new DictionaryEntry(entry.getKey(),
                        retrieveDataAndUpdateLink(cleaHtmlArticle(mdictionary.getText(value)))));
            }
        }
    }

    private String cleaHtmlArticle(final String mdictHtmlText) {
        Safelist whitelist = new Safelist();
        whitelist.addTags("b", "br", "span", "ul", "ol", "li");
        whitelist.addAttributes("font", "color", "face");
        whitelist.addAttributes("img", "src");
        return Jsoup.clean(mdictHtmlText, whitelist);
    }

    private String retrieveDataAndUpdateLink(final String mdictHtmlText) {
        Document document = Jsoup.parse(mdictHtmlText);
        // Support embeded image
        try {
            Elements elements = document.select("img[src]");
            for (Element element: elements) {
                String linkUrl = element.attr("src");
                if (linkUrl.startsWith("file://pic/")) {
                    String targetKey = linkUrl.substring(6);
                    byte[] rawData = getRawData(targetKey);
                    element.attr("src",
                            "data:image/png;base64," + convertImage2Base64("png", rawData));
                }
            }
        } catch (MDException | IOException e) {
            Log.log(e);
        }
        return document.body().html();
    }

    private byte[] getRawData(final String targetKey) throws MDException {
        byte[] result = null;
        for (Map.Entry<String, Object> entry: mData.getEntries(targetKey)) {
            if (entry.getKey().equals(targetKey)) {
                Object value = entry.getValue();
                if (value instanceof Long) {
                    result = mData.getData((Long) value);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * convert image data to base64.
     * @param format image format.
     * @param data image data
     * @return base64 string
     * @throws IOException when conversion failed
     */
    private static String convertImage2Base64(final String format, final byte[] data) throws IOException {
        byte[] bytes;
        Base64.Encoder base64Encoder = Base64.getEncoder();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final BufferedImage res = ImageIO.read(new ByteArrayInputStream(data));
            ImageIO.write(res, format, baos);
            baos.flush();
            bytes = baos.toByteArray();
        }
        return base64Encoder.encodeToString(bytes);
    }
}
