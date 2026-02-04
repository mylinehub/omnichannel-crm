package com.mylinehub.crm.rag.service;

import com.mylinehub.crm.rag.util.ArchiveExtractor;
import com.mylinehub.crm.rag.util.TikaTextExtractor;

import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ScraperService {

    @Value("${scraper.max.depth}")
    private int maxDepth;

    @Value("${scraper.max.links}")
    private int maxLinks;

    @Value("${scraper.chunk.size}")
    private int chunkSizeChars;

    @Value("${scraper.temp.dir:/tmp/rag-scrape}")
    private String baseTempDir;

    private final OkHttpClient okHttpClient;
    private final TikaTextExtractor tikaTextExtractor;
    private final TranscriptionService transcriptionService;
    private final ImageProcessingService imageProcessingService;
    private final Tika tika = new Tika();

    public ScraperService(OkHttpClient okHttpClient,
                          TikaTextExtractor tikaTextExtractor,
                          TranscriptionService transcriptionService,
                          ImageProcessingService imageProcessingService) {
        this.okHttpClient = okHttpClient;
        this.tikaTextExtractor = tikaTextExtractor;
        this.transcriptionService = transcriptionService;
        this.imageProcessingService = imageProcessingService;
    }

    /**
     * Scrape site, download assets, process downloaded files (Tika, OCR, Transcription) and
     * return list of text chunks ready for embedding.
     */
    public List<String> scrapeProcessAndChunk(String startUrl) throws Exception {
        System.out.println("[Scraper] Starting scrape for URL: " + startUrl);

        // Prepare temp directory for this run
        Path runDir = Files.createTempDirectory(Paths.get(baseTempDir), "run-");
        System.out.println("[Scraper] temp dir: " + runDir.toAbsolutePath());

        Map<String, String> urlToText = new LinkedHashMap<>();
        Set<String> visited = new HashSet<>();
        AtomicInteger count = new AtomicInteger(0);
        URL u = new URL(startUrl);
        String domain = u.getHost();

        dfsFetchAndProcess(startUrl, domain, 0, visited, count, urlToText, runDir);

        // Aggregate all page texts and produce chunks
        StringBuilder all = new StringBuilder();
        for (Map.Entry<String, String> e : urlToText.entrySet()) {
            all.append("\n--- URL: ").append(e.getKey()).append(" ---\n");
            all.append(e.getValue()).append("\n");
        }

        List<String> chunks = chunkText(all.toString(), chunkSizeChars);
        System.out.println("[Scraper] Completed. Chunks: " + chunks.size() + ". TempDir: " + runDir);
        // Note: do not delete runDir immediately if you want to inspect downloaded files
        return chunks;
    }

    private void dfsFetchAndProcess(String url, String domain, int depth, Set<String> visited,
                                    AtomicInteger count, Map<String, String> urlToText, Path runDir) {
        if (depth > maxDepth) return;
        if (count.get() >= maxLinks) return;
        if (visited.contains(url)) return;

        try {
            System.out.println("[Scraper] Fetching URL: " + url);
            org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
            visited.add(url);
            count.incrementAndGet();

            // Extract textual content from HTML (headings, paragraphs, tables, lists, captions, alt, titles)
            String pageText = extractCruxText(doc.body());
            StringBuilder pageAugmented = new StringBuilder(pageText);

            // Download images, media and linked files referenced on page
            // Images
            Elements imgs = doc.select("img[src]");
            for (Element img : imgs) {
                String src = img.absUrl("src");
                if (src == null || src.isBlank()) continue;
                try {
                    File saved = downloadToTemp(src, runDir);
                    if (saved != null && saved.exists()) {
                        System.out.println("[Scraper] Downloaded image: " + saved.getName());
                        // 1) OCR
                        String ocr = imageProcessingService.extractTextFromImage(saved);
                        if (ocr != null && !ocr.isBlank()) {
                            pageAugmented.append(" ").append("[Image OCR] ").append(ocr);
                        }
                        // 2) metadata
                        String meta = imageProcessingService.extractMetadataText(saved);
                        if (meta != null && !meta.isBlank()) {
                            pageAugmented.append(" ").append("[Image Meta] ").append(meta);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("[Scraper] Failed to download/process image " + src + " : " + ex.getMessage());
                }
            }

            // Audio/video tags
            Elements media = doc.select("audio[src], video[src], source[src]");
            for (Element m : media) {
                String src = m.absUrl("src");
                if (src == null || src.isBlank()) continue;
                try {
                    File saved = downloadToTemp(src, runDir);
                    if (saved != null && saved.exists()) {
                        System.out.println("[Scraper] Downloaded media: " + saved.getName());
                        String mime = tika.detect(saved);
                        // transcribe audio/video
                        if (mime != null && (mime.startsWith("audio/") || mime.startsWith("video/"))) {
                            try {
                                String transcript = transcriptionService.transcribe(saved, mime);
                                if (transcript != null && !transcript.isBlank()) {
                                    pageAugmented.append(" ").append("[Media Transcript] ").append(transcript);
                                }
                            } catch (Exception te) {
                                System.err.println("[Scraper] Transcription failed for " + saved + " : " + te.getMessage());
                            }
                        } else {
                            // fallback: try tika text extraction
                            String txt = tikaTextExtractor.extractTextFromFile(saved);
                            if (txt != null && !txt.isBlank()) pageAugmented.append(" ").append(txt);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("[Scraper] Failed to download/process media " + src + " : " + ex.getMessage());
                }
            }

            // Links to files (anchors)
            Elements links = doc.select("a[href]");
            for (Element e : links) {
                String href = e.absUrl("href");
                if (href == null || href.isBlank()) continue;
                try {
                    URL linkUrl = new URL(href);
                    if (!linkUrl.getHost().equals(domain)) continue; // same-domain only (optional)
                    // if link points to file-like resource (pdf, zip, docx, etc) -> download and process
                    String lower = href.toLowerCase();
                    if (looksLikeDownloadableFile(lower)) {
                        File saved = downloadToTemp(href, runDir);
                        if (saved != null && saved.exists()) {
                            System.out.println("[Scraper] Downloaded file: " + saved.getName());
                            processDownloadedFile(saved, pageAugmented, runDir);
                        }
                    } else {
                        // follow the link as page (DFS)
                        dfsFetchAndProcess(href, domain, depth + 1, visited, count, urlToText, runDir);
                        if (count.get() >= maxLinks) break;
                    }
                    if (count.get() >= maxLinks) break;
                } catch (Exception ex) {
                    // ignore malformed or cross-domain
                }
            }

            String finalText = pageAugmented.toString().replaceAll("\\s+", " ").trim();
            urlToText.put(url, finalText);
            System.out.println("[Scraper] Extracted text length for " + url + ": " + finalText.length());

        } catch (Exception ex) {
            System.err.println("[Scraper] Failed to fetch " + url + " : " + ex.getMessage());
        }
    }

    private boolean looksLikeDownloadableFile(String lowerUrl) {
        return lowerUrl.matches(".*\\.(pdf|docx|doc|pptx|xlsx|xls|zip|rar|7z|txt|md|csv|json|xml|epub)(\\?.*)?$");
    }

    private File downloadToTemp(String srcUrl, Path runDir) throws Exception {
        Request req = new Request.Builder().url(srcUrl).get().build();
        Call call = okHttpClient.newCall(req);
        try (Response resp = call.execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                System.err.println("[Scraper] Failed to download " + srcUrl + " : " + resp.code());
                return null;
            }
            String filename = deriveFilenameFromUrl(srcUrl);
            Path out = runDir.resolve(filename);
            Files.createDirectories(out.getParent());
            try (InputStream is = resp.body().byteStream()) {
                Files.copy(is, out, StandardCopyOption.REPLACE_EXISTING);
            }
            return out.toFile();
        }
    }

    private String deriveFilenameFromUrl(String url) {
        try {
            String name = Paths.get(new URL(url).getPath()).getFileName().toString();
            if (name == null || name.isBlank()) {
                name = "file-" + UUID.randomUUID();
            }
            // sanitize
            name = name.replaceAll("[^a-zA-Z0-9._-]", "_");
            return name;
        } catch (Exception e) {
            return "file-" + UUID.randomUUID();
        }
    }

    private void processDownloadedFile(File saved, StringBuilder pageAugmented, Path runDir) {
        try {
            String mime = tika.detect(saved);
            if (mime == null) mime = "";

            if (mime.startsWith("image/")) {
                String ocr = imageProcessingService.extractTextFromImage(saved);
                if (ocr != null && !ocr.isBlank()) pageAugmented.append(" [Image OCR] ").append(ocr);
                String meta = imageProcessingService.extractMetadataText(saved);
                if (meta != null && !meta.isBlank()) pageAugmented.append(" [Image Meta] ").append(meta);
            } else if (mime.startsWith("audio/") || mime.startsWith("video/")) {
                try {
                    String transcript = transcriptionService.transcribe(saved, mime);
                    if (transcript != null && !transcript.isBlank())
                        pageAugmented.append(" [Media Transcript] ").append(transcript);
                } catch (Exception te) {
                    System.err.println("[Scraper] Transcription error: " + te.getMessage());
                }
            } else if (saved.getName().toLowerCase().endsWith(".zip")) {
                // extract archive
                try {
                    Path extractDir = runDir.resolve(saved.getName() + "-extracted");
                    Files.createDirectories(extractDir);
                    ArchiveExtractor.extractZip(saved, extractDir);
                    // process inner files recursively
                    Files.walk(extractDir).filter(Files::isRegularFile).forEach(p -> {
                        try {
                            processDownloadedFile(p.toFile(), pageAugmented, runDir);
                        } catch (Exception ex) {
                            System.err.println("[Scraper] Failed inner file: " + ex.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    System.err.println("[Scraper] Zip extraction failed: " + ex.getMessage());
                }
            } else if (saved.getName().toLowerCase().endsWith(".rar")) {
                // optional: implement junrar extraction here
                System.err.println("[Scraper] RAR extraction not implemented by default; consider junrar");
            } else {
                // fallback to Tika text extraction (PDF, DOCX, text, etc.)
                try {
                    String text = tikaTextExtractor.extractTextFromFile(saved);
                    if (text != null && !text.isBlank()) pageAugmented.append(" ").append(text);
                } catch (Exception ex) {
                    System.err.println("[Scraper] Tika text extraction failed for " + saved + " : " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[Scraper] processDownloadedFile error: " + e.getMessage());
        }
    }

    // Recursive DOM walker to extract "crux" textual content
    private String extractCruxText(Element element) {
        StringBuilder sb = new StringBuilder();
        for (Node node : element.childNodes()) {
            if (node instanceof TextNode) {
                sb.append(((TextNode) node).text()).append(" ");
            } else if (node instanceof Element) {
                Element el = (Element) node;
                String tag = el.tagName().toLowerCase();
                switch (tag) {
                    case "script":
                    case "style":
                    case "noscript":
                        break;
                    case "p":
                    case "h1": case "h2": case "h3": case "h4": case "h5": case "h6":
                    case "li":
                    case "td":
                    case "th":
                    case "figcaption":
                        sb.append(el.text()).append(" ");
                        break;
                    case "img":
                        if (el.hasAttr("alt")) sb.append(el.attr("alt")).append(" ");
                        break;
                    case "video":
                    case "audio":
                        if (el.hasAttr("title")) sb.append(el.attr("title")).append(" ");
                        break;
                    default:
                        sb.append(extractCruxText(el)).append(" ");
                }
            }
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    private List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;
        int start = 0;
        int len = text.length();
        while (start < len) {
            int end = Math.min(len, start + chunkSize);
            // try to cut on sentence boundary (period) if available within chunk
            String piece = text.substring(start, end).trim();
            if (end < len) {
                // try to extend to next space to avoid breaking words
                int nextSpace = text.indexOf(' ', end);
                if (nextSpace > end && nextSpace - start <= chunkSize + 200) {
                    end = nextSpace;
                    piece = text.substring(start, end).trim();
                }
            }
            chunks.add(piece);
            start = end;
        }
        System.out.println("[Scraper] Split into " + chunks.size() + " chunks");
        return chunks;
    }
}