/*
 * Copyright 2003 - 2011 The eFaps Team
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


package org.efaps.ui.wicket.behaviors.dojo;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class RequireBehavior
    extends AbstractDojoBehavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;
    private final String[] packages;



    public RequireBehavior(final String... _packages)
    {
        this.packages = _packages;
    }
    /**
     * Render the links for the head.
     *
     * @param _component component the header will be rendered for
     * @param _response resonse to add
     */
    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        final StringBuilder js = new StringBuilder()
            .append("require([");
        for (int i = 0; i < this.packages.length; i++) {
            if (i > 0) {
                js.append(",");
            }
            js.append("\"").append(this.packages[i]).append("\"");
        }
        js.append("]);");
        _response.render(JavaScriptHeaderItem.forScript(js, _component.getId()));
    }
}
