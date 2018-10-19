# bulk-load-categories
Bulk Category Loader for Liferay DXP 7.1

Simple Liferay DXP 7.1 Widget (MVC Portlet) to allow the creation of multiple categories in one go.

Usage:
1. Deploy to Liferay Server
2. Deploy "Category Bulk-Loader" widget to page
3. Vocabularies for the site will be automatically loaded, select your chosen vocabulary
4. Select the Root category (mandatory) and optionally drill as far down the category hierarchy as you wish
5. UI will reflect currently chosen Vocabulary and Parent Category
6. Choose whether you want to grant "View" permissions to the "Guest" role or not
7. Type or Copy/Paste your list of categories into the multi-line text box, one category per line
8. Hit submit

If successful you'll get a friendly message.

If something broke it will break nastily due to lack of error handling!!!
(In particular it currently does not check to see if a category with the same name already exists).

Enjoy!
