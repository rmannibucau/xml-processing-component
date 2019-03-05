package org.talend.components.xml.processing.processor;


import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.function.Consumer;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.junit.JoinInputFactory;
import org.talend.sdk.component.junit.ServiceInjectionRule;
import org.talend.sdk.component.junit.SimpleComponentRule;
import org.talend.sdk.component.runtime.output.Processor;

public class XSDValidatorProcessorTest implements XSDFixture {

    @ClassRule
    public static final SimpleComponentRule COMPONENT_FACTORY = new SimpleComponentRule("org.talend.components.xml.processing");

    @Rule
    public final TestRule servicesInjector = new ServiceInjectionRule(COMPONENT_FACTORY, this);

    @Service
    private RecordBuilderFactory builderFactory;

    @Test
    public void validRecord() {
        final Record record = builderFactory.newRecordBuilder()
                .withString("xml", VALID_XML)
                .build();
        final List<Record> outputs = execute(record);
        assertEquals(1, outputs.size());
        assertEquals(singletonList(record), outputs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidRecord() {
        execute(builderFactory.newRecordBuilder()
                .withString("xml", INVALID_XML)
                .build());
    }

    @Test
    public void invalidAndIgnored() {
        final List<Record> result = execute(builderFactory.newRecordBuilder()
                .withString("xml", INVALID_XML)
                .build(), config -> config.setSkipOnInvalid(true));
        assertNull(result);
    }

    private List<Record> execute(final Record record) {
        return execute(record, null);
    }

    private List<Record> execute(final Record record, final Consumer<XSDValidatorProcessorConfiguration> customizer) {
        final XSDValidatorProcessorConfiguration configuration =  new XSDValidatorProcessorConfiguration();
        configuration.setContent(XSD);
        configuration.setField("xml");
        if (customizer != null) {
            customizer.accept(configuration);
        }

        final Processor processor = COMPONENT_FACTORY.createProcessor(XSDValidatorProcessor.class, configuration);
        final JoinInputFactory joinInputFactory =  new JoinInputFactory()
                .withInput("__default__", singletonList(record));
        final SimpleComponentRule.Outputs outputs = COMPONENT_FACTORY.collect(processor, joinInputFactory);
        return outputs.get(Record.class, "__default__");
    }

}