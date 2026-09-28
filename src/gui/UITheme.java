package gui;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

public class UITheme {
    public static void apply() {
        try {
            // Cố gắng dùng Nimbus cho đẹp
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            
            // Tùy chỉnh màu sắc và font chữ cho Nimbus
            UIManager.put("control", new Color(245, 245, 250));
            UIManager.put("info", new Color(242, 242, 242));
            UIManager.put("nimbusBase", new Color(18, 97, 160));
            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
            UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("nimbusGreen", new Color(176, 179, 50));
            UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
            UIManager.put("nimbusLightBackground", new Color(255, 255, 255));
            UIManager.put("nimbusOrange", new Color(191, 98, 4));
            UIManager.put("nimbusRed", new Color(169, 46, 34));
            UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
            UIManager.put("nimbusSelectionBackground", new Color(104, 156, 210));
            UIManager.put("text", new Color(0, 0, 0));
            
            // Cài font toàn cục
            Font font = new Font("Segoe UI", Font.PLAIN, 14);
            UIManager.put("defaultFont", font);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
            
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {}
        }
    }
}
