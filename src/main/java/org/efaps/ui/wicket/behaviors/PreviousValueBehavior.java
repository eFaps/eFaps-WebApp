/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.behaviors.dojo.OnDojoReadyHeaderItem;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;

/**
 * The Class PreviousValueBehavior.
 * Used to add the previous value from a dropdown to the parameters.
 * to be remove with the removal of .
 *
 * @author The eFaps Team
 */
public class PreviousValueBehavior
    extends AbstractDojoBehavior
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The check4select. */
    private boolean check4Select = false;

    /**
     * Checks if is check4select.
     *
     * @return the check4select
     */
    public boolean isCheck4Select()
    {
        return this.check4Select;
    }

    /**
     * Sets the check4 select.
     *
     * @param _check4Select the check4 select
     * @return the previous value behavior
     */
    public PreviousValueBehavior setCheck4Select(final boolean _check4Select)
    {
        this.check4Select = _check4Select;
        return this;
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        if (_component.getDefaultModelObject() instanceof String
                        && ((String)_component.getDefaultModelObject()).startsWith("<select")) {
            super.renderHead(_component, _response);
            _response.render(OnDojoReadyHeaderItem.forScript(getJavaScript(_component.getMarkupId(true))));
        }
    }

    /**
     * Gets the java script.
     *
     * @param _markupId the markup id
     * @return the java script
     */
    protected CharSequence getJavaScript(final String _markupId)
    {
        final StringBuilder ret = new StringBuilder()
            .append("if (\"SELECT\" == dom.byId('").append(_markupId).append("').tagName) {")
            .append("on(dom.byId('").append(_markupId).append("'), \"focus\", function(){")
            .append("if (!dojo.byId('").append(_markupId).append("_previous')) {")
            .append("domConstruct.place(\"<input name='\" + domAttr.get('")
                .append(_markupId).append("', 'name') + \"_eFapsPrevious' type='hidden' id='")
                .append(_markupId).append("_previous'>\", '").append(_markupId).append("'); ")
            .append("}")
            .append(" dojo.byId('").append(_markupId).append("_previous').value = this.value;")
            .append("});")
            .append("}")
            .append("});");
        return DojoWrapper.require(ret, DojoClasses.on, DojoClasses.dom, DojoClasses.domConstruct, DojoClasses.domAttr);
    }
}
