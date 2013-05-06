package io.openkit;

import java.util.List;
import org.json.JSONObject;

public interface OKAchievementsListResponseHandler {
	void onSuccess(List<OKAchievement> achievementList);
	void onFailure(Throwable e, JSONObject errorResponse);
}
