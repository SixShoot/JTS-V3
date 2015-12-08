package ru.jts_dev.authserver.packets.in;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.jts_dev.authserver.controller.SessionService;
import ru.jts_dev.authserver.model.Account;
import ru.jts_dev.authserver.model.GameSession;
import ru.jts_dev.authserver.packets.IncomingMessageWrapper;
import ru.jts_dev.authserver.packets.out.LoginOk;
import ru.jts_dev.authserver.repositories.AccountRepository;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author Camelion
 * @since 08.12.15
 */
@Scope(SCOPE_PROTOTYPE)
@Component
public class RequestAuthLogin extends IncomingMessageWrapper {
    @Autowired
    private SessionService sessionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository repository;

    @Autowired
    private Random random;

    @Value("${authserver.accounts.autocreate}")
    private boolean accountsAutocreate;

    private byte[] data;

    @Override
    public void prepare() {
        data = readBytes(128);
    }

    @Override
    public void run() {
        GameSession session = sessionService.getSessionBy(getConnectionId());

        byte[] decrypted;
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
            rsaCipher.init(Cipher.DECRYPT_MODE, session.getRSAKeyPair().getPrivate());
            decrypted = rsaCipher.doFinal(data, 0x00, 0x80);
        } catch (Exception e) {
            session.send(null);
            return;
        }

        String login = new String(decrypted, 0x5E, 14, StandardCharsets.UTF_8).trim();
        String password = new String(decrypted, 0x6C, 16, StandardCharsets.UTF_8).trim();

        if (!repository.exists(login)) {
            if (accountsAutocreate)
                repository.save(new Account(login, passwordEncoder.encode(password)));
            else
                throw new RuntimeException("Account with login '" + login + "' not found in database");
        }
        Account account = repository.findOne(login);

        if (!passwordEncoder.matches(password, account.getPasswordHash()))
            throw new RuntimeException("Password don't match for account '" + login + "'");

        session.send(new LoginOk(random.nextInt(), random.nextInt()));
    }
}