package com.dyslexia.dyslexia.domain.pdf;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TextBlockImpl extends BlockImpl implements TextBlock {
    private String text;
} 