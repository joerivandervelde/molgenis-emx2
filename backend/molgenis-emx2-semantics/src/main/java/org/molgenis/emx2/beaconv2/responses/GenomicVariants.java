package org.molgenis.emx2.beaconv2.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.List;
import org.molgenis.emx2.Table;
import org.molgenis.emx2.beaconv2.responses.datasets.DatasetsMeta;
import org.molgenis.emx2.beaconv2.responses.datasets.ResponseSummary;
import org.molgenis.emx2.beaconv2.responses.genomicvariants.GenomicVariantsResponse;
import spark.Request;

/** Genomic variants, analyses, cohorts, sequencing runs, individuals, samples */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GenomicVariants {

  DatasetsMeta meta;
  ResponseSummary responseSummary;
  GenomicVariantsResponse response;

  public GenomicVariants(Request request, List<Table> genomicVariantTables) throws Exception {
    this.meta = new DatasetsMeta("../beaconDatasetResponse.json", "datasets");
    this.response = new GenomicVariantsResponse(request, genomicVariantTables);
    this.responseSummary = new ResponseSummary();
  }
}