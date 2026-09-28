import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.BreakIterator;
import java.util.Base64;
import java.util.Locale;

/**
 * Coupures de ligne de java.text.BreakIterator, pour patchouli_layout.py.
 *
 * Patchouli (TextLayouter.layoutParagraph) coupe ses lignes avec
 * BreakIterator.getLineInstance(locale du jeu). Les regles sont celles du
 * JRE qui fait tourner Minecraft, Java 8 pour la 1.12.2 : ce programme doit
 * tourner sous le meme Java pour que la simulation soit exacte.
 *
 * Entree : une ligne par texte, en UTF-8 encode base64.
 * Sortie : une ligne par texte, les positions de coupure (indices UTF-16),
 * separees par des espaces, 0 et la longueur comprises.
 *
 * ASCII uniquement : javac sous Windows lit les sources en Cp1252.
 */
public class BreakPoints {
    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        BreakIterator it = BreakIterator.getLineInstance(Locale.FRANCE);
        String line;
        while ((line = in.readLine()) != null) {
            String text = new String(Base64.getDecoder().decode(line.trim()), StandardCharsets.UTF_8);
            it.setText(text);
            StringBuilder sb = new StringBuilder();
            for (int p = it.first(); p != BreakIterator.DONE; p = it.next()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(p);
            }
            out.println(sb.toString());
        }
    }
}
