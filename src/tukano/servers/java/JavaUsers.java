package tukano.servers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Users;
import tukano.persistence.Hibernate;

public class JavaUsers implements Users {

    private final Map<String,User> users = new HashMap<>();

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    @Override
    public Result<String> createUser(User user) {
        Log.info("Info Received createUser : " + user);

        // Check if user data is valid
        if(user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null) {
            Log.info("User object invalid.");
            return Result.error( ErrorCode.BAD_REQUEST);
        }

        // Check if user with same userId already exists
        List<User> existingUsers = Hibernate.getInstance().sql(String.format("SELECT * FROM User u WHERE u.userId LIKE '%%%s%%'", user.getUserId()), User.class);
        // Insert user, checking if name already exists
        if(!existingUsers.isEmpty()) {
            Log.info("User already exists.");
            return Result.error( ErrorCode.CONFLICT);
        }

        Hibernate.getInstance().persist(user);
        return Result.ok( user.userId() );
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        Log.info("Info Received getUser : userId = " + userId + "; pwd = " + pwd);
        // Check if user is valid
        if(userId == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error( ErrorCode.BAD_REQUEST);
        }

        User user = users.get(userId);
        // Check if user exists
        if( user == null ) {
            Log.info("User does not exist.");
            return Result.error( ErrorCode.NOT_FOUND);
        }

        //Check if the password is correct
        if( !user.pwd().equals( pwd)) {
            Log.info("Password is incorrect.");
            return Result.error( ErrorCode.FORBIDDEN);
        }

        return Result.ok(user);
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {
        Log.info("Info Received updateUser : userId = " + userId + "; pwd = " + pwd + "; email = " + user.getEmail() + "; displayName = " + user.getDisplayName());
        // Check if user is valid
        if (userId == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Check if user exists
        if (users.get(user.getUserId()) == null) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        //Check if the password is correct
        if (!user.pwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        users.replace(userId, user);
        return Result.ok(user);
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        Log.info("Info Received deleteUser : userId = " + userId + "; pwd = " + pwd);
        // Check if user is valid
        if (userId == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        User user = users.get(userId);
        // Check if user exists
        if (user == null) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        //Check if the password is correct
        if (!user.pwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        users.remove(userId);
        return Result.ok(user);
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Log.info("Info Received searchUsers : pattern = " + pattern);
        List<User> userList = new ArrayList<>();
        if (users.isEmpty()) {
            return Result.ok(userList);
        } else if (pattern == null) {
            userList.addAll(users.values());
            return Result.ok(userList);
        } else {
            String[] brokenPattern = pattern.split(" ");

            for (String s : brokenPattern) {
                if (users.containsKey(s)) {
                    User user = users.get(s);
                    userList.add(userList.size(), user);
                }
            }

        }
        return Result.ok(userList);
    }
}
