package org.molgenis.emx2.sql;

import static org.molgenis.emx2.ColumnType.AUTO_ID;
import static org.molgenis.emx2.utils.JavaScriptUtils.executeJavascript;
import static org.molgenis.emx2.utils.JavaScriptUtils.executeJavascriptOnMap;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.molgenis.emx2.*;
import org.molgenis.emx2.utils.TypeUtils;
import org.molgenis.emx2.utils.generator.SnowflakeIdGenerator;

public class SqlTypeUtils extends TypeUtils {

  private SqlTypeUtils() {
    // to hide the public constructor
  }

  public static List<Row> applyValidationAndComputed(List<Column> columns, List<Row> rows) {
    for (Row row : rows) {
      applyValidationAndComputed(columns, row);
    }
    return rows;
  }

  public static void applyValidationAndComputed(List<Column> columns, Row row) {
    Map<String, Object> graph = convertRowToMap(columns, row);
    addJavaScriptBindings(columns, graph);
    for (Column c : columns.stream().filter(c -> !c.isHeading()).toList()) {
      if (Constants.MG_EDIT_ROLE.equals(c.getName())) {
        row.setString(
            c.getName(), Constants.MG_USER_PREFIX + row.getString(Constants.MG_EDIT_ROLE));
      } else if (AUTO_ID.equals(c.getColumnType())) {
        applyAutoId(c, row);
      } else if (c.getDefaultValue() != null && !row.notNull(c.getName())) {
        if (c.getDefaultValue().startsWith("=")) {
          try {
            if (c.isRefArray()) {
              convertRefArrayToRow(
                  (List)
                      executeJavascriptOnMap(c.getDefaultValue().substring(1), graph, List.class),
                  row,
                  c);
            } else if (c.isRef()) {
              convertRefToRow(
                  (Map)
                      executeJavascriptOnMap(
                          "(" + c.getDefaultValue().substring(1) + ")", graph, Map.class),
                  row,
                  c);
            } else {
              row.set(c.getName(), executeJavascript(c.getDefaultValue().substring(1)));
            }
          } catch (Exception e) {
            throw new MolgenisException(
                "Error in defaultValue of column " + c.getName() + ": " + e.getMessage());
          }
        } else {
          row.set(c.getName(), c.getDefaultValue());
        }
      } else if (c.getComputed() != null) {
        Object computedValue = executeJavascriptOnMap(c.getComputed(), graph);
        TypeUtils.addFieldObjectToRow(c, computedValue, row);
      } else if (columnIsVisible(c, graph)) {
        checkRequired(c, row, graph);
        checkValidation(c, graph);
      } else {
        if (c.isReference()) {
          for (Reference ref : c.getReferences()) {
            row.clear(ref.getName());
          }
        } else {
          row.clear(c.getName());
        }
      }
    }
  }

  private static void applyAutoId(Column c, Row row) {
    if (row.isNull(c.getName(), c.getPrimitiveColumnType())) {
      String id = SnowflakeIdGenerator.getInstance().generateId();
      // do we use a template containing ${mg_autoid} for pre/postfixing ?
      if (c.getComputed() != null) {
        row.set(c.getName(), c.getComputed().replace(Constants.COMPUTED_AUTOID_TOKEN, id));
      }
      // otherwise simply put the id
      else row.set(c.getName(), id);
    }
  }

