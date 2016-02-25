package org.vault.app.mailchimp.rsg.mailchimp.api.data;

import org.vault.app.mailchimp.rsg.mailchimp.api.MailChimpApiException;
import org.vault.app.mailchimp.rsg.mailchimp.api.RPCStructConverter;

import java.util.Map;



/**
 * Simple utility class for representing a Folder as understood by the API
 * @author ericmuntz
 *
 */
public class FolderInfo implements RPCStructConverter {

	public Integer folderId;
	public String name;

	@SuppressWarnings("unchecked")
	public void populateFromRPCStruct(String key, Map struct) throws MailChimpApiException {
		this.folderId = ((Number) struct.get("folder_id")).intValue();
		this.name = (String) struct.get("name");
	}
}
