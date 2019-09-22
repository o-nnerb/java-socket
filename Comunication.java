import java.util.Optional;

public class Comunication {
      private Optional<Protocol> send = Optional.empty();
      private Integer buffer = 0;
      private Optional<ComunicationResult> result = Optional.empty();

      public static Comunication send(Protocol protocol) {
            Comunication self = new Comunication();
            self.send = Optional.of(protocol);
            return self;
      }

      public Comunication onResponse(ComunicationResult result) {
            this.result = Optional.of(result);
            return this;
      }

      public Comunication buffer(Integer buffer) {
            this.buffer = buffer;
            return this;
      }

      public static Comunication empty() {
            return new Comunication();
      }

      public ResultType execute(SocketProvider socket, Boolean isRecursive) {
            this.send.ifPresent(object -> {
                  socket.write(object.asRaw().bytes);
            });

            if (!this.result.isPresent()) {
                  return ResultType.init(this.send.get());
            }
            
            ResultType result = this.result.get().result(socket.read(this.buffer));

            if (result.comunication().isPresent() && !isRecursive) {
                  return result.comunication().get().execute(socket, false);
            }

            return result;
      }

      public ResultType recursive(SocketProvider socket) {
            ResultType result = ResultType.init(this);
            while (result.comunication().isPresent()) {
                  result = result.comunication().get().execute(socket, true);
            }

            return result;
      }

      public static Comunication accepted() {
            return Comunication.send(Protocol.accepted());
      }

      public static Comunication refused() {
            return Comunication.send(Protocol.refused());
      }

      public static Comunication notAllowed() {
            return Comunication.send(Protocol.notAllowed());
      }
}