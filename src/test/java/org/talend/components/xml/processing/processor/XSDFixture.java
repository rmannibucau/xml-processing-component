package org.talend.components.xml.processing.processor;

public interface XSDFixture {
    String XSD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <xs:element name=\"Employee\" type=\"employee\" />\n" +
            "  <xs:complexType name=\"employee\">\n" +
            "    <xs:sequence>\n" +
            "      <xs:element name=\"name\" type=\"xs:string\" />\n" +
            "      <xs:element name=\"age\" type=\"xs:int\" />\n" +
            "      <xs:element name=\"role\" type=\"xs:string\" />\n" +
            "      <xs:element name=\"gender\" type=\"xs:string\" />\n" +
            "    </xs:sequence>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>";

    String VALID_XML = "<?xml version=\"1.0\"?>\n" +
            "<Employee>\n" +
            "  <name>Somebody</name>\n" +
            "  <age>33</age>\n" +
            "  <role>Tester</role>\n" +
            "  <gender>Machine</gender>\n" +
            "</Employee>";

    String INVALID_XML = "<?xml version=\"1.0\"?>\n" +
            "<Employee>\n" +
            "  <age>33</age>\n" +
            "  <name>Somebody</name>\n" +
            "  <role>Tester</role>\n" +
            "  <gender>Machine</gender>\n" +
            "</Employee>";
}
