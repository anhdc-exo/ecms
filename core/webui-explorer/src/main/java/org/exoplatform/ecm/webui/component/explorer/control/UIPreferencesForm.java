/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ecm.webui.component.explorer.control;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.ecm.jcr.model.Preference;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorerPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Chien Nguyen
 *          chien.nguyen@exoplatform.org
 * July 28, 2010
 * 14:07:15 PM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/groovy/webui/component/explorer/UIPreferencesForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIPreferencesForm.SaveActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPreferencesForm.AdvanceActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIPreferencesForm.BackActionListener.class)
    })
public class UIPreferencesForm extends UIForm implements UIPopupComponent {

  final static public String FIELD_ENABLESTRUCTURE  = "enableStructure".intern();

  final static public String FIELD_SHOWSIDEBAR      = "showSideBar".intern();

  final static public String FIELD_SHOWNONDOCUMENT  = "showNonDocument".intern();

  final static public String FIELD_SHOWREFDOCUMENTS = "showRefDocuments".intern();

  final static public String FIELD_SHOW_HIDDEN_NODE = "showHiddenNode".intern();
  
  final static public String FIELD_SHOW_ITEMS_BY_USER = "showItemsByUserInTimeline".intern();
  
  final static public String FIELD_ENABLE_DRAG_AND_DROP = "enableDragAndDrop".intern();

  final static public String FIELD_SHORTBY          = "sortBy".intern();

  final static public String FIELD_ORDERBY          = "order".intern();

  final static public String FIELD_PROPERTY         = "property".intern();

  final static public String NODES_PER_PAGE         = "nodesPerPage".intern();
  
  final static public String FIELD_QUERY_TYPE       = "queryType".intern();
  
  private boolean advancePreferences = false;

