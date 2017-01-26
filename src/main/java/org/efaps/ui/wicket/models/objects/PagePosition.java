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


package org.efaps.ui.wicket.models.objects;


/**
 * The Enum PagePosition.
 *
 * @author The eFaps Team
 */
public enum PagePosition
{
    /** Page is in the main content area. */
    CONTENT,
    /** Page is in the modal belonging to the main content area. */
    CONTENTMODAL,
    /** Page is in an popup. */
    POPUP,
    /** Page is in the tree area. */
    TREE,
    /** Page is in the modal belonging to the tree area. */
    TREEMODAL,
    /** Page is opened as a modal in picker mode.*/
    PICKER;
}
