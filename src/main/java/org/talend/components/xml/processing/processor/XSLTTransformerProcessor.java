package org.talend.components.xml.processing.processor;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.talend.components.xml.processing.service.ColumnExtractor;
import org.talend.components.xml.processing.service.XmlProcessingService;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

@Version(1) // default version is 1, if some configuration changes happen between 2 versions you can add a migrationHandler
@Icon(Icon.IconType.STAR) // you can use a custom one using @Icon(value=CUSTOM, custom="filename") and adding icons/filename_icon32.png in resources
@Processor(name = "XSLTTransformer")
@Documentation("TODO fill the documentation for this processor")
public class XSLTTransformerProcessor implements Serializable {
    private final XSLTTransformerProcessorConfiguration configuration;
    private final XmlProcessingService service;
    private final RecordBuilderFactory builderFactory;

    private transient ColumnExtractor columnExtractor = new ColumnExtractor();
    private transient Function<byte[], String> transformer;

    public XSLTTransformerProcessor(@Option("configuration") final XSLTTransformerProcessorConfiguration configuration,
                                    final XmlProcessingService service,
                                    final RecordBuilderFactory builderFactory) {
        requireNonNull(configuration.getContent(), "No XSLT field set");
        requireNonNull(configuration.getField(), "No XML field set");
        this.configuration = configuration;
        this.service = service;
        this.builderFactory = builderFactory;
    }

    @ElementListener
    public Record onNext(@Input final Record input) {
        final String newXml = getTransformer().apply(extractXml(input));
        // copy the record replacing incoming xml column with the new one respecting the same type
        final Record.Builder builder = builderFactory.newRecordBuilder(input.getSchema());
        input.getSchema().getEntries().forEach(entry -> {
            if (entry.getName().equals(configuration.getField())) {
                switch (entry.getType()) {
                    case BYTES:
                        builder.withBytes(entry, newXml.getBytes(StandardCharsets.UTF_8));
                        break;
                    case STRING:
                        builder.withString(entry, newXml);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported type: " + entry.getType());
                }
            } else {
                service.copyEntry(input, builder, entry);
            }
        });
        return builder.build();

    }

    private byte[] extractXml(final Record record) {
        return (columnExtractor == null ? columnExtractor = new ColumnExtractor() : columnExtractor)
                .getOrCreate(record.getSchema(), configuration.getField()).apply(record);
    }

    private Function<byte[], String> getTransformer() {
        if (transformer == null) {
            transformer = service.createXSLTTransformer(configuration.getContent(), configuration.isIndent());
        }
        return transformer;
    }
}