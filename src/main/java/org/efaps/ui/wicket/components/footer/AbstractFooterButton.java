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
package org.efaps.ui.wicket.components.footer;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * The Class AbstractFooterButton.
 *
 * @author The eFaps Team
 * @param <T> the generic type
 */
public abstract class AbstractFooterButton<T>
    extends AjaxButton<T>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new abstract footer button.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @param _reference the reference
     * @param _label the label
     */
    public AbstractFooterButton(final String _wicketId,
                                final IModel<T> _model,
                                final EFapsContentReference _reference,
                                final String _label)
    {
        super(_wicketId, _model, _reference, _label);
    }

    @Override
    public Form<?> getForm()
    {
        return getPage().visitChildren(FormContainer.class, new IVisitor<Component, FormContainer>()
        {

            @Override
            public void component(final Component _component,
                                  final IVisit<FormContainer> _visit)
            {
                _visit.stop((FormContainer) _component);
            }
        });
    }
}
