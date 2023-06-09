package com.example.project.Controller;

import com.example.project.Security.Exception.TokenRefreshException;
import com.example.project.Entity.*;
import com.example.project.Payload.Request.*;
import com.example.project.Entity.Response.JwtResponse;
import com.example.project.Entity.Response.MessageResponse;
import com.example.project.Entity.Response.TokenRefreshResponse;
import com.example.project.Repository.RoleRepository;
import com.example.project.Repository.UserRepository;
import com.example.project.Security.Jwt.JwtUtils;
import com.example.project.Service.LocationService;
import com.example.project.Service.Impl.BlacklistService;
import com.example.project.Service.Impl.RefreshTokenService;
import com.example.project.Service.Impl.UserDetailsImpl;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    BlacklistService blacklistService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    LocationService locationService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;


    @PostMapping("/user/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserSignUp userSignUp) throws Exception {
        StringBuilder reqMessage = new StringBuilder();
        reqMessage.append("Message = \"Executing registerUser Endpoint\",");
        reqMessage.append("method = [POST],");
        reqMessage.append("path = [/api/auth/user/signup],");
        reqMessage.append("status = "+HttpStatus.OK.value());
        try {
            logger.debug("Checking if the username already exists");
            if (userRepository.existsUserByUsername(userSignUp.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
            }
            logger.debug("Checking if the Email already exists");
            if (userRepository.existsUsersByEmail(userSignUp.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email already exists"));
            }

            logger.debug("Creating new user object");
            // Create new user's account
            User user = new User(userSignUp.getUsername(), encoder.encode(userSignUp.getPassword()), userSignUp.getEmail());

            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            user.setRoles(roles);

            Location location = locationService.getLocation(userSignUp.getLocationId());
            user.setLocation(location);

            logger.debug("Persisting the new User details into database");
            userRepository.save(user);

            logger.info(reqMessage);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        }
        catch(Exception e){
            StringBuilder errMessage = new StringBuilder();
            errMessage.append("Message = \"Error while Registering User\",");
            errMessage.append("method = [POST],");
            errMessage.append("path = [/api/auth/user/signup],");
            errMessage.append("status = "+500);
            errMessage.append(",ExceptionMessage = "+e.getMessage());
            logger.error(errMessage);
            throw new Exception();
        }
    }

    @PostMapping("/user/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody UserLogin userLogin) throws Exception {
        StringBuilder reqMessage = new StringBuilder();
        reqMessage.append("Message = \"Executing authenticateUser Endpoint\",");
        reqMessage.append("method = [POST],");
        reqMessage.append("path = [/api/auth/user/signin],");
        reqMessage.append("status = "+HttpStatus.OK.value());
        try {
            //System.out.println(userLogin.getUsername()+userLogin.getPassword());
            logger.debug("Authenticating username and password");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Generating JWT token");
            String jwt = jwtUtils.generateJwtToken(authentication);

            logger.debug("Authorizing User role");
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            logger.debug("Creating refresh Token");
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            logger.debug("Returning Response which contains auth token");
            logger.info(reqMessage);
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles,
                    refreshToken.getToken()));
        }
        catch(Exception e){
            StringBuilder errMessage = new StringBuilder();
            errMessage.append("Message = \"Error Executing Authenticating User\",");
            errMessage.append("method = [POST],");
            errMessage.append("path = [/api/auth/user/signin],");
            errMessage.append("status = "+500);
            errMessage.append(",ExceptionMessage = "+e.getMessage());
            logger.error(errMessage);
            throw new Exception();
        }
    }

    @PostMapping("/user/refreshtoken")
    public ResponseEntity<?> refreshtoken(@RequestBody TokenRefreshRequest request) throws Exception {
        StringBuilder reqMessage = new StringBuilder();
        reqMessage.append("Message = \"Executing refreshtoken Endpoint\",");
        reqMessage.append("method = [POST],");
        reqMessage.append("path = [/api/auth/user/refreshtoken],");
        reqMessage.append("status = "+HttpStatus.OK.value());
        try {
            String requestRefreshToken = request.getRefreshToken();

            logger.debug("Generating new JWT using the refreshToken");
            logger.info(reqMessage);
            return refreshTokenService.findByToken(requestRefreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                        return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                    })
                    .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
        }
        catch(Exception e){
            StringBuilder errMessage = new StringBuilder();
            errMessage.append("Message = \"Error Executing JWT generation using RefreshToken\",");
            errMessage.append("method = [POST],");
            errMessage.append("path = [/api/auth/user/refreshtoken],");
            errMessage.append("status = "+500);
            errMessage.append(",ExceptionMessage = "+e.getMessage());
            logger.error(errMessage);
            throw new Exception();
        }
    }

    @GetMapping("/user/signout/{user_id}")
    @PreAuthorize("hasRole('USER')")
    public void signout(@RequestHeader(value="Authorization") String token, @PathVariable Integer user_id) throws Exception {
        StringBuilder reqMessage = new StringBuilder();
        reqMessage.append("Message = \"Executing signout Endpoint\",");
        reqMessage.append("method = [GET],");
        reqMessage.append("path = [/api/auth/user/signout/{user_id}],");
        reqMessage.append("status = "+HttpStatus.OK.value());
        try {
            logger.info(reqMessage);
            logger.debug("Blacklisting JWT token and deleting refresh token");
            Blacklist blacklist = new Blacklist(token);
            blacklistService.setToken(blacklist);
            blacklistService.deleteRefreshTokenUser(user_id);
        }
        catch(Exception e){
            StringBuilder errMessage = new StringBuilder();
            errMessage.append("Message = \"Error while Signing Out\",");
            errMessage.append("method = [GET],");
            errMessage.append("path = [/api/auth/user/signout/{user_id}],");
            errMessage.append("status = "+500);
            errMessage.append(",ExceptionMessage = "+e.getMessage());
            logger.error(errMessage);
            throw new Exception();
        }
    }

}
