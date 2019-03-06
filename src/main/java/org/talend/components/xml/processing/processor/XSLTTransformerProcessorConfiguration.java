package org.talend.components.xml.processing.processor;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.BuiltInSuggestable;
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
@Documentation("Configuration of XSLT transformer.")
public class XSLTTransformerProcessorConfiguration implements Serializable {
    @Option
    @BuiltInSuggestable(BuiltInSuggestable.Name.INCOMING_SCHEMA_ENTRY_NAMES)
    @Documentation("Record field containing the XML document")
    private String field;

    @Option
    @Documentation("Should the output be indented. " +
            "Concretely it sets `javax.xml.transform.Transformer` `indent` option to `yes`.")
    private boolean indent;

    @Option
    @TextArea
    @Documentation("The XSLT itself.")
    private String content;
}