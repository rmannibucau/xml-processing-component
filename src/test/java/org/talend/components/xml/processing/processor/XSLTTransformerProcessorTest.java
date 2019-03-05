package org.talend.components.xml.processing.processor;


import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.util.List;

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

public class XSLTTransformerProcessorTest {

    @ClassRule
    public static final SimpleComponentRule COMPONENT_FACTORY = new SimpleComponentRule("org.talend.components.xml.processing");

    @Rule
    public final TestRule servicesInjector = new ServiceInjectionRule(COMPONENT_FACTORY, this);

    @Service
    private RecordBuilderFactory builderFactory;

    @Test
    public void map() {
        final XSLTTransformerProcessorConfiguration configuration =  new XSLTTransformerProcessorConfiguration();
        configuration.setField("xml");
        configuration.setIndent(true);
        configuration.setContent(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root xsl:version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "<xsl:for-each select=\"breakfast_menu/food\">\n" +
                "<item><xsl:value-of select=\"name\"/></item>\n" +
                "</xsl:for-each>\n" +
                "</root>");

        final Processor processor = COMPONENT_FACTORY.createProcessor(XSLTTransformerProcessor.class, configuration);

        final JoinInputFactory joinInputFactory =  new JoinInputFactory()
                .withInput("__default__", singletonList(builderFactory.newRecordBuilder()
                    .withString("xml",
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<breakfast_menu>\n" +
                            "  <food>\n" +
                            "    <name>Belgian Waffles</name>\n" +
                            "  </food>\n" +
                            "</breakfast_menu>")
                .build()));


        final SimpleComponentRule.Outputs outputs = COMPONENT_FACTORY.collect(processor, joinInputFactory);
        assertEquals(1, outputs.size());

        final List<Record> result = outputs.get(Record.class, "__default__");
        assertEquals(1, result.size());

        final Record output = result.iterator().next();
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><item>Belgian Waffles</item></root>",
                output.getString("xml"));
    }
}