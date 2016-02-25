package org.vault.app.mailchimp.rsg.mailchimp.api.campaigns;

import org.vault.app.mailchimp.rsg.mailchimp.api.MailChimpApiException;
import org.vault.app.mailchimp.rsg.mailchimp.api.RPCStructConverter;
import org.vault.app.mailchimp.rsg.mailchimp.api.Utils;

import java.util.Map;



/**
 * Object representing the geo opens call result
 * @author ericmuntz
 *
 */
public class GeoOpens implements RPCStructConverter {
	
	public String code;
	public String name;
	public Integer opens;
	public Boolean regionDetail;

	@SuppressWarnings("unchecked")
	public void populateFromRPCStruct(String key, Map struct) throws MailChimpApiException {
		Utils.populateObjectFromRPCStruct(this, struct, true);
	}

}
