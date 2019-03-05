package org.talend.components.xml.processing.processor;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

import org.talend.components.xml.processing.service.ColumnExtractor;
import org.talend.components.xml.processing.service.XmlProcessingService;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Output;
import org.talend.sdk.component.api.processor.OutputEmitter;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;

@Version
@Icon(Icon.IconType.FILE_XML_O)
@Processor(name = "XSDValidator")
@Documentation("Validates a payload against a XSD")
public class XSDValidatorProcessor implements Serializable {
    private final XSDValidatorProcessorConfiguration configuration;
    private final XmlProcessingService service;

    private transient ColumnExtractor columnExtractor;
    private transient Function<byte[], Optional<String>> validator;

    public XSDValidatorProcessor(@Option("configuration") final XSDValidatorProcessorConfiguration configuration,
                                 final XmlProcessingService service) {
        requireNonNull(configuration.getField(), "No XML field set");
        requireNonNull(configuration.getContent(), "No XSD field set");
        this.configuration = configuration;
        this.service = service;
    }

    @ElementListener
    public void onNext(@Input final Record record, @Output final OutputEmitter<Record> output) {
        final Optional<String> error = getValidator().apply(extractXml(record));
        if (error.isPresent()) {
            if (configuration.isSkipOnInvalid()) {
                return;
            }
            throw new IllegalArgumentException("Invalid record: " + error.orElseThrow(IllegalStateException::new));
        }
        output.emit(record);
    }

    private byte[] extractXml(final Record record) {
        return (columnExtractor == null ? columnExtractor = new ColumnExtractor() : columnExtractor)
                .getOrCreate(record.getSchema(), configuration.getField()).apply(record);
    }

    private Function<byte[], Optional<String>> getValidator() {
        if (validator == null) {
            validator = service.createXSDValidator(configuration.getContent());
        }
        return validator;
    }
}