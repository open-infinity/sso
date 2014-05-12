package org.openinfinity.sso.authserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.TreeMap;

/**
 * Server for managing OAUTH client requests.
 * 
 * @author Mika Salminen
 * @author Ilkka Leinonen
 *
 */
@Controller
@SessionAttributes(types = AuthorizationRequest.class)
public class AccessConfirmationController {

    private ClientDetailsService clientDetailsService;

    @Autowired
    public void setClientDetailsService(ClientDetailsService clientDetailsService) {
        this.clientDetailsService = clientDetailsService;
    }

    /**
     * Provides information about the access
     * 
     * @param authorizationRequest Represents the authorization request from the client.
     * @return ModelAndView for rendering purposes.
     * @throws Exception Thrown when exceptional behaviour happens.
     */
    @RequestMapping("/oauth/confirm_access")
    public ModelAndView getAccessConfirmation(@ModelAttribute AuthorizationRequest authorizationRequest) throws Exception {
        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(authorizationRequest.getClientId());
        Map<String, Object> model = new TreeMap<String, Object>();
        model.put("auth_request", authorizationRequest);
        model.put("client", clientDetails);
        return new ModelAndView("access_confirmation", model);
    }
    
}