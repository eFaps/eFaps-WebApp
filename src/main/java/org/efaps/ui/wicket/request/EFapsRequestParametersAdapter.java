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

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsRequestParametersAdapter
    implements IWritableRequestParameters
{

    private final Map<String, List<StringValue>> parameters = new HashMap<String, List<StringValue>>();

    /**
     * @param _queryParameters
     * @param _postParameters
     */
    public EFapsRequestParametersAdapter(final IRequestParameters... _parameters)
    {
        for (final IRequestParameters p : _parameters) {
            for (final String name : p.getParameterNames()) {
                this.parameters.put(name, p.getParameterValues(name));
            }
        }
    }

    @Override
    public Set<String> getParameterNames()
    {
        return Collections.unmodifiableSet(this.parameters.keySet());
    }

    @Override
    public StringValue getParameterValue(final String _name)
    {
        final List<StringValue> values = this.parameters.get(_name);
        return (values != null && !values.isEmpty()) ? values.get(0)
                        : StringValue.valueOf((String) null);
    }

    @Override
    public List<StringValue> getParameterValues(final String _name)
    {
        final List<StringValue> values = this.parameters.get(_name);
        return values != null ? Collections.unmodifiableList(values) : null;
    }

    @Override
    public void setParameterValues(final String _name,
                                   final List<StringValue> _value)
    {
        this.parameters.put(_name, _value);
        try {
            Context.getThreadContext().getParameters().put(_name, ParameterUtil.stringValues2Array(_value));
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Sets value for given key.
     *
     * @param _name
     * @param _value
     */
    public void setParameterValue(final String _name,
                                  final String _value)
    {
        final List<StringValue> list = new ArrayList<StringValue>(1);
        list.add(StringValue.valueOf(_value));
        setParameterValues(_name, list);
    }

    /**
     * Adds value for given key.
     *
     * @param _name
     * @param _value
     */
    public void addParameterValue(final String _name,
                                  final String _value)
    {
        List<StringValue> list = this.parameters.get(_name);
        if (list == null)
        {
            list = new ArrayList<StringValue>(1);
            this.parameters.put(_name, list);
        }
        list.add(StringValue.valueOf(_value));
        try {
            Context.getThreadContext().getParameters().put(_name, ParameterUtil.stringValues2Array(list));
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void reset()
    {
        this.parameters.clear();
    }
}
