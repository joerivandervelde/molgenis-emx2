package org.molgenis.emx2.semantics;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.eclipse.rdf4j.model.IRI;

public class IriDeserializer extends JsonDeserializer<IRI> {
  @Override
  public IRI deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JacksonException {
    return new IRI() {
      @Override
      public String getNamespace() {
        return "irinamespace";
      }

      @Override
      public String getLocalName() {
        return "irilocalname";
      }

      @Override
      public String stringValue() {
        return "iristringvalue";
      }
    };
  }
}
