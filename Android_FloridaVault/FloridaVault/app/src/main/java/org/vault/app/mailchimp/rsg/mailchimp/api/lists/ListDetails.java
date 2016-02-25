package org.vault.app.mailchimp.rsg.mailchimp.api.lists;

import org.vault.app.mailchimp.rsg.mailchimp.api.data.GenericStructConverter;

import java.util.Date;



public class ListDetails extends GenericStructConverter {

	public String id;
	public Integer webId;
	public String name;
	public Date dateCreated;
	public Integer memberCount;
	public Integer unsubscribeCount;
	public Integer cleanedCount;
	public Boolean emailTypeOption;
	public String defaultFromName;
	public String defaultFromEmail;
	public String defaultSubject;
	public String defaultLanguage;
	public Double listRating;
	public Integer memberCountSinceSend;
	public Integer unsubscribeCountSinceSend;
	public Integer cleanedCountSinceSend;
	
}
