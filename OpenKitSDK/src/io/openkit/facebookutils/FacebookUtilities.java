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

import java.util.List;

import org.json.JSONArray;

import io.openkit.*;
import io.openkit.facebook.*;
import io.openkit.facebook.Request.GraphUserCallback;
import io.openkit.facebook.Request.GraphUserListCallback;
import io.openkit.facebook.model.GraphUser;
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
						long fbID = Long.parseLong(user.getId());

						if(OKUser.getCurrentUser().getFBUserID() == 0) {
							OKUser.getCurrentUser().setFBUserID(fbID);
							//TODO
							// Should we set the user's nickname to their facebook name?
							OKLog.v("Updating cached user with Facebook ID");
							OKUserUtilities.updateOKUser(OKUser.getCurrentUser(), requestHandler);
						} else {
							// If the FB ID of the login is different from the cached FB id, create a new OKUser with the new FB ID
							if(fbID != OKUser.getCurrentUser().getFBUserID()) {
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



	private static void GetFacebookUserInfo(final GetFBUserIDRequestHandler requestHandler)
	{
		Session session = Session.getActiveSession();

		if(isFBSessionOpen())
		{
			Request.executeMeRequestAsync(session, new GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if(user != null) {
						requestHandler.onCompletion(user);
					} else {
						requestHandler.onCompletion(null);
					}
				}
			});
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
		public void onSuccess(JSONArray friendsArray);
		public void onFail(FacebookRequestError error);
	}

	public static void GetFBFriends(final GetFBFriendsRequestHandler requestHandler)
	{
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
						JSONArray array = new JSONArray();
						for(int x = 0; x < users.size(); x++) {
							GraphUser user = users.get(x);
							array.put(user.getId());
						}
						requestHandler.onSuccess(array);
					}
				}
			});

			friendsRequest.executeAsync();
		} else {
			requestHandler.onFail(new FacebookRequestError(FacebookRequestError.INVALID_ERROR_CODE, "OpenKit", "Facebook session is not open"));
		}
	}


	/**
	 * Given a facebook login exception, returns a string to display as an error message if one should be shown, otherwise returns null
	 * @param exception
	 * @return Error message to display, null if no error to show
	 */
	public static String ShouldShowFacebookError(Exception exception)
	{
		OKLog.v("Facebook login failed");

		if(exception != null && exception.getClass() == io.openkit.facebook.FacebookOperationCanceledException.class)
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


}
