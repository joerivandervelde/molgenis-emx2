package org.molgenis.emx2.beaconv2.responses.datasets;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.molgenis.emx2.beaconv2.common.CommonMeta;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DatasetsMeta extends CommonMeta {

  public DatasetsMeta(String $schema, String entityType) {
    super($schema, entityType);
  }

  String returnedGranularity = "record";
  ReceivedRequestSummary receivedRequestSummary = new ReceivedRequestSummary();
}