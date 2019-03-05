package org.talend.components.xml.processing.processor;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollection;
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
import org.talend.sdk.component.junit.beam.Data;
import org.talend.sdk.component.runtime.beam.TalendFn;
import org.talend.sdk.component.runtime.manager.service.api.Unwrappable;
import org.talend.sdk.component.runtime.output.Processor;

public class XSDValidatorProcessorBeamTest implements XSDFixture, Serializable {
    @ClassRule
    public static final SimpleComponentRule COMPONENT_FACTORY = new SimpleComponentRule("org.talend.components.xml.processing");

    @Rule
    public transient final TestPipeline pipeline = TestPipeline.create();

    @Rule
    public transient final TestRule servicesInjector = new ServiceInjectionRule(COMPONENT_FACTORY, this);

    @Service
    private transient RecordBuilderFactory builderFactory;

    @Test
    public void processor() {
        final Record xml = builderFactory.newRecordBuilder().withString("xml", VALID_XML).build();

        final XSDValidatorProcessorConfiguration configuration =  new XSDValidatorProcessorConfiguration();
        configuration.setContent(XSD);
        configuration.setField("xml");

        final Processor processor = COMPONENT_FACTORY.createProcessor(XSDValidatorProcessor.class, configuration);
        final JoinInputFactory joinInputFactory =  new JoinInputFactory()
                .withInput("__default__", singletonList(xml));

        final PCollection<Record> inputs =
                pipeline.apply(Data.of(processor.plugin(), joinInputFactory.asInputRecords()));
        final PCollection<Map<String, Record>> outputs = inputs.apply(TalendFn.asFn(processor))
                .apply(Data.map(processor.plugin(), Record.class));

        final String expectedOutput = toAvro(xml).toString();
        PAssert.that(outputs).satisfies((SerializableFunction<Iterable<Map<String, Record>>, Void>) input -> {
            final List<Map<String, Record>> result = StreamSupport.stream(input.spliterator(), false).collect(toList());
            assertEquals(1, result.size());
            final Record output = result.iterator().next().get("__default__");
            assertNotNull(output);
            assertEquals(expectedOutput, toAvro(output).toString());
            return null;
        });
        assertEquals(PipelineResult.State.DONE, pipeline.run().waitUntilFinish());
    }

    private IndexedRecord toAvro(final Record xml) {
        return Unwrappable.class.cast(xml).unwrap(IndexedRecord.class);
    }
}