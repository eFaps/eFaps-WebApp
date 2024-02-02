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
package org.efaps.ui.wicket.models.field.validators;

import org.apache.wicket.Component;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.ui.wicket.components.values.AbstractField;
import org.efaps.ui.wicket.components.values.IUIField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @param <T> object
 */
public class StandartValidator<T>
    implements IValidator<T>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractField.class);

    /**
     * Component this validator belongs to.
     */
    private final Component component;

    /**
     * Instantiates a new validator.
     *
     * @param _component the component
     */
    public StandartValidator(final Component _component)
    {
        this.component = _component;
    }

    @Override
    public void validate(final IValidatable<T> _validatable)
    {
        if (this.component instanceof IUIField) {
            try {
                final AbstractUIField uiField = ((IUIField) this.component).getUIField();
                final UIValue uiValue = UIValue.get(uiField.getValue().getField(), uiField.getValue().getAttribute(),
                                _validatable.getValue());
                final String msg = uiField.getValue().getUIProvider().validateValue(uiValue);
                if (msg != null) {
                    _validatable.error(new ValidationError(msg));
                }
            } catch (final CacheReloadException e) {
                StandartValidator.LOG.error("Catched error on validation", e);
            } catch (final EFapsException e) {
                StandartValidator.LOG.error("Catched error on validation", e);
            }
        }
    }
}
