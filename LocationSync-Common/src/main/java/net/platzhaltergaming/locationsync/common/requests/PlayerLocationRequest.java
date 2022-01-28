package net.platzhaltergaming.locationsync.common.requests;

import java.util.UUID;

import lombok.Data;
import net.platzhaltergaming.networker.common.requests.Request;

@Data
public class PlayerLocationRequest implements Request {

    public final static String SUBJECT = "location";

    private final UUID uniqueId;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public String getSubject() {
        return SUBJECT;
    }

}
