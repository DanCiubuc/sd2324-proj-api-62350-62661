package tukano.servers.java;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Shorts;
import tukano.clients.ShortsClientFactory;
import tukano.api.java.Users;
import tukano.persistence.Hibernate;

public class JavaUsers implements Users {
    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    @Override
    public Result<String> createUser(User user) {
        Log.info("Info Received createUser : " + user);

        // Check if user data is valid
        if (user.userId() == null || user.displayName() == null) {
            Log.info("User object invalid.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Check if user with same userId already exists
        List<User> existingUsers = getUserHibernate(user.getUserId());
        // Insert user, checking if name already exists
        if (!existingUsers.isEmpty()) {
            Log.info("User already exists.");
            return Result.error(ErrorCode.CONFLICT);
        }
        // Creating User
        Hibernate.getInstance().persist(user);
        return Result.ok(user.userId());
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        Log.info("Info Received getUser : userId = " + userId + "; pwd = " + pwd);
        // Check if user is valid
        if (userId == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        List<User> existingUsers = getUserHibernate(userId);
        // Check if user exists
        if (existingUsers.isEmpty()) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        User user = existingUsers.get(0);
        // Check if the password is correct
        if (!user.pwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        return Result.ok(user);
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {
        Log.info("Info Received updateUser : userId = " + userId + "; pwd = " + pwd + "; email = " + user.getEmail()
                + "; displayName = " + user.getDisplayName());

        // Check if user is valid
        if (userId == null) {
            Log.info("UserId may not be null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }


        // The id of the User can't be changed
        if (user.getUserId() != null) {
            if (!user.getUserId().equals(userId)) {
                Log.info("Can't change userId.");
                return Result.error(ErrorCode.BAD_REQUEST);
            }
        }

        List<User> existingUsers = getUserHibernate(userId);

        // Check if user exists
        if (existingUsers.isEmpty()) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        User newUserInfo = existingUsers.get(0);

        // Check if the password is correct
        if (!newUserInfo.getPwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // Change Password
        if (user.getPwd() != null) {
            newUserInfo.setPwd(user.getPwd());
        }
        // Change Display Name
        if (user.getDisplayName() != null) {
            newUserInfo.setDisplayName(user.displayName());
        }
        // Change Email
        if (user.getEmail() != null) {
            newUserInfo.setEmail(user.getEmail());
        }
        // Updating User
        Hibernate.getInstance().update(newUserInfo);
        return Result.ok(newUserInfo);
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        Log.info("Info Received deleteUser : userId = " + userId + "; pwd = " + pwd);
        // Check if user is valid
        if (userId == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        List<User> existingUsers = getUserHibernate(userId);
        // Check if user exists
        if (existingUsers.isEmpty()) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        User user = existingUsers.get(0);
        // Check if the password is correct
        if (!user.pwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        Shorts shortsService = ShortsClientFactory.getClient();

        Result<List<String>> shortsResponse = shortsService.getShorts(userId);
        if (shortsResponse.isOK()) {
            List<String> shortIds = shortsResponse.value();

            for (String shortId : shortIds) {
                // Removing each Short
                shortsService.deleteShort(shortId, pwd);
            }
        }

        Result<List<String>> likeHistory = shortsService.likeHistory(userId, pwd);

        if (likeHistory.isOK()) {
            List<String> shortIds = likeHistory.value();

            for (String shortId : shortIds) {
                // Removing each Like
                shortsService.like(shortId, userId, false, pwd);
            }
        }
        // Deleting User
        Hibernate.getInstance().delete(user);
        return Result.ok(user);
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Log.info("Info Received searchUsers : pattern = " + pattern);
        List<User> userList = new ArrayList<>();
        List<User> existing_users = Hibernate.getInstance().sql("SELECT * FROM User", User.class);

        if (pattern == null || pattern.trim().isEmpty()) {
            for (int i = 0; i < existing_users.size(); i++) {
                // Returning all the users
                userList.add(i, existing_users.get(i));
            }
            return Result.ok(userList);
        } else {
            for (User user : existing_users) {
                String userId = user.getUserId();
                // Comparing each userId to the pattern received
                if (userId.toLowerCase().contains(pattern.toLowerCase())) {
                    userList.add(user);
                }
            }
        }
        return Result.ok(userList);
    }

    private List<User> getUserHibernate(String userId) {
        // Query used to select a user based on its id
        return Hibernate.getInstance().sql(String.format("SELECT * FROM User u WHERE u.userId LIKE '%%%s%%'", userId),
                User.class);
    }
}
