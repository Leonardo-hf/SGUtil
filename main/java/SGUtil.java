import com.beust.jcommander.JCommander;

import java.io.*;
import java.util.Random;

public interface SGUtil {

    void decodeWithImage(String path);

    void decodeWithText(String path);

    void encodeWithImage(String path, String hideImgPath);

    void encodeWithText(String path, String text);

    default void encodeWithTextFromFile(String path, String textPath) {
        try {
            encodeWithText(path, getTextFromFile(textPath));
        } catch (SteganographyException e) {
            e.printStackTrace();
        }
    }

    private String getTextFromFile(String path) throws SteganographyException {
        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new SteganographyException("作为密文的文件不存在");
            }
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = bf.readLine()) != null) {
                text.append(line);
            }
            return text.toString();
        } catch (IOException e) {
            throw new SteganographyException("未知错误：" + e.getLocalizedMessage());
        }
    }

    default String getRandomName(int length) {
        String charSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder s = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            s.append(charSet.charAt(Math.abs(random.nextInt()) % charSet.length()));
        }
        return s.toString();
    }

    static void main(String[] args) {
        //args = new String[]{"-c", "src/main/resources/8qUVfl71.png", "-o", "decode"};
        args = new String[]{"--help"};
        try {
            SGArgs sgArgs = new SGArgs();
            JCommander cmd = JCommander.newBuilder().addObject(sgArgs).build();
            cmd.parse(args);
            if (sgArgs.isHelp()) {
                cmd.setProgramName("java -jar sgUtil.jar");
                cmd.usage();
                return;
            }
            SGUtil sgUtil;
            if (sgArgs.getMode().equals("LSB")) {
                sgUtil = new LSBSGUtil();
            } else if (sgArgs.getMode().equals("Simple")) {
                sgUtil = new SimpleSGUtil();
            } else {
                throw new SteganographyException("不存在的隐写模式");
            }
            if (sgArgs.getOption().equals("encode")) {
                if (!sgArgs.isImage() && sgArgs.getPath() != null) {
                    sgUtil.encodeWithTextFromFile(sgArgs.getPath(), sgArgs.getPath());
                } else if (!sgArgs.isImage() && sgArgs.getHideText() != null) {
                    sgUtil.encodeWithText(sgArgs.getPath(), sgArgs.getHideText());
                } else if (sgArgs.isImage() && sgArgs.getPath() != null) {
                    sgUtil.encodeWithImage(sgArgs.getPath(), sgArgs.getPath());
                } else {
                    throw new SteganographyException("至少需要一个隐写的内容");
                }
            } else if (sgArgs.getOption().equals("decode")) {
                if (sgArgs.isImage() && sgArgs.getPath() != null) {
                    sgUtil.decodeWithImage(sgArgs.getPath());
                } else if (!sgArgs.isImage() && sgArgs.getPath() != null) {
                    sgUtil.decodeWithText(sgArgs.getPath());
                } else {
                    throw new SteganographyException("至少需要一个解码的对象");
                }
            } else {
                throw new SteganographyException("应在encode或decode中选择");
            }
        } catch (SteganographyException e) {
            e.printStackTrace();
        }
    }
}
