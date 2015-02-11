

import java.io.File;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;


/**
 * Generates PDF outline from html list
 * 2 levels max depth hardcoded
 *
 * @author Ilya Kantor
 */
public class PdfMakeOutline {
    /**
     * Constructor.
     */
    private PdfMakeOutline() {
        //utility class
    }

    /**
     * This will read in a document and replace all of the urls with
     * http://www.pdfbox.org.
     * <br />
     * see usage() for commandline
     *
     * @param args Command line arguments.
     * @throws Exception If there is an error during the process.
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 4) {
            usage();
            return;
        }

        int widthTolerance = 10;

        PDDocument doc = PDDocument.load(new File(args[0]));

        ArrayList<LinkInfo> linkInfoList = new ArrayList<LinkInfo>();

        LinkInfoExtractor extractor = new LinkInfoExtractor(doc, widthTolerance);

        for (int pageNum = Integer.parseInt(args[3]); pageNum < Integer.parseInt(args[4]); pageNum++) {
            linkInfoList.addAll( extractor.extractPage(pageNum) );
        }


        PDDocumentOutline outline = new PDDocumentOutline();
        doc.getDocumentCatalog().setDocumentOutline(outline);

        double firstLevelX = linkInfoList.get(0).getRectangle().getX();
        for(int i = 0; i<linkInfoList.size(); i++) {

            LinkInfo linkInfo = linkInfoList.get(i);

            // add 2nd level
            while (linkInfo.getRectangle().getX() > firstLevelX) {
                PDOutlineItem outlineItem = new PDOutlineItem();
                outlineItem.setTitle(linkInfo.getText());
                outlineItem.setDestination(linkInfo.getDestination());
                outline.getLastChild().appendChild(outlineItem);

                if (i == linkInfoList.size()-1) break;

                linkInfo = linkInfoList.get(++i);
            }

            if (i == linkInfoList.size()-1) break;

            PDOutlineItem outlineItem = new PDOutlineItem();
            outlineItem.setTitle(linkInfo.getText());
            outlineItem.setDestination(linkInfo.getDestination());

            outline.appendChild(outlineItem);
        }


        doc.save(args[1]);

    }

    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage() {
        System.err.println("usage: " + PdfMakeOutline.class.getName() + " <input-file> <output-file> <page-from> <page-to>");
    }
}