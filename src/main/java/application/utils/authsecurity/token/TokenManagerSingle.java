package application.utils.authsecurity.token;

/**
 * Created by mj on 9/6/16.
 */

import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * Implements simple token manager, that keeps a single token for each user. If user logs in again,
 * older token is invalidated.
 */
public class TokenManagerSingle implements TokenManager {

    private Map<String, UserDetails> validUsers = new HashMap<>();

    /**
     * This maps system users to tokens because equals/hashCode is delegated to User entity.
     * This can store either one token or list of them for each user, depending on what you want to do.
     * Here we store single token, which means, that any older tokens are invalidated.
     */
    private Map<UserDetails, TokenInfo> tokens = new HashMap<>();

    @Override
    public TokenInfo createNewToken(UserDetails userDetails) {
        String token;
        do {
            token = TokenUtils.generateToken();
        } while (validUsers.containsKey(token));

        TokenInfo tokenInfo = new TokenInfo(token, userDetails);
        removeUserDetails(userDetails);
        UserDetails previous = validUsers.put(token, userDetails);
        if (previous != null) {
            System.out.println(" *** SERIOUS PROBLEM HERE - we generated the same token (randomly?)!");
            return null;
        }
        tokens.put(userDetails, tokenInfo);

        return tokenInfo;
    }



    @Override
    public void removeUserDetails(UserDetails userDetails) {
        TokenInfo token = tokens.remove(userDetails);
        if (token != null) {
            validUsers.remove(token.getToken());
        }
    }

    @Override
    public UserDetails removeToken(String token) {
        UserDetails userDetails = validUsers.remove(token);
        if (userDetails != null) {
            tokens.remove(userDetails);
        }
        return userDetails;
    }

    @Override
    public UserDetails getUserDetails(String token) {
        return validUsers.get(token);
    }

    @Override
    public Collection<TokenInfo> getUserTokens(UserDetails userDetails) {
        return Arrays.asList(tokens.get(userDetails));
    }

    @Override
    public Map<String, UserDetails> getValidUsers() {
        return Collections.unmodifiableMap(validUsers);
    }
}