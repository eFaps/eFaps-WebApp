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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.index.ISearch;
import org.efaps.admin.index.Index;
import org.efaps.admin.index.SearchConfig;
import org.efaps.admin.index.Searcher;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.json.index.SearchResult;
import org.efaps.json.index.result.DimValue;
import org.efaps.json.index.result.Dimension;
import org.efaps.json.index.result.Element;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class IndexSearch.
 *
 * @author The eFaps Team
 */
public class IndexSearch
    implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IndexSearch.class);

    /** The base query (The Query as set by the User). */
    private String currentQuery = "";
    /** The base query (The Query as set by the User). */
    private String previousQuery = "";

    /** The dimensions. */
    private List<Dimension> dimensions;

    /** The search. */
    private ISearch search;

    /** The result. */
    private SearchResult result;

    /** The dimension provider. */
    private DimensionProvider dimensionProvider;

    /** The element data provider. */
    private ElementDataProvider elementDataProvider;

    /** The previous search name. */
    private String previousSearchName;

    /**
     * Gets the query.
     *
     * @return the query
     */
    protected String getQuery()
    {
        final StringBuilder ret = new StringBuilder();
        try {
            final String clazzname;
            if (EFapsSystemConfiguration.get().containsAttributeValue("org.efaps.kernel.index.QueryBuilder")) {
                clazzname = EFapsSystemConfiguration.get().getAttributeValue("org.efaps.kernel.index.QueryBuilder");
            } else {
                clazzname = "org.efaps.esjp.admin.index.LucenceQueryBuilder";
            }
            final Class<?> clazz = Class.forName(clazzname, false, EFapsClassLoader.getInstance());
            final Object obj = clazz.newInstance();
            final Method method = clazz.getMethod("getQuery4DimValues", String.class, List.class, List.class);
            final Object newQuery = method.invoke(obj, getCurrentQuery(), getIncluded(), getExcluded());
            ret.append(newQuery);
        } catch (final EFapsException | ClassNotFoundException | InstantiationException | IllegalAccessException
                        | NoSuchMethodException | SecurityException | IllegalArgumentException
                        | InvocationTargetException e) {
            IndexSearch.LOG.error("Catched", e);
            ret.append(getCurrentQuery());
        }
        return ret.toString();
    }

    /**
     * Search.
     */
    public void search()
    {
        try {
            this.previousSearchName = this.search == null ? null : this.search.getName();
            this.search = Index.getSearch();
            this.search.setQuery(getQuery());
            this.result = Searcher.search(this.search);
            if (StringUtils.isNotEmpty(getCurrentQuery()) && !getCurrentQuery().equals(getPreviousQuery())
                            && dimFilterApplied(null)) {
                final ISearch tmpSearch = new ISearch()
                {

                    /** The Constant serialVersionUID. */
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void setQuery(final String _query)
                    {
                    }

                    @Override
                    public String getQuery()
                    {
                        return getCurrentQuery();
                    }

                    @Override
                    public int getNumHits()
                    {
                        return 1;
                    }

                    @Override
                    public List<SearchConfig> getConfigs()
                        throws EFapsException
                    {
                        final List<SearchConfig> ret = new ArrayList<>();
                        ret.add(SearchConfig.ACTIVATE_DIMENSION);
                        return ret;
                    };
                };
                final SearchResult tmpResult = Searcher.search(tmpSearch);
                this.dimensions = tmpResult.getDimensions();
            } else {
                this.dimensions = this.result.getDimensions();
            }
            getDataProvider().setElements(this.result.getElements());
            fillDimensionProvider(!getCurrentQuery().equals(getPreviousQuery()));
        } catch (final EFapsException e) {
            IndexSearch.LOG.error("Catched", e);
        }
    }

    /**
     * Gets the excluded.
     *
     * @return the excluded
     */
    private List<DimValue> getExcluded()
    {
        final List<DimValue> ret = new ArrayList<>();
        for (final DimTreeNode node : getDimensionProvider().getRootList()) {
            ret.addAll(node.getExcluded());
        }
        return ret;
    }

    /**
     * Gets the included.
     *
     * @return the included
     */
    private List<DimValue> getIncluded()
    {
        final List<DimValue> ret = new ArrayList<>();
        for (final DimTreeNode node : getDimensionProvider().getRootList()) {
            ret.addAll(node.getIncluded());
        }
        return ret;
    }

    /**
     * Check if a dimension filter applied.
     *
     * @param _nodes the nodes
     * @return true, if successful
     */
    private boolean dimFilterApplied(final List<DimTreeNode> _nodes)
    {
        final List<DimTreeNode> nodes = _nodes == null ? getDimensionProvider().getRootList() : _nodes;

        boolean ret = false;
        for (final DimTreeNode node : nodes) {
            if (node.getStatus() != null) {
                ret = true;
                break;
            }
            ret = dimFilterApplied(node.getChildren());
            if (ret) {
                break;
            }
        }
        return ret;
    }

    /**
     * Fill dimension provider.
     *
     * @param _updateDim the update dim
     */
    private void fillDimensionProvider(final boolean _updateDim)
    {
        final DimensionProvider provider = getDimensionProvider();
        final Iterator<? extends DimTreeNode> currentIter = provider.getRoots();

        final List<Dimension> dims = getDimensions();
        if (_updateDim && dims.isEmpty()) {
            provider.getRootList().clear();
        } else {
            Collections.sort(dims, new Comparator<Dimension>()
            {

                @Override
                public int compare(final Dimension _arg0,
                                   final Dimension _arg1)
                {
                    final String dim0 = DBProperties.getProperty(DimensionPanel.class.getName() + "." + _arg0.getKey());
                    final String dim1 = DBProperties.getProperty(DimensionPanel.class.getName() + "." + _arg1.getKey());
                    return dim0.compareTo(dim1);
                }
            });

            final Iterator<Dimension> newDimsIter = dims.iterator();
            while (currentIter.hasNext()) {
                final DimTreeNode current = currentIter.next();
                if (newDimsIter.hasNext()) {
                    final Dimension newDim = newDimsIter.next();
                    if (!newDim.getKey().equals(((Dimension) current.getValue()).getKey())) {
                        currentIter.remove();
                    } else if (_updateDim) {
                        current.update(newDim.getValues());
                    }
                }
            }
            // add new ones
            while (newDimsIter.hasNext()) {
                final DimTreeNode node = new DimTreeNode().setValue(newDimsIter.next());
                provider.getRootList().add(node);
            }
        }
    }

    /**
     * Gets the search.
     *
     * @return the search
     */
    public ISearch getSearch()
    {
        return this.search;
    }

    /**
     * Gets the result.
     *
     * @return the result
     */
    public SearchResult getResult()
    {
        return this.result;
    }

    /**
     * Gets the data provider.
     *
     * @return the data provider
     */
    public ElementDataProvider getDataProvider()
    {
        if (this.elementDataProvider == null) {
            this.elementDataProvider = new ElementDataProvider();
        }
        return this.elementDataProvider;
    }

    /**
     * Gets the dimension provider.
     *
     * @return the dimension provider
     */
    public DimensionProvider getDimensionProvider()
    {
        if (this.dimensionProvider == null) {
            this.dimensionProvider = new DimensionProvider();
        }
        return this.dimensionProvider;
    }

    /**
     * Gets the base query (The Query as set by the User).
     *
     * @return the base query (The Query as set by the User)
     */
    public String getCurrentQuery()
    {
        return this.currentQuery;
    }

    /**
     * Sets the base query (The Query as set by the User).
     *
     * @param _currentQuery the new base query (The Query as set by the User)
     */
    public void setCurrentQuery(final String _currentQuery)
    {
        this.previousQuery = this.currentQuery;
        this.currentQuery = _currentQuery;
    }

    /**
     * Gets the base query (The Query as set by the User).
     *
     * @return the base query (The Query as set by the User)
     */
    public String getPreviousQuery()
    {
        return this.previousQuery;
    }

    /**
     * Sets the base query (The Query as set by the User).
     *
     * @param _previousQuery the new base query (The Query as set by the User)
     */
    public void setPreviousQuery(final String _previousQuery)
    {
        this.previousQuery = _previousQuery;
    }

    /**
     * Gets the dimensions.
     *
     * @return the dimensions
     */
    public List<Dimension> getDimensions()
    {
        return this.dimensions;
    }

    /**
     * Checks if is update table.
     *
     * @return true, if is update table
     */
    public boolean isUpdateTable()
    {
        return !getSearch().getName().equals(this.previousSearchName);
    }

    /**
     * The Class ElementDataProvider.
     *
     * @author The eFaps Team
     */
    public static class ElementDataProvider
        extends ListDataProvider<Element>
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The elements. */
        private List<Element> elements = new ArrayList<>();

        @Override
        protected List<Element> getData()
        {
            return this.elements;
        }

        /**
         * Gets the elements.
         *
         * @return the elements
         */
        public List<Element> getElements()
        {
            return this.elements;
        }

        /**
         * Sets the elements.
         *
         * @param _elements the new elements
         */
        public void setElements(final List<Element> _elements)
        {
            this.elements = _elements;
        }
    }

    /**
     * The Class DimensionProvider.
     */
    public static class DimensionProvider
        implements ITreeProvider<DimTreeNode>
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The roots. */
        private final List<DimTreeNode> roots = new ArrayList<>();

        @Override
        public void detach()
        {
        }

        @Override
        public Iterator<? extends DimTreeNode> getRoots()
        {
            return this.roots.iterator();
        }

        /**
         * Gets the dimensions.
         *
         * @return the dimensions
         */
        public List<DimTreeNode> getRootList()
        {
            return this.roots;
        }

        @Override
        public boolean hasChildren(final DimTreeNode _node)
        {
            return !_node.getChildren().isEmpty();
        }

        @Override
        public Iterator<? extends DimTreeNode> getChildren(final DimTreeNode _node)
        {
            return _node.getChildren().iterator();
        }

        @Override
        public IModel<DimTreeNode> model(final DimTreeNode _object)
        {
            return Model.<DimTreeNode>of(_object);
        }
    }

    /**
     * The Class TreeValue.
     *
     */
    public static class DimTreeNode
        implements Serializable
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The value. */
        private Object value;

        /** The status. */
        private Boolean status;

        /** The children. */
        private List<DimTreeNode> children = new ArrayList<>();

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
         * Update.
         *
         * @param _set the set
         */
        public void update(final Set<DimValue> _set)
        {
            final List<DimValue> values = new ArrayList<>(_set);
            Collections.sort(values, new Comparator<DimValue>()
            {

                @Override
                public int compare(final DimValue _o1,
                                   final DimValue _o2)
                {
                    return _o1.getLabel().compareTo(_o2.getLabel());
                }
            });
            final List<DimTreeNode> newChildren = new ArrayList<>();
            for (final DimValue val : values) {
                final DimTreeNode existing = getChild(val);
                final DimTreeNode newNode = new DimTreeNode().setValue(val).setStatus(existing == null ? null
                                : existing.getStatus());
                newChildren.add(newNode);
                if (existing != null) {
                    existing.update(val.getChildren());
                    newNode.setChildren(existing.getChildren());
                }
            }
            setChildren(newChildren);
        }

        /**
         * Gets the child.
         *
         * @param _val the val
         * @return the child
         */
        private DimTreeNode getChild(final DimValue _val)
        {
            DimTreeNode ret = null;
            for (final DimTreeNode node : this.children) {
                if (node.getLabel().equals(_val.getLabel())) {
                    ret = node;
                    break;
                }
            }
            return ret;
        }

        /**
         * Gets the excluded.
         *
         * @return the excluded
         */
        public List<DimValue> getExcluded()
        {
            final List<DimValue> ret = new ArrayList<>();
            if (BooleanUtils.isFalse(this.status)) {
                ret.add((DimValue) this.value);
            }
            for (final DimTreeNode child : getChildren()) {
                ret.addAll(child.getExcluded());
            }
            return ret;
        }

        /**
         * Gets the included.
         *
         * @return the included
         */
        public List<DimValue> getIncluded()
        {
            final List<DimValue> ret = new ArrayList<>();
            if (BooleanUtils.isTrue(this.status)) {
                ret.add((DimValue) this.value);
            }
            for (final DimTreeNode child : getChildren()) {
                ret.addAll(child.getIncluded());
            }
            return ret;
        }

        /**
         * Sets the value.
         *
         * @param _value the new value
         * @return the value wrapper
         */
        public DimTreeNode setValue(final Object _value)
        {
            this.value = _value;
            if (_value instanceof Dimension) {
                final List<DimValue> values = new ArrayList<>(((Dimension) _value).getValues());
                Collections.sort(values, new Comparator<DimValue>()
                {

                    @Override
                    public int compare(final DimValue _o1,
                                       final DimValue _o2)
                    {
                        return _o1.getLabel().compareTo(_o2.getLabel());
                    }
                });
                for (final DimValue dimvalue : values) {
                    this.children.add(new DimTreeNode().setValue(dimvalue));
                }
            } else {
                final List<DimValue> values = new ArrayList<>(((DimValue) _value).getChildren());
                Collections.sort(values, new Comparator<DimValue>()
                {

                    @Override
                    public int compare(final DimValue _o1,
                                       final DimValue _o2)
                    {
                        return _o1.getLabel().compareTo(_o2.getLabel());
                    }
                });
                for (final DimValue dimvalue : values) {
                    this.children.add(new DimTreeNode().setValue(dimvalue));
                }
            }
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
            if (_obj instanceof DimTreeNode) {
                ret = getValue().equals(((DimTreeNode) _obj).getValue());
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

        /**
         * Gets the status.
         *
         * @return the status
         */
        public Boolean getStatus()
        {
            return this.status;
        }

        /**
         * Sets the status.
         *
         * @param _status the status
         * @return the dim tree node
         */
        public DimTreeNode setStatus(final Boolean _status)
        {
            this.status = _status;
            return this;
        }

        /**
         * Gets the children.
         *
         * @return the children
         */
        public List<DimTreeNode> getChildren()
        {
            return this.children;
        }

        /**
         * Sets the children.
         *
         * @param _children the new children
         */
        public void setChildren(final List<DimTreeNode> _children)
        {
            this.children = _children;
        }
    }

}
