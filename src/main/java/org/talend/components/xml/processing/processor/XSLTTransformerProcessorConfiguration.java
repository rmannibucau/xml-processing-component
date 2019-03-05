package org.talend.components.xml.processing.processor;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.configuration.ui.widget.TextArea;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@OptionsOrder({
        "field",
        "indent",
        "content"
})
@Documentation("TODO fill the documentation for this configuration")
public class XSLTTransformerProcessorConfiguration implements Serializable {
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private String field;

    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private boolean indent;

    @Option
    @TextArea
    @Documentation("TODO fill the documentation for this parameter")
    private String content;
}