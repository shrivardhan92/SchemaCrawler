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
package schemacrawler.tools.integration.scripting;


import java.util.Arrays;
import java.util.Collection;

import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.executable.CommandProvider;
import schemacrawler.tools.executable.Executable;
import schemacrawler.tools.options.OutputOptions;

public class ScriptCommandProvider
  implements CommandProvider
{

  @Deprecated
  @Override
  public Executable configureNewExecutable(final SchemaCrawlerOptions schemaCrawlerOptions,
                                           final OutputOptions outputOptions)
    throws SchemaCrawlerException
  {
    throw new RuntimeException("Accessing deprecated method");
  }

  @Override
  public Executable configureNewExecutable(final String command,
                                           final SchemaCrawlerOptions schemaCrawlerOptions,
                                           final OutputOptions outputOptions)
  {
    final ScriptExecutable executable = new ScriptExecutable();
    executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
    executable.setOutputOptions(outputOptions);
    return executable;
  }

  @Deprecated
  @Override
  public String getCommand()
  {
    throw new RuntimeException("Accessing deprecated method");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getHelpAdditionalText()
  {
    return "";
  }

  @Override
  public String getHelpResource()
  {
    return "/help/ScriptExecutable.txt";
  }

  @Override
  public Collection<String> getSupportedCommands()
  {
    return Arrays.asList(ScriptExecutable.COMMAND);
  }

  @Override
  public boolean supportsCommand(final String command,
                                 final SchemaCrawlerOptions schemaCrawlerOptions,
                                 final OutputOptions outputOptions)
  {
    return ScriptExecutable.COMMAND.equals(command);
  }

}
