package com.minitwit.config;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.minitwit.App;
import com.minitwit.model.*;
import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;

import com.minitwit.service.impl.MiniTwitService;

import spark.ModelAndView;
import spark.Request;
import spark.template.freemarker.FreeMarkerEngine;
import spark.utils.StringUtils;

public class WebConfig {
	
	private static final String USER_SESSION_ID = "user";
	private MiniTwitService service;
	 

	public WebConfig(MiniTwitService service) {
		this.service = service;
		staticFileLocation("/public");
		setupRoutes();
	}



	private void setupRoutes() {
		/*
		 * Shows a users timeline or if no user is logged in,
		 *  it will redirect to the public timeline.
		 *  This timeline shows the user's messages as well
		 *  as all the messages of followed users.
		 */
		get("/", (req, res) -> {
			User user = getAuthenticatedUser(req);
			Map<String, Object> map = new HashMap<>();
			map.put("pageTitle", "Timeline");
			map.put("user", user);
			List<Feed> feedList = service.getFeedList(user);
			map.put("feedList", feedList);
			List<FeedMessage> feedMessages = service.getFeedMessagesForMainPage(user);
			map.put("feedMessages", feedMessages);
			return new ModelAndView(map, "timeline.ftl");
        }, new FreeMarkerEngine());
		before("/", (req, res) -> {
			User user = getAuthenticatedUser(req);
			if(user == null) {
				res.redirect("/public");
				halt();
			}
		});

		/*
		 * Displays the latest messages of all users.
		 */
		get("/public", (req, res) -> {
			User user = getAuthenticatedUser(req);
			Map<String, Object> map = new HashMap<>();
			map.put("pageTitle", "Rss Reader");
			map.put("user", user);
			return new ModelAndView(map, "timeline.ftl");
		}, new FreeMarkerEngine());

		get("/addNewFeed", (req, res) -> {
			User user = getAuthenticatedUser(req);
			Map<String, Object> map = new HashMap<>();
			map.put("pageTitle", "Timeline");
			map.put("user", user);
			return new ModelAndView(map, "addNewFeed.ftl");
		}, new FreeMarkerEngine());
		before("/addNewFeed", (req, res) -> {
			User user = getAuthenticatedUser(req);
			if(user == null) {
				res.redirect("/public");
				halt();
			}
		});

		get( "/f/:feedName", (req, res) -> {
			User user = getAuthenticatedUser(req);
			String feedName = req.params(":feedName");
			Map<String, Object> map = new HashMap<>();
			map.put("pageTitle", "Parser");
			map.put("user", user);
			List<Feed> feedList = service.getFeedList(user);
			map.put("feedList", feedList);
			List<FeedMessage> feedMessages = service.getFeedMessages(user, feedName);
			map.put("feedMessages", feedMessages);
			return new ModelAndView(map, "parser.ftl");
		}, new FreeMarkerEngine());
		before("/f/:feedName", (req, res) -> {
			User user = getAuthenticatedUser(req);
			if(user == null) {
				res.redirect("/public");
				halt();
			}
		});


		/*
		* Add new feed
		* */
		post("/newFeed", (req, res) -> {
			User user = getAuthenticatedUser(req);
			MultiMap<String> params = new MultiMap<String>();
			UrlEncoded.decodeTo(req.body(), params, "UTF-8");

			String name = params.getString("name");
			String link = params.getString("link");


			service.addNewFeed(user, name, link);
			res.redirect("/");
			return null;
		});

		/*
		 * Checks if the user exists
		 */
		before("/t/:username", (req, res) -> {
			String username = req.params(":username");
			User profileUser = service.getUserbyUsername(username);
			if(profileUser == null) {
				halt(404, "User not Found");
			}
		});
		
		
		/*
		 * Presents the login form or redirect the user to
		 * her timeline if it's already logged in
		 */
		get("/login", (req, res) -> {
			Map<String, Object> map = new HashMap<>();
			if(req.queryParams("r") != null) {
				map.put("message", "You were successfully registered and can login now");
			}
			return new ModelAndView(map, "login.ftl");
        }, new FreeMarkerEngine());
		/*
		 * Logs the user in.
		 */
		post("/login", (req, res) -> {
			Map<String, Object> map = new HashMap<>();
			User user = new User();
			try {
				MultiMap<String> params = new MultiMap<String>();
				UrlEncoded.decodeTo(req.body(), params, "UTF-8");
				BeanUtils.populate(user, params);
			} catch (Exception e) {
				halt(501);
				return null;
			}
			LoginResult result = service.checkUser(user);
			if(result.getUser() != null) {
				addAuthenticatedUser(req, result.getUser());
				res.redirect("/");
				halt();
			} else {
				map.put("error", result.getError());
			}
			map.put("username", user.getUsername());
			return new ModelAndView(map, "login.ftl");
        }, new FreeMarkerEngine());
		/*
		 * Checks if the user is already authenticated
		 */
		before("/login", (req, res) -> {
			User authUser = getAuthenticatedUser(req);
			if(authUser != null) {
				res.redirect("/");
				halt();
			}
		});
		
		
		/*
		 * Presents the register form or redirect the user to
		 * her timeline if it's already logged in
		 */
		get("/register", (req, res) -> {
			Map<String, Object> map = new HashMap<>();
			return new ModelAndView(map, "register.ftl");
        }, new FreeMarkerEngine());
		/*
		 * Registers the user.
		 */
		post("/register", (req, res) -> {
			Map<String, Object> map = new HashMap<>();
			User user = new User();
			try {
				MultiMap<String> params = new MultiMap<String>();
				UrlEncoded.decodeTo(req.body(), params, "UTF-8");
				BeanUtils.populate(user, params);
			} catch (Exception e) {
				halt(501);
				return null;
			}
			String error = user.validate();
			if(StringUtils.isEmpty(error)) {
				User existingUser = service.getUserbyUsername(user.getUsername());
				if(existingUser == null) {
					service.registerUser(user);
					res.redirect("/login?r=1");
					halt();
				} else {
					error = "The username is already taken";
				}
			}
			map.put("error", error);
			map.put("username", user.getUsername());
			map.put("email", user.getEmail());
			return new ModelAndView(map, "register.ftl");
        }, new FreeMarkerEngine());
		/*
		 * Checks if the user is already authenticated
		 */
		before("/register", (req, res) -> {
			User authUser = getAuthenticatedUser(req);
			if(authUser != null) {
				res.redirect("/");
				halt();
			}
		});

		/*
		 * Logs the user out and redirects to the public timeline
		 */
		get("/logout", (req, res) -> {
			removeAuthenticatedUser(req);
			res.redirect("/public");
			return null;
        });
	}

	private void addAuthenticatedUser(Request request, User u) {
		request.session().attribute(USER_SESSION_ID, u);
		
	}

	private void removeAuthenticatedUser(Request request) {
		request.session().removeAttribute(USER_SESSION_ID);
		
	}

	private User getAuthenticatedUser(Request request) {
		return request.session().attribute(USER_SESSION_ID);
	}
}
