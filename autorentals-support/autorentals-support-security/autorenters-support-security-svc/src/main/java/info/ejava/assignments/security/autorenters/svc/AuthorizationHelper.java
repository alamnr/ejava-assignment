package info.ejava.assignments.security.autorenters.svc;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import info.ejava.examples.common.exceptions.ClientErrorException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationHelper {
    public Optional<UserDetails> getUserDetails(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(null!=auth && auth.getPrincipal() instanceof UserDetails){
            return Optional.of((UserDetails)auth.getPrincipal());
        } else {
            log.warn("no UserDetails found for thread, check security configuration");
            return Optional.empty();
        }

    }

    public Optional<String> getUsername() {
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
        Optional<String> username = Optional.empty();
        if(null==auth && auth.getPrincipal()==null){
            log.warn("no UserDetails found for thread, check security configuration.");
            return Optional.empty();
        } else {
            Object principal = auth.getPrincipal();
            if(principal instanceof UserDetails){
                username = Optional.of(((UserDetails)principal).getUsername());

            } else if (principal instanceof String) {
                username = Optional.of((String)principal);
            } else if (null != principal){
                log.warn("unable to determine principal type for {}", principal);
            }
        }
        return username;
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return null != auth ?  auth.isAuthenticated(): false;
    }

    public boolean isUsername( String requiredUsername) {
        String username = getUsername().orElse(null);
        return StringUtils.equals(username,requiredUsername);
    }

    public void assertUsername(Supplier<String> requiredUsername) {
        try {
            if (isUsername(requiredUsername.get())) {
                return;
            }
        } catch (ClientErrorException.NotFoundException ex) {
            /* resource not found -- deny access */
        }
        throw new AccessDeniedException(
                String.format("%s is not required username for %s", getUsername().orElse("null"), requiredUsername.get()));
    }
     public boolean hasAuthority(String authority) {
        return getUserDetails()
                .map(
                        // ud->ud.getAuthorities().stream()
                        //     .map(a->a.getAuthority().equals(authority))
                        //     .filter(match->match)
                        //     .findFirst()
                        //     .orElse(false)
                        // or anyMatch() automatically stops after finding the first true (short-circuiting).
                        // No need for .map(), .filter(), or .findFirst().
                        ud->ud.getAuthorities().stream().anyMatch(a->a.getAuthority().equals(authority))
                    )
                .orElse(false);
                
                
    }


    public boolean hasAnyAuthority(List<String> authorities) {
        return authorities.stream()
                .map(a->hasAuthority(a))
                .filter(match->match)
                .findFirst()
                .orElse(false);
        
        // or anyMatch() automatically stops after finding the first true (short-circuiting).
        // No need for .map(), .filter(), or .findFirst().
        // return authorities.stream().anyMatch(this::hasAuthority); 
    }

    public void assertRules(Supplier<Boolean> rules, Function<String, String> reason) {
        if (rules!=null && !rules.get()) {
            throw new AccessDeniedException(reason.apply(getUsername().orElse(null)));
        }
    }


    @PreAuthorize("hasRole('MEMBER')")
    public boolean assertMember() { 
        return true; 
    }
    @PreAuthorize("hasRole('ADMIN')")
    public boolean assertAdmin() { 
        return true; 
    }
    @PreAuthorize("hasRole('MGR')")
    public boolean assertMgr() { 
        return true; 
    }
    
    @PreAuthorize("hasAuthority('PROXY')")
    public boolean assertProxy() { 
        return true; 
    }
}
