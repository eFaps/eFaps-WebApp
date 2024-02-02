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
package org.efaps.ui.wicket.components.menu.ajax;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.efaps.ui.wicket.behaviors.dojo.OnDojoReadyHeaderItem;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class AbstractSubmitBehavior
    extends AjaxFormSubmitBehavior
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new abstract submit behavior.
     *
     * @param _event the event
     */
    public AbstractSubmitBehavior(final String _event)
    {
        super(_event);
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        if (_component.isEnabledInHierarchy()) {
            final CharSequence js = getCallbackScript(_component);
            final Optional<AjaxRequestTarget> optional = _component.getRequestCycle().find(AjaxRequestTarget.class);
            if (optional.isPresent()) {
                optional.get().appendJavaScript(js);
            } else {
                _response.render(OnDojoReadyHeaderItem.forScript(js.toString()));
            }
        }
    }

    /**
     * Finds form that will be submitted.
     *
     * @return form to submit or {@code null} if none found
     */
    @Override
    protected Form<?> findForm()
    {
        return ((AbstractContentPage) getComponent().getPage()).getForm();
    }

    /**
     * On error nothing is done.
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onError(final AjaxRequestTarget _target)
    {
        // nothing
    }

}
