package org.vault.app.service;

public interface ServiceManager {
	ServiceContext getServiceContext();

	VaultApiInterface getVaultService();

}
