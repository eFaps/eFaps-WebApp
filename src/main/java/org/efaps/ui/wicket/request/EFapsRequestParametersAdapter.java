/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.ui.wicket.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.IWritableRequestParameters;
import org.apache.wicket.util.string.StringValue;
import org.efaps.db.Context;
import org.efaps.ui.wicket.util.ParameterUtil;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsRequestParametersAdapter
    implements IWritableRequestParameters
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsRequestParametersAdapter.class);

    /**
     * Mapping of the parameters.
     */
    private final Map<String, List<StringValue>> parameters = new HashMap<String, List<StringValue>>();

    /**
     * @param _parameters paramters to work on
     */
    public EFapsRequestParametersAdapter(final IRequestParameters... _parameters)
    {
        for (final IRequestParameters p : _parameters) {
            for (final String name : p.getParameterNames()) {
                final List<StringValue> values = p.getParameterValues(name);
                this.parameters.put(name, values);
                EFapsRequestParametersAdapter.LOG.trace("adding parameter from request. Name: '{}', value: {}", name,
                                values);
            }
        }
    }

    /**
     * Returns immutable set of all available parameter names.
     * @return list of parameter names
     */
    @Override
    public Set<String> getParameterNames()
    {
        return Collections.unmodifiableSet(this.parameters.keySet());
    }

    /**
     * Returns single value for parameter with specified name. This method always returns non-null
     * result even if the parameter does not exist.
     *
     * @param _name parameter name
     * @return {@link StringValue} wrapping the actual value
     */
    @Override
    public StringValue getParameterValue(final String _name)
    {
        final List<StringValue> values = this.parameters.get(_name);
        return (values != null && !values.isEmpty()) ? values.get(0)
                        : StringValue.valueOf((String) null);
    }

    /**
     * Returns list of values for parameter with specified name. If the parameter does not exist
     * this method returns <code>null</code>
     *
     * @param _name parameter name
     * @return list of all values for given parameter or <code>null</code> if parameter does not
     *         exist
     */
    @Override
    public List<StringValue> getParameterValues(final String _name)
    {
        final List<StringValue> values = this.parameters.get(_name);
        return values != null ? Collections.unmodifiableList(values) : null;
    }

    /**
     * Sets the values for given parameter.
     *
     * @param _name parameter name
     * @param _value values
     */
    @Override
    public void setParameterValues(final String _name,
                                   final List<StringValue> _value)
    {
        this.parameters.put(_name, _value);
        try {
            Context.getThreadContext().getParameters().put(_name, ParameterUtil.stringValues2Array(_value));
        } catch (final EFapsException e) {
            EFapsRequestParametersAdapter.LOG.error("Could not set parameter '{}' in Context.", _name);
        }
    }

    /**
     * Sets value for given key.
     *
     * @param _key key for the value
     * @param _value value
     */
    public void setParameterValue(final String _key,
                                  final String _value)
    {
        final List<StringValue> list = new ArrayList<StringValue>(1);
        list.add(StringValue.valueOf(_value));
        setParameterValues(_key, list);
    }

    /**
     * Adds a value for given key.
     *
     * @param _key key for the value
     * @param _value value
     */
    public void addParameterValue(final String _key,
                                  final String _value)
    {
        List<StringValue> list = this.parameters.get(_key);
        if (list == null) {
            list = new ArrayList<StringValue>(1);
            this.parameters.put(_key, list);
        }
        list.add(StringValue.valueOf(_value));
        try {
            Context.getThreadContext().getParameters().put(_key, ParameterUtil.stringValues2Array(list));
        } catch (final EFapsException e) {
            EFapsRequestParametersAdapter.LOG.error("Could not add parameter '{}' in Context.", _key);
        }
    }

    /**
     * Clears all parameters.
     */
    @Override
    public void reset()
    {
        this.parameters.clear();
    }
}
