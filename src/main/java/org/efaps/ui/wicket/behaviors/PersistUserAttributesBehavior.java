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
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.util.string.StringValue;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PersistUserAttributesBehavior.
 */
public class PersistUserAttributesBehavior
    extends AbstractDefaultAjaxBehavior
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PersistUserAttributesBehavior.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        final StringBuilder js = new StringBuilder().append("var eFaps = eFaps || {};\n")
                        .append("eFaps.persistUserAttr = function(_key, _value){\n")
                        .append(getCallbackFunctionBody(CallbackParameter.explicit("_key"),
                                        CallbackParameter.explicit("_value")))
                        .append("};");

        _response.render(JavaScriptHeaderItem.forScript(js, PersistUserAttributesBehavior.class.getSimpleName()));
    }

    @Override
    protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
    {
        super.updateAjaxAttributes(_attributes);
        _attributes.setMethod(Method.POST);
    }

    @Override
    protected void respond(final AjaxRequestTarget _target)
    {
        try {
            final StringValue key = getComponent().getRequest().getRequestParameters().getParameterValue("_key");
            final StringValue value = getComponent().getRequest().getRequestParameters().getParameterValue("_value");
            if (!key.isEmpty() && !value.isEmpty()) {
                Context.getThreadContext().setUserAttribute(key.toString(), value.toString());
            }
        } catch (final EFapsException e) {
            LOG.error("Catched error", e);
        }
    }
}
