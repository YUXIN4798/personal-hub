package com.tianshi.hub.service;

import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class MarkdownService {

    private static final int RENDER_CACHE_MAX_SIZE = 200;

    private final Parser parser;
    private final HtmlRenderer renderer;
    private final Map<String, String> renderCache;

    public MarkdownService() {
        this.parser = Parser.builder().build();
        this.renderer = HtmlRenderer.builder()
                .escapeHtml(true)
                .sanitizeUrls(true)
                .nodeRendererFactory(new VideoNodeRendererFactory())
                .build();
        this.renderCache = new LinkedHashMap<>(RENDER_CACHE_MAX_SIZE, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > RENDER_CACHE_MAX_SIZE;
            }
        };
    }

    public String render(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        String cacheKey = sha256(markdown);
        synchronized (renderCache) {
            String cached = renderCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        String rendered = renderer.render(parser.parse(markdown));
        synchronized (renderCache) {
            renderCache.put(cacheKey, rendered);
        }
        return rendered;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte current : hash) {
                hex.append(String.format("%02x", current));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    private static class VideoNodeRendererFactory implements HtmlNodeRendererFactory {

        @Override
        public NodeRenderer create(HtmlNodeRendererContext context) {
            return new VideoNodeRenderer(context);
        }
    }

    private static class VideoNodeRenderer implements NodeRenderer {

        private final HtmlNodeRendererContext context;
        private final HtmlWriter html;

        private VideoNodeRenderer(HtmlNodeRendererContext context) {
            this.context = context;
            this.html = context.getWriter();
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Set.of(Link.class);
        }

        @Override
        public void render(Node node) {
            Link link = (Link) node;
            String destination = link.getDestination();
            if (isDirectVideo(destination) && isAllowedVideoUrl(destination)) {
                html.raw("<video controls preload=\"metadata\" class=\"markdown-video\" src=\"");
                html.raw(escapeAttribute(destination));
                html.raw("\"></video>");
                return;
            }
            String sanitizedUrl = context.urlSanitizer().sanitizeLinkUrl(destination);
            if (sanitizedUrl == null || sanitizedUrl.isBlank()) {
                sanitizedUrl = "#";
            }
            html.raw("<a href=\"");
            html.raw(escapeAttribute(sanitizedUrl));
            html.raw("\">");
            renderChildren(node);
            html.raw("</a>");
        }

        private void renderChildren(Node node) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                context.render(child);
            }
        }

        private boolean isDirectVideo(String destination) {
            if (destination == null) {
                return false;
            }
            String lowerCase = destination.toLowerCase(Locale.ROOT);
            return lowerCase.endsWith(".mp4") || lowerCase.endsWith(".webm");
        }

        private boolean isAllowedVideoUrl(String destination) {
            String lowerCase = destination.toLowerCase(Locale.ROOT);
            return lowerCase.startsWith("http://")
                    || lowerCase.startsWith("https://")
                    || (destination.startsWith("/") && !destination.startsWith("//"));
        }

        private String escapeAttribute(String value) {
            return value
                    .replace("&", "&amp;")
                    .replace("\"", "&quot;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
        }
    }
}
