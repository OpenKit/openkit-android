package io.openkit.user;

import io.openkit.OKUser;

public abstract class UpdateUserRequestHandler
{
	public abstract void onSuccess(OKUser user);
	public abstract void onFail(Throwable error);
}