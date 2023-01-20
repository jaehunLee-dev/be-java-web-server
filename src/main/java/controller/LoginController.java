package controller;

import model.domain.User;
import model.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HtmlEditor;
import util.HttpStatus;
import util.Session;
import view.RequestMessage;
import view.Response;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.Collection;

public class LoginController implements Controller{
    private static LoginController loginController;
    private final UserService userService = UserService.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private static final String RELATIVE_PATH = "./src/main/resources";

    private LoginController(){}

    public static LoginController getInstance(){
        if (loginController == null){
            synchronized (UserController.class){
                if (loginController == null){
                    loginController = new LoginController();
                }
            }
        }
        return loginController;
    }

    @Override
    public void control(RequestMessage requestMessage, OutputStream out) {
        byte[] body = getStaticBody(requestMessage);
        logger.info("body with static page:\n"+body.toString());
        if (requestMessage.getRequestHeaderMessage().getHttpOnlyURL().contains("html"))
            body = changeNavbar(body, requestMessage);
        if (requestMessage.getRequestHeaderMessage().getHttpOnlyURL().startsWith("/user/list.html"))
            body = dynamicListPage(body);
        logger.info("body after dynamic change:\n"+body.toString());
        Response response = new Response(new DataOutputStream(out));
        response.response(body,requestMessage.getRequestHeaderMessage(), HttpStatus.Success);
    }

    private byte[] changeNavbar(byte[] body, RequestMessage requestMessage){
        String userID = Session.loginSession.get(requestMessage.getRequestHeaderMessage().getSessionId());
        body = HtmlEditor.removeHref(body);
        body = HtmlEditor.editNavbar(body,userID);
        return body;
    }

    //new branch

    private byte[] dynamicListPage(byte[] body){
        Collection<User> users = userService.findUsers();
        return HtmlEditor.appendUserList(body,users);
    }

    private byte[] getStaticBody(RequestMessage requestMessage){
        if (requestMessage.getRequestHeaderMessage().getHttpOnlyURL().contains(".")) {
            String fileURL = RELATIVE_PATH + requestMessage.getRequestHeaderMessage().getSubPath() + requestMessage.getRequestHeaderMessage().getHttpOnlyURL();
            return getBodyFile(fileURL);
        }
        if (requestMessage.getRequestHeaderMessage().getHttpOnlyURL().startsWith("/user")) {
            //todo: 로그인 후 user에 대한 조작 명령이 있을 때 여기서 처리
        }
        return new byte[0];
    }
}
