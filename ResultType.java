import java.util.Optional;

public class ResultType {
      final Object result;

      private ResultType(Object o) {
            this.result = o;
      }

      public static ResultType init(Protocol p) {
            return new ResultType(p);
      }

      public static ResultType init(Comunication c) {
            return new ResultType(c);
      }

      public static ResultType init(Boolean b) {
            return new ResultType(b);
      }

      public Optional<Protocol> protocol() {
            if(this.result instanceof Protocol) {
                  return Optional.of((Protocol) this.result);
            }

            return Optional.empty();
      }

      public Optional<Comunication> comunication() {
            if(this.result instanceof Comunication) {
                  return Optional.of((Comunication) this.result);
            }

            return Optional.empty();
      }

      public Optional<Boolean> bool() {
            if(this.result instanceof Boolean) {
                  return Optional.of((Boolean) this.result);
            }

            return Optional.empty();
      }

      public Boolean isFalse() {
            return this.bool().isPresent() && !this.bool().get();
      }

}