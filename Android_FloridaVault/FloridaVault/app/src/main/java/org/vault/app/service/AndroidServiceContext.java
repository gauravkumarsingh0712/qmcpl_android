package org.vault.app.service;

import android.app.Application;

public class AndroidServiceContext implements ServiceContext {
	private Application mApp;

	public AndroidServiceContext(Application app) {
		mApp = app;
	}

	@Override
	public Application getApplication() {
		return mApp;
	}
}
