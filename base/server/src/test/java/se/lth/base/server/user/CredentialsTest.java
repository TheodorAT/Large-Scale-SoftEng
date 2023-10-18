package se.lth.base.server.user;

import org.junit.Test;
import se.lth.base.server.user.Credentials;
import se.lth.base.server.user.Role;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CredentialsTest {
    /**
     * Tests if salt is applied correctly when generating password hashes.
     * 
     * @desc Tests if salt works correctly.
     */
    @Test
    public void saltApplied() {
        Credentials a = new Credentials("a", "123", Role.NONE, "User", "User", "user@user1.se", "+4600000001");
        Credentials b = new Credentials("b", "123", Role.NONE, "User", "User", "user@user2.se", "+4600000001");
        UUID pwd = a.generatePasswordHash(1L);
        UUID m = b.generatePasswordHash(2L);
        assertNotEquals(pwd, m);
        assertEquals(pwd, a.generatePasswordHash(1L));
    }
}
