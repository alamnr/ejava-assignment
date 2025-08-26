package info.ejava.alamnr.assignment3.security.identity;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class IdentityController {

    public static final String WHO_AM_I_PATH = "/api/whoAmI";
    public static final String AUTHORITIES_PATH = "/api/authorities";

    @RequestMapping(path = WHO_AM_I_PATH, method = RequestMethod.GET,produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> whoAmIGet(@AuthenticationPrincipal UserDetails userDetails) {
        String username = Optional.ofNullable(userDetails).map(ud->ud.getUsername()).orElse("(null)");
        log.info("username={}", username);
        return ResponseEntity.ok(username);
    }

    @RequestMapping(path = WHO_AM_I_PATH, method = RequestMethod.POST, produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> whoAmIPost(){
        Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();
        Object principal = Optional.ofNullable(authentication).map(auth->auth.getPrincipal()).orElse(null);
        String username = principal instanceof UserDetails ? ((UserDetails)principal).getUsername() : "(null)";
        log.info("username = {}", username);
        return ResponseEntity.ok(username);
    }

    @RequestMapping(path = AUTHORITIES_PATH, method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> hasAuthority(@RequestParam(name = "authority") String authority, 
                                    @AuthenticationPrincipal UserDetails userDetails){
            boolean result = null != userDetails && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(authority));
            return ResponseEntity.ok(Boolean.toString(result));
    }
    
}
