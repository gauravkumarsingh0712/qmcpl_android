package org.vault.app.mailchimp.rsg.mailchimp.api.lists;

import org.vault.app.mailchimp.rsg.mailchimp.api.Constants;
import org.vault.app.mailchimp.rsg.mailchimp.api.MailChimpApiException;
import org.vault.app.mailchimp.rsg.mailchimp.api.RPCStructConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Represents interest group information as returned by listInterestGroups
 * @author ericmuntz
 *
 */
public class InterestGroupInfo implements RPCStructConverter {

	public String name;
	public Constants.InterestGroupType type;
	public List<String> groups;
	
	@SuppressWarnings("unchecked")
	public void populateFromRPCStruct(String key, Map struct) throws MailChimpApiException {
		name = (String) struct.get("name");
		
		Object obj = struct.get("form_field");
		if (obj != null) type = Constants.InterestGroupType.valueOf((String) obj);
		
		obj = struct.get("groups");
		if (obj != null && obj instanceof Object[]) {
			Object[] values = (Object[]) obj;
			groups = new ArrayList<String> (values.length);
			for (Object o : values) {
				groups.add((String) o);
			}
		}
	}
	
	
}
