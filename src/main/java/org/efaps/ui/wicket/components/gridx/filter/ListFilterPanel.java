/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.ui.wicket.components.gridx.filter;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.api.ui.IListFilter;
import org.efaps.api.ui.IOption;

/**
 * The Class StatusFilterPanel.
 *
 * @author The eFaps Team
 */
public class ListFilterPanel
    extends GenericPanel<IListFilter>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new list filter panel.
     *
     * @param _wicketid the wicketid
     * @param _model the model
     */
    public ListFilterPanel(final String _wicketid,
                           final IModel<IListFilter> _model)
    {
        super(_wicketid, _model);
        final List<IOption> selected = _model.getObject().stream().filter(new Predicate<IOption>()
        {
            @Override
            public boolean test(final IOption _option)
            {
                return _option.isSelected();
            }
        }).collect(Collectors.toList());

        @SuppressWarnings({ "rawtypes", "unchecked" })
        final CheckBoxMultipleChoice<IOption> checkBoxes = new CheckBoxMultipleChoice("checkBoxes", Model.of(selected),
                        (List) getModelObject(), new ChoiceRenderer());
        add(checkBoxes);
    }

    /**
     * The renderer for this checkbox.
     */
    public final class ChoiceRenderer
        implements IChoiceRenderer<IOption>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(final IOption _option)
        {
            return _option.getLabel();
        }

        @Override
        public String getIdValue(final IOption _object,
                                 final int _index)
        {
            return String.valueOf(_index);
        }

        @Override
        public IOption getObject(final String _id,
                                 final IModel<? extends List<? extends IOption>> _choices)
        {
            final List<?> choices = _choices.getObject();
            final IOption ret = (IOption) choices.get(Integer.valueOf(_id));
            return ret;
        }
    }
}
