package com.liferay.sales.demo.BulkLoadCategories.portlet;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.sales.demo.BulkLoadCategories.constants.BulkLoadCategoriesPortletKeys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import javax.portlet.ProcessAction;

/**
 * @author Ben
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=Liferay Presales",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + BulkLoadCategoriesPortletKeys.BulkLoadCategories,
		"javax.portlet.display-name=Bulk Category Loader",
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.init-param.add-process-action-success-action=false"
	},
	service = Portlet.class
)
public class BulkLoadCategoriesPortlet extends MVCPortlet {
	
	public Long vocabularyId = new Long(0);
	public Long parentCategoryId = new Long(0);
	public Long categoryLevel = new Long(0);
	public String vocabularyName = "";
	public String parentCategoryName = "";
	
	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
		// Load the Vocabularies and display
		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		long companyId = themeDisplay.getCompanyId();
		long groupId = themeDisplay.getSiteGroupId();
		List<AssetVocabulary> vocabulariesList = AssetVocabularyLocalServiceUtil.getCompanyVocabularies(companyId);

		HashMap<Long, String> vocabList = new HashMap<Long, String>();
		
		//_log.info("Vocab Count: " + vocabulariesList.size());
		
		for (AssetVocabulary vocabItem: vocabulariesList) {

			if (vocabItem.getGroupId() == groupId) {
				//_log.info("Vocab " + vocabItem.getName() + ", " + vocabItem.getVocabularyId());
				vocabList.put(vocabItem.getVocabularyId(), vocabItem.getName());
			}
			if (vocabItem.getVocabularyId() == vocabularyId) {
				vocabularyName = vocabItem.getName();
			}
		}

		renderRequest.setAttribute("vocabularyName", vocabularyName);
		renderRequest.setAttribute("vocabList", vocabList);

		
		if (vocabularyId > 0) {
			//Load the Root Categories for the chosen Vocabulary ID

			HashMap<Long, String> rootCategoryList = new HashMap<Long, String>();

			List<AssetCategory> rootCategoriesList = AssetCategoryLocalServiceUtil.getVocabularyRootCategories(vocabularyId, -1, -1, null);

			for (AssetCategory rootCategoryItem: rootCategoriesList) {
				//_log.info("Root Category " + rootCategoryItem.getName() + ", " + rootCategoryItem.getCategoryId());
				rootCategoryList.put(rootCategoryItem.getCategoryId(), rootCategoryItem.getName());
				if (rootCategoryItem.getCategoryId() == parentCategoryId) {
					//_log.info("Root Category: " + rootCategoryItem.getName() + " (" + parentCategoryId + ")");
					parentCategoryName = rootCategoryItem.getName();
				}
			}
			
			renderRequest.setAttribute("rootCategoryList", rootCategoryList);
		}

		renderRequest.setAttribute("vocabularyId", vocabularyId);
		//_log.info("Selected Vocabulary ID (render phase) " + vocabularyId);
		
		if (parentCategoryId > 0) {
			//Load the Child Categories for the chosen Parent Category
			//First time this function is called the Parent Category ID will be a Root Category ID
			
			HashMap<Long, String> childCategoryList = new HashMap<Long, String>();
			
			//Use the current parentCategoryId to get the name of the category to pass back to the UI

			try {
				AssetCategory parentCategory = AssetCategoryLocalServiceUtil.getCategory(parentCategoryId);
				parentCategoryName = parentCategory.getName();
			} catch (PortalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			List<AssetCategory> childCategoriesList = AssetCategoryLocalServiceUtil.getChildCategories(parentCategoryId);
			categoryLevel++;
			for (AssetCategory childCategoryItem: childCategoriesList) {
				childCategoryList.put(childCategoryItem.getCategoryId(), childCategoryItem.getName());
			}
			
			renderRequest.setAttribute("childCategoryList", childCategoryList);
		}
		
		renderRequest.setAttribute("parentCategoryId", parentCategoryId);
		renderRequest.setAttribute("parentCategoryName", parentCategoryName);

		super.render(renderRequest, renderResponse);

	}
	
	public void processStep1(ActionRequest actionRequest, ActionResponse actionResponse){
		// Get the form data
		String categoryList = ParamUtil.getString(actionRequest, "categoryList");
		String[] categoryArray = categoryList.split("\n");

		if (categoryArray.length > 0) {
			for (String category:categoryArray) {
				addBulkCategory(category, actionRequest);
				_log.info("Category Item: " + category);
			}
		}
		SessionMessages.add(actionRequest, "categoriesAdded");
		// actionResponse.setRenderParameter("mvcPath","/submitted.jsp");
	}
	
	public void addBulkCategory(String categoryItem, ActionRequest actionRequest) {
		
		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
		long userId = themeDisplay.getUserId();
		long groupId = themeDisplay.getSiteGroupId();

		Map<Locale, String> titleMap = new HashMap<Locale, String>();
		Locale newLocale = new Locale.Builder().setLanguage("en").setRegion("US").build();
		titleMap.put(newLocale, categoryItem);

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());
		
		if (ParamUtil.getBoolean(actionRequest, "guestViewPermissions")) {
			serviceContext.setGuestPermissions(new String[] {ActionKeys.VIEW});	
		}

		try {
			AssetCategory newBulkCategory = AssetCategoryLocalServiceUtil.addCategory(
					userId, 
					groupId, 
					parentCategoryId, 
					titleMap, 
					null, 
					vocabularyId, 
					null, 
					serviceContext);
			
		} catch (SystemException | PortalException e) {
			// TODO Auto-generated catch block
			_log.info("Something went wrong...");
			e.printStackTrace();
		}

	}


	private static Log _log = LogFactoryUtil
			.getLog("outage-report.OutageReportmvcportletPortlet.java");


	@ProcessAction(name = "getRootCategories")
	public void getRootCategories(ActionRequest actionRequest, ActionResponse actionResponse) {
		vocabularyId = ParamUtil.getLong(actionRequest, "vocabularies");
		_log.info("Selected Vocabulary ID = " + vocabularyId.toString());
	}

	@ProcessAction(name = "getRootCategoryChildren")
	public void getChildCategories(ActionRequest actionRequest, ActionResponse actionResponse) {
		parentCategoryId = ParamUtil.getLong(actionRequest, "rootCategories");
		_log.info("Selected Root Category ID = " + parentCategoryId.toString());
	}

	@ProcessAction(name = "getChildCategoryChildren")
	public void getChildCategoryChildren(ActionRequest actionRequest, ActionResponse actionResponse) {
		parentCategoryId = ParamUtil.getLong(actionRequest, "childCategories");
		_log.info("Selected Child Category ID = " + parentCategoryId.toString());
	}
	
	
}