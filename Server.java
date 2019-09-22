

public class Server implements SocketDelegate {
      protected final ServerTransaction transaction;
      
      Server(ServerTransaction transaction) {
            this.transaction = transaction;
      }

      public void didSend(Protocol data, final long clientId) {}

      public void send(Protocol data, final long clientId) {
            transaction.thread(clientId).ifPresent(t -> {
                  synchronized(t) {
                        t.send(data);
                        t.notify();
                        this.didSend(data, clientId);
                  }
            });
      }

      public void received(Protocol data, final long clientId) {
            return;
      }

      public final void close(long clientId) {
            this.transaction.thread(clientId).ifPresent(t -> {
                  t.socket.close();
            });
      }
}