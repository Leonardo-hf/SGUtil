import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.Parameter;
import lombok.Data;

@Data
public class SGArgs {
    @Parameter(names = {"--carrier", "-c"}, description = "input image path", arity = 1, required = true)
    private String path;

    @Parameter(names = {"--image", "-i"}, description = "change steganography object to image")
    private boolean image = false;

    @Parameter(names = {"--text", "-t"}, description = "text to Steganography", arity = 1)
    private String hideText;

    @Parameter(names = {"--path", "-p"}, description = "path of content to Steganography", arity = 1)
    private String hidePath;

    @Parameter(names = "--help", help = true)
    private boolean help;

    @Parameter(names = {"--mode", "-m"}, description = "mode of Steganography\n\tSimple: add content to the end of carrier\n\tLSB: lsb Steganography", arity = 1)
    private String mode = "LSB";

    @Parameter(names = {"--option", "-o"}, description = "encode or decode", required = true, arity = 1)
    private String option;
}
