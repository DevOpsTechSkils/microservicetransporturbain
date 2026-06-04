package cm.transport.service_clients_2.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ClientDejaExistantException extends RuntimeException {
    public ClientDejaExistantException(String message) {
        super(message);
    }
}