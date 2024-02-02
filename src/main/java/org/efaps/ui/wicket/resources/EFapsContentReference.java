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
package org.efaps.ui.wicket.resources;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.io.IClusterable;

/**
 * This class is used as a Reference to an Object in the eFaps-DataBase.<br>
 *
 * @author The eFaps Team
 */
public class EFapsContentReference
    implements IClusterable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * the name of this EFapsContentReference. With this name the Object from
     * the eFpas-DataBase will be identified.
     */
    private final String name;

    /**
     * Constructor setting the name of the EFapsContentReference, from combining
     * the name of the class and the name.
     *
     * @param _scope class the name will be included
     * @param _name name of the Object
     */
    public EFapsContentReference(final Class<?> _scope,
                                 final String _name)
    {
        this(_scope.getPackage().getName() + "." + _name);
    }

    /**
     * Constructor setting the name of the EFapsContentReference.
     *
     * @param _name Name to set
     */
    public EFapsContentReference(final String _name)
    {
        this.name = _name;
    }

    /**
     * This is the getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * get the URL to an Image from the eFaps-DataBase.
     *
     * @return URL as a String
     */
    public String getImageUrl()
    {
        return EFapsContentReference.getImageURL(this.name);
    }

    /**
     * get the URL to a Static Content from the eFaps-DataBase.
     *
     * @return URL as a String
     */
    public String getStaticContentUrl()
    {
        return EFapsContentReference.getContentURL(this.name);
    }

    /**
     * Gets the image URL.
     *
     * @param _name the name
     * @return the image URL
     */
    public static String getImageURL(final String _name)
    {
        return RequestCycle.get().getUrlRenderer().renderContextRelativeUrl("servlet/image/" + _name);
    }

    /**
     * Gets the content URL.
     *
     * @param _name the name
     * @return the content URL
     */
    public static String getContentURL(final String _name)
    {
        return RequestCycle.get().getUrlRenderer().renderContextRelativeUrl("servlet/static/" + _name);
    }
}
