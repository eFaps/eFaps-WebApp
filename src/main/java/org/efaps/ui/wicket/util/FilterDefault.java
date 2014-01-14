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
 * Revision:        $Rev: 8890 $
 * Last Changed:    $Date: 2013-02-19 19:54:03 -0500 (mar, 19 feb 2013) $
 * Last Changed By: $Author: jan@moxter.net $
 */

package org.efaps.ui.wicket.util;
/**
 * Enum is used to validate the filter default for a field in a table.
 *
 * @author The eFaps Team
 * @version $Id: EFapsKey.java 8890 2013-02-20 00:54:03Z jan@moxter.net $
 */
public enum FilterDefault
{
    /** the filter default is for today. */
    TODAY,
    /** the filter default is for the last week */
    WEEK,
    /** the filter default is for the last month. */
    MONTH,
    /** the filter default is for the last year. */
    YEAR,
    /** the filter default is for all the existing elements. */
    ALL,
    /** the filter default is for nothing of the existing elements. */
    NONE;
}
