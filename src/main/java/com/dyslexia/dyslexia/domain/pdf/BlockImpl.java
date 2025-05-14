package com.dyslexia.dyslexia.domain.pdf;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonSubTypes.Type(value = TableBlockImpl.class, name = "TABLE"),
    @JsonSubTypes.Type(value = PageTipBlockImpl.class, name = "PAGE_TIP"),
    @JsonSubTypes.Type(value = PageImageBlockImpl.class, name = "PAGE_IMAGE")
})
public abstract class BlockImpl implements Block {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("type")
    private String type;

    @Override
    public String getId() { 
        return id; 
    }
    
    @Override
    public BlockType getType() { 
        if (type == null) {
            return BlockType.TEXT;
        }
        try {
            return BlockType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return BlockType.TEXT;
        }
    }
} 