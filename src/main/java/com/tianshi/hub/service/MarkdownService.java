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

import java.util.Set;

@Service
public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownService() {
        this.parser = Parser.builder().build();
        this.renderer = HtmlRenderer.builder()
                .escapeHtml(true)
                .sanitizeUrls(true)
                .nodeRendererFactory(new VideoNodeRendererFactory())
                .build();
    }

    public String render(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        return renderer.render(parser.parse(markdown));
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
            if (isDirectVideo(destination)) {
                html.raw("<video controls preload=\"metadata\" class=\"markdown-video\" src=\"");
                html.raw(escapeAttribute(destination));
                html.raw("\"></video>");
                return;
            }
            String sanitizedUrl = context.urlSanitizer().sanitizeLinkUrl(destination);
            if (sanitizedUrl == null) {
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
            String lowerCase = destination.toLowerCase();
            return lowerCase.endsWith(".mp4") || lowerCase.endsWith(".webm");
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
