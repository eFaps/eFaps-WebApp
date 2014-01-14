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


package org.efaps.ui.wicket.components.tabs;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;


/**
 * Extending TabbedPanel to add full screen indicator.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxIndicatingTabbedPanel
    extends AjaxTabbedPanel<ITab>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicketId of the component
     * @param _tabs tabs to be added
     */
    public AjaxIndicatingTabbedPanel(final String _wicketId,
                                     final List<ITab> _tabs)
    {
        super(_wicketId, _tabs);
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel#newLink(java.lang.String, int)
     */
    @Override
    protected WebMarkupContainer newLink(final String _linkId,
                                         final int _index)
    {
        return new IndicatingAjaxLink(_linkId)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget _target)
            {
                setSelectedTab(_index);
                if (_target != null) {
                    _target.add(AjaxIndicatingTabbedPanel.this);
                }
                onAjaxUpdate(_target);
            }
        };
    }
}
