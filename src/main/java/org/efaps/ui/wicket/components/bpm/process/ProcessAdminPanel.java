/*
 * Copyright 2003 - 2013 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.bpm.process;

import org.apache.wicket.PageReference;
import org.apache.wicket.markup.html.panel.Panel;
import org.efaps.ui.wicket.components.bpm.AbstractSortableProvider;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: ProcessAdminPanel.java 9946 2013-08-02 22:04:49Z jan@moxter.net
 *          $
 */
public class ProcessAdminPanel
    extends Panel
{

    /**
     * Reference to the style sheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(AbstractSortableProvider.class,
                    "BPM.css");

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _id
     */
    public ProcessAdminPanel(final String _id,
                             final PageReference _pageReference)
        throws EFapsException
    {
        super(_id);
        final ProcessTablePanel taskTable = new ProcessTablePanel("processTable", _pageReference,
                        new ProcessInstanceProvider());
        add(taskTable);
    }

}
