import java.util.Optional;

public class ClientConstructor {
      final String ip;
      final Integer port;
      Optional<TransactionSocket> socket = Optional.empty();
      Optional<SocketDelegate> delegate = Optional.empty();

      ClientConstructor(String ip, Integer port) {
            this.ip = ip;
            this.port = port;
      }

      public ClientConstructor andConnection(Optional<TransactionSocket> socket) {
            this.socket = socket;
            return this;
      }

      public ClientConstructor withDelegate(Optional<SocketDelegate> delegate) {
            this.delegate = delegate;
            return this;
      }

      public Optional<ClientThread> create() {
            return ClientThread.constructor(this);
      }
}