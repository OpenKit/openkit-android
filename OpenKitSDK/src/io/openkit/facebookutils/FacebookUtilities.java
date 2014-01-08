/**
 * Copyright 2012 OpenKit
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openkit.facebookutils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.Toast;
import io.openkit.*;
import com.facebook.*;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import io.openkit.user.OKUserIDType;
import io.openkit.user.OKUserUtilities;
import io.openkit.user.CreateOrUpdateOKUserRequestHandler;


public class FacebookUtilities
{
	static String keyhashErrorString = "remote_app_id does not match stored id ";

	public interface CreateOKUserRequestHandler
	{
		public void onSuccess(OKUser user);
		public void onFail(Error error);
	}

	public interface GetFBUserIDRequestHandler
	{
		public void onCompletion(GraphUser user);
	}



	public static void showAppRequestsDialog(String message, Activity activity, final Context applicationContext)
	{
		Bundle params = new Bundle();
		params.putString("message", message);

		if(!isFBSessionOpen())
			return;

	    WebDialog requestsDialog = (
	        new WebDialog.RequestsDialogBuilder(activity,
	            Session.getActiveSession(),
	            params))
	            .setOnCompleteListener(new OnCompleteListener() {

	                @Override
	                public void onComplete(Bundle values,
	                    FacebookException error) {
	                    if (error != null) {
	                        if (error instanceof FacebookOperationCanceledException) {
	                          // request cancelled
	                        } else {
	                            Toast.makeText(applicationContext,
	                                "Network Error",
	                                Toast.LENGTH_SHORT).show();
	                        }
	                    } else {
	                        final String requestId = values.getString("request");
	                        if (requestId != null) {
	                           // request sent
	                        } else {
	                            //Request cancelled
	                        }
	                    }
	                }
	            })
	            .build();
	    requestsDialog.show();
	}


	/**
	 * CreateOrUpdate the OKUser from Facebook and cache the user locally if successful
	 * @param context Context required to store cached user
	 */
	public static void CreateOrUpdateOKUserFromFacebook(final Context context)
	{
		CreateOrUpdateOKUserFromFacebook(new CreateOrUpdateOKUserRequestHandler() {

			@Override
			public void onSuccess(OKUser user) {
				OKManager.INSTANCE.handlerUserLoggedIn(user, context.getApplicationContext());
			}

			@Override
			public void onFail(Throwable error) {
				OKLog.v("Failed to create or update OKUser with FacebookID, error: " + error);
			}
		});
	}


	/**
	 * Update the
	 * @param requestHandler
	 */
	public static void CreateOrUpdateOKUserFromFacebook(final CreateOrUpdateOKUserRequestHandler requestHandler)
	{
		if(OKUser.getCurrentUser() != null) {
			GetFacebookUserInfo(new GetFBUserIDRequestHandler() {

				@Override
				public void onCompletion(GraphUser user) {
					if(user != null) {
						String fbIDString = user.getId();

						if(OKUser.getCurrentUser().getFBUserID() == null) {
							OKUser.getCurrentUser().setFBUserID(fbIDString);
							OKUser.getCurrentUser().setUserNick(user.getName());
							OKLog.v("Updating cached user with Facebook ID");
							OKUserUtilities.updateOKUser(OKUser.getCurrentUser(), requestHandler);
						} else {
							// If the FB ID of the login is different from the cached FB id, create a new OKUser with the new FB ID
							if(!FacebookUtilities.isFBIDEqual(fbIDString,OKUser.getCurrentUser().getFBUserID())) {
								OKLog.v("Cached user has different FB ID than logged in user, creating new OKUser with new FB ID");
								OKUserUtilities.createOKUser(OKUserIDType.FacebookID, user.getId(), user.getName(), requestHandler);
							} else {
								// The user already has a cached FB ID, and the fb ID matches the retrieved one, so do nothing
								requestHandler.onSuccess(OKUser.getCurrentUser());
							}
						}
					} else {
						requestHandler.onFail(new Error("Couldn't update current user because could not get Facebook user info"));
					}
				}
			});
		} else {
			CreateOKUserFromFacebook(requestHandler);
		}
	}

	private static boolean isFBIDEqual(String fbID1, String fbID2)
	{
		if(fbID1 == null || fbID2 == null) {
			return false;
		} else {
			return fbID1.equalsIgnoreCase(fbID2);
		}
	}



	private static void GetFacebookUserInfo(final GetFBUserIDRequestHandler requestHandler)
	{
		Session session = Session.getActiveSession();

		if(isFBSessionOpen())
		{
			Request meRequest = Request.newMeRequest(session, new GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if(user != null) {
						requestHandler.onCompletion(user);
					} else {
						requestHandler.onCompletion(null);
					}
				}
			});

			meRequest.executeAsync();
		} else {
			OKLog.v("Tried to get FB user ID without being logged into FB");
			requestHandler.onCompletion(null);
		}
	}


	/**
	 * Makes a call to Facebook to get the user's facebook ID, then gets the corresponding OKUser from OpenKit, and responds with the request handler. Expects that the user is already authenticated with Facebook
	 * @param requestHandler
	 */
	public static void CreateOKUserFromFacebook(final CreateOrUpdateOKUserRequestHandler requestHandler)
	{
		OKLog.v("Creating new OKUser from facebook ID");

		GetFacebookUserInfo(new GetFBUserIDRequestHandler() {

			@Override
			public void onCompletion(GraphUser user) {
				if(user != null) {
					String userID = user.getId();
					String userNick = user.getName();
					OKUserUtilities.createOKUser(OKUserIDType.FacebookID, userID, userNick, requestHandler);
				} else {
					requestHandler.onFail(new Error("Could not get FB user info"));
				}
			}
		});
	}

	public static boolean isFBSessionOpen()
	{
		Session session = Session.getActiveSession();

		if(session != null && session.isOpened())
			return true;
		else
			return false;
	}

	public static void logSessionState(SessionState state)
	{
		//Log what is happening with the Facebook session for debug help
		switch (state) {
		case OPENING:
			OKLog.v("SessionState Opening");
			break;
		case CREATED:
			OKLog.v("SessionState Created");
			break;
		case OPENED:
			OKLog.v("SessionState Opened");
			break;
		case CLOSED_LOGIN_FAILED:
			OKLog.v("SessionState Closed Login Failed");
			break;
		case OPENED_TOKEN_UPDATED:
			OKLog.v("SessionState Opened Token Updated");
			break;
		case CREATED_TOKEN_LOADED:
			OKLog.v("SessionState created token loaded" );
			break;
		case CLOSED:
			OKLog.v("SessionState closed");
			break;
		default:
			OKLog.v("Session State Default case");
			break;
		}
	}

	public interface GetFBFriendsRequestHandler
	{
		public void onSuccess(ArrayList<Long> friendsArray);
		public void onFail(FacebookRequestError error);
	}

	public static void GetFBFriends(final GetFBFriendsRequestHandler requestHandler)
	{
		if(OKManager.INSTANCE.getFbFriendsArrayList() != null) {
			OKLog.v("Using cached list of FB friends");
			requestHandler.onSuccess(OKManager.INSTANCE.getFbFriendsArrayList());
			return;
		}

		OKLog.d("Getting list of FB friends");

		Session session = Session.getActiveSession();

		if(session != null && session.isOpened())
		{
			Request friendsRequest = Request.newMyFriendsRequest(session, new GraphUserListCallback() {

				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					FacebookRequestError error = response.getError();

					if(error != null) {
						OKLog.d("Error getting Facebook friends");
						requestHandler.onFail(error);
					} else {
						OKLog.d("Got %d facebook friends", users.size());
						// Munge the Facebook friends into a JSONArray of friend IDs

						ArrayList<Long> friendsIDsArrayList = new ArrayList<Long>();

						for(int x = 0; x < users.size(); x++) {
							String friendFbID = users.get(x).getId();
							friendsIDsArrayList.add(Long.parseLong(friendFbID));
						}

						OKManager.INSTANCE.setFbFriendsArrayList(friendsIDsArrayList);

						requestHandler.onSuccess(friendsIDsArrayList);
					}
				}
			});

			friendsRequest.executeAsync();
		} else {
			requestHandler.onFail(new FacebookRequestError(FacebookRequestError.INVALID_ERROR_CODE, "OpenKit", "Facebook session is not open"));
		}
	}


	public static String getSerializedListOfFBFriends(ArrayList<Long> friendsArray)
	{
		StringBuilder sb = new StringBuilder();

		int x;
		for (x = 0; x < friendsArray.size(); x++) {
			sb.append(friendsArray.get(x));
			sb.append(",");
		}

		if(x > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	/**
	 * Given a facebook login exception, returns a string to display as an error message if one should be shown, otherwise returns null
	 * @param exception
	 * @return Error message to display, null if no error to show
	 */
	public static String ShouldShowFacebookError(Exception exception)
	{
		OKLog.v("Facebook login failed");

		if(exception != null && exception.getClass() == com.facebook.FacebookOperationCanceledException.class)
		{
			OKLog.v("User cancelled Facebook login");

			//Special check for the keyhash issue, otherwise just dismiss because the user cancelled
			if(exception.getMessage().equalsIgnoreCase(keyhashErrorString))
			{
				return "There was an error logging in with Facebook. Your Facebook application may not be configured correctly. Make sure you have added the correct Android keyhash(es) to your Facebook application";
			} else {
				return null;
			}
		}
		else {
			return "There was an unknown error while logging into Facebook. Please try again";
		}
	}

	public static void showErrorMessage(String message, Context context)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Error");
		builder.setMessage(message);
		builder.setNegativeButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		// create alert dialog
		AlertDialog alertDialog = builder.create();

		// show it
		alertDialog.show();
	}


}
