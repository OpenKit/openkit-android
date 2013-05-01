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

import io.openkit.*;
import io.openkit.facebook.*;
import io.openkit.facebook.Request.GraphUserCallback;
import io.openkit.facebook.model.GraphUser;
import io.openkit.user.OKUserIDType;
import io.openkit.user.OKUserUtilities;



public class FacebookUtilities 
{
	
	public interface CreateOKUserRequestHandler
	{
		public void onSuccess(OKUser user);
		public void onFail(Error error);
	}
	
	/**
	 * Makes a call to Facebook to get the user's facebook ID, then gets the corresponding OKUser from OpenKit, and responds with the request handler. Expects that the user is already authenticated with Facebook
	 * @param requestHandler
	 */
	public static void AuthorizeUserWithFacebook(final CreateOKUserRequestHandler requestHandler)
	{
		Session session = Session.getActiveSession();
		
		if(session.isOpened())
		{
			//Perform a 'ME' request to get user info
			Request.executeMeRequestAsync(session, new GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if(user != null) {
						String userID = user.getId();
						String userNick = user.getName();
						
						OKUserUtilities.createOKUser(OKUserIDType.FacebookID, userID, userNick, requestHandler);
					}
				}
			});
		}
		else
		{
			requestHandler.onFail(new Error("Not current logged into FB"));
		}
	}
	

}
