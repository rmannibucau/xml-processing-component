package org.talend.components.xml.processing.processor;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.BuiltInSuggestable;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Code;
import org.talend.sdk.component.api.configuration.ui.widget.TextArea;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@OptionsOrder({
    "field",
    "skipOnInvalid",
    "content"
})
@Documentation("Configuration of the XSD valdiator component.")
public class XSDValidatorProcessorConfiguration implements Serializable {
    @Option
    @BuiltInSuggestable(BuiltInSuggestable.Name.INCOMING_SCHEMA_ENTRY_NAMES)
    @Documentation("Record field containing the XML document")
    private String field;

    @Option
    @Documentation("Should record be ignored when invalid, default will fail the execution")
    private boolean skipOnInvalid;

    @Option
    @Code("xml")
    @Documentation("The XSD content")
    private String content;
}