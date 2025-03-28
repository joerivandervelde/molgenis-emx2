package org.molgenis.emx2.io;

import java.util.*;
import org.molgenis.emx2.Schema;
import org.molgenis.emx2.Table;
import org.molgenis.emx2.io.tablestore.TableStore;
import org.molgenis.emx2.tasks.Task;

/**
 * Task to import schema from a table store, i.e., will run ImportMetadataTask and for each table a
 * ImportTableTask.
 */
public class ImportDataTask extends Task {
  private final TableStore tableStore;
  private final Schema schema;
  private Set<String> includeTableNames;

  public ImportDataTask(
      String description,
      TableStore store,
      Schema schema,
      boolean strict,
      String... includeTableNames) {
    super(description, strict);
    Objects.requireNonNull(store, "tableStore cannot be null");
    Objects.requireNonNull(schema, "schema cannot be null");
    this.tableStore = store;
    this.schema = schema;

    if (includeTableNames.length > 0) {
      this.includeTableNames = new HashSet<>(Arrays.asList(includeTableNames));
    }
  }

  public ImportDataTask(
      Schema schema, TableStore store, boolean strict, String... includeTableNames) {
    this("Import from store", store, schema, strict, includeTableNames);
  }

  @Override
  public void run() {
    this.start();

    // create a task for each table
    boolean skipped = true;

    // create task for the import, including subtasks for each sheet
    for (Table table : schema.getTablesSorted()) {
      if (tableStore.containsTable(table.getName())
          && (includeTableNames == null || includeTableNames.contains(table.getName()))) {
        ImportTableTask importTableTask = new ImportTableTask(tableStore, table, isStrict());
        this.addSubTask(importTableTask);
        importTableTask.run();
        skipped = false;
      }
    }

    // check what files we skipped
    try {
      for (String tableName : tableStore.getTableNames()) {
        String sheet = tableName.toLowerCase().replace(" ", "");
        if (!sheet.startsWith("_files/")
            && !"molgenis".equals(sheet)
            && !"molgenis_settings".equals(sheet)
            && !"molgenis_members".equals(sheet)
            && !schema.hasTableWithNameOrIdCaseInsensitive(sheet)) {
          this.addSubTask(
                  "Sheet with name '" + sheet + "' was skipped: no table with that name found")
              .setSkipped();
        }
      }
    } catch (UnsupportedOperationException e) {
      // ignore, not important
    }

    // execute the import tasks
    if (skipped) {
      this.addSubTask("Import data skipped: No data sheet included").setSkipped();
    }
    this.complete();
  }
}
