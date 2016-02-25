package org.vault.app.mailchimp.rsg.mailchimp.api.lists;

import org.vault.app.mailchimp.rsg.mailchimp.api.Constants;
import org.vault.app.mailchimp.rsg.mailchimp.api.MailChimpApiException;
import org.vault.app.mailchimp.rsg.mailchimp.api.RPCStructConverter;
import org.vault.app.mailchimp.rsg.mailchimp.api.Utils;

import java.util.Map;


public class MergeFieldInfo implements RPCStructConverter {

	public String name;
	public boolean required;
	public Constants.MergeFieldType type;
	public Boolean isPublic;
	public Boolean show;
	public String order;
	public String defaultValue;
	public String size;
	public String tag;
	
	@SuppressWarnings("unchecked")
	public void populateFromRPCStruct(String key, Map struct) throws MailChimpApiException {
		Utils.populateObjectFromRPCStruct(this, struct, true, "req", "fieldType", "public", "default");
		
		// req -> "required"
		required = (Boolean) struct.get("req");
		
		// handle reserved word mappings
		defaultValue = (String) struct.get("default");//yay, reserved words!
		isPublic = (Boolean) struct.get("public");//see: above
		
		// handle the type enum
		type = Constants.MergeFieldType.valueOf((String) struct.get("field_type"));
	}
	
	
}
