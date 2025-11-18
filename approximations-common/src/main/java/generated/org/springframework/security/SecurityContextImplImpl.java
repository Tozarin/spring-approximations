package generated.org.springframework.security;

import generated.org.springframework.boot.SymbolicValueFactory;
import generated.org.springframework.boot.pinnedValues.PinnedValueSource;
import org.jacodb.approximation.annotation.Approximate;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.usvm.api.Engine;
import org.usvm.spring.api.SpringEngine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static generated.org.springframework.boot.pinnedValues.PinnedValueStorage.writePinnedValue;

@Approximate(SecurityContextImpl.class)
public class SecurityContextImplImpl {

    private static Authentication cachedAuthentication = null;

    private static Class<? extends UserDetails> _getUserClass() {
        throw new IllegalStateException("This method must be approximated!");
    }

    private static void assumeUserInvariants(UserDetails user, Class<?> userClass) {
        Field[] fields = userClass.getDeclaredFields();
        for (Field field : fields) {
            if (shouldSymbolize(field)) symbolizeField(user, field);
        }

        String username = user.getUsername();
        String password = user.getPassword();

        Engine.assume(username != null);
        Engine.assume(password != null);
        Engine.assumeSoft(username.equals("Test user"));
        Engine.assumeSoft(password.equals("Test password"));

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assumeRolesCorrectness(authorities);
    }

    private static boolean shouldSymbolize(Field field) {
        Class<?> fieldType = field.getType();
        return fieldType == Set.class || fieldType == Collection.class;
    }

    private static void assumeRolesCorrectness(Collection<? extends GrantedAuthority> authorities) {
        Engine.assume(authorities.size() < 3);
        for (Object authority : authorities) {
            Engine.assume(authority != null);
            Engine.assume(authority instanceof GrantedAuthority);
            Engine.assume(((GrantedAuthority)authority).getAuthority() != null);
        }
    }

    private static void symbolizeField(UserDetails user, Field field) {
        try {
            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            if (Set.class == fieldType) {
                Object set = SymbolicValueFactory.createSymbolic(HashSet.class, false, false);
                Engine.assume(((HashSet<?>) set).size() < 4);
                field.set(user, set);
            } else if (Collection.class == fieldType) {
                Object arrayList = SymbolicValueFactory.createSymbolic(ArrayList.class, false, false);
                Engine.assume(((ArrayList<?>) arrayList).size() < 4);
                field.set(user, arrayList);
            } else {
                SpringEngine.println(
                        String.format("Warning! Can't symbolize field %s in UserInvariants", field.getName()));
            }

            SpringEngine.println(String.format(
                    "user.%s is rewritten to new %s",
                    field.getName(),
                    fieldType.getSimpleName()
            ));

        } catch (IllegalAccessException e) {
            SpringEngine.println("Warning! Error assuming field data: " + e.getMessage());
        }
    }

    private static Authentication createAuthenticationToken(UserDetails user) {
        Authentication result = new UsernamePasswordAuthenticationToken(
                user,
                null,
                Collections.emptyList()
        );
        try {
            Field authoritiesField = AbstractAuthenticationToken.class.getDeclaredField("authorities");
            authoritiesField.setAccessible(true);
            authoritiesField.set(result, user.getAuthorities());
        } catch (Exception e) {
            SpringEngine.println("Warning! Error setting authorities! " + e.getMessage());
        }
        return result;
    }

    private static Authentication getSymbolicAuthentication() {
        Class<? extends UserDetails> userClass = _getUserClass();
        UserDetails user = Engine.makeKotlinSymbolicSubtype(userClass);
        assumeUserInvariants(user, userClass);
        writePinnedValue(PinnedValueSource.REQUEST_USER, user);

        return createAuthenticationToken(user);
    }

    public Authentication getAuthentication() {
        if (cachedAuthentication != null)
            return cachedAuthentication;

        Authentication result = getSymbolicAuthentication();
        cachedAuthentication = result;
        return result;
    }
}
