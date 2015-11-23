package ru.mail.track.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.track.AuthorizationService;
import ru.mail.track.commands.base.Command;
import ru.mail.track.message.CommandResultMessage;
import ru.mail.track.message.LoginMessage;
import ru.mail.track.message.Message;
import ru.mail.track.message.User;
import ru.mail.track.net.SessionManager;
import ru.mail.track.session.Session;

/**
 * Выполняем авторизацию по этой команде
 */
public class LoginCommand extends Command {

    static Logger log = LoggerFactory.getLogger(LoginCommand.class);

    private AuthorizationService authService;
    private SessionManager sessionManager;

    public LoginCommand() {
        super();
        name = "login";
        description = "<login> <password> или <login> <password> <repeat_password> Залогиниться или зарегестрироваться.";
    }

    public LoginCommand(AuthorizationService authService, SessionManager sessionManager) {
        this();
        this.authService = authService;
        this.sessionManager = sessionManager;
    }


    @Override
    public CommandResultMessage execute(Session session, Message msg) {
        CommandResultMessage commandResult = new CommandResultMessage();
        commandResult.setStatus(CommandResultMessage.Status.OK);

        LoginMessage loginMsg = (LoginMessage) msg;
        String name = loginMsg.getLogin();
        String password = loginMsg.getPass();
        switch (loginMsg.getArgType()) {
            case LOGIN:
                if (session.getSessionUser() != null) {
                    log.info("User {} already logged in.", session.getSessionUser());
                    commandResult.setResponse("You have already logged in.");
                } else {
                    User user = authService.login(name, password);
                    if (user == null) {
                        log.info("login: Wrong login or password.");
                        commandResult.setResponse("Wrong login or password.");
                    } else {
                        session.setSessionUser(user);
                        sessionManager.registerUser(user.getId(), session.getId());
                        log.info("Success login: {}", user);
                        UserInfoCommand userInfoCommand = new UserInfoCommand(authService.getUserStore());

                        // Вывести информацию о себе
                        LoginMessage userInfoMessage = new LoginMessage();
                        userInfoMessage.setArgType(LoginMessage.ArgType.SELF_INFO);
                        return userInfoCommand.execute(session, userInfoMessage);
                    }
                }
                break;
            case CREAT_USER:
                User newUser = authService.creatUser(name, password);
                if (newUser == null) {
                    log.info("creat_user: The user with this name has already existed.");
                    commandResult.setResponse("The user with this name has already existed.");
                } else {
                    log.info("Success creat_user: {}", newUser);
                    commandResult.setResponse("The new user successfully was created.");
                }
                break;
            default:
                log.info("Wrong argType: {}", loginMsg.getArgType());
        }

        return commandResult;
    }
}
