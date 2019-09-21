import java.util.ArrayList;
import java.util.Optional;

public class Data {
      final byte[] bytes;
  
      private Data(byte[] bytes) {
          this.bytes = bytes;
      }
  
      public static Data init(ArrayList<Byte> array) {
          byte[] data = new byte[array.size()];
          for(Integer i = 0; i < array.size(); i++) {
              data[i] = array.get(i);
          }
  
          return new Data(data);
      }
  
      public static Data init(byte[] data) {
          return new Data(data);
      }

      public static Data empty() {
            return new Data(new byte[0]);
      }

      public Integer size() {
            return this.bytes.length;
      }
  
      public Boolean startsWith(String prefix) {
            Optional<Data> data = this.slice(0, prefix.length());
            
            if (!data.isPresent()) {
                  return false;
            }

            return (new String(data.get().bytes)).equals(prefix);
      }

      public Boolean endsWith(String sufix) {
            Optional<Data> data = this.slice(this.bytes.length-sufix.length(), this.bytes.length);
            
            if (!data.isPresent()) {
                  return false;
            }

            return (new String(data.get().bytes)).equals(sufix);
      }
  
      public Optional<Data> slice(final Integer start, final Integer end) {
          if (start >= this.bytes.length) {
              return Optional.empty();
          }
  
          if (end > this.bytes.length) {
              return Optional.empty();
          }
  
          byte[] slice = new byte[end - start];
          for (Integer i = start; i < end; i++) {
            slice[i - start] = this.bytes[i];
          }
  
          return Optional.of(Data.init(slice));
      }
      
      public Data merge(Data left) {
            byte[] destination = new byte[this.bytes.length + left.bytes.length];
            
            System.arraycopy(this.bytes, 0, destination, 0, this.bytes.length);
            System.arraycopy(left.bytes, 0, destination, this.bytes.length, left.bytes.length);

            return Data.init(destination);
      }
}