  private static void checkRequired(Column c, Row row, Map<String, Object> values) {
    if (!row.isDraft() && c.getComputed() == null && !AUTO_ID.equals(c.getColumnType())) {
      if (c.isRequired() && hasEmptyFields(c, row)) {
        throw new MolgenisException("column '" + c.getName() + "' is required in " + row);
      } else if (c.isConditionallyRequired()) {
        String error = checkRequiredExpression(c.getRequired(), values);
        if (error != null && hasEmptyFields(c, row)) {
          throw new MolgenisException(
              "column '" + c.getName() + "' is required: " + error + " in " + row);
        }
      }
    }
    if (c.isReference()) {
      List<Reference> refs = c.getReferences();
      // PostgreSQL considers the foreign key constraint not applicable if any part of the composite
      // key is NULL.therefore we must make sure it is complete
      // exclude overlapping
      int countNotNullNotOverlapping = 0;
      int countNotNull = 0;
      for (Reference ref : refs) {
        if (!row.isNull(ref.getName(), ref.getPrimitiveType())) {
          if (!ref.isOverlapping()) {
            countNotNullNotOverlapping++;
          }
          countNotNull++;
        }
      }
      if (countNotNullNotOverlapping > 0 && countNotNull != refs.size()) {
        throw new MolgenisException(
            String.format(
                "Key (%s)=(%s) not present in table \"%s\"",
                refs.stream().map(ref -> ref.getName()).collect(Collectors.joining(",")),
                refs.stream()
                    .map(
                        ref ->
                            row.isNull(ref.getName(), ref.getPrimitiveType())
                                ? "NULL"
                                : row.getValueMap().get(ref.getName()).toString())
                    .collect(Collectors.joining(",")),
                c.getRefTableName()));
      }
    }
  }

  private static boolean hasEmptyFields(Column c, Row row) {
    if (c.isReference()) {
      for (Reference r : c.getReferences()) {
        if (row.isNull(r.getName(), r.getPrimitiveType())) {
          return true;
        }
      }
    } else {
      return row.isNull(c.getName(), c.getColumnType());
    }
    return false;
  }

  private static boolean columnIsVisible(Column column, Map values) {
    if (column.getVisible() != null) {
      Object visibleResult = executeJavascriptOnMap(column.getVisible(), values);
      if (visibleResult == null || Boolean.FALSE.equals(visibleResult)) {
        return false;
      }
    }
    return true;
  }

  public static Object getTypedValue(Column c, Row row) {
    String name = c.getName();
    return switch (c.getPrimitiveColumnType()) {
      case FILE -> row.getBinary(name);
      case UUID -> row.getUuid(name);
      case UUID_ARRAY -> row.getUuidArray(name);
      case STRING, EMAIL, HYPERLINK -> row.getString(name);
      case STRING_ARRAY, EMAIL_ARRAY, HYPERLINK_ARRAY -> row.getStringArray(name);
      case BOOL -> row.getBoolean(name);
      case BOOL_ARRAY -> row.getBooleanArray(name);
      case INT -> row.getInteger(name);
      case INT_ARRAY -> row.getIntegerArray(name);
      case LONG -> row.getLong(name);
      case LONG_ARRAY -> row.getLongArray(name);
      case DECIMAL -> row.getDecimal(name);
      case DECIMAL_ARRAY -> row.getDecimalArray(name);
      case TEXT -> row.getText(name);
      case TEXT_ARRAY -> row.getTextArray(name);
      case DATE -> row.getDate(name);
      case DATE_ARRAY -> row.getDateArray(name);
      case DATETIME -> row.getDateTime(name);
      case DATETIME_ARRAY -> row.getDateTimeArray(name);
      case PERIOD -> row.getPeriod(name);
      case PERIOD_ARRAY -> row.getPeriodArray(name);
      case JSON -> row.getJsonb(name);
      default ->
          throw new UnsupportedOperationException(
              "Unsupported columnType found:" + c.getColumnType());
    };
  }

  static String getPsqlType(Column column) {
    return getPsqlType(column.getPrimitiveColumnType());
  }

  static String getPsqlType(ColumnType type) {
    return switch (type) {
      case STRING, EMAIL, HYPERLINK, TEXT -> "character varying";
      case STRING_ARRAY, EMAIL_ARRAY, HYPERLINK_ARRAY, TEXT_ARRAY -> "character varying[]";
      case UUID -> "uuid";
      case UUID_ARRAY -> "uuid[]";
      case BOOL -> "bool";
      case BOOL_ARRAY -> "bool[]";
      case INT -> "int";
      case INT_ARRAY -> "int[]";
      case LONG -> "bigint";
      case LONG_ARRAY -> "bigint[]";
      case DECIMAL -> "decimal";
      case DECIMAL_ARRAY -> "decimal[]";
      case DATE -> "date";
      case DATE_ARRAY -> "date[]";
      case DATETIME -> "timestamp without time zone";
      case DATETIME_ARRAY -> "timestamp without time zone[]";
      case JSON -> "jsonb";
      default ->
          throw new MolgenisException(
              "Unknown type: Internal error: data cannot be mapped to psqlType " + type);
    };
  }

