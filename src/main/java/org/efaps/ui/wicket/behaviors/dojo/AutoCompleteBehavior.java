/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.behaviors.dojo;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.page.XmlPartialPageUpdate;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.ILogData;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.efaps.api.ci.UIFormFieldProperty;
import org.efaps.api.ui.IOption;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.behaviors.SetSelectedRowBehavior;
import org.efaps.ui.wicket.models.field.AutoCompleteSettings;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.EFapsKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class AutoCompleteBehavior
    extends AbstractDojoBehavior
    implements IRequestListener
{
    /**
     * Reference to the stylesheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(AutoCompleteBehavior.class,
                    "AutoComplete.css");

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AutoCompleteBehavior.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** the component that this handler is bound to. */
    private Component component;

    /**
     * Settings for this AutoComplete.
     */
    private final AutoCompleteSettings settings;

    /**
     * The related fieldUpdate behavior.
     */
    private AjaxFieldUpdateBehavior fieldUpdate;

    /**
     * @param _settings settings for this behavior
     */
    public AutoCompleteBehavior(final AutoCompleteSettings _settings)
    {
        if (_settings == null) {
            this.settings = new AutoCompleteSettings();
        } else {
            this.settings = _settings;
        }
    }

    /**
     * Bind this handler to the given component.
     *
     * @param _hostComponent the component to bind to
     */
    @Override
    public final void bind(final Component _hostComponent)
    {
        Args.notNull(_hostComponent, "hostComponent");

        if (this.component != null) {
            throw new IllegalStateException("this kind of handler cannot be attached to "
                            + "multiple components; it is already attached to component " + this.component
                            + ", but component " + _hostComponent + " wants to be attached too");
        }
        this.component = _hostComponent;
        this.component.setOutputMarkupId(true);
    }

    @Override
    public final void unbind(final Component _component)
    {
        this.component = null;
        super.unbind(_component);
    }

    /**
     * Gets the component that this handler is bound to.
     *
     * @return the component that this handler is bound to
     */
    protected final Component getComponent()
    {
        return this.component;
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        // add wicket ajax to be sure that is is included
        CoreLibrariesContributor.contributeAjax(this.component.getApplication(), _response);

        final CharSequence ajaxBaseUrl = Strings.escapeMarkup(this.component.getRequestCycle().getUrlRenderer()
                        .getBaseUrl().toString());
        _response.render(JavaScriptHeaderItem.forScript("Wicket.Ajax.baseUrl=\"" + ajaxBaseUrl
                        + "\";", "wicket-ajax-base-url"));

        _response.render(AbstractEFapsHeaderItem.forCss(AutoCompleteBehavior.CSS));

        final String comboBoxId = "cb" + _component.getMarkupId();
        final StringBuilder js = new StringBuilder()
            .append("var ").append(comboBoxId);

        switch (this.settings.getAutoType()) {
            case SUGGESTION:
                js.append(" = new AutoSuggestion({");
                break;
            case TOKEN:
                js.append(" = new AutoTokenInput({");
                break;
            default:
                js.append(" = new AutoComplete({");
                break;
        }

        js.append("id:\"").append(_component.getMarkupId()).append("\",")
            .append("name:\"").append(this.settings.getFieldName()).append("\",")
            .append("placeHolder:ph,")
            .append("store: as,")
            .append("value: \"\",")
            .append("callbackUrl:\"" + getCallbackUrl() + "\",");

        switch (this.settings.getAutoType()) {
            case TOKEN:
                break;
            default:
                final String id = ((AutoCompleteField) _component).getItemValue();
                final String label = ((AutoCompleteField) _component).getItemLabel();
                // only if both value are valid it makes sence to add it
                if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(label)) {
                    js.append("item: { id:\"").append(id).append("\", name:\"").append(label)
                        .append("\", label:\"").append(label) .append("\"},");
                }
                break;
        }

        if (this.settings.getFieldConfiguration().hasProperty(UIFormFieldProperty.WIDTH.value())
                        && !this.settings.getFieldConfiguration().isTableField()) {
            js.append("style:\"width:").append(this.settings.getFieldConfiguration().getWidth()).append("\",");
        }
        if (!this.settings.isHasDownArrow()) {
            js.append("hasDownArrow:").append(this.settings.isHasDownArrow()).append(",");
        }
        if (this.settings.getMinInputLength() > 1) {
            js.append("minInputLength:").append(this.settings.getMinInputLength()).append(",");
        }
        if (this.settings.getSearchDelay() != 500) {
            js.append("searchDelay:").append(this.settings.getSearchDelay()).append(",");
        }

        if (!"p".equals(this.settings.getParamName())) {
            js.append("paramName:\"").append(this.settings.getParamName()).append("\",");
        }

        if (!this.settings.getExtraParameters().isEmpty()) {
            js.append("extraParameters:[");
            boolean first = true;
            for (final String ep : this.settings.getExtraParameters()) {
                if (first) {
                    first = false;
                } else {
                    js.append(",");
                }
                js.append("\"").append(ep).append("\"");
            }
            js.append("],");
        }
        if (Type.SUGGESTION.equals(this.settings.getAutoType())) {
            js.append("labelAttr: \"label\",");
        }

        js.append("searchAttr: \"name\"}, \"").append(_component.getMarkupId()).append("\");\n");

        if (this.settings.isRequired() && !Type.TOKEN.equals(this.settings.getAutoType())) {
            js.append("on(").append(comboBoxId).append(", 'change', function() {")
                 .append("var label=").append(comboBoxId).append(".item.label;")
                 .append("if (!(label === undefined || label === null)) {")
                 .append(comboBoxId).append(".item.name=label;")
                 .append(comboBoxId).append(".set(\"item\",").append(comboBoxId).append(".item);")
                 .append("}");
            if (this.fieldUpdate != null) {
                js.append(this.fieldUpdate.getCallbackScript4Dojo());
            }
            js.append("});\n");
        } else if (this.fieldUpdate != null) {
            js.append("on(").append(comboBoxId).append(", 'change', function() {")
                .append(this.fieldUpdate.getCallbackScript4Dojo())
                .append("});\n");
        }

        if (!_component.getBehaviors(SetSelectedRowBehavior.class).isEmpty()) {
            js.append("on(").append(comboBoxId).append(", 'focus', function() {")
                .append(_component.getBehaviors(
                                SetSelectedRowBehavior.class).get(0).getJavaScript("this.valueNode"))
                .append("});\n");
        }
        if (Type.TOKEN.equals(this.settings.getAutoType())) {
            final List<IOption> tokens = ((AutoCompleteField) _component).getTokens();
            for (final IOption token : tokens) {
                js.append(comboBoxId).append(".addToken(\"")
                    .append(StringEscapeUtils.escapeEcmaScript(token.getValue().toString())).append("\",\"")
                    .append(StringEscapeUtils.escapeEcmaScript(token.getLabel())).append("\");\n");
            }
        }
        _response.render(AutoCompleteHeaderItem.forScript(js.toString(), EnumSet.of(this.settings.getAutoType())));
    }

    @Override
    public void onRequest()
    {
        final WebApplication app = (WebApplication) getComponent().getApplication();
        final ACAjaxRequestTarget target = new ACAjaxRequestTarget(app.newAjaxRequestTarget(getComponent().getPage()));

        final RequestCycle requestCycle = RequestCycle.get();
        requestCycle.scheduleRequestHandlerAfterCurrent(target);

        final String val = requestCycle.getRequest()
                        .getRequestParameters()
                        .getParameterValue(this.settings.getParamName())
                        .toOptionalString();
        respond(target, val);
    }


    /**
     * @param _target   target
     * @param _val      value
     */
    protected void respond(final ACAjaxRequestTarget _target,
                           final String _val)
    {
        final AutoCompleteField field = (AutoCompleteField) getComponent();

        final Iterator<Map<String, String>> choices = field.getChoices(_val);

        try {
            final JSONArray jsonArray = new JSONArray();
            int i = 0;
            while (choices.hasNext() && (i < this.settings.getMaxResult() || this.settings.getMaxResult() == -1)) {
                final Map<String, String> map = choices.next();
                final String key = map.get(EFapsKey.AUTOCOMPLETE_KEY.getKey()) != null
                                ? map.get(EFapsKey.AUTOCOMPLETE_KEY.getKey())
                                : map.get(EFapsKey.AUTOCOMPLETE_VALUE.getKey());
                final String choice = map.get(EFapsKey.AUTOCOMPLETE_CHOICE.getKey()) != null
                                ? map.get(EFapsKey.AUTOCOMPLETE_CHOICE.getKey())
                                : map.get(EFapsKey.AUTOCOMPLETE_VALUE.getKey());
                final String value = map.get(EFapsKey.AUTOCOMPLETE_VALUE.getKey());

                final JSONObject object = new JSONObject();
                if (Type.SUGGESTION.equals(this.settings.getAutoType())) {
                    if (this.settings.getMaxChoiceLength() > 0 && choice.length() > this.settings
                                    .getMaxChoiceLength()) {
                        object.put("label", StringUtils.left(choice, this.settings.getMaxChoiceLength()) + "...");
                    } else {
                        object.put("label", choice);
                    }
                    if (this.settings.getMaxValueLength() > 0 && value.length() > this.settings.getMaxValueLength()) {
                        object.put("name", StringUtils.left(value, this.settings.getMaxChoiceLength()) + "...");
                    } else {
                        object.put("name", value);
                    }
                } else {
                    object.put("id", key);
                    if (this.settings.getMaxChoiceLength() > 0 && choice.length() > this.settings
                                    .getMaxChoiceLength()) {
                        object.put("name", StringUtils.left(choice, this.settings.getMaxChoiceLength()) + "...");
                    } else {
                        object.put("name", choice);
                    }
                    if (!choice.equals(value)) {
                        if (this.settings.getMaxValueLength() > 0 && value.length() > this.settings
                                        .getMaxValueLength()) {
                            object.put("label", StringUtils.left(value, this.settings.getMaxChoiceLength()) + "...");
                        } else {
                            object.put("label", value);
                        }
                    }
                }
                jsonArray.put(object);
                i++;
            }
            if (!(i < this.settings.getMaxResult() || this.settings.getMaxResult() == -1)) {
                final JSONObject object = new JSONObject();
                object.put("id", "MAXRESULT");
                object.put("name", "...");
                object.put("label", "...");
                jsonArray.put(object);
            }
            _target.appendJSON(jsonArray.toString());
        } catch (final JSONException e) {
            AutoCompleteBehavior.LOG.error("Catched JSONException", e);
        }
    }

    /**
     * Gets the url that references this handler.
     *
     * @return the url that references this handler
     */
    public CharSequence getCallbackUrl()
    {
        if (getComponent() == null) {
            throw new IllegalArgumentException("Behavior must be bound to a component to create the URL");
        }
        return getComponent().urlForListener(this, new PageParameters());
    }

    /**
     * @param _fieldUpdate fieldUpdate to add
     */
    public void addFieldUpdate(final AjaxFieldUpdateBehavior _fieldUpdate)
    {
        this.fieldUpdate = _fieldUpdate;
    }

    /**
     * Getter method for the instance variable {@link #settings}.
     *
     * @return value of instance variable {@link #settings}
     */
    public AutoCompleteSettings getSettings()
    {
        return this.settings;
    }

    /**
     * Config types.
     */
    public enum Type
    {
        /** Render AutomComplete.*/
        COMPLETE,
        /** Render AutoSuggestion. */
        SUGGESTION,
        /** Render AutoTokenInput. */
        TOKEN;
    }

    /**
     * Interface the component using the behavior must implement.
     */
    public interface AutoCompleteField
    {
        /**
         * @param _input the choices
         * @return new Iteratro
         */
        Iterator<Map<String, String>> getChoices(String _input);

        /**
         * @return the value for the current item
         */
        String getItemValue();

        /**
         * @return the lable for the current item
         */
        String getItemLabel();

        /**
         * @return list of tokens
         */
        List<IOption> getTokens();
    }

    /**
     * Request target wrapping the orginal target.
     */
    public static class ACAjaxRequestTarget
        implements AjaxRequestTarget
    {

        /**
         * Original target.
         */
        private final AjaxRequestTarget target;

        /**
         * The added json string.
         */
        private String jsonString;

        /**
         * @param _target target to be wrapped
         */
        public ACAjaxRequestTarget(final AjaxRequestTarget _target)
        {
            this.target = _target;
        }

        /**
         * @param _json json to be appended
         */
        public void appendJSON(final String _json)
        {
            this.jsonString = _json;
        }

        @Override
        public Integer getPageId()
        {
            return this.target.getPageId();
        }

        @Override
        public boolean isPageInstanceCreated()
        {
            return this.target.isPageInstanceCreated();
        }

        @Override
        public Integer getRenderCount()
        {
            return this.target.getRenderCount();
        }

        @Override
        public Class<? extends IRequestablePage> getPageClass()
        {
            return this.target.getPageClass();
        }

        @Override
        public PageParameters getPageParameters()
        {
            return this.target.getPageParameters();
        }

        @Override
        public void respond(final IRequestCycle _requestCycle)
        {
            final WebResponse response = (WebResponse) _requestCycle.getResponse();

            this.target.respond(_requestCycle);
            response.write("<json><![CDATA[");
            response.write(this.jsonString);
            response.write("]]></json>");
            response.write(XmlPartialPageUpdate.END_ROOT_ELEMENT);
        }

        @Override
        public void detach(final IRequestCycle _requestCycle)
        {
            this.target.detach(_requestCycle);

        }

        @Override
        public ILogData getLogData()
        {

            return this.target.getLogData();
        }

        @Override
        public void add(final Component _component,
                        final String _markupId)
        {
            this.target.add(_component, _markupId);

        }

        @Override
        public void add(final Component... _components)
        {
            this.target.add(_components);
        }

        @Override
        public void addChildren(final MarkupContainer _parent,
                                final Class<?> _childCriteria)
        {
            this.target.addChildren(_parent, _childCriteria);

        }

        @Override
        public void addListener(final IListener _listener)
        {
            this.target.addListener(_listener);

        }

        @Override
        public void appendJavaScript(final CharSequence _javascript)
        {
            this.target.appendJavaScript(_javascript);
        }

        @Override
        public void prependJavaScript(final CharSequence _javascript)
        {
            this.target.prependJavaScript(_javascript);

        }

        @Override
        public void registerRespondListener(final ITargetRespondListener _listener)
        {
            this.target.registerRespondListener(_listener);

        }

        @Override
        public Collection<? extends Component> getComponents()
        {
            return this.target.getComponents();
        }

        @Override
        public void focusComponent(final Component _component)
        {
            this.target.focusComponent(_component);
        }

        @Override
        public IHeaderResponse getHeaderResponse()
        {
            return this.target.getHeaderResponse();
        }

        @Override
        public String getLastFocusedElementId()
        {
            return this.target.getLastFocusedElementId();
        }

        @Override
        public Page getPage()
        {
            return this.target.getPage();
        }
    }
}
