package org.vault.app.mailchimp.rsg.mailchimp.api.campaigns;

import org.vault.app.mailchimp.rsg.mailchimp.api.MailChimpApiException;
import org.vault.app.mailchimp.rsg.mailchimp.api.data.GenericStructConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



/**
 * Has the action, timestamp and url for the action taken by a recipient of a given Email address
 * @author ericmuntz
 *
 */
public class EmailAIMStats {

	public String email;
	public List<Stats> stats;
	
	/**
	 * Nested class to hold the stats for a given email address
	 * @author ericmuntz
	 *
	 */
	public static class Stats extends GenericStructConverter {
		public String action;
		public Date timestamp;
		public String url;
	}

	public void build(Entry<String, Object[]> entry) throws MailChimpApiException {
		this.email = entry.getKey();
		Object[] statsArr = entry.getValue();
		buildStatsList(statsArr);
	}

	@SuppressWarnings("unchecked")
	public void buildStatsList(Object[] statsArr) throws MailChimpApiException {
		this.stats = new ArrayList(statsArr.length);
		for (Object o : statsArr) {
			Stats stat = new Stats();
			stat.populateFromRPCStruct(null, (Map) o);
			this.stats.add(stat);
		}
	}

	
	
}
