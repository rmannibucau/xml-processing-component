package org.talend.components.xml.processing.service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;

public class ColumnExtractor {
    private final Map<Schema, Function<Record, byte[]>> columnExtractor = new LinkedHashMap<Schema, Function<Record, byte[]>>() {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<Schema, Function<Record, byte[]>> eldest) {
            return size() > 100;
        }
    };

    public Function<Record, byte[]> getOrCreate(final Schema schema, final String field) {
        return columnExtractor.computeIfAbsent(schema, s -> s.getEntries().stream()
                .filter(it -> it.getName().equals(field))
                .findFirst()
                .map(entry -> {
                    final String entryName = entry.getName();
                    switch (entry.getType()) {
                        case STRING:
                            return (Function<Record, byte[]>) r -> r.getString(entryName).getBytes(StandardCharsets.UTF_8);
                        case BYTES:
                            return (Function<Record, byte[]>) r -> r.getBytes(entryName);
                        default:
                            throw new IllegalArgumentException("Unsupported XML column type: " + entry);
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("No column: " + field + " in schema " + schema)));
    }
}
