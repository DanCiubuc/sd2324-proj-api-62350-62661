package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Likes {
    @Id
    String shortId;
    @Id
    String userId;
    public Likes() {
    }

    public Likes(String shortId, String userId) {
        super();
        this.shortId = shortId;
        this.userId = userId;
    }

    public String getShortId() {
        return shortId;
    }

    public String getUserId() {
        return userId;
    }
}
