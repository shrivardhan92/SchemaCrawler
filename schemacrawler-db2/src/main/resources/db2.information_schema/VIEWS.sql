SELECT
  NULLIF(1, 1)
    AS TABLE_CATALOG,
  STRIP(SYSCAT.VIEWS.VIEWSCHEMA)
    AS TABLE_SCHEMA,
  STRIP(SYSCAT.VIEWS.VIEWNAME)
    AS TABLE_NAME,
  SYSCAT.VIEWS.TEXT
    AS VIEW_DEFINITION,
  CASE WHEN STRIP(SYSCAT.VIEWS.VIEWCHECK) = 'N' THEN 'NONE' ELSE 'CASCADED' END
    AS CHECK_OPTION,
  CASE WHEN STRIP(SYSCAT.VIEWS.READONLY) = 'Y' THEN 'NO' ELSE 'YES' END
    AS IS_UPDATABLE
FROM
  SYSCAT.VIEWS
ORDER BY
  SYSCAT.VIEWS.VIEWSCHEMA,
  SYSCAT.VIEWS.VIEWNAME,
  SYSCAT.VIEWS.SEQNO
WITH UR  