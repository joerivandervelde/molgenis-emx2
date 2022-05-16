package org.molgenis.emx2.semantics;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.eclipse.rdf4j.model.IRI;

public class IriSerializer extends JsonSerializer<IRI> {

  @Override
  public void serialize(IRI value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {}
}
