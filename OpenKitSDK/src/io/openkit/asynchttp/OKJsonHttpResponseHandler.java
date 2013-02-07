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

package io.openkit.asynchttp;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class OKJsonHttpResponseHandler extends JsonHttpResponseHandler 
{	
	@Override
	public abstract void onSuccess(JSONArray array);
	
	@Override
	public abstract void onSuccess(JSONObject object);
	
	@Override
    public abstract void onFailure(Throwable e, JSONObject errorResponse);
	
	@Override
    public abstract void onFailure(Throwable e, JSONArray errorResponse);
	
	@Override
	public abstract void onFailure(Throwable error, String content);
	
}
