package tukano.api;

import jakarta.persistence.Id;

public class Follow {
    @Id
    String following;
    String followedBy;

    public Follow(String following, String followedBy) {
        this.following = following;
        this.followedBy = followedBy;
    }

    public String getFollowing() {
        return following;
    }

    public String getFollowedBy() {
        return followedBy;
    }
}
