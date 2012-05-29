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

package org.efaps.ui.wicket.components.datatable;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.data.IDataProvider;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Table<T>
    extends DataTable<T>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _id
     * @param _columns
     * @param _dataProvider
     * @param _rowsPerPage
     */
    public Table(final String _id,
                 final List<IColumn<T>> _columns,
                 final IDataProvider<T> _dataProvider,
                 final long _rowsPerPage)
    {
        super(_id, _columns, _dataProvider, _rowsPerPage);
    }

}
