package com.dyslexia.dyslexia.domain.pdf;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextBlockImpl.class, name = "TEXT"),
        @JsonSubTypes.Type(value = TextBlockImpl.class, name = "HEADING1"),
        @JsonSubTypes.Type(value = TextBlockImpl.class, name = "HEADING2"),
        @JsonSubTypes.Type(value = TextBlockImpl.class, name = "HEADING3"),
        @JsonSubTypes.Type(value = ListBlockImpl.class, name = "LIST"),
        @JsonSubTypes.Type(value = ListBlockImpl.class, name = "DOTTED"),
        @JsonSubTypes.Type(value = ImageBlockImpl.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = TableBlockImpl.class, name = "TABLE"),
        @JsonSubTypes.Type(value = PageTipBlockImpl.class, name = "PAGE_TIP"),
        @JsonSubTypes.Type(value = PageImageBlockImpl.class, name = "PAGE_IMAGE")
})
public abstract class BlockImpl implements Block {
    private String id;
    private BlockType type;

    @Override
    public String getId() { return id; }
    @Override
    public BlockType getType() { return type; }

    public void setId(String id) { this.id = id; }
    public void setType(BlockType type) { this.type = type; }
} 