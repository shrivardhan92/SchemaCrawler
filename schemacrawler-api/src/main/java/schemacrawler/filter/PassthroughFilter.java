package schemacrawler.filter;


import schemacrawler.schema.DatabaseObject;

class PassthroughFilter<D extends DatabaseObject>
  implements NamedObjectFilter<D>
{

  @Override
  public boolean include(final D databaseObject)
  {
    return true;
  }

}
