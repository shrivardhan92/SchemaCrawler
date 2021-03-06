/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2018, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.crawl;


import static java.util.Objects.requireNonNull;
import static sf.util.Utility.isBlank;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;

import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.JavaSqlType;
import schemacrawler.schema.Schema;
import schemacrawler.schema.SchemaReference;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.utility.TypeMap;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;

/**
 * Base class for retriever that uses database metadata to get the
 * details about the schema.
 *
 * @author Sualeh Fatehi
 */
abstract class AbstractRetriever
  implements Retriever
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(AbstractRetriever.class.getName());

  private final RetrieverConnection retrieverConnection;
  final MutableCatalog catalog;
  private final SchemaCrawlerOptions options;

  AbstractRetriever(final RetrieverConnection retrieverConnection,
                    final MutableCatalog catalog,
                    final SchemaCrawlerOptions options)
    throws SQLException
  {
    this.retrieverConnection = requireNonNull(retrieverConnection,
                                              "No retriever connection provided");
    this.catalog = catalog;
    this.options = requireNonNull(options, "No SchemaCrawler options provided");
  }

  /**
   * Checks whether the provided database object belongs to the
   * specified schema.
   *
   * @param dbObject
   *        Database object to check
   * @param catalogName
   *        Database catalog to check against
   * @param schemaName
   *        Database schema to check against
   * @return Whether the database object belongs to the specified schema
   */
  final boolean belongsToSchema(final DatabaseObject dbObject,
                                final String catalogName,
                                final String schemaName)
  {
    if (dbObject == null)
    {
      return false;
    }

    final boolean supportsCatalogs = retrieverConnection.isSupportsCatalogs();

    boolean belongsToCatalog = true;
    boolean belongsToSchema = true;
    if (supportsCatalogs)
    {
      final String dbObjectCatalogName = dbObject.getSchema().getCatalogName();
      if (catalogName != null && !catalogName.equals(dbObjectCatalogName))
      {
        belongsToCatalog = false;
      }
    }
    final String dbObjectSchemaName = dbObject.getSchema().getName();
    if (schemaName != null && !schemaName.equals(dbObjectSchemaName))
    {
      belongsToSchema = false;
    }
    return belongsToCatalog && belongsToSchema;
  }

  final NamedObjectList<SchemaReference> getAllSchemas()
  {
    return catalog.getAllSchemas();
  }

  final Connection getDatabaseConnection()
  {
    return retrieverConnection.getConnection();
  }

  final DatabaseMetaData getMetaData()
  {
    return retrieverConnection.getMetaData();
  }

  final RetrieverConnection getRetrieverConnection()
  {
    return retrieverConnection;
  }

  final InclusionRule getSchemaInclusionRule()
  {
    return options.getSchemaInclusionRule();
  }

  final void logPossiblyUnsupportedSQLFeature(final StringFormat message,
                                              final SQLException e)
  {
    // HYC00 = Optional feature not implemented
    // HY000 = General error
    // (HY000 is thrown by the Teradata JDBC driver for unsupported
    // functions)
    if ("HYC00".equalsIgnoreCase(e.getSQLState())
        || "HY000".equalsIgnoreCase(e.getSQLState()))
    {
      logSQLFeatureNotSupported(message, e);
    }
    else
    {
      LOGGER.log(Level.WARNING, message, e);
    }
  }

  final void logSQLFeatureNotSupported(final StringFormat message,
                                       final Throwable e)
  {
    LOGGER.log(Level.WARNING, message);
    LOGGER.log(Level.FINE, message, e);
  }

  /**
   * Creates a data type from the JDBC data type id, and the database
   * specific type name, if it does not exist.
   *
   * @param schema
   *        Schema
   * @param javaSqlType
   *        JDBC data type
   * @param databaseSpecificTypeName
   *        Database specific type name
   * @return Column data type
   */
  final MutableColumnDataType lookupOrCreateColumnDataType(final Schema schema,
                                                           final int javaSqlType,
                                                           final String databaseSpecificTypeName)
  {
    return lookupOrCreateColumnDataType(schema,
                                        javaSqlType,
                                        databaseSpecificTypeName,
                                        null);
  }

  /**
   * Creates a data type from the JDBC data type id, and the database
   * specific type name, if it does not exist.
   *
   * @param schema
   *        Schema
   * @param javaSqlTypeInt
   *        JDBC data type
   * @param databaseSpecificTypeName
   *        Database specific type name
   * @return Column data type
   */
  final MutableColumnDataType lookupOrCreateColumnDataType(final Schema schema,
                                                           final int javaSqlTypeInt,
                                                           final String databaseSpecificTypeName,
                                                           final String mappedClassName)
  {
    MutableColumnDataType columnDataType = catalog
      .lookupColumnDataType(schema, databaseSpecificTypeName).orElse(catalog
        .lookupSystemColumnDataType(databaseSpecificTypeName).orElse(null));
    // Create new data type, if needed
    if (columnDataType == null)
    {
      columnDataType = new MutableColumnDataType(schema,
                                                 databaseSpecificTypeName);
      final JavaSqlType javaSqlType = retrieverConnection.getJavaSqlTypes()
        .get(javaSqlTypeInt);
      columnDataType.setJavaSqlType(javaSqlType);
      if (isBlank(mappedClassName))
      {
        final TypeMap typeMap = retrieverConnection.getTypeMap();
        final Class<?> mappedClass;
        if (typeMap.containsKey(databaseSpecificTypeName))
        {
          mappedClass = typeMap.get(databaseSpecificTypeName);
        }
        else
        {
          mappedClass = typeMap.get(javaSqlType.getJavaSqlTypeName());
        }
        columnDataType.setTypeMappedClass(mappedClass);
      }
      else
      {
        columnDataType.setTypeMappedClass(mappedClassName);
      }

      catalog.addColumnDataType(columnDataType);
    }
    return columnDataType;
  }

  final Optional<MutableRoutine> lookupRoutine(final String catalogName,
                                               final String schemaName,
                                               final String routineName,
                                               final String specificName)
  {
    return catalog.lookupRoutine(Arrays
      .asList(catalogName, schemaName, routineName, specificName));
  }

  final Optional<MutableTable> lookupTable(final String catalogName,
                                           final String schemaName,
                                           final String tableName)
  {
    return catalog
      .lookupTable(Arrays.asList(catalogName, schemaName, tableName));
  }

  final String normalizeCatalogName(final String name)
  {
    if (retrieverConnection.isSupportsCatalogs())
    {
      return name;
    }
    else
    {
      return null;
    }
  }

  final String normalizeSchemaName(final String name)
  {
    if (retrieverConnection.isSupportsSchemas())
    {
      return name;
    }
    else
    {
      return null;
    }
  }

}
