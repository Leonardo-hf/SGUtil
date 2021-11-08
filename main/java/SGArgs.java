import com.beust.jcommander.Parameter;

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

    @Parameter(names = {"--option", "-o"}, description = "encode or decode", arity = 1)
    private String option = "encode";

    @Override
    public String toString() {
        return "SGArgs{" +
                "path='" + path + '\'' +
                ", image=" + image +
                ", hideText='" + hideText + '\'' +
                ", hidePath='" + hidePath + '\'' +
                ", help=" + help +
                ", mode='" + mode + '\'' +
                ", option='" + option + '\'' +
                '}';
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isImage() {
        return image;
    }

    public void setImage(boolean image) {
        this.image = image;
    }

    public String getHideText() {
        return hideText;
    }

    public void setHideText(String hideText) {
        this.hideText = hideText;
    }

    public String getHidePath() {
        return hidePath;
    }

    public void setHidePath(String hidePath) {
        this.hidePath = hidePath;
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
