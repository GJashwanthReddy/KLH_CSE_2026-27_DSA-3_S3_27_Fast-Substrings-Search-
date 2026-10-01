import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DocumentReader.java
 * ===================
 * Separate module for reading documents (.txt, .docx) and calculating metadata.
 * Keeps document I/O parsing separated from the core Suffix Array DSA algorithm.
 */
public class DocumentReader {

    public static class DocumentMetadata {
        public final String fileName;
        public final String fileType;
        public final long fileSizeBytes;
        public final String fileSizeFormatted;
        public final int characterCount;
        public final int wordCount;

        public DocumentMetadata(String fileName, String fileType, long fileSizeBytes, String fileSizeFormatted, int characterCount, int wordCount) {
            this.fileName = fileName;
            this.fileType = fileType;
            this.fileSizeBytes = fileSizeBytes;
            this.fileSizeFormatted = fileSizeFormatted;
            this.characterCount = characterCount;
            this.wordCount = wordCount;
        }
    }

    public static class ReadResult {
        public final String text;
        public final DocumentMetadata metadata;

        public ReadResult(String text, DocumentMetadata metadata) {
            this.text = text;
            this.metadata = metadata;
        }
    }

    public static ReadResult readFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        String fileName = file.getName();
        long fileSizeBytes = file.length();
        String ext = getFileExtension(fileName).toLowerCase();

        String text;
        String fileType;

        if (ext.equals(".txt")) {
            text = readTxtFile(file);
            fileType = "Plain Text (.txt)";
        } else if (ext.equals(".docx")) {
            text = readDocxFile(file);
            fileType = "Microsoft Word (.docx)";
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + ext + ". Only .txt and .docx are supported.");
        }

        DocumentMetadata metadata = calculateMetadata(text, fileName, fileType, fileSizeBytes);
        return new ReadResult(text, metadata);
    }

    public static ReadResult readManualText(String text, String name) {
        String safeName = (name == null || name.isEmpty()) ? "Manual Text Entry" : name;
        long bytes = text.getBytes(StandardCharsets.UTF_8).length;
        DocumentMetadata metadata = calculateMetadata(text, safeName, "Manual Text", bytes);
        return new ReadResult(text, metadata);
    }

    private static String readTxtFile(File file) throws IOException {
        // Read file using UTF-8, normalize CRLF to LF
        byte[] bytes = Files.readAllBytes(file.toPath());
        String text = new String(bytes, StandardCharsets.UTF_8);
        return text.replace("\r\n", "\n");
    }

    private static String readDocxFile(File file) throws IOException {
        // Pure Java standard library extraction for .docx (ZipFile + DOM XML parser)
        StringBuilder textBuilder = new StringBuilder();

        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry docXmlEntry = zipFile.getEntry("word/document.xml");
            if (docXmlEntry == null) {
                throw new IOException("Invalid .docx file structure: missing word/document.xml entry.");
            }

            try (InputStream is = zipFile.getInputStream(docXmlEntry)) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(is);

                // Word XML paragraphs: <w:p>
                NodeList paragraphNodes = doc.getElementsByTagNameNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "p");
                if (paragraphNodes.getLength() == 0) {
                    paragraphNodes = doc.getElementsByTagName("w:p");
                }

                for (int i = 0; i < paragraphNodes.getLength(); i++) {
                    Node pNode = paragraphNodes.item(i);
                    NodeList textNodes = pNode.getChildNodes();
                    StringBuilder pBuilder = new StringBuilder();
                    extractTextFromNode(pNode, pBuilder);

                    String paragraphText = pBuilder.toString().trim();
                    if (!paragraphText.isEmpty()) {
                        if (textBuilder.length() > 0) {
                            textBuilder.append("\n");
                        }
                        textBuilder.append(paragraphText);
                    }
                }
            } catch (Exception e) {
                throw new IOException("Failed to parse XML content of .docx document: " + e.getMessage(), e);
            }
        }

        return textBuilder.toString();
    }

    private static void extractTextFromNode(Node node, StringBuilder sb) {
        if (node == null) return;
        String nodeName = node.getNodeName();

        if (nodeName.endsWith(":t") || nodeName.equals("w:t")) {
            sb.append(node.getTextContent());
        } else {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                extractTextFromNode(children.item(i), sb);
            }
        }
    }

    public static DocumentMetadata calculateMetadata(String text, String fileName, String fileType, long fileSizeBytes) {
        int charCount = text == null ? 0 : text.length();
        int wordCount = 0;

        if (text != null && !text.trim().isEmpty()) {
            String[] words = text.trim().split("\\s+");
            wordCount = words.length;
        }

        String formattedSize = formatFileSize(fileSizeBytes);
        return new DocumentMetadata(fileName, fileType, fileSizeBytes, formattedSize, charCount, wordCount);
    }

    public static String formatFileSize(long sizeBytes) {
        if (sizeBytes < 1024) {
            return sizeBytes + " Bytes";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeBytes / 1024.0);
        } else if (sizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", sizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    private static String getFileExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot >= 0) ? fileName.substring(dot) : "";
    }
}
