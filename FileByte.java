import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Optional;

public class FileByte {
    final File file;
    ArrayList<ArrayList<Byte>> fragments;
    final Integer bufferSize;
    final Boolean isWritabble;
    final String name;
    Integer dataCount = 0;
    Integer lastReaded = 0;

    private Boolean eof;

    public FileByte(String fileName, File file) {
        this.file = file;
        this.fragments = new ArrayList<ArrayList<Byte>>();
        this.bufferSize = 0;
        this.eof = false;
        this.isWritabble = false;
        this.name = fileName;
        this.dataCount = 0;
    }

    public FileByte(String fileName, File file, Integer bufferSize, Boolean isWritabble) {
        this.file = file;
        this.fragments = new ArrayList<ArrayList<Byte>>();
        this.bufferSize = bufferSize;
        this.eof = false;
        this.isWritabble = isWritabble;
        this.name = fileName;
        this.dataCount = 0;

        if (isWritabble && file.exists()) {
            this.eraseFile();
        }
    }

    private void eraseFile() {
        try {
            this.file.delete();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Boolean isEndOfFile() {
        return this.eof;
    }

    public ArrayList<Byte> chunck(Integer bufferSize) {
        bufferSize = bufferSize >= 1 ? bufferSize : this.bufferSize;

        ArrayList<Byte> toReturn = new ArrayList<Byte>();

        try {
            final FileInputStream fis = new FileInputStream(this.file);
            fis.skip(this.lastReaded);

            for (Integer i = 0; i < bufferSize; i++) {
                final Integer readed = fis.read();
                if (readed < 0) {
                    this.eof = true;
                    break;
                }

                toReturn.add(readed.byteValue());
                this.lastReaded += 1;
            }

            fis.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return toReturn;
    }

    public Optional<ArrayList<Byte>> next(Integer fragmentSize, Integer bufferSize) {
        if (this.isEndOfFile()) {
            return Optional.empty();
        }

        if (this.fragments.isEmpty()) {
            for (Integer i = 0; i < fragmentSize; i++) {
                ArrayList<Byte> chunck = this.chunck(bufferSize);
                if (chunck.isEmpty()) {
                    this.eof = true;
                    return Optional.of(chunck);
                }

                this.fragments.add(chunck);
            }
        }

        if (this.fragments.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(this.fragments.remove(0));
    }

    public Optional<ArrayList<Byte>> next(Integer fragmentSize) {
        return this.next(fragmentSize, 0);
    }

    private void writeFragments() {
        try {
            FileOutputStream out = new FileOutputStream(this.file, true);
            
            for (ArrayList<Byte> fragment : this.fragments) {
                byte[] barray = new byte[fragment.size()];
                for(Integer i = 0; i < fragment.size(); i++) {
                    barray[i] = fragment.get(i);
                }

                out.write(barray);
            }

            this.fragments.clear();
            out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void append(ArrayList<Byte> data, Integer fragmentSize) {
        if (this.fragments.size() >= fragmentSize) {
            this.writeFragments();
        }

        this.fragments.add(data);
    }

    public void saveAll() {
        if (!this.isWritabble) {
            return;
        }

        this.writeFragments();
    }

    public void saveAndClose() {
        this.saveAll();
        this.close();
    }

    public static Optional<FileByte> open(String fileName, Integer bufferSize, Boolean isWritabble) {        
        File file = Paths.get(fileName).toFile();

        if (!isWritabble && !file.exists()) {
            System.out.println("Do not exists");
            return Optional.empty();
        }

        FileByte fb = new FileByte(fileName, file, bufferSize, isWritabble);
        return Optional.of(fb);
    }

    public void close() {
        this.fragments.removeAll(null);
    }

    public String md5() {
        try {
            File file = this.file.getAbsoluteFile();
            FileInputStream input = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            md.update(input.readAllBytes());
            input.close();

            StringBuilder sb = new StringBuilder();
            
            for(byte b: md.digest()) {
                sb.append(String.format("%02x", b));
            }

            return  sb.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }
}