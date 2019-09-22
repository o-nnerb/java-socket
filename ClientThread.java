import java.util.Optional;

public class ClientThread extends Thread {
      final String ip;
      final Integer port;
      final TransactionSocket socket;

      private ClientThread(String ip, Integer port, TransactionSocket socket) {
            this.ip = ip;
            this.port = port;
            this.socket = socket;
      }

      public static Optional<ClientThread> constructor(ClientConstructor constructor) {
            if (!constructor.socket.isPresent()) {
                  return Optional.empty();
            }

            return Optional.of(new ClientThread(
                  constructor.ip,
                  constructor.port,
                  constructor.socket.get()
            ));
      }

      private ResultType asGuest(Protocol protocol) {
            // if protocol.code == ResponseCode.auth:
            //     return self.auth(protocol)

            // if something
            //    return

            // else [error]
            return ResultType.init(Comunication.send(Protocol.notAllowed()));
      }

      private void didReceived(Protocol protocol) {
            Optional<ResultType> result = Optional.empty();
            // if self.isAuthenticated:
            //     toCommit = self.asUser(protocol)
            if (false) {}
            else {
                  result = Optional.of(this.asGuest(protocol));
            }

            if (result.isEmpty()) {
                  this.socket.commit(Comunication.refused());
                  return;
            }
            
            result.ifPresent(r -> {
                  r.comunication() .ifPresent(c -> {
                        this.socket.commit(c);
                  });
            });
      }

      private Boolean observe() {
            ResultType result = this.socket.waitForResponse();
            
            if (result.isFalse()) {
                  this.socket.commit(Comunication.send(Protocol.cutConnection()));
                  return false;
            }

            if (result.comunication().isPresent()) {
                  System.out.println("[Error] Comunication transaction did not finish");
                  this.socket.commit(Comunication.send(Protocol.cutConnection()));
                  return false;
            }

            System.out.println(result.protocol().get().asString());
            this.didReceived(result.protocol().get());
            return true;
      }

      @Override
      public void run() {
            while (this.observe());
            this.socket.close();
            this.socket.notifyTransaction();
      }

      public static ClientConstructor open(String ip, Integer port) {
            return new ClientConstructor(ip, port);
      }
}