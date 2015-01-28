/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.ui.wicket.components.embeddedlink;

import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.export.GenericElementHtmlHandler;
import net.sf.jasperreports.engine.export.JRHtmlExporterContext;

import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.models.EmbeddedLink;

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
        final EmbeddedLink link = (EmbeddedLink) _element.getParameterValue(EmbeddedLink.JASPER_PARAMETERKEY);
        EFapsSession.get().addEmbededLink(link);

        final StringBuilder html = new StringBuilder();
        html.append("<div style=\"left:")
            .append(_element.getX()).append("px;top:").append(_element.getY())
            .append("px;width:").append(_element.getWidth()).append("px;height:")
            .append(_element.getWidth()).append("px;\">")
            .append(link.getTag())
            .append("</div>");
        return html.toString();
    }
}
