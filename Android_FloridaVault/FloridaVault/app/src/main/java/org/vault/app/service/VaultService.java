package org.vault.app.service;


public class VaultService {
	protected ServiceContext context;
	protected ServiceManager serviceManager;

	public VaultService(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
		this.context = serviceManager.getServiceContext();
	}

	protected ServiceContext getContext() {
		return context;
	}

	protected ServiceManager getServiceManager() {
		return serviceManager;
	}

	public void init() {
	}

}
