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

package org.efaps.ui.wicket.components.dashboard;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.models.EsjpInvoker;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EsjpComponent
    extends WebMarkupContainer
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EsjpComponent.class);


    /**
     * @param _id
     * @param _model
     */
    public EsjpComponent(final String _wicketId,
                         final IModel<EsjpInvoker> _model)
    {
        super(_wicketId, _model);
        add(new AbstractDojoBehavior()
        {

            private static final long serialVersionUID = 1L;
        });
    }

    @Override
    public boolean isVisible()
    {
        final EsjpInvoker invoker = (EsjpInvoker) getDefaultModelObject();
        return invoker.isVisible();
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        final EsjpInvoker invoker = (EsjpInvoker) getDefaultModelObject();
        try {
            replaceComponentTagBody(_markupStream, _openTag, invoker.getHtmlSnipplet());
        } catch (final EFapsException e) {
            EsjpComponent.LOG.error("EFapsException", e);
        }
    }
}
