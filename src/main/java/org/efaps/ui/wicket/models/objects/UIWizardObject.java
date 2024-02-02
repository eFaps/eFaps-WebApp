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
package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.util.io.IClusterable;

/**
 * Class is used to create wizard like behavior for a webpage. Meaning that it
 * is possible to go forward and backward in the pages without loosing
 * parameters, which were already given from the user.<br>
 * It contains a list that stores the UIObjects hyrachical so that they can be
 * accessed using methods like previous.
 *
 * @author The eFaps Team
 */
public class UIWizardObject
    implements IClusterable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Mapping between UIObject and parameters.
     */
    private final Map<IWizardElement, Map<String, String[]>> parameters
            = new HashMap<>();

    /**
     * List of UIObjects in this wizard.
     */
    private final List<IWizardElement> elements = new ArrayList<>();

    /**
     * Current UIObject.
     */
    private IWizardElement current = null;

    /**
     * Constructor setting the current UIObject.
     *
     * @param _element current UIOBject
     */
    public UIWizardObject(final IWizardElement _element)
    {
        this.elements.add(_element);
        this.current = _element;
        _element.setUIWizardObject(this);
    }

    /**
     * Add a UIOBject to the list of objects.
     *
     * @param _element Object to add
     */
    public void add(final IWizardElement _element)
    {
        this.elements.add(_element);
        _element.setUIWizardObject(this);
    }

    /**
     * Insert a UIObject before the current one.
     *
     * @param _element Object to insert
     */
    public void insertBefore(final IWizardElement _element)
    {
        int i = 0;
        for (final IWizardElement uiObject : this.elements) {
            if (uiObject == this.current) {
                this.elements.add(i, _element);
                break;
            }
            i++;
        }
    }

    /**
     * Method to get the previous object.
     *
     * @return previous object
     */
    public IWizardElement getPrevious()
    {
        IWizardElement ret = null;
        for (final IWizardElement uiObject : this.elements) {
            if (uiObject == this.current) {
                break;
            } else {
                ret = uiObject;
            }
        }
        this.current = ret;
        return ret;
    }

    /**
     * Add parameters.
     *
     * @param _element object used as key
     * @param _parameters parameters
     */
    public void addParameters(final IWizardElement _element,
                              final Map<String, String[]> _parameters)
    {
        this.parameters.put(_element, _parameters);
    }

    /**
     * Get a parameter map from the mapping.
     *
     * @param _element key fot the parameter map.
     * @return parameter map
     */
    public Map<String, String[]> getParameters(final IWizardElement _element)
    {
        return this.parameters.get(_element);
    }

    /**
     * Getter method for the instance variable {@link #elements}.
     *
     * @return value of instance variable {@link #elements}
     */
    public List<IWizardElement> getWizardElement()
    {
        return this.elements;
    }
}
