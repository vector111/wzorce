package com.minitwit.dao;

import com.minitwit.model.Feed;
import com.minitwit.model.FeedMessage;
import com.minitwit.model.Message;
import com.minitwit.model.User;

import java.util.List;

public interface FeedDao {
	List<FeedMessage> getFeedMesseges(User user);

	List<Feed> getFeedList(User user);
	
	void insertNewFeed(String link);
}