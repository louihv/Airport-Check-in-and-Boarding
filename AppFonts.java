import java.awt.*;
import java.io.InputStream;

public class AppFonts {

    private static Font poppinsRegular;
    private static Font poppinsItalic;
    private static Font poppinsMedium;
    private static Font poppinsSemiBold;
    private static Font poppinsBold;

    public static void loadFonts() {
        try {
            GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();

            poppinsRegular = loadFont("/fonts/poppins/Poppins-Regular.ttf");
            poppinsItalic = loadFont("/fonts/poppins/Poppins-Italic.ttf");
            poppinsMedium = loadFont("/fonts/poppins/Poppins-Medium.ttf");
            poppinsSemiBold = loadFont("/fonts/poppins/Poppins-SemiBold.ttf");
            poppinsBold = loadFont("/fonts/poppins/Poppins-Bold.ttf");

            ge.registerFont(poppinsRegular);
            ge.registerFont(poppinsItalic);
            ge.registerFont(poppinsMedium);
            ge.registerFont(poppinsSemiBold);
            ge.registerFont(poppinsBold);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Font loadFont(String path) throws Exception {
        InputStream is = AppFonts.class.getResourceAsStream(path);

        if (is == null) {
            throw new Exception("Font not found: " + path);
        }

        return Font.createFont(Font.TRUETYPE_FONT, is);
    }

    public static Font regular(float size) {
        return poppinsRegular.deriveFont(size);
    }

    public static Font italic(float size) {
        return poppinsItalic.deriveFont(size);
    }

    public static Font medium(float size) {
        return poppinsMedium.deriveFont(size);
    }

    public static Font semiBold(float size) {
        return poppinsSemiBold.deriveFont(size);
    }

    public static Font bold(float size) {
        return poppinsBold.deriveFont(size);
    }
}