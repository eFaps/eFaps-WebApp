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

package org.efaps.ui.wicket.behaviors.update;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AbstractRemoteUpdateBehavior
    extends AbstractDefaultAjaxBehavior
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Map<String, IRemoteUpdateListener> key2listener = new HashMap<String, IRemoteUpdateListener>();

    private final String funtionName;

    public AbstractRemoteUpdateBehavior(final String _funtionName)
    {
        this.funtionName = _funtionName;
    }

    /**
     * @param _listener
     */
    public void register(final IRemoteUpdateListener _listener)
    {
        this.key2listener.put(_listener.getKey(), _listener);
    }

    /**
     * Getter method for the instance variable {@link #funtionName}.
     *
     * @return value of instance variable {@link #funtionName}
     */
    public String getFuntionName()
    {
        return this.funtionName;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(org.apache
     * .wicket.ajax.AjaxRequestTarget)
     */
    @Override
    protected void respond(final AjaxRequestTarget _target)
    {
        final RequestCycle requestCycle = RequestCycle.get();
        final StringValue key = requestCycle.getRequest().getRequestParameters()
                        .getParameterValue(IRemoteUpdateListener.PARAMETERKEY);
        if (this.key2listener.containsKey(key.toString())) {
            this.key2listener.get(key.toString()).onEvent(getComponent(), _target);
        }
    }

    @Override
    protected CharSequence getCallbackScript(final Component _component)
    {
        return "var " + getFuntionName() + "=" + getCallbackFunction(IRemoteUpdateListener.PARAMETERKEY);
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);

        if (_component.isEnabledInHierarchy()) {
            final CharSequence js = getCallbackScript(_component);

            final AjaxRequestTarget target = _component.getRequestCycle().find(AjaxRequestTarget.class);
            if (target == null) {
                _response.render(JavaScriptHeaderItem.forScript(js.toString(), getClass().getName()));
            } else {
                target.appendJavaScript(js);
            }
        }
    }

}
