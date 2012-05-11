/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.ui.wicket.components.form.set;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.efaps.ui.wicket.models.objects.UIForm;

/**
 *  Class used to render a ajax link to remove a new field from the set.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxRemoveNew
    extends WebComponent
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * name of the related component.
     */
    private final String name;

    /**
     * count.
     */
    private final Integer count;

    /**
     * @param _wicketId wicket id forthis component
     * @param _name name for this component
     * @param _count count
     */
    public AjaxRemoveNew(final String _wicketId,
                         final String _name,
                         final Integer _count)
    {
        super(_wicketId);
        this.name = _name;
        this.count = _count;
        add(new AjaxRemoveNewBehavior());
    }

    /**
     * Get the JavaScript from the behavior.
     *
     * @return javascript
     */
    public String getJavaScript()
    {
        return ((AjaxRemoveNewBehavior) super.getBehaviors().get(0)).getJavaScript();
    }

    /**
     * Nothing must be rendered, because JavaScript is used.
     *
     * @param _markupStream MarkupStream
     */
    @Override
    protected void onRender(final MarkupStream _markupStream)
    {
        _markupStream.next();
    }

    /**
     * Behavior to remove a newly added line.
     */
    public class AjaxRemoveNewBehavior
        extends AjaxEventBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
     *
     */
        public AjaxRemoveNewBehavior()
        {
            super("onclick");
        }

        /**
         * @return the callbackScript
         */
        public String getJavaScript()
        {
            return super.getCallbackScript().toString().replace("'", "\\\'");
        }

        /**
         * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final UIForm formmodel = (UIForm) getComponent().getPage().getDefaultModelObject();
            final Map<String, String[]> newmap = formmodel.getNewValues();

            final String keyName = AjaxRemoveNew.this.name + "_eFapsNew";

            if (newmap.containsKey(keyName)) {
                final String[] oldvalues = newmap.get(keyName);
                final List<String> newvalues = new ArrayList<String>();
                for (final String oldValue : oldvalues) {
                    if (!oldValue.equals(AjaxRemoveNew.this.count.toString())) {
                        newvalues.add(oldValue);
                    }
                }
                newmap.put(keyName, newvalues.toArray(new String[newvalues.size()]));
            }

            final StringBuilder script = new StringBuilder();
            script.append("var thisNode = document.getElementById('").append(getComponent().getMarkupId())
                            .append("');").append("thisNode.parentNode.removeChild(thisNode);");

            _target.appendJavascript(script.toString());
        }

        /**
         * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getPreconditionScript()
         * @return null
         */
        @Override
        protected CharSequence getPreconditionScript()
        {
            return null;
        }
    }
}
