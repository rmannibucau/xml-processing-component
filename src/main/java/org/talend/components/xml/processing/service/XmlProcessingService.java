package org.talend.components.xml.processing.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Function;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.Service;
import org.xml.sax.SAXException;

@Service
public class XmlProcessingService {
    public void copyEntry(final Record input, final Record.Builder builder,
                          final org.talend.sdk.component.api.record.Schema.Entry entry) {
        switch (entry.getType()) {
            case BYTES:
                builder.withBytes(entry, input.getBytes(entry.getName()));
                break;
            case STRING:
                builder.withString(entry, input.getString(entry.getName()));
                break;
            case INT:
                input.getOptionalInt(entry.getName()).ifPresent(v -> builder.withInt(entry, v));
                break;
            case LONG:
                input.getOptionalLong(entry.getName()).ifPresent(v -> builder.withLong(entry, v));
                break;
            case FLOAT:
                input.getOptionalFloat(entry.getName()).ifPresent(v -> builder.withFloat(entry, (float) v));
                break;
            case DOUBLE:
                input.getOptionalDouble(entry.getName()).ifPresent(v -> builder.withDouble(entry, v));
                break;
            case BOOLEAN:
                input.getOptionalBoolean(entry.getName()).ifPresent(v -> builder.withBoolean(entry, v));
                break;
            case DATETIME:
                input.getOptionalDateTime(entry.getName()).ifPresent(v -> builder.withDateTime(entry, v));
                break;
            case RECORD:
                input.getOptionalRecord(entry.getName()).ifPresent(v -> builder.withRecord(entry, v));
                break;
            case ARRAY:
                input.getOptionalArray(Object.class, entry.getName()).ifPresent(v -> builder.withArray(entry, v));
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + entry.getType());
        }
    }

    public Function<byte[], Optional<String>> createXSDValidator(final String xsd) {
        final SchemaFactory factory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema;
        try {
            schema = factory.newSchema(toSource(xsd));
        } catch (final SAXException e) {
            throw new IllegalArgumentException(e);
        }
        final Validator validator = schema.newValidator();
        return content -> {
            try {
                validator.validate(toSource(content));
                return Optional.empty();
            } catch (final SAXException | IOException e) {
                return Optional.of(e.getClass().getName() + " :" + e.getMessage());
            }
        };
    }

    public Function<byte[], String> createXSLTTransformer(final String xslt, final boolean indent) {
        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer transformer;
        try {
            transformer = factory.newTransformer(toSource(xslt));
        } catch (final TransformerConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        transformer.setParameter(OutputKeys.INDENT, indent ? "yes" : "no");
        return content -> {
            final StringWriter output = new StringWriter();
            try {
                transformer.transform(toSource(content), new StreamResult(output));
                return output.toString();
            } catch (final TransformerException e) {
                throw new IllegalArgumentException(e);
            }
        };
    }

    private StreamSource toSource(final byte[] content) {
        return new StreamSource(new ByteArrayInputStream(content));
    }

    private StreamSource toSource(final String xsd) {
        return new StreamSource(new StringReader(xsd));
    }
}