/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.ui.wicket.components.footer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

/**
 * Link using Ajax to close the ModalWindow the FooterPanel was opened in.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxCancelLink
    extends AjaxLink<Object>
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketid ic of the component
     */
    public AjaxCancelLink(final String _wicketid)
    {
        super(_wicketid);
    }

    @Override
    public void onClick(final AjaxRequestTarget _target)
    {
        final FooterPanel footer = this.findParent(FooterPanel.class);
        footer.getModalWindow().setReloadChild(false);
        footer.getModalWindow().close(_target);
    }
}
