import java.util.ArrayList;
import java.util.Optional;

public class ServerTransaction extends Transaction {
      Optional<SocketDelegate> delegate = Optional.empty();
      ServerTransaction(Provider provider) {
            super(provider);
      }

      ServerTransaction(Provider provider, Integer buffer) {
            super(provider, buffer);
      }

      ArrayList<Optional<Thread>> threads = new ArrayList<>();
      //final String name;
      //final Integer port;

      //private ServerTransaction(St)

      public static Optional<ServerTransaction> tcp(Integer port) {
            Optional<Transaction> transaction = connect(ProviderType.tcp, port);
            if(transaction.isEmpty()) {
                  return Optional.empty();
            }

            return Optional.of(new ServerTransaction(transaction.get().provider, transaction.get().buffer));
      }

      public static Optional<ServerTransaction> udp(Integer port) {
            Optional<Transaction> transaction = connect(ProviderType.udp, port);
            if(transaction.isEmpty()) {
                  return Optional.empty();
            }

            return Optional.of(new ServerTransaction(transaction.get().provider, transaction.get().buffer));
      }
      
      public void updateThreads(Thread thread) {
            thread.start();
            this.threads.add(Optional.of(thread));
      }

      public Optional<ClientThread> openThread(String ip, Integer port, Optional<TransactionSocket> socket) {
            return ClientThread.open(ip, port)
                  .andConnection(socket)
                  .withDelegate(this.delegate)
                  .create();
      }

      public Boolean connect() {
            Optional<TransactionSocket> socket = this.accept();
            socket.ifPresent(t -> {
                  String ip = t.socket.ip();
                  Integer port = t.socket.port();
                  System.out.println("Got connection from " + ip + " " + port.toString());

                  this.openThread(ip, port, Optional.of(t)).ifPresent(thread -> {
                        this.updateThreads(thread);
                  });
            });

            return socket.isPresent();
      }
      
      @Override
      public void socketDidClose() {
            super.socketDidClose();

            for(int i = 0; i < this.threads.size(); i++) {
                  if (this.threads.get(i).isEmpty()) {
                        this.threads.remove(i);
                  } else {
                        if (!this.threads.get(i).get().isAlive()) {
                              this.threads.set(i, Optional.empty());
                        }
                  }
            }
      }

      public static void start(Optional<ServerTransaction> optional) {
            optional.ifPresent(server -> {
                  while (server.connect());

                  for (Optional<Thread> t: server.threads) {
                        t.ifPresent(thread -> {
                              try { thread.join(); }
                              catch (Exception e) {
                                    System.out.println(e.getMessage());
                              }
                        });
                  }
            });
      }

      public Optional<ClientThread> thread(final long clientId) {
            for (Optional<Thread> client: this.threads) {
                  if (client.isPresent() && client.get().isAlive()) {
                        if (client.get().getId() == clientId) {
                              return Optional.of((ClientThread) client.get());
                        }
                  }
            }

            return Optional.empty();
      }
}