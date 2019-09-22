import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

public class ServerApp extends Server {
      private ArrayList<Integer> queue = new ArrayList<>();
      private final Integer buffer = (new Random()).nextInt(101);
      
      ServerApp(ServerTransaction transaction) {
            super(transaction);
      }

      private static Optional<ServerApp> mount() {
            Optional<ServerTransaction> server = ServerTransaction.tcp(9889);
            if (server.isEmpty()) {
                  return Optional.empty();
            }

            ServerApp app = new ServerApp(server.get());
            server.get().delegate = Optional.of(app);
            ServerTransaction.start(server);
            return Optional.of(app);
      }

      private void lock() {}

      @Override
      public void didSend(Protocol data, final long clientId) {
            super.didSend(data, clientId);

            // System.out.println("Closing connection");
            // this.close(clientId);
      }


      @Override
      public void received(Protocol data, long clientId) {
            super.received(data, clientId);

            System.out.println(data.asString());
            if (data.code == ResponseCode.data) {
                  Integer type = Integer.parseInt(new String(data.data.bytes));
                  
                  if (type == 2) {
                        System.out.println("Consumer send");
                        if (this.queue.isEmpty()) {
                              this.send(Protocol.refused(), clientId);
                              return;
                        } 

                        this.queue.remove(0);
                        this.send(Protocol.accepted(), clientId);
                        return;
                  }

                  if (type == 1) {
                        System.out.println("Worker send");
                        if (this.queue.size() >= this.buffer) {
                              this.send(Protocol.refused(), clientId);
                              return;
                        } 

                        this.queue.add(0);
                        this.send(Protocol.accepted(), clientId);
                        return;
                  }
            }

            this.send(Protocol.cutConnection(), clientId);
      }
      
      public static void main(String[] args) {
            ServerApp.mount().ifPresent(s -> { s.lock(); });
      }
}