package org.vault.app.mailchimp.rsg.mailchimp.api.campaigns;

import org.vault.app.mailchimp.rsg.mailchimp.api.MailChimpApiException;
import org.vault.app.mailchimp.rsg.mailchimp.api.RPCStructConverter;
import org.vault.app.mailchimp.rsg.mailchimp.api.Utils;

import java.util.Map;



/**
 * Object representing the URL Click Stat from the API.
 * @author ericmuntz
 *
 */
public class URLClickStats implements RPCStructConverter {

	public String url;
	public Integer clicks;
	public Integer unique;
	
	@SuppressWarnings("unchecked")
	public void populateFromRPCStruct(String key, Map struct) throws MailChimpApiException {
		url = key;
		Utils.populateObjectFromRPCStruct(this, struct, true);
	}

}
