package ru.mail.track.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.track.commands.base.Command;
import ru.mail.track.message.CommandResultMessage;
import ru.mail.track.message.Message;
import ru.mail.track.message.MessageStore;
import ru.mail.track.message.SendMessage;
import ru.mail.track.session.Session;

import java.util.List;

/**
 * Вывести список всех чатов пользователя
 */
public class ChatListCommand extends Command {

    static Logger log = LoggerFactory.getLogger(ChatListCommand.class);

    private MessageStore messageStore;

    public ChatListCommand() {
        super();
        name = "chat_list";
        description = "<> Получить список чатов пользователя (только для залогиненных пользователей)";
    }

    public ChatListCommand(MessageStore messageStore) {
        this();
        this.messageStore = messageStore;
    }


    @Override
    public CommandResultMessage execute(Session session, Message msg) {
        CommandResultMessage commandResult = new CommandResultMessage();
        commandResult.setStatus(CommandResultMessage.Status.OK);

        SendMessage chatListMsg = (SendMessage) msg;
        if (session.getSessionUser() != null) {
            List<Long> chatIds = messageStore.getChatsByUserId(session.getSessionUser().getId());
            if (chatIds.isEmpty()) {
                commandResult.setResponse("You have no any chats.");
            } else {
                String answer = "Your chats:";
                for (Long chatId : chatIds) {
                    answer += " " + chatId;
                }
                commandResult.setResponse(answer);
            }
            log.info("Success chat_list: {}", session.getSessionUser());
        } else {
            commandResult.setStatus(CommandResultMessage.Status.NOT_LOGGINED);
            log.info("User isn't logged in.");
        }

        return commandResult;
    }
}
