package org.vault.app.mailchimp.rsg.mailchimp.api.campaigns;

import org.vault.app.mailchimp.rsg.mailchimp.api.MailChimpApiException;
import org.vault.app.mailchimp.rsg.mailchimp.api.RPCStructConverter;

import java.util.Map;



public class CampaignAdvice implements RPCStructConverter {
	
	public String message;
	public String type;

	@SuppressWarnings("unchecked")
	public void populateFromRPCStruct(String key, Map struct) throws MailChimpApiException {
		this.message = (String) struct.get("msg");
		this.type = (String) struct.get("type");
	}

}
