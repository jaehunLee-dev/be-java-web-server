package controller;

import model.domain.User;
import model.repository.MemoryUserRepository;
import model.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtil;
import util.HttpStatus;
import util.Redirect;
import view.RequestHeaderMessage;
import view.Response;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class UserController implements Controller{
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final String RELATIVE_PATH = "./src/main/resources";
    private static final String USER_ID = "userId";
    private static final String PASSWORD = "password";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private Map<String,String> headerKV= new HashMap<>();
    byte[] body = new byte[0];
    private HttpStatus httpStatus = HttpStatus.ClientError;
    private RequestHeaderMessage requestHeaderMessage;
    private UserService userService = new UserService(new MemoryUserRepository());
    private OutputStream out;
    public UserController(){};
    public UserController(RequestHeaderMessage requestHeaderMessage, OutputStream out){
        this.requestHeaderMessage = requestHeaderMessage;
        this.out = out;
    }
    @Override
    public void control() {
        userCommand();
        Response response = new Response(new DataOutputStream(out));
        response.response(body,requestHeaderMessage, httpStatus, headerKV);
    }

    public void userCommand(){
        if (requestHeaderMessage.getRequestAttribute().equals("/create")){
            try{
                userCreate(requestHeaderMessage);
                setLocation(Redirect.getRedirectLink(requestHeaderMessage.getHttpOnlyURL()));
            } catch (IllegalStateException e){
                setLocation("/user/form.html");
                logger.debug(e.getMessage());
            }
        }
    }

    private void userCreate(RequestHeaderMessage requestHeaderMessage){
        Map<String,String> userInfo = HttpRequestUtil.parseQueryString(requestHeaderMessage.getHttpReqParams());
        userService.join(new User(userInfo.get(USER_ID),userInfo.get(PASSWORD),userInfo.get(NAME),userInfo.get(EMAIL)));
    }

    private void setLocation(String redirectLink){
        if (!redirectLink.equals("")){
            httpStatus = HttpStatus.Redirection;
            headerKV.put("Location",redirectLink);
        }
    }

    public String toString(){
        return "UserController: request " + requestHeaderMessage.getHttpOnlyURL();
    }

}