  public UIPreferencesForm() throws Exception {
    RequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    String sortByNodeName;
    String sortByNodeType;
    String sortByCreatedDate;
    String sortByModifiedDate;
    String ascendingOrder;
    String descendingOrder;
    String SQLQuery;
    String XPathQuery;    
    try {
      sortByNodeName = res.getString("UIPreferencesForm.label." + Preference.SORT_BY_NODENAME);
      sortByNodeType = res.getString("UIPreferencesForm.label." + Preference.SORT_BY_NODETYPE);
      sortByCreatedDate = res.getString("UIPreferencesForm.label." + Preference.SORT_BY_CREATED_DATE);
      sortByModifiedDate = res.getString("UIPreferencesForm.label." + Preference.SORT_BY_MODIFIED_DATE);
      ascendingOrder = res.getString("UIPreferencesForm.label." + Preference.ASCENDING_ORDER);
      descendingOrder = res.getString("UIPreferencesForm.label." + Preference.DESCENDING_ORDER);
      SQLQuery = res.getString("UIPreferencesForm.label." + Preference.SQL_QUERY);
      XPathQuery = res.getString("UIPreferencesForm.label." + Preference.XPATH_QUERY);
    } catch (Exception e) {
      sortByNodeName = Preference.SORT_BY_NODENAME;
      sortByNodeType = Preference.SORT_BY_NODETYPE;
      sortByCreatedDate = Preference.SORT_BY_CREATED_DATE;
      sortByModifiedDate = Preference.SORT_BY_MODIFIED_DATE;
      ascendingOrder = Preference.ASCENDING_ORDER;
      descendingOrder = Preference.DESCENDING_ORDER;
      SQLQuery = Preference.SQL_QUERY;
      XPathQuery = Preference.XPATH_QUERY;
    }    
    List<SelectItemOption<String>> sortOptions = new ArrayList<SelectItemOption<String>>();        
    sortOptions.add(new SelectItemOption<String>(sortByNodeName, Preference.SORT_BY_NODENAME));
    sortOptions.add(new SelectItemOption<String>(sortByNodeType, Preference.SORT_BY_NODETYPE));
    sortOptions.add(new SelectItemOption<String>(sortByCreatedDate, Preference.SORT_BY_CREATED_DATE));
    sortOptions.add(new SelectItemOption<String>(sortByModifiedDate, Preference.SORT_BY_MODIFIED_DATE));
    
    List<SelectItemOption<String>> orderOption = new ArrayList<SelectItemOption<String>>();
    orderOption.add(new SelectItemOption<String>(ascendingOrder, Preference.ASCENDING_ORDER));
    orderOption.add(new SelectItemOption<String>(descendingOrder, Preference.DESCENDING_ORDER));
    
    List<SelectItemOption<String>> nodesPerPagesOptions = new ArrayList<SelectItemOption<String>>();
    nodesPerPagesOptions.add(new SelectItemOption<String>("5", "5"));
    nodesPerPagesOptions.add(new SelectItemOption<String>("10", "10"));
    nodesPerPagesOptions.add(new SelectItemOption<String>("15", "15"));
    nodesPerPagesOptions.add(new SelectItemOption<String>("20", "20"));
    nodesPerPagesOptions.add(new SelectItemOption<String>("30", "30"));
    nodesPerPagesOptions.add(new SelectItemOption<String>("40", "40"));
    nodesPerPagesOptions.add(new SelectItemOption<String>("50", "50"));
    
    List<SelectItemOption<String>> queryOption = new ArrayList<SelectItemOption<String>>();
    queryOption.add(new SelectItemOption<String>(SQLQuery, Preference.SQL_QUERY));
    queryOption.add(new SelectItemOption<String>(XPathQuery, Preference.XPATH_QUERY));

    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ENABLESTRUCTURE, FIELD_ENABLESTRUCTURE,
        null));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHOWSIDEBAR, FIELD_SHOWSIDEBAR, null));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHOWNONDOCUMENT, FIELD_SHOWNONDOCUMENT,
        null));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHOWREFDOCUMENTS, FIELD_SHOWREFDOCUMENTS,
        null));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHOW_HIDDEN_NODE, FIELD_SHOW_HIDDEN_NODE,
        null));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHOW_ITEMS_BY_USER, 
                                                    FIELD_SHOW_ITEMS_BY_USER, null));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ENABLE_DRAG_AND_DROP,
                                                    FIELD_ENABLE_DRAG_AND_DROP, null));
        
    addUIFormInput(new UIFormSelectBox(FIELD_QUERY_TYPE, FIELD_QUERY_TYPE, queryOption));
    addUIFormInput(new UIFormSelectBox(FIELD_SHORTBY, FIELD_SHORTBY, sortOptions));
    addUIFormInput(new UIFormSelectBox(FIELD_ORDERBY, FIELD_ORDERBY, orderOption));
    addUIFormInput(new UIFormSelectBox(NODES_PER_PAGE, NODES_PER_PAGE, nodesPerPagesOptions));
  }
  
  public boolean isAdvancePreferences() { 
    return advancePreferences; 
  }
  
  public void setAdvancePreferences(boolean adPreferences) { 
    advancePreferences = adPreferences; 
  }
  
  public void begin() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    String b = context.getURLBuilder().createURL(this, null, null);   
    
    Writer writer = context.getWriter();
    writer.append("<form class=\"").append(getId()).append("\" id=\"").append(getId()).append("\" action=\"").append(b).append('\"');
    if(getSubmitAction() != null) writer.append(" onsubmit=\"").append(getSubmitAction()).append("\"");
    if(isMultipart()) writer.append(" enctype=\"multipart/form-data\"");
    writer.append(" method=\"post\">");
    writer.append("<div><input type=\"hidden\" name=\"").append(ACTION).append("\" value=\"\"/></div>");
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void update(Preference pref) {
    getUIFormCheckBoxInput(FIELD_ENABLESTRUCTURE).setChecked(pref.isJcrEnable());
    UIFormCheckBoxInput<Boolean> showSideBar = getUIFormCheckBoxInput(FIELD_SHOWSIDEBAR);
    showSideBar.setChecked(pref.isShowSideBar() );
   	showSideBar.setEnable(this.getAncestorOfType(UIJCRExplorerPortlet.class).isShowSideBar());
   	
    getUIFormCheckBoxInput(FIELD_SHOWNONDOCUMENT).setChecked(pref.isShowNonDocumentType());
    getUIFormCheckBoxInput(FIELD_SHOWREFDOCUMENTS).setChecked(pref.isShowPreferenceDocuments());
    getUIFormCheckBoxInput(FIELD_SHOW_HIDDEN_NODE).setChecked(pref.isShowHiddenNode());
    getUIFormCheckBoxInput(FIELD_SHOW_ITEMS_BY_USER).setChecked(pref.isShowItemsByUser());
    getUIFormCheckBoxInput(FIELD_ENABLE_DRAG_AND_DROP).setChecked(pref.isEnableDragAndDrop());
    getUIFormSelectBox(FIELD_SHORTBY).setValue(pref.getSortType());
    getUIFormSelectBox(FIELD_ORDERBY).setValue(pref.getOrder());
    getUIFormSelectBox(NODES_PER_PAGE).setValue(Integer.toString(pref.getNodesPerPage()));
    getUIFormSelectBox(FIELD_QUERY_TYPE).setValue(pref.getQueryType());
  }
  
  private Cookie createNewCookie(String cookieName, String cookieValue) {
    String userId = Util.getPortalRequestContext().getRemoteUser();
    cookieName += userId; 
    return new Cookie(cookieName, cookieValue);
  }
  
  private void savePreferenceInCookies() {
    HttpServletResponse response = Util.getPortalRequestContext().getResponse();
    if (getUIFormCheckBoxInput(FIELD_ENABLESTRUCTURE).isChecked())
      response.addCookie(createNewCookie(Preference.PREFERENCE_ENABLESTRUCTURE, "true"));
    else
      response.addCookie(createNewCookie(Preference.PREFERENCE_ENABLESTRUCTURE, "false"));
    if (getUIFormCheckBoxInput(FIELD_SHOWSIDEBAR).isChecked())
      response.addCookie(createNewCookie(Preference.PREFERENCE_SHOWSIDEBAR, "true"));
    else
      response.addCookie(createNewCookie(Preference.PREFERENCE_SHOWSIDEBAR, "false"));
    if (getUIFormCheckBoxInput(FIELD_SHOWNONDOCUMENT).isChecked()) 
      response.addCookie(createNewCookie(Preference.SHOW_NON_DOCUMENTTYPE, "true"));
    else
      response.addCookie(createNewCookie(Preference.SHOW_NON_DOCUMENTTYPE, "false"));
    if (getUIFormCheckBoxInput(FIELD_SHOWREFDOCUMENTS).isChecked()) 
      response.addCookie(createNewCookie(Preference.PREFERENCE_SHOWREFDOCUMENTS, "true"));
    else
      response.addCookie(createNewCookie(Preference.PREFERENCE_SHOWREFDOCUMENTS, "false"));
    if (getUIFormCheckBoxInput(FIELD_SHOW_HIDDEN_NODE).isChecked()) 
      response.addCookie(createNewCookie(Preference.PREFERENCE_SHOW_HIDDEN_NODE, "true"));
    else
      response.addCookie(createNewCookie(Preference.PREFERENCE_SHOW_HIDDEN_NODE, "false"));
    /*
    if (getUIFormCheckBoxInput(FIELD_SHOW_ITEMS_BY_USER).isChecked()) 
      response.addCookie(createNewCookie(Preference.PREFERENCE_SHOW_ITEMS_BY_USER, "true"));
    else
      response.addCookie(createNewCookie(Preference.PREFERENCE_SHOW_ITEMS_BY_USER, "false"));
    */
    if (getUIFormCheckBoxInput(FIELD_ENABLE_DRAG_AND_DROP).isChecked())  
      response.addCookie(createNewCookie(Preference.ENABLE_DRAG_AND_DROP, "true"));
    else
      response.addCookie(createNewCookie(Preference.ENABLE_DRAG_AND_DROP, "false"));
    response.addCookie(createNewCookie(Preference.PREFERENCE_QUERY_TYPE, getUIFormSelectBox(FIELD_QUERY_TYPE).getValue()));
    response.addCookie(createNewCookie(Preference.PREFERENCE_SORT_BY, getUIFormSelectBox(FIELD_SHORTBY).getValue()));
    response.addCookie(createNewCookie(Preference.PREFERENCE_ORDER_BY, getUIFormSelectBox(FIELD_ORDERBY).getValue()));
    response.addCookie(createNewCookie(Preference.NODES_PER_PAGE, getUIFormSelectBox(NODES_PER_PAGE).getValue()));
  }

  @SuppressWarnings("unused")
  static public class SaveActionListener extends EventListener<UIPreferencesForm> {
    public void execute(Event<UIPreferencesForm> event) throws Exception {
      UIPreferencesForm uiForm = event.getSource();
      UIJCRExplorerPortlet explorerPorltet = uiForm.getAncestorOfType(UIJCRExplorerPortlet.class);
      UIJCRExplorer uiExplorer = explorerPorltet.findFirstComponentOfType(UIJCRExplorer.class);
      Preference pref = uiExplorer.getPreference();
      pref.setJcrEnable(uiForm.getUIFormCheckBoxInput(FIELD_ENABLESTRUCTURE).isChecked());
      pref.setShowSideBar(uiForm.getUIFormCheckBoxInput(FIELD_SHOWSIDEBAR).isChecked());
      pref.setShowNonDocumentType(uiForm.getUIFormCheckBoxInput(FIELD_SHOWNONDOCUMENT).isChecked());
      pref.setShowPreferenceDocuments(uiForm.getUIFormCheckBoxInput(FIELD_SHOWREFDOCUMENTS).isChecked());
      pref.setShowHiddenNode(uiForm.getUIFormCheckBoxInput(FIELD_SHOW_HIDDEN_NODE).isChecked());
      //pref.setShowItemsByUser(uiForm.getUIFormCheckBoxInput(FIELD_SHOW_ITEMS_BY_USER).isChecked());
      pref.setEnableDragAndDrop(uiForm.getUIFormCheckBoxInput(FIELD_ENABLE_DRAG_AND_DROP).isChecked());
      pref.setSortType(uiForm.getUIFormSelectBox(FIELD_SHORTBY).getValue());
      pref.setQueryType(uiForm.getUIFormSelectBox(FIELD_QUERY_TYPE).getValue());
      pref.setOrder(uiForm.getUIFormSelectBox(FIELD_ORDERBY).getValue());
      pref.setNodesPerPage(Integer.parseInt(uiForm.getUIFormSelectBox(NODES_PER_PAGE).getValue()));
      uiForm.savePreferenceInCookies();
      uiExplorer.setPreferencesSaved(true);
      //uiExplorer.getPreference();
      uiExplorer.refreshExplorer();
      explorerPorltet.setRenderedChild(UIJCRExplorer.class);
      uiExplorer.updateAjax(event);
    }
  }

  static public class BackActionListener extends EventListener<UIPreferencesForm> {
    public void execute(Event<UIPreferencesForm> event) throws Exception {
      UIPreferencesForm uiForm = event.getSource();
      UIJCRExplorerPortlet explorerPorltet = uiForm.getAncestorOfType(UIJCRExplorerPortlet.class);
      UIJCRExplorer uiExplorer = explorerPorltet.findFirstComponentOfType(UIJCRExplorer.class);
      uiExplorer.getChild(UIPopupContainer.class).cancelPopupAction();
    }
  }
  
  static public class AdvanceActionListener extends EventListener<UIPreferencesForm> {
    public void execute(Event<UIPreferencesForm> event) throws Exception {
      UIPreferencesForm uiPreferencesForm = event.getSource();
      if (uiPreferencesForm.isAdvancePreferences()) uiPreferencesForm.setAdvancePreferences(false); 
      else uiPreferencesForm.setAdvancePreferences(true);
      UIJCRExplorerPortlet explorerPorltet = uiPreferencesForm.getAncestorOfType(UIJCRExplorerPortlet.class);
      UIJCRExplorer uiExplorer = explorerPorltet.findFirstComponentOfType(UIJCRExplorer.class);
      Preference pref = uiExplorer.getPreference();
      uiPreferencesForm.update(pref);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPreferencesForm.getParent());
    }
  }
}
