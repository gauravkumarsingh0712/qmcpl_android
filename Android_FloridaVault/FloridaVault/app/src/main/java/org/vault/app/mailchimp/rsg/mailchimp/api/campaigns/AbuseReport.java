package org.vault.app.mailchimp.rsg.mailchimp.api.campaigns;

import org.vault.app.mailchimp.rsg.mailchimp.api.data.GenericStructConverter;

import java.util.Date;



/**
 * Typed class for the Abuse Report that comes back from the API
 * @author ericmuntz
 *
 */
public class AbuseReport extends GenericStructConverter {

	public Date date;
	public String email;
	public String type;
	public String campaignId;
	
}
