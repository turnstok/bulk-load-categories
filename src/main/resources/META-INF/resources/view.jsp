<%@ include file="init.jsp" %>
<liferay-ui:success key="categoriesAdded" message="categories-added" />
<%
HashMap<Long, String> vocabList = (HashMap<Long, String>) renderRequest.getAttribute("vocabList");
Long vocabularyId = ParamUtil.getLong(renderRequest,"vocabularyId");
String vocabularyName = ParamUtil.getString(renderRequest, "vocabularyName");
Long parentCategoryId = ParamUtil.getLong(renderRequest, "parentCategoryId");
String parentCategoryName = ParamUtil.getString(renderRequest, "parentCategoryName");
HashMap<Long, String> rootCategoryList = (HashMap<Long, String>) renderRequest.getAttribute("rootCategoryList");
HashMap<Long, String> childCategoryList = (HashMap<Long, String>) renderRequest.getAttribute("childCategoryList");
%>
<portlet:actionURL name="processStep1" var="processStep1URL" windowState="normal" />
<portlet:actionURL name="getRootCategories" var="getRootCategoriesURL" windowState="normal" />
<portlet:actionURL name="getRootCategoryChildren" var="getRootCategoryChildrenURL" windowState="normal" />
<portlet:actionURL name="getChildCategoryChildren" var="getChildCategoryChildrenURL" windowState="normal" />

<script>
function getRootCategories() {
	submitForm(document.<portlet:namespace/>fm, '<%=getRootCategoriesURL%>' );
}

function getRootCategoryChildren() {
	submitForm(document.<portlet:namespace/>fm, '<%=getRootCategoryChildrenURL%> ');
}

function getChildCategoryChildren() {
	submitForm(document.<portlet:namespace/>fm, '<%=getChildCategoryChildrenURL%> ');
}
</script>

<aui:form method="POST" name="fm" action="<%=processStep1URL%>" windowState="<%= LiferayWindowState.NORMAL.toString() %>" >
	<aui:fieldset column="false" label="">
		<aui:row>
			<aui:col span="4">
				<aui:select name="vocabularies" label="Vocabulary" onChange="getRootCategories()">
					<aui:option label="Choose an Option" value="0" />
					<c:forEach var="element" items="${vocabList}">
						<aui:option label="${element.value}" value="${element.key}" />
					</c:forEach>				
				
				</aui:select>
			</aui:col>
			<aui:col span="4">
				<aui:select name="rootCategories" label="Root Category" onChange="getRootCategoryChildren()">
					<aui:option label="Choose an Option" value="0" />
					<c:forEach var="rootCategory" items="${rootCategoryList}">
						<aui:option label="${rootCategory.value}" value="${rootCategory.key}" />
					</c:forEach>
				</aui:select>
			</aui:col>
			<aui:col span="4">
				<aui:select id="childCategories" name="childCategories" label="Child Category" onChange="getChildCategoryChildren()">
					<aui:option label="Select to go deeper..." value="0" />
					<c:forEach var="childCategory" items="${childCategoryList}">
						<aui:option label="${childCategory.value}" value="${childCategory.key}" />
					</c:forEach>
				</aui:select>
			</aui:col>
		</aui:row>
		<aui:row>
			<aui:col span="12">
				<label>Currently Selected:&nbsp;</label>
				<c:if test = "${vocabularyId > '0'}">
					<span>Vocabulary: ${vocabularyName} (${vocabularyId});</span>
					<c:if test="${parentCategoryId > '0'}">
						<span>Parent Category: ${parentCategoryName} (${parentCategoryId});</span>
					</c:if>
				</c:if>
				<c:if test = "${vocabularyId == '0'}">
					<span>Nothing selected yet</span>
				</c:if>
			</aui:col>
		</aui:row>
		<aui:row>
			<aui:col span="6">
				<aui:field-wrapper label="Guest has View Permissions?" inlineLabel="inLine" inlineField="false" name="guestViewPermissionsLabel">&nbsp;
					<aui:input checked="true" inlineLabel="right" inlineField="true" name="guestViewPermissions" type="radio" label="Yes" value="true" />
					<aui:input checked="false" inlineLabel="right" inlineField="true" name="guestViewPermissions" type="radio" label="No" value="false" />
				</aui:field-wrapper>
			</aui:col>
		</aui:row>
		<aui:row>
			<aui:col span="12">
				<aui:input type="textarea" name="categoryList" label="Categories" cssClass="input100" />
			</aui:col>
		</aui:row>
		<aui:button-row>
			<c:if test="${parentCategoryId > '0'}">
				<aui:button type="submit" value="Submit" />		
			</c:if>
			<c:if test = "${parentCategoryId == '0'}">
				<aui:button type="submit" value="Submit" disabled="true" />
			</c:if>
		</aui:button-row>	
	</aui:fieldset>

</aui:form>
