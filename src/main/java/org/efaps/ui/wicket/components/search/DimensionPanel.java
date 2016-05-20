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

package org.efaps.ui.wicket.components.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.json.index.SearchResult;
import org.efaps.json.index.result.DimValue;
import org.efaps.json.index.result.Dimension;
import org.efaps.ui.wicket.components.tree.StructurBrowserTree;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class DimensionPanel
    extends Panel
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new taxonomy panel.
     *
     * @param _wicketId the _wicket id
     * @param _model the model
     */
    public DimensionPanel(final String _wicketId,
                          final IModel<SearchResult> _model)
    {
        super(_wicketId, _model);
        final DimensionProvider provider = new DimensionProvider(_model.getObject());
        final NestedTree<ValueWrapper> dimTree = new NestedTree<ValueWrapper>("dimTree", provider)
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            protected Component newContentComponent(final String _wicketId,
                                                    final IModel<ValueWrapper> _model)
            {
                return new Label(_wicketId, _model.getObject().getLabel());
            }
        };
        add(dimTree);
        dimTree.getModelObject().addAll(provider.getDimensions());
        dimTree.add(new HumanTheme());
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(StructurBrowserTree.CSS));
    }

    /**
     * The Class TreeValue.
     *
     */
    public static class ValueWrapper
        implements Serializable
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The value. */
        private  Object value;

        /**
         * Gets the value.
         *
         * @return the value
         */
        public Object getValue()
        {
            return this.value;
        }

        /**
         * Sets the value.
         *
         * @param _value the new value
         * @return the value wrapper
         */
        public ValueWrapper setValue(final Object _value)
        {
            this.value = _value;
            return this;
        }

        /**
         * Gets the label.
         *
         * @return the label
         */
        public String getLabel()
        {
            final String ret;
            if (this.value instanceof Dimension) {
                ret = DBProperties.getProperty(DimensionPanel.class.getName() + "." + ((Dimension) this.value)
                                .getKey());
            } else {
                ret = ((DimValue) this.value).getLabel();
            }
            return ret;
        }

        @Override
        public boolean equals(final Object _obj)
        {
            final boolean ret;
            if (_obj instanceof ValueWrapper) {
                ret = getValue().equals(((ValueWrapper) _obj).getValue());
            } else {
                ret = super.equals(_obj);
            }
            return ret;
        }

        @Override
        public int hashCode()
        {
            return getValue().hashCode();
        }
    }

    /**
     * The Class DimensionProvider.
     */
    public static class DimensionProvider
        implements ITreeProvider<ValueWrapper>
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The result. */
        private final SearchResult result;

        /**
         * Instantiates a new dimension provider.
         *
         * @param _result the result
         */
        public DimensionProvider(final SearchResult _result)
        {
            this.result = _result;
        }

        @Override
        public void detach()
        {
        }

        @Override
        public Iterator<? extends ValueWrapper> getRoots()
        {
            return getDimensions().iterator();
        }

        /**
         * Gets the dimensions.
         *
         * @return the dimensions
         */
        public List<ValueWrapper> getDimensions()
        {
            final List<ValueWrapper> ret = new ArrayList<>();
            if (this.result != null) {
                for (final Dimension dim : this.result.getDimensions()) {
                    ret.add(new ValueWrapper().setValue(dim));
                }
            }
            Collections.sort(ret, new Comparator<ValueWrapper>()
            {
                @Override
                public int compare(final ValueWrapper _o1,
                                   final ValueWrapper _o2)
                {
                    return _o1.getLabel().compareTo(_o2.getLabel());
                }
            });
            return ret;
        }

        @Override
        public boolean hasChildren(final ValueWrapper _node)
        {
            boolean ret = false;
            if (_node.getValue() instanceof Dimension) {
                ret = true;
            } else {
                ret = !((DimValue) _node.getValue()).getChildren().isEmpty();
            }
            return ret;
        }

        @Override
        public Iterator<? extends ValueWrapper> getChildren(final ValueWrapper _node)
        {
            final List<ValueWrapper> ret = new ArrayList<>();

            final Set<DimValue> values;
            if (_node.getValue() instanceof Dimension) {
                values = ((Dimension) _node.getValue()).getValues();
            } else {
                values = ((DimValue) _node.getValue()).getChildren();
            }
            for (final DimValue dimValue : values) {
                ret.add(new ValueWrapper().setValue(dimValue));
            }
            Collections.sort(ret, new Comparator<ValueWrapper>()
            {
                @Override
                public int compare(final ValueWrapper _o1,
                                   final ValueWrapper _o2)
                {
                    return _o1.getLabel().compareTo(_o2.getLabel());
                }
            });
            return ret.iterator();
        }

        @Override
        public IModel<ValueWrapper> model(final ValueWrapper _object)
        {
            return Model.<ValueWrapper>of(_object);
        }
    }
}
