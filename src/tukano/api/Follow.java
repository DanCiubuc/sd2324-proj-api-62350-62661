package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Follow {
    @Id
    String following;
    @Id
    String followedBy;

    public Follow() {
    }

    public Follow(String following, String followedBy) {
        super();
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
