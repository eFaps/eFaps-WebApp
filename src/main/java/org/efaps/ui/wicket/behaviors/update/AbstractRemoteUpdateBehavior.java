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
package org.efaps.ui.wicket.behaviors.update;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class AbstractRemoteUpdateBehavior
    extends AbstractDefaultAjaxBehavior
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Mapping from key to listener.
     */
    private final Map<String, IRemoteUpdateListener> key2listener = new HashMap<>();

    /**
     * Name of the function.
     */
    private final String funtionName;

    /**
     * @param _funtionName name of the function
     */
    public AbstractRemoteUpdateBehavior(final String _funtionName)
    {
        this.funtionName = _funtionName;
    }

    /**
     * @param _listener listener that will be registered
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
        return "var " + getFuntionName() + "="
                        + getCallbackFunction(CallbackParameter.explicit(IRemoteUpdateListener.PARAMETERKEY));
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        if (_component.isEnabledInHierarchy()) {
            final CharSequence js = getCallbackScript(_component);

            final Optional<AjaxRequestTarget> optional = _component.getRequestCycle().find(AjaxRequestTarget.class);
            if (optional.isPresent()) {
                optional.get().appendJavaScript(js);
            } else {
                _response.render(JavaScriptHeaderItem.forScript(js.toString(), getClass().getName()));
            }
        }
    }
}
