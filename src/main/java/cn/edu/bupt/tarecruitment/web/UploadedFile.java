package cn.edu.bupt.tarecruitment.web;

public class UploadedFile {

    private final String fieldName;
    private final String originalFileName;
    private final String contentType;
    private final byte[] content;

    public UploadedFile(
            String fieldName, String originalFileName, String contentType, byte[] content) {
        this.fieldName = fieldName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.content = content;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }
}