  public static void checkValidation(Column column, Map<String, Object> values) {
    if (values.get(column.getName()) != null) {
      column.getColumnType().validate(values.get(column.getName()));
      // validation
      if (column.getValidation() != null) {
        // check if validation script contains js functions that are bound to java functions
        String errorMessage = checkValidation(column.getValidation(), values);
        if (errorMessage != null)
          throw new MolgenisException(
              "Validation error on column '" + column.getName() + "': " + errorMessage + ".");
      }
    }
  }

  private static void addJavaScriptBindings(List<Column> columns, Map<String, Object> values) {
    if (columns.isEmpty()) return;
    Column column = columns.get(0);
    if (column.getTable() == null) return;
    if (column.getSchema() == null) return;
    if (column.getSchema().getDatabase() == null) return;
    Map<String, Supplier<Object>> bindings =
        column.getSchema().getDatabase().getJavaScriptBindings();
    for (String key : bindings.keySet()) {
      values.put(key, bindings.get(key).get());
    }
  }

  public static String checkValidation(String validationScript, Map<String, Object> values) {
    try {
      Object error = executeJavascriptOnMap(validationScript, values);
      if (error != null) {
        if (error instanceof Boolean && (Boolean) error == false) {
          // you can have a validation rule that simply returns true or false;
          // false means not valid.
          return validationScript;
        } else if (error instanceof Boolean && (Boolean) error == true) {
          return null;
        }
        // you can have a validation script returning true which means valid, and undefined also
        else {
          return error.toString();
        }
      } else {
        return null;
      }
    } catch (MolgenisException me) {
      // seperate syntax errors
      throw me;
    }
  }

  public static String checkRequiredExpression(
      String validationScript, Map<String, Object> values) {
    try {
      Object error = executeJavascriptOnMap(validationScript, values);
      if (error instanceof Boolean) {
        if ((Boolean) error) return validationScript;
        return null;
      }
      if (error != null) return error.toString();
    } catch (MolgenisException me) {
      throw me;
    }
    return null;
  }

  static Map<String, Object> convertRowToMap(List<Column> columns, Row row) {
    Map<String, Object> map = new LinkedHashMap<>();
    for (Column c : columns) {
      if (c.isReference()) {
        map.put(c.getIdentifier(), getRefFromRow(row, c));
      } else if (c.isFile()) {
        // skip file
      } else {
        map.put(c.getIdentifier(), row.get(c));
      }
    }
    return map;
  }

  static Object getRefFromRow(Row row, Column c) {
    if (c.isRefArray() || c.isRefback()) {
      List<Map> result = new ArrayList<>();
      for (Reference ref : c.getReferences()) {
        if (!ref.isOverlapping()) {
          // must be a list
          if (row.getValueMap().get(ref.getName()) != null) {
            int i = 0;
            for (Object value : (Object[]) row.get(ref.getName(), ref.getPrimitiveType())) {
              if (i == result.size()) {
                result.add(new LinkedHashMap<>());
              }
              putMap(result.get(i), ref.getPath(), value);
              i++;
            }
          }
        }
      }
      return result;
    } else {
      // returns a value
      Map<String, Object> result = new LinkedHashMap<>();
      for (Reference ref : c.getReferences()) {
        if (!ref.isOverlapping()) {
          List<String> path = new ArrayList();
          path.add(c.getIdentifier());
          path.addAll(ref.getPath());
          putMap(result, ref.getPath(), row.get(ref.getName(), ref.getPrimitiveType()));
        }
      }
      return result;
    }
  }

  // put map hierarchy
  private static void putMap(Map<String, Object> result, List<String> path, Object value) {
    if (path.size() == 1) {
      result.put(path.get(0), value);
    } else {
      if (result.get(path.get(0)) == null) {
        result.put(path.get(0), new LinkedHashMap<>());
      }
      putMap((Map) result.get(path.get(0)), path.subList(1, path.size()), value);
    }
  }
}
