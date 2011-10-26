/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.scv;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.AbstractActionComponent;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.ecm.webui.presentation.removeattach.RemoveAttachmentComponent;
import org.exoplatform.ecm.webui.presentation.removecomment.RemoveCommentComponent;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.mimetype.DMSMimeTypeResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.friendly.FriendlyService;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Jun 9, 2008
 */
@ComponentConfig(
    lifecycle = Lifecycle.class,
    events = {
      @EventConfig(listeners = UIPresentation.DownloadActionListener.class)
  }
)

public class UIPresentation extends UIBaseNodePresentation {

  private static final Log LOG  = ExoLogger.getLogger("org.exoplatform.wcm.webui.scv.UIPresentation");

  private NodeLocation originalNodeLocation;

  private NodeLocation viewNodeLocation;

  String templatePath = null;

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getOriginalNode()
   */
  public Node getOriginalNode() throws Exception {
    return originalNodeLocation == null ? null :
           Utils.getViewableNodeByComposer(originalNodeLocation.getRepository(),
                                           originalNodeLocation.getWorkspace(),
                                           originalNodeLocation.getPath(),
                                           WCMComposer.BASE_VERSION);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getNode()
   */
  public void setOriginalNode(Node node) throws Exception{
    originalNodeLocation = NodeLocation.make(node);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getNode()
   */
  public Node getNode() throws Exception {
    if (viewNodeLocation == null) return null;
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = portletRequestContext.getRequest().getPreferences();
    String sharedCache = preferences.getValue(UISingleContentViewerPortlet.ENABLE_CACHE, "true");
    sharedCache = "true".equals(sharedCache) ? WCMComposer.VISIBILITY_PUBLIC:WCMComposer.VISIBILITY_USER;
    return Utils.getViewableNodeByComposer(viewNodeLocation.getRepository(),
                                           viewNodeLocation.getWorkspace(),
                                           viewNodeLocation.getPath(),
                                           null,
                                           sharedCache);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#setNode(javax.jcr.Node)
   */
  public void setNode(Node node) {
    viewNodeLocation = NodeLocation.make(node);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getRepositoryName()
   */
  public String getRepositoryName() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
    return originalNodeLocation != null ? originalNodeLocation.getRepository()
                                       : portletPreferences.getValue(UISingleContentViewerPortlet.REPOSITORY,
                                                                     "repository");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.webui.portal.UIPortalComponent#getTemplate()
   */
  public String getTemplate() {
    return templatePath;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getTemplatePath()
   */
  public String getTemplatePath() throws Exception {
    return templatePath;
  }

  public void setTemplatePath(String templatePath) {
    this.templatePath = templatePath;
  }



  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.
   * exoplatform.webui.application.WebuiRequestContext, java.lang.String)
   */
  @Deprecated
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    String workspace = dmsConfiguration.getConfig().getSystemWorkspace();
    return new JCRResourceResolver(workspace);
  }

  public ResourceResolver getTemplateResourceResolver() {
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    String workspace = dmsConfiguration.getConfig().getSystemWorkspace();
    return new JCRResourceResolver(workspace);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getNodeType()
   */
  public String getNodeType() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#isNodeTypeSupported()
   */
  public boolean isNodeTypeSupported() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getCommentComponent()
   */
  public UIComponent getCommentComponent() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getCommentComponent()
   */
  public UIComponent getRemoveAttach() throws Exception {
    removeChild(RemoveAttachmentComponent.class);
    UIComponent uicomponent = addChild(RemoveAttachmentComponent.class, null, "PresentationRemoveAttach");
    ((AbstractActionComponent) uicomponent).setLstComponentupdate(Arrays.asList(new Class[] { UIPresentationContainer.class }));
    return uicomponent;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getCommentComponent()
   */
  public UIComponent getRemoveComment() throws Exception {
    removeChild(RemoveCommentComponent.class);
    UIComponent uicomponent = addChild(RemoveCommentComponent.class, null, "PresentationRemoveComment");
    ((AbstractActionComponent)uicomponent).setLstComponentupdate(Arrays.asList(new Class[] {UIPresentationContainer.class}));
    return uicomponent;
  }

  public UIComponent getUIComponent(String mimeType) throws Exception {
    return org.exoplatform.ecm.webui.utils.Utils.getUIComponent(mimeType, this);
  }

  /**
   * use getViewableLink(Node attNode, Parameter[] params) instead
   * @param attNode
   * @param params
   * @return
   * @throws Exception
   */
  @Deprecated
  public String getAttachmentURL(Node attNode, Parameter[] params) throws Exception {
    return getViewableLink(attNode, params);
  }

  /**
   * Gets the viewable link (attachment link, relation document link)
   *
   * @param node the node
   * @return the attachment URL
   * @throws Exception the exception
   */
  public String getViewableLink(Node node, Parameter[] params) throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletRequest portletRequest = portletRequestContext.getRequest();
    NodeLocation nodeLocation = NodeLocation.getNodeLocationByNode(node);
    String baseURI = portletRequest.getScheme() + "://" + portletRequest.getServerName() + ":"
        + String.format("%s", portletRequest.getServerPort());
    String basePath = Utils.getPortletPreference(UISingleContentViewerPortlet.PREFERENCE_TARGET_PAGE);
    String scvWith = Utils.getPortletPreference(UISingleContentViewerPortlet.PREFERENCE_SHOW_SCV_WITH);
    if (scvWith == null || scvWith.length() == 0)
        scvWith = UISingleContentViewerPortlet.DEFAULT_SHOW_SCV_WITH;

    String param = "/" + nodeLocation.getRepository() + "/" + nodeLocation.getWorkspace();

    if (node.isNodeType("nt:frozenNode")) {
      String uuid = node.getProperty("jcr:frozenUuid").getString();
      Node originalNode = node.getSession().getNodeByUUID(uuid);
      param += originalNode.getPath();
    } else {
      param += node.getPath();
    }

    NodeURL nodeURL = Util.getPortalRequestContext().createURL(NodeURL.TYPE);
    NavigationResource resource = new NavigationResource(SiteType.PORTAL, Util.getPortalRequestContext().getPortalOwner(), basePath);
    nodeURL.setResource(resource).setQueryParameterValue(scvWith, param);
    String link = baseURI + nodeURL.toString();

    FriendlyService friendlyService = getApplicationComponent(FriendlyService.class);
    link = friendlyService.getFriendlyUri(link);

    return link;
  }

  /**
   * Gets the attachment nodes.
   * @param node the node that contains Attachment
   * @return the attachment Nodes
   * @throws Exception the exception
   * @author vinh_nguyen
   */
  public List<Node> getAttachments() throws Exception {
    List<Node> attachments = new ArrayList<Node>() ;
    Node parent  = getOriginalNode();
    NodeIterator childrenIterator = parent.getNodes();;
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String strRepository = getRepository();
    while (childrenIterator.hasNext()) {
      Node childNode = childrenIterator.nextNode();
      String nodeType = childNode.getPrimaryNodeType().getName();
      List<String> listCanCreateNodeType =
      	org.exoplatform.ecm.webui.utils.Utils.getListAllowedFileType(parent, strRepository, templateService) ;
      if (listCanCreateNodeType.contains(nodeType)) attachments.add(childNode);
    }
    return attachments;
  }

  static public class DownloadActionListener extends EventListener<UIPresentation> {
    public void execute(Event<UIPresentation> event) throws Exception {
      UIPresentation uiComp = event.getSource();
      UIApplication uiApp = uiComp.getAncestorOfType(UIApplication.class);
      try {
        String downloadLink = Utils.getDownloadLink(Utils.getFileLangNode(uiComp.getNode()));
        event.getRequestContext().getJavascriptManager().addCustomizedOnLoadScript("ajaxRedirect('" + downloadLink + "');");
      } catch(RepositoryException e) {
         LOG.error("Repository cannot be found", e);
        return ;
      } catch (Exception e) {
        JCRExceptionManager.process(uiApp, e);
        return;
      }
    }
  }
}
