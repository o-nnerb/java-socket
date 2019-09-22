import java.util.Optional;

public class ClientThread extends Thread {
      final String ip;
      final Integer port;
      final TransactionSocket socket;
      final SocketDelegate delegate;

      private ClientThread(String ip, Integer port, TransactionSocket socket, SocketDelegate delegate) {
            this.ip = ip;
            this.port = port;
            this.socket = socket;
            this.delegate = delegate;
      }

      public static Optional<ClientThread> constructor(ClientConstructor constructor) {
            if (!constructor.socket.isPresent()) {
                  return Optional.empty();
            }

            if (!constructor.delegate.isPresent()) {
                  return Optional.empty();
            }

            return Optional.of(new ClientThread(
                  constructor.ip,
                  constructor.port,
                  constructor.socket.get(),
                  constructor.delegate.get()
            ));
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

            this.delegate.received(result.protocol().get(), this.getId());
            this.waitForSend();
            this.send.ifPresent(protocol -> {
                  this.socket.commit(Comunication.send(protocol));
                  this.send = Optional.empty();
            });

            return true;
      }

      private Optional<Protocol> send = Optional.empty();

      public void send(Protocol protocol) {
            this.send = Optional.of(protocol);
      }

      private void waitForSend() {
            try {
                  synchronized(this) {
                        while(this.send.isEmpty()) {
                              this.wait();
                        }
                  }
            } catch (Exception e) {
                  System.out.println(e.getMessage());
                  while(this.send.isEmpty());
            }
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