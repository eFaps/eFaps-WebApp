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
package org.efaps.ui.wicket.components.gridx.filter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.Model;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.ui.field.Field;
import org.efaps.api.ui.IClassificationFilter;
import org.efaps.ui.wicket.util.DojoClass;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ClassificationFilter.
 */
public class ClassificationFilter
    extends WebComponent
{

    /** The Constant INPUTNAME. */
    public static final String INPUTNAME = "eFapsClassification";

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClassificationFilter.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new classification filter panel.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     */
    public ClassificationFilter(final String _wicketId,
                                final Model<IClassificationFilter> _model)
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream, final ComponentTag _openTag)
    {
        try {
            final Set<DojoClass> dojoClasses = new HashSet<>();
            Collections.addAll(dojoClasses, DojoClasses.Memory, DojoClasses.ObjectStoreModel, DojoClasses.Tree,
                            DojoClasses.CheckBox);
            final StringBuilder js = new StringBuilder()
                            .append("var classStore = new Memory({\n")
                            .append("data: [\n")
                            .append(getData())
                            .append("],\n")
                            .append("getChildren: function(object){\n")
                            .append(" return this.query({parent: object.id});\n")
                            .append("}\n")
                            .append("});\n")
                            .append("var classModel = new ObjectStoreModel({\n")
                            .append("store: classStore,\n")
                            .append(" query: {id: 'root'}\n")
                            .append("});\n")
                            .append("\n")
                            .append("var tree = new Tree({\n")
                            .append("model: classModel,\n")
                            .append("showRoot: false,\n")
                            .append("autoExpand: true,\n")
                            .append("_createTreeNode: ").append(getCreateTreeNodeScript())
                            .append(" });\n")
                            .append("\n")
                            .append("tree.placeAt('").append(getMarkupId(true)).append("');\n")
                            .append("tree.startup();\n");

            final StringBuilder html = new StringBuilder().append(JavaScriptUtils.SCRIPT_OPEN_TAG)
                            .append(DojoWrapper.require(js, dojoClasses.toArray(new DojoClass[dojoClasses.size()])))
                            .append("\n" + JavaScriptUtils.SCRIPT_CLOSE_TAG);

            replaceComponentTagBody(_markupStream, _openTag, html);
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
    }

    /**
     * Gets the creates the tree node script.
     *
     * @return the creates the tree node script
     */
    protected CharSequence getCreateTreeNodeScript() {
        final StringBuilder js = new StringBuilder();
        js.append("function(args) {\n")
        .append("var tn = new dijit._TreeNode(args);\n")
        .append("tn.labelNode.innerHTML = args.label;\n")
        .append("var cb = new CheckBox({\n")
        .append("name: '").append(INPUTNAME).append("',\n")
        .append("value: args.item.id,\n")
        .append("checked: args.item.selected,\n")
        .append("onChange: function(b){\n")
        .append("this.focusNode.checked=this.checked;\n")
        .append("\n")
        .append("}\n")
        .append("});\n")
        .append("cb.placeAt(tn.labelNode, 'first');\n")
        .append("tn.cb = cb;\n")
        .append("return tn;\n")
        .append("}\n");
        return js;
    }

    /**
     * Gets the data.
     *
     * @return the data
     * @throws EFapsException the e faps exception
     */
    protected CharSequence getData()
        throws EFapsException
    {
        final StringBuilder js = new StringBuilder();
        js.append("{ id: 'root', name: 'You cannot see this one.'}");
        final IClassificationFilter filter = (IClassificationFilter) getDefaultModelObject();
        final Field field = Field.get(filter.getFieldId());
        final String[] names = field.getClassificationName().split(";");
        for (final String className : names) {
            final Classification clazz = Classification.get(className);
            js.append(getDataLine(clazz, filter));
        }
        return js;
    }

    /**
     * Gets the data line.
     *
     * @param _clazz the clazz
     * @param _filter the filter
     * @return the data line
     * @throws EFapsException the e faps exception
     */
    protected CharSequence getDataLine(final Classification _clazz, final IClassificationFilter _filter)
        throws EFapsException
    {
        final StringBuilder js = new StringBuilder();
        if (_clazz.hasAccess(null, AccessTypeEnums.SHOW.getAccessType())) {
            js.append(",\n{id:'").append(_clazz.getUUID()).append("', name:'")
                .append(StringEscapeUtils.escapeEcmaScript(_clazz.getLabel())).append("', parent:'")
                .append(_clazz.isRoot() ? "root" : _clazz.getParentClassification().getUUID()).append("', selected:")
                .append(_filter.contains(_clazz.getUUID())).append("}");
            for (final Classification childClazz: _clazz.getChildClassifications()) {
                js.append(getDataLine(childClazz, _filter));
            }
        }
        return js;
    }
}
