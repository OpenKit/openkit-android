package io.openkit.user;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class GoogleUtils {
	
	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	
	public static String[] getAccountNames(Context ctx) 
	{
        AccountManager manager = AccountManager.get(ctx);
        Account[] accounts = manager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }
	
	private static Account getAccount(Context ctx) {
		AccountManager manager  = AccountManager.get(ctx);
        Account[] accounts = manager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        return accounts[0];
    }
	
	public static void getAccountAuthToken(final Activity activity)
    {
    	AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				getAccountAuthTokenBlocking(activity);
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result)
			{
				Log.d("TestGoog", "Finished async task");
			}
		};
		
		task.execute();
    }
	
	public static void getAccountAuthTokenBlocking(Activity activity)
    {
    	Account account = getAccount(activity);
    	
    	try 
    	{
			String token = GoogleAuthUtil.getToken(activity, account.name, SCOPE);
			Log.d("TestGoog", "Got auth token: " + token);
		} 
    	catch (UserRecoverableAuthException userException) 
    	{
			// Unable to authenticate, but the user can fix this.
			// Forward the user to the appropriate activity.
    		activity.startActivityForResult(userException.getIntent(), 53);
	        userException.printStackTrace();
		} 
    	catch (IOException e) 
    	{	
			e.printStackTrace();
		} 
    	catch (GoogleAuthException e) 
    	{
			Log.d("TestGoog", "Unrecoverable error: " + e.getMessage());
			e.printStackTrace();
		}
    }

}
