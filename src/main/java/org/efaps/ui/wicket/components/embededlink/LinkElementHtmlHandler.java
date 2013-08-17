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

package org.efaps.ui.wicket.components.embededlink;

import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.GenericElementHtmlHandler;
import net.sf.jasperreports.engine.export.JRHtmlExporterContext;

import org.apache.commons.lang3.RandomStringUtils;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.models.LinkObject;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LinkElementHtmlHandler
    implements GenericElementHtmlHandler
{

    @Override
    public boolean toExport(final JRGenericPrintElement _element)
    {
        return true;
    }

    @Override
    public String getHtmlFragment(final JRHtmlExporterContext _exporterContext,
                                  final JRGenericPrintElement _element)
    {
        final LinkObject link = (LinkObject) _element.getParameterValue(LinkObject.PARAMETERKEY);
        final String id = RandomStringUtils.randomAlphanumeric(8);
        System.out.println(link);
        EFapsSession.get().addLinkElement(id);
        return "<span id=\"" + id + "\">hallo<span>";
    }
}
