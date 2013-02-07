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

package io.openkit.okcloud;

public interface OKCloudHandler {
	// In the set case, the obj will be the object that you can expect back 
	// the next time you fetch the associated key from redis.  That is, 
	// we verify that we can both serialize and deserialize the object
	// properly before storing it in redis.  Therefore, obj will be of the 
	// same format for set and get requests.
	public void complete(Object obj, OKCloudException e);
}
